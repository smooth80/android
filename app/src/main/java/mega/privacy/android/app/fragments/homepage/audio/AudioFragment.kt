package mega.privacy.android.app.fragments.homepage.audio

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.SimpleItemAnimator
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.MimeTypeList
import mega.privacy.android.app.R
import mega.privacy.android.app.R.string
import mega.privacy.android.app.components.CustomizedGridLayoutManager
import mega.privacy.android.app.components.ListenScrollChangesHelper
import mega.privacy.android.app.components.NewGridRecyclerView
import mega.privacy.android.app.components.PositionDividerItemDecoration
import mega.privacy.android.app.databinding.FragmentAudioBinding
import mega.privacy.android.app.fragments.BaseFragment
import mega.privacy.android.app.fragments.homepage.ActionModeCallback
import mega.privacy.android.app.fragments.homepage.ActionModeViewModel
import mega.privacy.android.app.fragments.homepage.EventObserver
import mega.privacy.android.app.fragments.homepage.HomepageSearchable
import mega.privacy.android.app.fragments.homepage.ItemOperationViewModel
import mega.privacy.android.app.fragments.homepage.NodeGridAdapter
import mega.privacy.android.app.fragments.homepage.NodeItem
import mega.privacy.android.app.fragments.homepage.SortByHeaderViewModel
import mega.privacy.android.app.fragments.homepage.documents.DocumentsAdapter
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop
import mega.privacy.android.app.lollipop.ManagerActivityLollipop
import mega.privacy.android.app.lollipop.controllers.NodeController
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.Constants.AUDIO_BROWSE_ADAPTER
import mega.privacy.android.app.utils.Constants.AUDIO_SEARCH_ADAPTER
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_ADAPTER_TYPE
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_FILE_NAME
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_HANDLE
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_HANDLES_NODES_SEARCH
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_ORDER_GET_CHILDREN
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_POSITION
import mega.privacy.android.app.utils.Constants.INTENT_EXTRA_KEY_SCREEN_POSITION
import mega.privacy.android.app.utils.Constants.SNACKBAR_TYPE
import mega.privacy.android.app.utils.DraggingThumbnailCallback
import mega.privacy.android.app.utils.FileUtils.getLocalFile
import mega.privacy.android.app.utils.LogUtil.logDebug
import mega.privacy.android.app.utils.LogUtil.logError
import mega.privacy.android.app.utils.LogUtil.logWarning
import mega.privacy.android.app.utils.MegaApiUtils
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.Util.showSnackbar
import mega.privacy.android.app.utils.getThumbnailLocationOnScreen
import nz.mega.sdk.MegaApiJava.INVALID_HANDLE
import nz.mega.sdk.MegaNode
import java.io.File
import java.lang.ref.WeakReference

@AndroidEntryPoint
class AudioFragment : BaseFragment(), HomepageSearchable {

    private val viewModel by viewModels<AudioViewModel>()
    private val actionModeViewModel by viewModels<ActionModeViewModel>()
    private val itemOperationViewModel by viewModels<ItemOperationViewModel>()
    private val sortByHeaderViewModel by viewModels<SortByHeaderViewModel>()

    private lateinit var binding: FragmentAudioBinding
    private lateinit var listView: NewGridRecyclerView
    private lateinit var listAdapter: DocumentsAdapter
    private lateinit var gridAdapter: NodeGridAdapter
    private lateinit var itemDecoration: PositionDividerItemDecoration

    private var actionMode: ActionMode? = null
    private lateinit var actionModeCallback: ActionModeCallback

    private lateinit var activity: ManagerActivityLollipop

    private var draggingNodeHandle = INVALID_HANDLE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAudioBinding.inflate(inflater, container, false).apply {
            viewModel = this@AudioFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        activity = getActivity() as ManagerActivityLollipop

        setupListView()
        setupListAdapter()
        setupFastScroller()
        setupActionMode()
        setupNavigation()
        setupDraggingThumbnailCallback()

        viewModel.items.observe(viewLifecycleOwner) {
            if (!viewModel.searchMode) {
                activity.invalidateOptionsMenu()  // Hide the search icon if no file
            }

            actionModeViewModel.setNodesData(it.filter{nodeItem -> nodeItem.node != null})
        }
    }

    private fun doIfOnline(operation: () -> Unit) {
        if (Util.isOnline(context)) {
            operation()
        } else {
            activity.hideKeyboardSearch()  // Make the snack bar visible to the user
            activity.showSnackbar(
                SNACKBAR_TYPE,
                context.getString(R.string.error_server_connection_problem),
                -1
            )
        }
    }

