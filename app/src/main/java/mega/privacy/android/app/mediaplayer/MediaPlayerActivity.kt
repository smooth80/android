package mega.privacy.android.app.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.exoplayer2.util.Util.startForegroundService
import com.jeremyliao.liveeventbus.LiveEventBus
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.MimeTypeList
import mega.privacy.android.app.R
import mega.privacy.android.app.activities.OfflineFileInfoActivity
import mega.privacy.android.app.activities.PasscodeActivity
import mega.privacy.android.app.components.attacher.MegaAttacher
import mega.privacy.android.app.components.dragger.DragToExitSupport
import mega.privacy.android.app.components.saver.NodeSaver
import mega.privacy.android.app.databinding.ActivityAudioPlayerBinding
import mega.privacy.android.app.databinding.ActivityVideoPlayerBinding
import mega.privacy.android.app.di.MegaApi
import mega.privacy.android.app.interfaces.ActionNodeCallback
import mega.privacy.android.app.interfaces.ActivityLauncher
import mega.privacy.android.app.interfaces.SnackbarShower
import mega.privacy.android.app.interfaces.showSnackbar
import mega.privacy.android.app.listeners.BaseListener
import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop
import mega.privacy.android.app.lollipop.FileInfoActivityLollipop
import mega.privacy.android.app.lollipop.controllers.ChatController
import mega.privacy.android.app.lollipop.controllers.NodeController
import mega.privacy.android.app.mediaplayer.service.AudioPlayerService
import mega.privacy.android.app.mediaplayer.service.MediaPlayerService
import mega.privacy.android.app.mediaplayer.service.MediaPlayerServiceBinder
import mega.privacy.android.app.mediaplayer.service.VideoPlayerService
import mega.privacy.android.app.mediaplayer.trackinfo.TrackInfoFragment
import mega.privacy.android.app.mediaplayer.trackinfo.TrackInfoFragmentArgs
import mega.privacy.android.app.utils.*
import mega.privacy.android.app.utils.AlertsAndWarnings.Companion.showSaveToDeviceConfirmDialog
import mega.privacy.android.app.utils.ChatUtil.removeAttachmentMessage
import mega.privacy.android.app.utils.Constants.*
import mega.privacy.android.app.utils.FileUtil.shareUri
import mega.privacy.android.app.utils.LogUtil.logDebug
import mega.privacy.android.app.utils.LogUtil.logError
import mega.privacy.android.app.utils.MegaNodeDialogUtil.moveToRubbishOrRemove
import mega.privacy.android.app.utils.MegaNodeDialogUtil.showRenameNodeDialog
import mega.privacy.android.app.utils.MegaNodeUtil.handleSelectFolderToImportResult
import mega.privacy.android.app.utils.MegaNodeUtil.selectFolderToCopy
import mega.privacy.android.app.utils.MegaNodeUtil.selectFolderToMove
import mega.privacy.android.app.utils.MegaNodeUtil.shareLink
import mega.privacy.android.app.utils.MegaNodeUtil.shareNode
import mega.privacy.android.app.utils.MegaNodeUtil.showShareOption
import mega.privacy.android.app.utils.MegaNodeUtil.showTakenDownAlert
import mega.privacy.android.app.utils.MegaNodeUtil.showTakenDownNodeActionNotAvailableDialog
import mega.privacy.android.app.utils.MenuUtils.toggleAllMenuItemsVisibility
import mega.privacy.android.app.utils.RunOnUIThreadUtils.post
import mega.privacy.android.app.utils.RunOnUIThreadUtils.runDelay
import nz.mega.sdk.*
import nz.mega.sdk.MegaApiJava.INVALID_HANDLE
import javax.inject.Inject

@AndroidEntryPoint
abstract class MediaPlayerActivity : PasscodeActivity(), SnackbarShower, ActivityLauncher {

    @MegaApi
    @Inject
    lateinit var megaApi: MegaApiAndroid

    @Inject
    lateinit var megaChatApi: MegaChatApiAndroid

