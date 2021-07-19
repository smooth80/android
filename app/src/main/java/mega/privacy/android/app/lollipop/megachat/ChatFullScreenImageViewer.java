package mega.privacy.android.app.lollipop.megachat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.ExtendedViewPager;
import mega.privacy.android.app.components.TouchImageView;
import mega.privacy.android.app.components.dragger.DragToExitSupport;
import mega.privacy.android.app.components.saver.NodeSaver;
import mega.privacy.android.app.interfaces.SnackbarShower;
import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.LoginActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.activities.PasscodeActivity;
import mega.privacy.android.app.lollipop.adapters.MegaChatFullScreenImageAdapter;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.utils.AlertsAndWarnings;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;
import nz.mega.sdk.MegaUserAlert;

import static android.graphics.Color.*;
import static mega.privacy.android.app.utils.AlertsAndWarnings.showOverDiskQuotaPaywallWarning;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.FileUtil.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.MegaApiUtils.*;
import static mega.privacy.android.app.utils.Util.*;
import static nz.mega.sdk.MegaApiJava.STORAGE_STATE_PAYWALL;

public class ChatFullScreenImageViewer extends PasscodeActivity implements OnPageChangeListener,
		MegaRequestListenerInterface, MegaGlobalListenerInterface,SnackbarShower {

	boolean fromChatSavedInstance = false;
	RelativeLayout relativeImageViewerLayout;
	private Handler handler;

	private DisplayMetrics outMetrics;

	private boolean aBshown = true;

	ProgressDialog statusDialog;

	float scaleText;
	AppBarLayout appBarLayout;
	Toolbar tB;
	ActionBar aB;

	private MenuItem downloadIcon;
	private MenuItem importIcon;
	private MenuItem saveForOfflineIcon;
	private MenuItem removeIcon;

	private MegaChatFullScreenImageAdapter adapterMega;
	private int positionG;
	private ArrayList<Long> imageHandles;
	private RelativeLayout fragmentContainer;
	private TextView fileNameTextView;
	private RelativeLayout bottomLayout;
	private ExtendedViewPager viewPager;

	static ChatFullScreenImageViewer fullScreenImageViewer;
    private MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;

	MegaNode nodeToImport;

	long [] messageIds;
	long chatId = -1;

	ArrayList<MegaChatMessage> messages;

	DatabaseHandler dbH = null;

	boolean isDeleteDialogShow = false;

	ChatController chatC;

	private final NodeSaver nodeSaver = new NodeSaver(this, this, this,
			AlertsAndWarnings.showSaveToDeviceConfirmDialog(this));
	private final DragToExitSupport dragToExit = new DragToExitSupport(this, this::onDragActivated, () -> {
		finish();
		overridePendingTransition(0, android.R.anim.fade_out);

		return Unit.INSTANCE;
	});

	@Override
	public void onDestroy(){
		if(megaApi != null)
		{
			megaApi.removeRequestListener(this);
		}

		nodeSaver.destroy();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		logDebug("onCreateOptionsMenu");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_chat_full_screen_image_viewer, menu);

		downloadIcon = menu.findItem(R.id.chat_full_image_viewer_download);
		importIcon = menu.findItem(R.id.chat_full_image_viewer_import);
		saveForOfflineIcon = menu.findItem(R.id.chat_full_image_viewer_save_for_offline);
		removeIcon = menu.findItem(R.id.chat_full_image_viewer_remove);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		logDebug("onPrepareOptionsMenu");

		MegaNode node = null;

		if(!messages.isEmpty()){
			if(messages.get(positionG).getMegaNodeList()!=null && messages.get(positionG).getMegaNodeList().size()>0){
				node = messages.get(positionG).getMegaNodeList().get(0);
			}
		}

        if(megaApi==null || !isOnline(this)){
            downloadIcon.setVisible(false);
            importIcon.setVisible(false);
            saveForOfflineIcon.setVisible(false);

            if (messages.get(positionG).getUserHandle() == megaChatApi.getMyUserHandle()
					&& messages.get(positionG).isDeletable()) {
                removeIcon.setVisible(true);
            }
            else{
                removeIcon.setVisible(false);
            }
        }
        else if (node != null){
            downloadIcon.setVisible(true);
            if (chatC.isInAnonymousMode()) {
                importIcon.setVisible(false);
                saveForOfflineIcon.setVisible(false);
            }
            else {
                importIcon.setVisible(true);
                saveForOfflineIcon.setVisible(true);
            }

            if (messages.get(positionG).getUserHandle()==megaChatApi.getMyUserHandle() && messages.get(positionG).isDeletable()) {
                removeIcon.setVisible(true);
            }
            else {
                removeIcon.setVisible(false);
            }
        }
        else {
            downloadIcon.setVisible(false);
            importIcon.setVisible(false);
            saveForOfflineIcon.setVisible(false);
            removeIcon.setVisible(false);
        }

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		logDebug("onRequestPermissionsResult");
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		nodeSaver.handleRequestPermissionsResult(requestCode);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		logDebug("onOptionsItemSelected");
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home: {
				onBackPressed();
				break;
			}
			case R.id.chat_full_image_viewer_download: {
				logDebug("Download option");

				MegaNode node = chatC.authorizeNodeIfPreview(messages.get(positionG).getMegaNodeList().get(0), megaChatApi.getChatRoom(chatId));
				nodeSaver.saveNode(node, true, false, true, true);
				break;
			}

			case R.id.chat_full_image_viewer_import: {
				logDebug("Import option");
				MegaNode node = chatC.authorizeNodeIfPreview(messages.get(positionG).getMegaNodeList().get(0), megaChatApi.getChatRoom(chatId));
				importNode(node);
				break;
			}
			case R.id.chat_full_image_viewer_save_for_offline: {
				logDebug("Save for offline option");
//				showSnackbar("Coming soon...");
				if (messages.get(positionG) != null){
					chatC.saveForOffline(messages.get(positionG).getMegaNodeList(),
							megaChatApi.getChatRoom(chatId), true, this);
				}
				break;
			}
			case R.id.chat_full_image_viewer_remove: {
				logDebug("Remove option");
				MegaChatMessage msg = messages.get(positionG);
				showConfirmationDeleteNode(chatId, msg);
				break;
			}

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean shouldSetStatusBarTextColor() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logDebug("onCreate");

		Window window = getWindow();
		window.setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
		window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		handler = new Handler();
		fullScreenImageViewer = this;

		chatC = new ChatController(this);

		Display display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
		display.getMetrics(outMetrics);
		float density  = getResources().getDisplayMetrics().density;

		float scaleW = getScaleW(outMetrics, density);
		float scaleH = getScaleH(outMetrics, density);
		if (scaleH < scaleW){
			scaleText = scaleH;
		}
		else{
			scaleText = scaleW;
		}
		if (savedInstanceState != null){
			isDeleteDialogShow = savedInstanceState.getBoolean("isDeleteDialogShow", false);

			nodeSaver.restoreState(savedInstanceState);
		}
		else {
			isDeleteDialogShow = false;
		}

		dbH = DatabaseHandler.getDbHandler(this);

		MegaApplication app = (MegaApplication)getApplication();

		if(isOnline(this)){
			megaApi = app.getMegaApi();

			if((megaApi==null||megaApi.getRootNode()==null) && !chatC.isInAnonymousMode()){
				logDebug("Refresh session - sdk");
				Intent intent = new Intent(this, LoginActivityLollipop.class);
				intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				return;
			}
		}

		if (megaChatApi == null) {
			megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
		}

		if (megaChatApi == null || megaChatApi.getInitState() == MegaChatApi.INIT_ERROR) {
			logDebug("Refresh session - karere");
			Intent intent = new Intent(this, LoginActivityLollipop.class);
			intent.putExtra(VISIBLE_FRAGMENT, LOGIN_FRAGMENT);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}

		if(megaApi!=null){
			megaApi.addGlobalListener(this);
		}

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

		setContentView(dragToExit.wrapContentView(R.layout.activity_chat_full_screen_image_viewer));

		relativeImageViewerLayout = (RelativeLayout) findViewById(R.id.full_image_viewer_layout);
		fragmentContainer = (RelativeLayout) findViewById(R.id.chat_full_image_viewer_parent_layout);
		appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
		viewPager = (ExtendedViewPager) findViewById(R.id.image_viewer_pager);
		viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			// optional
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

			// optional
			@Override
			public void onPageSelected(int position) {
				logDebug("Position: " + position);
				supportInvalidateOptionsMenu();
			}

			// optional
			@Override
			public void onPageScrollStateChanged(int state) { }
		});

		viewPager.setPageMargin(40);

		tB = findViewById(R.id.call_toolbar);
		if (tB == null) {
			logWarning("Tb is Null");
			return;
		}

		tB.setVisibility(View.VISIBLE);
		setSupportActionBar(tB);
		aB = getSupportActionBar();
		aB.setHomeButtonEnabled(true);
		aB.setDisplayHomeAsUpEnabled(true);
		aB.setTitle(" ");

		Intent intent = getIntent();
		positionG = intent.getIntExtra("position", 0);

		messageIds = intent.getLongArrayExtra("messageIds");
		chatId = intent.getLongExtra("chatId", -1);

		messages = new ArrayList<MegaChatMessage>();

		imageHandles = new ArrayList<Long>();

		if(messageIds==null){
			return;
		}

		for(int j=0; j<messageIds.length; j++){
			MegaChatMessage message = megaChatApi.getMessage(chatId, messageIds[j]);
			if(message==null){
				message = megaChatApi.getMessageFromNodeHistory(chatId, messageIds[j]);
			}

			if(message!=null){
				MegaNodeList list = message.getMegaNodeList();
				if(list.size()==1){
					MegaNode node = list.get(0);
					if(MimeTypeList.typeForName(node.getName()).isImage()){
						messages.add(message);
					}
				}
				else{
					logWarning("Messages with more than one attachment - do not supported");
				}
			}
			else{
				logError("ERROR - the message is NULL");
			}
		}

		if(messages.size() == 0)
		{
			finish();
			return;
		}

		int imageNumber = 0;
		for (int i=0;i<messages.size();i++){
			MegaNode n = messages.get(i).getMegaNodeList().get(0);
			if (MimeTypeList.typeForName(n.getName()).isImage()){
				imageHandles.add(n.getHandle());
				if (i == positionG){
					positionG = imageNumber;
				}
				imageNumber++;
			}
		}

		if(positionG >= imageHandles.size())
		{
			positionG = 0;
		}

		adapterMega = new MegaChatFullScreenImageAdapter(this, fullScreenImageViewer,messages, megaApi);

		viewPager.setAdapter(adapterMega);

		viewPager.setCurrentItem(positionG);

		viewPager.setOnPageChangeListener(this);

		bottomLayout = (RelativeLayout) findViewById(R.id.chat_image_viewer_layout_bottom);
		fileNameTextView = (TextView) findViewById(R.id.chat_full_image_viewer_file_name);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			fileNameTextView.setMaxWidth(scaleWidthPx(300, outMetrics));
		}
		else{
			fileNameTextView.setMaxWidth(scaleWidthPx(300, outMetrics));
		}
		fileNameTextView.setText(messages.get(positionG).getMegaNodeList().get(0).getName());

		if (isDeleteDialogShow && chatId != -1 && messages.get(positionG) != null) {
			showConfirmationDeleteNode(chatId, messages.get(positionG));
		}

		if (savedInstanceState == null && adapterMega!= null) {
			dragToExit.runEnterAnimation(intent, viewPager, animationStart -> {
				if (animationStart) {
					updateViewForAnimation();
				} else if (!isFinishing()) {
					showActionBar();

					fragmentContainer.setBackgroundColor(BLACK);
					relativeImageViewerLayout.setBackgroundColor(BLACK);
					appBarLayout.setBackgroundColor(BLACK);
				}

				return null;
			});
		} else {
			fromChatSavedInstance = true;
		}
	}

	@Override
	public void onPageSelected(int position) {
		return;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		return;
	}

	@Override
	public void onPageScrollStateChanged(int state) {

		if (state == ViewPager.SCROLL_STATE_IDLE){
			if (viewPager.getCurrentItem() != positionG){
				int oldPosition = positionG;
				int newPosition = viewPager.getCurrentItem();
				positionG = newPosition;

				try{
					TouchImageView tIV = (TouchImageView) adapterMega.getVisibleImage(oldPosition);
					if (tIV != null){
						tIV.setZoom(1);
					}
				}catch(Exception e){}
				fileNameTextView.setText(messages.get(positionG).getMegaNodeList().get(0).getName());
			}
		}
	}

	public void importNode(MegaNode node){
		logDebug("Node Handle: " + node.getHandle());

		nodeToImport = node;
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_IMPORT_FOLDER);
		startActivityForResult(intent, REQUEST_CODE_SELECT_IMPORT_FOLDER);
	}

	@Override
	public void onSaveInstanceState (Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		if (getIntent() != null) {
			getIntent().putExtra("position", positionG);
		}
		savedInstanceState.putBoolean("aBshown", adapterMega.isaBshown());
		savedInstanceState.putBoolean("overflowVisible", adapterMega.isMenuVisible());
		savedInstanceState.putBoolean("isDeleteDialogShow", isDeleteDialogShow);

		nodeSaver.saveState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);

		aBshown = savedInstanceState.getBoolean("aBshown");
		adapterMega.setaBshown(aBshown);
	}

	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		logDebug("onRequestStart: " + request.getRequestString());
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {

		logDebug("onRequestFinish: " + e.getErrorCode());
		if(request.getType() == MegaRequest.TYPE_COPY){
			if (e.getErrorCode() != MegaError.API_OK) {

				logWarning("e.getErrorCode() != MegaError.API_OK");

				if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
					logWarning("OVERQUOTA ERROR: "+e.getErrorCode());
					Intent intent = new Intent(this, ManagerActivityLollipop.class);
					intent.setAction(ACTION_OVERQUOTA_STORAGE);
					startActivity(intent);
					finish();
				}
				else if(e.getErrorCode()==MegaError.API_EGOINGOVERQUOTA){
					logWarning("OVERQUOTA ERROR: "+e.getErrorCode());
					Intent intent = new Intent(this, ManagerActivityLollipop.class);
					intent.setAction(ACTION_PRE_OVERQUOTA_STORAGE);
					startActivity(intent);
					finish();
				}
				else if(e.getErrorCode()==MegaError.API_ENOENT){
					showSnackbar(SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, 1, 1));
				}
				else
				{
					showSnackbar(SNACKBAR_TYPE, getString(R.string.import_success_error));
				}

			}else{
				showSnackbar(SNACKBAR_TYPE, getString(R.string.import_success_message));
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		logWarning("onRequestTemporaryError: " + request.getRequestString());
	}

	public void showConfirmationDeleteNode(final long chatId, final MegaChatMessage message){
		logDebug("showConfirmationDeleteNode");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						chatC.deleteMessage(message, chatId);
						isDeleteDialogShow = false;
						finish();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						isDeleteDialogShow = false;
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Mega_MaterialAlertDialog);
		builder.setMessage(R.string.confirmation_delete_one_attachment);

		builder.setPositiveButton(R.string.context_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

		isDeleteDialogShow = true;

		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				isDeleteDialogShow = false;
			}
		});
	}

    private Uri extractUri(Intent intent, int resultCode) {
        if (intent == null) {
            logWarning("extractUri: result intent is null");
            if (resultCode != Activity.RESULT_OK) {
                showSnackbar(SNACKBAR_TYPE, getString(R.string.download_requires_permission));
            } else {
                showSnackbar(SNACKBAR_TYPE, getString(R.string.no_external_SD_card_detected));
            }
            return null;
        }
        return intent.getData();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent == null) {
			return;
		}

		if (nodeSaver.handleActivityResult(requestCode, resultCode, intent)) {
			return;
		}

		if (requestCode == REQUEST_CODE_SELECT_IMPORT_FOLDER && resultCode == RESULT_OK) {
			logDebug("REQUEST_CODE_SELECT_IMPORT_FOLDER OK");

			if (!isOnline(this) || megaApi == null) {
				try {
					statusDialog.dismiss();
				} catch (Exception ex) {
				}

				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem));
				return;
			}

			final long toHandle = intent.getLongExtra("IMPORT_TO", 0);

			MegaNode target = null;
			target = megaApi.getNodeByHandle(toHandle);
			if (target == null) {
				target = megaApi.getRootNode();
			}
			logDebug("TARGET HANDLE: " + target.getHandle());
			if (nodeToImport != null) {
				logDebug("DOCUMENT HANDLE: " + nodeToImport.getHandle());
				if (target != null) {
					megaApi.copyNode(nodeToImport, target, this);
				} else {
					logError("TARGET: null");
					showSnackbar(SNACKBAR_TYPE, getString(R.string.import_success_error));
				}
			} else {
				logError("DOCUMENT: null");
				showSnackbar(SNACKBAR_TYPE, getString(R.string.import_success_error));
			}

		}
	}
	

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {}
	
	public void downloadTo(String parentPath, String url, long size, long [] hashes){

		if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
			showOverDiskQuotaPaywallWarning();
			return;
		}

		if(!isOnline(this)||megaApi==null) {
			try{
				statusDialog.dismiss();
			} catch(Exception ex) {};

			showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem));
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						REQUEST_WRITE_STORAGE);
			}
		}
		
		double availableFreeSpace = Double.MAX_VALUE;
		try{
			StatFs stat = new StatFs(parentPath);
			availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
		}
		catch(Exception ex){}
		
		
		if (hashes == null){
			if(url != null) {
				if(availableFreeSpace < size) {
					showSnackbar(NOT_SPACE_SNACKBAR_TYPE, null);
					return;
				}
				
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_URL, url);
				service.putExtra(DownloadService.EXTRA_SIZE, size);
				service.putExtra(DownloadService.EXTRA_PATH, parentPath);
				service.putExtra(DownloadService.EXTRA_FOLDER_LINK, false);
				startService(service);
			}
		}
		else{
			if(hashes.length == 1){
				MegaNode tempNode = megaApi.getNodeByHandle(hashes[0]);
				if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
					logDebug("ISFILE");
					String localPath = getLocalFile(this, tempNode.getName(), tempNode.getSize());
					if(localPath != null){	
						try { 
							copyFile(new File(localPath), new File(parentPath, tempNode.getName()));
						}
						catch(Exception e) {}

						try {

							Intent viewIntent = new Intent(Intent.ACTION_VIEW);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								viewIntent.setDataAndType(FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							} else {
								viewIntent.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							}
							viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							if (isIntentAvailable(this, viewIntent))
								startActivity(viewIntent);
							else {
								Intent intentShare = new Intent(Intent.ACTION_SEND);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
									intentShare.setDataAndType(FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
								} else {
									intentShare.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
								}
								intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								if (isIntentAvailable(this, intentShare))
									startActivity(intentShare);
								String message = getString(R.string.general_already_downloaded) + ": " + localPath;
								showSnackbar(SNACKBAR_TYPE, message);
							}
						}
						catch (Exception e){
							String message = getString(R.string.general_already_downloaded) + ": " + localPath;
							showSnackbar(SNACKBAR_TYPE, message);
						}
						return;
					}
				}
			}
			
			for (long hash : hashes) {
				MegaNode node = megaApi.getNodeByHandle(hash);
				if(node != null){
					Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
					if (node.getType() == MegaNode.TYPE_FOLDER) {
//						getDlList(dlFiles, node, new File(parentPath, new String(node.getName())));
					} else {
						dlFiles.put(node, parentPath);
					}
					
					for (MegaNode document : dlFiles.keySet()) {
						
						String path = dlFiles.get(document);
						
						if(availableFreeSpace < document.getSize()){
							showSnackbar(NOT_SPACE_SNACKBAR_TYPE, null);
							continue;
						}
						
						Intent service = new Intent(this, DownloadService.class);
						service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
						service.putExtra(DownloadService.EXTRA_URL, url);
						service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
						service.putExtra(DownloadService.EXTRA_PATH, path);
						service.putExtra(DownloadService.EXTRA_FOLDER_LINK, false);
						startService(service);
					}
				}
				else if(url != null) {
					if(availableFreeSpace < size) {
						showSnackbar(NOT_SPACE_SNACKBAR_TYPE, null);
						continue;
					}
					
					Intent service = new Intent(this, DownloadService.class);
					service.putExtra(DownloadService.EXTRA_HASH, hash);
					service.putExtra(DownloadService.EXTRA_URL, url);
					service.putExtra(DownloadService.EXTRA_SIZE, size);
					service.putExtra(DownloadService.EXTRA_PATH, parentPath);
					service.putExtra(DownloadService.EXTRA_FOLDER_LINK, false);
					startService(service);
				}
				else {
					logWarning("Node not found");
				}
			}
		}
	}

	public void showSnackbar(int type, String s){
		showSnackbar(type, fragmentContainer, s);
	}

	public void touchImage() {
		logDebug("touchImage");
		if(aB.isShowing()){
			hideActionBar();
		}else{
			showActionBar();
		}
	}

	protected void hideActionBar(){
		if (aB != null && aB.isShowing()) {
			if(tB != null) {
				tB.animate().translationY(-220).setDuration(ANIMATION_DURATION)
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								aB.hide();
							}
						}).start();
				bottomLayout.animate().translationY(220).setDuration(ANIMATION_DURATION).start();
			} else {
				aB.hide();
			}
		}
	}
	protected void showActionBar(){
		if (aB != null && !aB.isShowing()) {
			aB.show();
			if(tB != null) {
				tB.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
				bottomLayout.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
			}

		}
	}
	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {}

	@Override
	public void onUserAlertsUpdate(MegaApiJava api, ArrayList<MegaUserAlert> userAlerts) {
		logDebug("onUserAlertsUpdate");
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> nodeList) {}

	@Override
	public void onReloadNeeded(MegaApiJava api) {}

	@Override
	public void onAccountUpdate(MegaApiJava api) {}

	@Override
	public void onEvent(MegaApiJava api, MegaEvent event) {

	}

	@Override
	public void onContactRequestsUpdate(MegaApiJava api, ArrayList<MegaContactRequest> requests) {}

	public void setNormalizedScale(float normalizedScale) {
		dragToExit.setNormalizedScale(normalizedScale);
	}

	public void setDraggable(boolean draggable) {
		dragToExit.setDraggable(draggable);
	}

	private Unit onDragActivated(boolean activated) {
		logDebug("activated: " + activated);

		if (activated) {
			updateViewForAnimation();

			if (fromChatSavedInstance) {
				dragToExit.setCurrentView(null);
			} else {
				dragToExit.setCurrentView(adapterMega.getVisibleImage(positionG));
			}
		} else {
			handler.postDelayed(() -> {
				fragmentContainer.setBackgroundColor(BLACK);
				relativeImageViewerLayout.setBackgroundColor(BLACK);
				appBarLayout.setBackgroundColor(BLACK);
			}, 300);
		}

		return null;
	}

	private void updateViewForAnimation() {
		if (aB != null && aB.isShowing()) {
			if(tB != null) {
				tB.animate().translationY(-220).setDuration(0)
						.withEndAction(() -> aB.hide()).start();
				bottomLayout.animate().translationY(220).setDuration(0).start();
			} else {
				aB.hide();
			}
		}

		fragmentContainer.setBackgroundColor(TRANSPARENT);
		relativeImageViewerLayout.setBackgroundColor(TRANSPARENT);
		appBarLayout.setBackgroundColor(TRANSPARENT);

		fragmentContainer.setElevation(0);
		relativeImageViewerLayout.setElevation(0);
		appBarLayout.setElevation(0);
	}

	@Override
	public void showSnackbar(int type, @Nullable String content, long chatId) {
		showSnackbar(type, fragmentContainer, content, chatId);
	}
}