    private fun setupNavigation() {
        itemOperationViewModel.openItemEvent.observe(viewLifecycleOwner, EventObserver {
            val node = it.node
            if (node != null) {
                openNode(node, it.index)
            }
        })

        itemOperationViewModel.showNodeItemOptionsEvent.observe(viewLifecycleOwner, EventObserver {
            doIfOnline { activity.showNodeOptionsPanel(it.node) }
        })

        sortByHeaderViewModel.showDialogEvent.observe(viewLifecycleOwner, EventObserver {
            activity.showNewSortByPanel()
        })

        sortByHeaderViewModel.orderChangeEvent.observe(viewLifecycleOwner, EventObserver {
            if (sortByHeaderViewModel.isList) {
                listAdapter.notifyItemChanged(POSITION_HEADER)
            } else {
                gridAdapter.notifyItemChanged(POSITION_HEADER)
            }
            viewModel.loadAudio(true, it)
        })

        sortByHeaderViewModel.listGridChangeEvent.observe(
            viewLifecycleOwner,
            EventObserver { isList ->
                switchListGridView(isList)
            })
    }

    private fun switchListGridView(isList: Boolean) {
        if (isList) {
            listView.switchToLinear()
            listView.adapter = listAdapter
            listView.addItemDecoration(itemDecoration)
        } else {
            listView.switchBackToGrid()
            listView.adapter = gridAdapter
            listView.removeItemDecoration(itemDecoration)

            (listView.layoutManager as CustomizedGridLayoutManager).apply {
                spanSizeLookup = gridAdapter.getSpanSizeLookup(spanCount)
            }
        }
        viewModel.refreshUi()
    }