    private lateinit var rootLayout: ViewGroup
    private lateinit var toolbar: Toolbar
    private val viewModel: MediaPlayerViewModel by viewModels()

    private lateinit var actionBar: ActionBar
    private lateinit var navController: NavController

    private var optionsMenu: Menu? = null
    private var searchMenuItem: MenuItem? = null

    private var viewingTrackInfo: TrackInfoFragmentArgs? = null

    private var serviceBound = false
    private var playerService: MediaPlayerService? = null

    private val nodeAttacher by lazy { MegaAttacher(this) }

    private val nodeSaver by lazy {
        NodeSaver(this, this, this, showSaveToDeviceConfirmDialog(this))
    }

    private val dragToExit by lazy {
        DragToExitSupport(this, this::onDragActivated) {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        /**
         * Called after a successful bind with our AudioPlayerService.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MediaPlayerServiceBinder) {
                playerService = service.service

                refreshMenuOptionsVisibility()

                service.service.metadata.observe(this@MediaPlayerActivity) {
                    dragToExit.nodeChanged(service.service.viewModel.playingHandle)
                }

                service.service.viewModel.error.observe(
                    this@MediaPlayerActivity, this@MediaPlayerActivity::onError
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras == null) {
            finish()
            return
        }

        val rebuildPlaylist = intent.getBooleanExtra(INTENT_EXTRA_KEY_REBUILD_PLAYLIST, true)
        val adapterType = intent.getIntExtra(INTENT_EXTRA_KEY_ADAPTER_TYPE, INVALID_VALUE)
        if (adapterType == INVALID_VALUE && rebuildPlaylist) {
            finish()
            return
        }

        if (savedInstanceState != null) {
            nodeAttacher.restoreState(savedInstanceState)
            nodeSaver.restoreState(savedInstanceState)
        }

        val isAudioPlayer = isAudioPlayer(intent)

        if (isAudioPlayer) {
            val binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
            setContentView(binding.root)

            rootLayout = binding.rootLayout
            toolbar = binding.toolbar
        } else {
            val binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
            setContentView(dragToExit.wrapContentView(binding.root))

            rootLayout = binding.rootLayout
            toolbar = binding.toolbar

            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white_alpha_087))

            MediaPlayerService.pauseAudioPlayer(this)

            dragToExit.viewerFrom = intent.getIntExtra(INTENT_EXTRA_KEY_VIEWER_FROM, INVALID_VALUE)
            dragToExit.observeThumbnailLocation(this)
        }

        toolbar.setBackgroundColor(Color.TRANSPARENT)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupToolbar()
        setupNavDestListener()

        val playerServiceIntent = Intent(
            this,
            if (isAudioPlayer) AudioPlayerService::class.java else VideoPlayerService::class.java
        )

        playerServiceIntent.putExtras(extras)

        if (rebuildPlaylist && savedInstanceState == null) {
            playerServiceIntent.setDataAndType(intent.data, intent.type)
            if (isAudioPlayer) {
                startForegroundService(this, playerServiceIntent)
            } else {
                startService(playerServiceIntent)
            }
        }

        bindService(playerServiceIntent, connection, Context.BIND_AUTO_CREATE)
        serviceBound = true

        viewModel.itemToRemove.observe(this) {
            playerService?.viewModel?.removeItem(it)
        }

        if (savedInstanceState == null && !isAudioPlayer) {
            // post to next UI cycle so that MediaPlayerFragment's onCreateView is called
            post {
                getFragmentFromNavHost(R.id.nav_host_fragment, MediaPlayerFragment::class.java)
                    ?.runEnterAnimation(dragToExit)
            }
        }

        if (!isAudioPlayer) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        if (CallUtil.participatingInACall()) {
            showNotAllowPlayAlert()
        }

        LiveEventBus.get(EVENT_NOT_ALLOW_PLAY, Boolean::class.java)
            .observe(this) {
                showNotAllowPlayAlert()
            }
    }

    private fun showNotAllowPlayAlert() {
        showSnackbar(StringResourcesUtils.getString(R.string.not_allow_play_alert))
    }

    override fun onResume() {
        super.onResume()

        refreshMenuOptionsVisibility()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (isAudioPlayer()) {
            window.setFormat(PixelFormat.RGBA_8888) // Needed to fix bg gradient banding
        }
    }

    abstract fun isAudioPlayer(): Boolean

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        nodeAttacher.saveState(outState)
        nodeSaver.saveState(outState)
    }

    private fun stopPlayer() {
        playerService?.stopAudioPlayer()
        finish()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        actionBar = supportActionBar!!
        actionBar.setHomeButtonEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.title = ""

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (psaWebBrowser.consumeBack()) return
        if (!navController.navigateUp()) {
            finish()
        }
    }

    private fun setupNavDestListener() {
        navController.addOnDestinationChangedListener { _, dest, args ->
            if (isAudioPlayer()) {
                toolbar.elevation = 0F

                val color = ContextCompat.getColor(
                    this,
                    if (dest.id == R.id.main_player) R.color.grey_020_grey_800 else R.color.white_dark_grey
                )

                window.statusBarColor = color
            } else {
                window.statusBarColor = Color.BLACK
            }

            when (dest.id) {
                R.id.main_player -> {
                    actionBar.title = ""
                    viewingTrackInfo = null
                }
                R.id.playlist -> {
                    viewingTrackInfo = null
                }
                R.id.track_info -> {
                    actionBar.title = StringResourcesUtils.getString(R.string.audio_track_info)

                    if (args != null) {
                        viewingTrackInfo = TrackInfoFragmentArgs.fromBundle(args)
                    }
                }
            }

            refreshMenuOptionsVisibility()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            playerService?.mainPlayerUIClosed()
            dragToExit.showPreviousHiddenThumbnail()
        }

        playerService = null
        if (serviceBound) {
            unbindService(connection)
        }

        nodeSaver.destroy()

        if (isFinishing && !isAudioPlayer(intent)) {
            MediaPlayerService.resumeAudioPlayer(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        optionsMenu = menu

        menuInflater.inflate(R.menu.media_player, menu)

        searchMenuItem = menu.findItem(R.id.action_search)

        val searchView = searchMenuItem?.actionView
        if (searchView is SearchView) {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    playerService?.viewModel?.playlistSearchQuery = newText
                    return true
                }

            })
        }

        searchMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                playerService?.viewModel?.playlistSearchQuery = null
                return true
            }
        })

        refreshMenuOptionsVisibility()

        return true
    }

    private fun refreshMenuOptionsVisibility() {
        val menu = optionsMenu
        if (menu == null) {
            logDebug("refreshMenuOptionsVisibility menu is null")
            return
        }

        val currentFragment = navController.currentDestination?.id
        if (currentFragment == null) {
            logDebug("refreshMenuOptionsVisibility currentFragment is null")
            return
        }

        val service = playerService
        if (service == null) {
            logDebug("refreshMenuOptionsVisibility null service")

            menu.toggleAllMenuItemsVisibility(false)
            return
        }

        val adapterType = service.viewModel.currentIntent
            ?.getIntExtra(INTENT_EXTRA_KEY_ADAPTER_TYPE, INVALID_VALUE)

        if (adapterType == null) {
            logDebug("refreshMenuOptionsVisibility null adapterType")

            menu.toggleAllMenuItemsVisibility(false)
            return
        }

        when (currentFragment) {
            R.id.playlist -> {
                menu.toggleAllMenuItemsVisibility(false)
                searchMenuItem?.isVisible = true
            }
            R.id.main_player, R.id.track_info -> {
                if (adapterType == OFFLINE_ADAPTER) {
                    menu.toggleAllMenuItemsVisibility(false)

                    menu.findItem(R.id.properties).isVisible =
                        currentFragment == R.id.main_player

                    menu.findItem(R.id.share).isVisible =
                        currentFragment == R.id.main_player

                    return
                }

                if (adapterType == RUBBISH_BIN_ADAPTER
                    || megaApi.isInRubbish(megaApi.getNodeByHandle(service.viewModel.playingHandle))
                ) {
                    menu.toggleAllMenuItemsVisibility(false)

                    menu.findItem(R.id.properties).isVisible =
                        currentFragment == R.id.main_player

                    val moveToTrash = menu.findItem(R.id.move_to_trash) ?: return
                    moveToTrash.isVisible = true
                    moveToTrash.title = StringResourcesUtils.getString(R.string.context_remove)

                    return
                }

                if (adapterType == FROM_CHAT) {
                    menu.toggleAllMenuItemsVisibility(false)

                    menu.findItem(R.id.save_to_device).isVisible = true
                    menu.findItem(R.id.chat_import).isVisible = true
                    menu.findItem(R.id.chat_save_for_offline).isVisible = true

                    // TODO: share option will be added in AND-12831
                    menu.findItem(R.id.share).isVisible = false

                    val moveToTrash = menu.findItem(R.id.move_to_trash) ?: return

                    val pair = getChatMessage()
                    val message = pair.second

                    val canRemove = message != null &&
                            message.userHandle == megaChatApi.myUserHandle && message.isDeletable

                    if (!canRemove) {
                        moveToTrash.isVisible = false
                        return
                    }

                    moveToTrash.isVisible = true
                    moveToTrash.title = StringResourcesUtils.getString(R.string.context_remove)

                    return
                }

                if (adapterType == FILE_LINK_ADAPTER || adapterType == ZIP_ADAPTER) {
                    menu.toggleAllMenuItemsVisibility(false)

                    menu.findItem(R.id.save_to_device).isVisible = true
                    menu.findItem(R.id.share).isVisible = true

                    return
                }

                if (adapterType == FOLDER_LINK_ADAPTER) {
                    menu.toggleAllMenuItemsVisibility(false)

                    menu.findItem(R.id.save_to_device).isVisible = true

                    return
                }

                val node = megaApi.getNodeByHandle(service.viewModel.playingHandle)
                if (node == null) {
                    logDebug("refreshMenuOptionsVisibility node is null")

                    menu.toggleAllMenuItemsVisibility(false)
                    return
                }

                menu.toggleAllMenuItemsVisibility(true)
                searchMenuItem?.isVisible = false

                menu.findItem(R.id.save_to_device).isVisible = true

                menu.findItem(R.id.properties).isVisible = currentFragment == R.id.main_player

                menu.findItem(R.id.share).isVisible =
                    currentFragment == R.id.main_player && showShareOption(
                        adapterType, adapterType == FOLDER_LINK_ADAPTER, node.handle
                    )

                menu.findItem(R.id.send_to_chat).isVisible = true

                if (megaApi.getAccess(node) == MegaShare.ACCESS_OWNER) {
                    if (node.isExported) {
                        menu.findItem(R.id.get_link).isVisible = false
                        menu.findItem(R.id.remove_link).isVisible = true
                    } else {
                        menu.findItem(R.id.get_link).isVisible = true
                        menu.findItem(R.id.remove_link).isVisible = false
                    }
                } else {
                    menu.findItem(R.id.get_link).isVisible = false
                    menu.findItem(R.id.remove_link).isVisible = false
                }

                menu.findItem(R.id.chat_import).isVisible = false
                menu.findItem(R.id.chat_save_for_offline).isVisible = false

                val access = megaApi.getAccess(node)
                when (access) {
                    MegaShare.ACCESS_READWRITE, MegaShare.ACCESS_READ, MegaShare.ACCESS_UNKNOWN -> {
                        menu.findItem(R.id.rename).isVisible = false
                        menu.findItem(R.id.move).isVisible = false
                    }
                    MegaShare.ACCESS_FULL, MegaShare.ACCESS_OWNER -> {
                        menu.findItem(R.id.rename).isVisible = true
                        menu.findItem(R.id.move).isVisible = true
                    }
                }

                menu.findItem(R.id.move_to_trash).isVisible =
                    node.parentHandle != megaApi.rubbishNode.handle
                            && (access == MegaShare.ACCESS_FULL || access == MegaShare.ACCESS_OWNER)

                menu.findItem(R.id.copy).isVisible = adapterType != FOLDER_LINK_ADAPTER
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val service = playerService ?: return false
        val launchIntent = service.viewModel.currentIntent ?: return false
        val playingHandle = service.viewModel.playingHandle
        val adapterType = launchIntent.getIntExtra(INTENT_EXTRA_KEY_ADAPTER_TYPE, INVALID_VALUE)
        val isFolderLink = adapterType == FOLDER_LINK_ADAPTER

        when (item.itemId) {
            R.id.save_to_device -> {
                when (adapterType) {
                    OFFLINE_ADAPTER -> nodeSaver.saveOfflineNode(playingHandle, true)
                    ZIP_ADAPTER -> {
                        val uri = service.exoPlayer.currentMediaItem?.playbackProperties?.uri
                            ?: return false
                        val playlistItem =
                            service.viewModel.getPlaylistItem(service.exoPlayer.currentMediaItem?.mediaId)
                                ?: return false

                        nodeSaver.saveUri(uri, playlistItem.nodeName, playlistItem.size, true)
                    }
                    FROM_CHAT -> {
                        val node = getChatMessageNode() ?: return true

                        nodeSaver.saveNode(
                            node, highPriority = true, fromMediaViewer = true, needSerialize = true
                        )
                    }
                    else -> {
                        nodeSaver.saveHandle(
                            playingHandle,
                            isFolderLink = isFolderLink,
                            fromMediaViewer = true
                        )
                    }
                }

                return true
            }
            R.id.properties -> {
                if (isAudioPlayer()) {
                    val uri =
                        service.exoPlayer.currentMediaItem?.playbackProperties?.uri ?: return true
                    navController.navigate(
                        MediaPlayerFragmentDirections.actionPlayerToTrackInfo(
                            adapterType, adapterType == INCOMING_SHARES_ADAPTER, playingHandle, uri
                        )
                    )
                } else {
                    val intent: Intent

                    if (adapterType == OFFLINE_ADAPTER) {
                        intent = Intent(this, OfflineFileInfoActivity::class.java)
                        intent.putExtra(HANDLE, playingHandle.toString())

                        logDebug("onOptionsItemSelected properties offline handle $playingHandle")
                    } else {
                        intent = Intent(this, FileInfoActivityLollipop::class.java)
                        intent.putExtra(HANDLE, playingHandle)

                        val node = megaApi.getNodeByHandle(playingHandle)
                        if (node == null) {
                            logError("onOptionsItemSelected properties non-offline null node")

                            return false
                        }

                        intent.putExtra(NAME, node.name)

                        val fromIncoming =
                            if (adapterType == SEARCH_ADAPTER || adapterType == RECENTS_ADAPTER) {
                                NodeController(this).nodeComesFromIncoming(node)
                            } else {
                                false
                            }

                        when {
                            adapterType == INCOMING_SHARES_ADAPTER || fromIncoming -> {
                                intent.putExtra(INTENT_EXTRA_KEY_FROM, FROM_INCOMING_SHARES)
                                intent.putExtra(INTENT_EXTRA_KEY_FIRST_LEVEL, false)
                            }
                            adapterType == INBOX_ADAPTER -> {
                                intent.putExtra(INTENT_EXTRA_KEY_FROM, FROM_INBOX)
                            }
                        }
                    }

                    startActivity(intent)
                }
                return true
            }
            R.id.chat_import -> {
                val intent = Intent(this, FileExplorerActivityLollipop::class.java)
                intent.action = FileExplorerActivityLollipop.ACTION_PICK_IMPORT_FOLDER
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMPORT_FOLDER)
                return true
            }
            R.id.share -> {
                when (adapterType) {
                    OFFLINE_ADAPTER, ZIP_ADAPTER -> {
                        val nodeName =
                            service.viewModel.getPlaylistItem(service.exoPlayer.currentMediaItem?.mediaId)?.nodeName
                                ?: return false
                        val uri = service.exoPlayer.currentMediaItem?.playbackProperties?.uri
                            ?: return false

                        shareUri(this, nodeName, uri)
                    }
                    FILE_LINK_ADAPTER -> {
                        shareLink(this, launchIntent.getStringExtra(URL_FILE_LINK))
                    }
                    else -> {
                        shareNode(this, megaApi.getNodeByHandle(service.viewModel.playingHandle))
                    }
                }
                return true
            }
            R.id.send_to_chat -> {
                nodeAttacher.attachNode(playingHandle)
                return true
            }
            R.id.get_link -> {
                if (showTakenDownNodeActionNotAvailableDialog(
                        megaApi.getNodeByHandle(playingHandle), this
                    )
                ) {
                    return true
                }
                LinksUtil.showGetLinkActivity(this, playingHandle)
                return true
            }
            R.id.remove_link -> {
                val node = megaApi.getNodeByHandle(playingHandle) ?: return true
                if (showTakenDownNodeActionNotAvailableDialog(node, this)) {
                    return true
                }

                AlertsAndWarnings.showConfirmRemoveLinkDialog(this) {
                    megaApi.disableExport(node, object : BaseListener(this) {
                        override fun onRequestFinish(
                            api: MegaApiJava, request: MegaRequest, e: MegaError
                        ) {
                            if (e.errorCode == MegaError.API_OK) {
                                // Some times checking node.isExported immediately will still
                                // get true, so let's add some delay here.
                                runDelay(100L) {
                                    refreshMenuOptionsVisibility()
                                }
                            }
                        }
                    })
                }
                return true
            }
            R.id.chat_save_for_offline -> {
                val pair = getChatMessage()
                val message = pair.second

                if (message != null) {
                    ChatController(this).saveForOffline(
                        message.megaNodeList, megaChatApi.getChatRoom(pair.first), true, this
                    )
                }

                return true
            }
            R.id.rename -> {
                val node = megaApi.getNodeByHandle(playingHandle) ?: return true
                showRenameNodeDialog(this, node, this, object : ActionNodeCallback {
                    override fun finishRenameActionWithSuccess(newName: String) {
                        playerService?.viewModel?.updateItemName(node.handle, newName)
                        updateTrackInfoNodeNameIfNeeded(node.handle, newName)
                    }
                })
                return true
            }
            R.id.move -> {
                selectFolderToMove(this, longArrayOf(playingHandle))
                return true
            }
            R.id.copy -> {
                selectFolderToCopy(this, longArrayOf(playingHandle))
                return true
            }
            R.id.move_to_trash -> {
                if (adapterType == FROM_CHAT) {
                    val pair = getChatMessage()
                    val message = pair.second

                    if (message != null) {
                        removeAttachmentMessage(this, pair.first, message)
                    }
                } else {
                    moveToRubbishOrRemove(playingHandle, this, this)
                }
                return true
            }
        }
        return false
    }

    /**
     * Get chat id and chat message from the launch intent.
     *
     * @return first is chat id, second is chat message
     */
    private fun getChatMessage(): Pair<Long, MegaChatMessage?> {
        val chatId = intent.getLongExtra(INTENT_EXTRA_KEY_CHAT_ID, INVALID_HANDLE)
        val msgId = intent.getLongExtra(INTENT_EXTRA_KEY_MSG_ID, INVALID_HANDLE)

        if (chatId == INVALID_HANDLE || msgId == INVALID_HANDLE) {
            return Pair(chatId, null)
        }

        var message = megaChatApi.getMessage(chatId, msgId)

        if (message == null) {
            message = megaChatApi.getMessageFromNodeHistory(chatId, msgId)
        }

        return Pair(chatId, message)
    }

    /**
     * Get chat message node from the launch intent.
     *
     * @return chat message node
     */
    private fun getChatMessageNode(): MegaNode? {
        val pair = getChatMessage()
        val message = pair.second ?: return null

        return ChatController(this).authorizeNodeIfPreview(
            message.megaNodeList.get(0), megaChatApi.getChatRoom(pair.first)
        )
    }

    /**
     * Update node name if current displayed fragment is TrackInfoFragment.
     *
     * @param handle node handle
     * @param newName new node name
     */
    private fun updateTrackInfoNodeNameIfNeeded(handle: Long, newName: String) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) ?: return
        val firstChild = navHostFragment.childFragmentManager.fragments.firstOrNull() ?: return
        if (firstChild is TrackInfoFragment) {
            firstChild.updateNodeNameIfNeeded(handle, newName)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        nodeSaver.handleRequestPermissionsResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (nodeAttacher.handleActivityResult(requestCode, resultCode, data, this)) {
            return
        }

        if (nodeSaver.handleActivityResult(requestCode, resultCode, data)) {
            return
        }

        if (requestCode == REQUEST_CODE_SELECT_IMPORT_FOLDER) {
            val node = getChatMessageNode() ?: return

            val toHandle = data?.getLongExtra(INTENT_EXTRA_KEY_IMPORT_TO, INVALID_HANDLE)
            if (toHandle == null || toHandle == INVALID_HANDLE) {
                return
            }

            handleSelectFolderToImportResult(resultCode, toHandle, node, this, this)
        } else {
            viewModel.handleActivityResult(requestCode, resultCode, data, this, this)
        }
    }

    fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    fun closeSearch() {
        searchMenuItem?.collapseActionView()
    }

    fun hideToolbar(animate: Boolean = true, hideStatusBar: Boolean = true) {
        if (animate) {
            toolbar.animate()
                .translationY(-toolbar.measuredHeight.toFloat())
                .setDuration(MEDIA_PLAYER_TOOLBAR_SHOW_HIDE_DURATION_MS)
                .start()
        } else {
            toolbar.animate().cancel()
            toolbar.translationY = -toolbar.measuredHeight.toFloat()
        }

        if (!isAudioPlayer() && hideStatusBar) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun showToolbar(animate: Boolean = true) {
        if (animate) {
            toolbar.animate()
                .translationY(0F)
                .setDuration(MEDIA_PLAYER_TOOLBAR_SHOW_HIDE_DURATION_MS)
                .start()
        } else {
            toolbar.animate().cancel()
            toolbar.translationY = 0F
        }

        if (!isAudioPlayer()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    fun showToolbarElevation(withElevation: Boolean) {
        // This is the actual color when using Util.changeToolBarElevation, but video player
        // use different toolbar theme (to force dark theme), which breaks
        // Util.changeToolBarElevation, so we just use the actual color here.
        val darkElevationColor = Color.parseColor("#282828")

        if (!isAudioPlayer() || Util.isDarkMode(this)) {
            toolbar.setBackgroundColor(
                when {
                    withElevation -> darkElevationColor
                    isAudioPlayer() -> Color.TRANSPARENT
                    else -> ContextCompat.getColor(this, R.color.dark_grey)
                }
            )

            post {
                window.statusBarColor = if (withElevation) darkElevationColor else Color.BLACK
            }
        } else {
            toolbar.elevation =
                if (withElevation) resources.getDimension(R.dimen.toolbar_elevation) else 0F
        }
    }

    fun setDraggable(draggable: Boolean) {
        dragToExit.setDraggable(draggable)
    }

    private fun onDragActivated(activated: Boolean) {
        getFragmentFromNavHost(R.id.nav_host_fragment, MediaPlayerFragment::class.java)
            ?.onDragActivated(dragToExit, activated)
    }

    private fun onError(code: Int) {
        when (code) {
            MegaError.API_EOVERQUOTA -> showGeneralTransferOverQuotaWarning()
            MegaError.API_EBLOCKED -> showTakenDownAlert(this)
            MegaError.API_ENOENT -> stopPlayer()
        }
    }

    override fun showSnackbar(type: Int, content: String?, chatId: Long) {
        showSnackbar(type, rootLayout, content, chatId)
    }

    override fun launchActivity(intent: Intent) {
        startActivity(intent)
        stopPlayer()
    }

    override fun launchActivityForResult(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }

    companion object {
        fun isAudioPlayer(intent: Intent?): Boolean {
            val nodeName = intent?.getStringExtra(INTENT_EXTRA_KEY_FILE_NAME) ?: return true

            return MimeTypeList.typeForName(nodeName).isAudio
        }
    }
}