    private fun openNode(node: MegaNode, index: Int) {
        val file: MegaNode = node

        val mimeType = MimeTypeList.typeForName(file.name)

        val mediaIntent: Intent
        val internalIntent: Boolean
        var opusFile = false
        if (mimeType.isAudioNotSupported) {
            mediaIntent = Intent(Intent.ACTION_VIEW)
            internalIntent = false
            val parts = file.name.split("\\.")
            if (parts.size > 1 && parts.last() == "opus") {
                opusFile = true
            }
        } else {
            mediaIntent = Intent(context, AudioVideoPlayerLollipop::class.java)
            internalIntent = true
        }
        mediaIntent.putExtra(INTENT_EXTRA_KEY_POSITION, index)
        mediaIntent.putExtra(INTENT_EXTRA_KEY_ORDER_GET_CHILDREN, viewModel.order)
        if (viewModel.searchMode) {
            mediaIntent.putExtra(INTENT_EXTRA_KEY_ADAPTER_TYPE, AUDIO_SEARCH_ADAPTER)
            mediaIntent.putExtra(
                INTENT_EXTRA_KEY_HANDLES_NODES_SEARCH, viewModel.getHandlesOfAudio()
            )
        } else {
            mediaIntent.putExtra(INTENT_EXTRA_KEY_ADAPTER_TYPE, AUDIO_BROWSE_ADAPTER)
        }
        listView.findViewHolderForLayoutPosition(index)?.itemView?.findViewById<ImageView>(R.id.thumbnail)
            ?.let {
                mediaIntent.putExtra(
                    INTENT_EXTRA_KEY_SCREEN_POSITION, getThumbnailLocationOnScreen(it)
                )
            }

        mediaIntent.putExtra(INTENT_EXTRA_KEY_FILE_NAME, file.name)

        val localPath = getLocalFile(context, file.name, file.size)

        if (localPath != null) {
            val mediaFile = File(localPath)
            if (VERSION.SDK_INT >= VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().path)) {
                logDebug("itemClick:FileProviderOption")
                val mediaFileUri = FileProvider.getUriForFile(
                    context, "mega.privacy.android.app.providers.fileprovider", mediaFile
                )
                if (mediaFileUri == null) {
                    logDebug("itemClick:ERROR:NULLmediaFileUri")
                    showSnackbar(context, SNACKBAR_TYPE, getString(string.general_text_error), -1)
                } else {
                    mediaIntent.setDataAndType(mediaFileUri, mimeType.type)
                }
            } else {
                val mediaFileUri = Uri.fromFile(mediaFile)
                if (mediaFileUri == null) {
                    logError("itemClick:ERROR:NULLmediaFileUri")
                    showSnackbar(context, SNACKBAR_TYPE, getString(string.general_text_error), -1)
                } else {
                    mediaIntent.setDataAndType(mediaFileUri, mimeType.type)
                }
            }
            mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            logDebug("itemClick:localPathNULL")
            if (megaApi.httpServerIsRunning() == 0) {
                megaApi.httpServerStart()
            } else {
                logWarning("itemClick:ERROR:httpServerAlreadyRunning")
            }
            val mi = MemoryInfo()
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.getMemoryInfo(mi)
            if (mi.totalMem > Constants.BUFFER_COMP) {
                logDebug("itemClick:total mem: " + mi.totalMem + " allocate 32 MB")
                megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB)
            } else {
                logDebug("itemClick:total mem: " + mi.totalMem + " allocate 16 MB")
                megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB)
            }
            val url = megaApi.httpServerGetLocalLink(file)
            if (url != null) {
                val parsedUri = Uri.parse(url)
                if (parsedUri != null) {
                    mediaIntent.setDataAndType(parsedUri, mimeType.type)
                } else {
                    logError("itemClick:ERROR:httpServerGetLocalLink")
                    showSnackbar(context, SNACKBAR_TYPE, getString(string.general_text_error), -1)
                }
            } else {
                logError("itemClick:ERROR:httpServerGetLocalLink")
                showSnackbar(context, SNACKBAR_TYPE, getString(string.general_text_error), -1)
            }
        }
        mediaIntent.putExtra(INTENT_EXTRA_KEY_HANDLE, file.handle)
        if (opusFile) {
            mediaIntent.setDataAndType(mediaIntent.data, "audio/*")
        }
        if (internalIntent) {
            setupDraggingThumbnailCallback()
            startActivity(mediaIntent)
            activity.overridePendingTransition(0, 0)
            draggingNodeHandle = node.handle
        } else {
            logDebug("itemClick:externalIntent")
            if (MegaApiUtils.isIntentAvailable(context, mediaIntent)) {
                startActivity(mediaIntent)
            } else {
                logWarning("itemClick:noAvailableIntent")
                showSnackbar(context, SNACKBAR_TYPE, getString(string.intent_not_available), -1)
                val nC = NodeController(context)
                nC.prepareForDownload(arrayListOf(node.handle), true)
            }
        }
    }

    /**
     * Only refresh the list items of uiDirty = true
     */
    private fun updateUi() = viewModel.items.value?.let { it ->
        val newList = ArrayList<NodeItem>(it)
        if (sortByHeaderViewModel.isList) {
            listAdapter.submitList(newList)
        } else {
            gridAdapter.submitList(newList)
        }
    }

    private fun preventListItemBlink() {
        val animator = listView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private fun elevateToolbarWhenScrolling() = ListenScrollChangesHelper().addViewToListen(
        listView
    ) { v: View?, _, _, _, _ ->
        activity.changeActionBarElevation(v!!.canScrollVertically(-1))
    }

    private fun setupListView() {
        listView = binding.audioList
        preventListItemBlink()
        elevateToolbarWhenScrolling()
        itemDecoration = PositionDividerItemDecoration(context, outMetrics)

        listView.clipToPadding = false
        listView.setHasFixedSize(true)
    }

    private fun setupActionMode() {
        actionModeCallback = ActionModeCallback(context, actionModeViewModel, megaApi)

        observeItemLongClick()
        observeSelectedItems()
        observeAnimatedItems()
        observeActionModeDestroy()
    }

    private fun observeItemLongClick() =
        actionModeViewModel.longClick.observe(viewLifecycleOwner, EventObserver {
            doIfOnline { actionModeViewModel.enterActionMode(it) }
        })

    private fun observeSelectedItems() =
        actionModeViewModel.selectedNodes.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                actionMode?.apply {
                    finish()
                }
            } else {
                viewModel.items.value?.let { items ->
                    actionModeCallback.nodeCount = items.size - 1   // The "sort by" header isn't counted
                }

                if (actionMode == null) {
                    activity.hideKeyboardSearch()
                    actionMode = (activity as AppCompatActivity).startSupportActionMode(
                        actionModeCallback
                    )
                } else {
                    actionMode?.invalidate()  // Update the action items based on the selected nodes
                }

                actionMode?.title = it.size.toString()
            }
        })

    private fun observeAnimatedItems() {
        var animatorSet: AnimatorSet? = null

        actionModeViewModel.animNodeIndices.observe(viewLifecycleOwner, Observer {
            animatorSet?.run {
                // End the started animation if any, or the view may show messy as its property
                // would be wrongly changed by multiple animations running at the same time
                // via contiguous quick clicks on the item
                if (isStarted) {
                    end()
                }
            }

            // Must create a new AnimatorSet, or it would keep all previous
            // animation and play them together
            animatorSet = AnimatorSet()
            val animatorList = mutableListOf<Animator>()

            animatorSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    updateUi()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })

            it.forEach { pos ->
                listView.findViewHolderForAdapterPosition(pos)?.let { viewHolder ->
                    val itemView = viewHolder.itemView

                    val imageView: ImageView? = if (sortByHeaderViewModel.isList) {
                        if (listAdapter.getItemViewType(pos) != DocumentsAdapter.TYPE_HEADER) {
                            itemView.setBackgroundColor(resources.getColor(R.color.new_multiselect_color))
                        }
                        itemView.findViewById(R.id.thumbnail)
                    } else {
                        itemView.findViewById(R.id.ic_selected)
                    }

                    imageView?.run {
                        setImageResource(R.drawable.ic_select_folder)
                        visibility = View.VISIBLE

                        val animator =
                            AnimatorInflater.loadAnimator(context, R.animator.icon_select)
                        animator.setTarget(this)
                        animatorList.add(animator)
                    }
                }
            }

            animatorSet?.playTogether(animatorList)
            animatorSet?.start()
        })
    }

    private fun observeActionModeDestroy() =
        actionModeViewModel.actionModeDestroy.observe(viewLifecycleOwner, EventObserver {
            actionMode = null
            activity.showKeyboardForSearch()
        })

    private fun setupFastScroller() = binding.scroller.setRecyclerView(listView)

    private fun setupListAdapter() {
        listAdapter =
            DocumentsAdapter(actionModeViewModel, itemOperationViewModel, sortByHeaderViewModel)
        gridAdapter =
            NodeGridAdapter(actionModeViewModel, itemOperationViewModel, sortByHeaderViewModel)

        switchListGridView(sortByHeaderViewModel.isList)
    }

    override fun shouldShowSearchMenu(): Boolean = viewModel.shouldShowSearchMenu()

    override fun searchReady() {
        // Rotate screen in action mode, the keyboard would pop up again, hide it
        if (actionMode != null) {
            Handler().post { activity.hideKeyboardSearch() }
        }
        if (viewModel.searchMode) return

        viewModel.searchMode = true
        viewModel.searchQuery = ""
        viewModel.refreshUi()
    }

    override fun exitSearch() {
        if (!viewModel.searchMode) return

        viewModel.searchMode = false
        viewModel.searchQuery = ""
        viewModel.refreshUi()
    }

    override fun searchQuery(query: String) {
        if (viewModel.searchQuery == query) return
        viewModel.searchQuery = query
        viewModel.loadAudio()
    }

    /** All below methods are for supporting functions of FullScreenImageViewer */

    private fun getDraggingThumbnailLocationOnScreen(): IntArray? {
        val thumbnailView = getThumbnailViewByHandle(draggingNodeHandle) ?: return null
        return getThumbnailLocationOnScreen(thumbnailView)
    }

    private fun getThumbnailViewByHandle(handle: Long): ImageView? {
        val position = viewModel.getNodePositionByHandle(handle)
        val viewHolder = listView.findViewHolderForLayoutPosition(position) ?: return null
        return viewHolder.itemView.findViewById(R.id.thumbnail)
    }

    private fun setupDraggingThumbnailCallback() =
        AudioVideoPlayerLollipop.addDraggingThumbnailCallback(
            AudioFragment::class.java, AudioDraggingThumbnailCallback(WeakReference(this))
        )

    fun scrollToPhoto(handle: Long) {
        val position = viewModel.getNodePositionByHandle(handle)
        if (position == Constants.INVALID_POSITION) return

        listView.scrollToPosition(position)
        notifyThumbnailLocationOnScreen()
    }

    fun hideDraggingThumbnail(handle: Long) {
        getThumbnailViewByHandle(draggingNodeHandle)?.apply { visibility = View.VISIBLE }
        getThumbnailViewByHandle(handle)?.apply { visibility = View.INVISIBLE }
        draggingNodeHandle = handle
        notifyThumbnailLocationOnScreen()
    }

    private fun notifyThumbnailLocationOnScreen() {
        val location = getDraggingThumbnailLocationOnScreen() ?: return
        location[0] += location[2] / 2
        location[1] += location[3] / 2

        val intent = Intent(Constants.BROADCAST_ACTION_INTENT_FILTER_UPDATE_IMAGE_DRAG)
        intent.putExtra(INTENT_EXTRA_KEY_SCREEN_POSITION, location)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    companion object {
        private const val POSITION_HEADER = 0

        private class AudioDraggingThumbnailCallback(private val fragmentRef: WeakReference<AudioFragment>) :
            DraggingThumbnailCallback {

            override fun setVisibility(visibility: Int) {
                val fragment = fragmentRef.get() ?: return
                fragment.getThumbnailViewByHandle(fragment.draggingNodeHandle)
                    ?.apply { this.visibility = visibility }
            }

            override fun getLocationOnScreen(location: IntArray) {
                val fragment = fragmentRef.get() ?: return
                val result = fragment.getDraggingThumbnailLocationOnScreen() ?: return
                result.copyInto(location, 0, 0, 2)
            }
        }
    }
}
