package mega.privacy.android.app.lollipop.managerSections;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.CustomizedGridLayoutManager;
import mega.privacy.android.app.components.NewGridRecyclerView;
import mega.privacy.android.app.components.NewHeaderItemDecoration;
import mega.privacy.android.app.components.scrollBar.FastScroller;
import mega.privacy.android.app.globalmanagement.SortOrderManagement;
import mega.privacy.android.app.lollipop.FullScreenImageViewerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaNodeAdapter;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.utils.CloudStorageOptionControlUtil;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.utils.MegaNodeUtil;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;

import static mega.privacy.android.app.components.dragger.DragToExitSupport.observeDragSupportEvents;
import static mega.privacy.android.app.components.dragger.DragToExitSupport.putThumbnailLocation;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.FileUtil.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.MegaApiUtils.*;
import static mega.privacy.android.app.utils.MegaNodeUtil.manageTextFileIntent;
import static mega.privacy.android.app.utils.TimeUtils.*;
import static mega.privacy.android.app.utils.Util.*;

@AndroidEntryPoint
public class FileBrowserFragmentLollipop extends RotatableFragment{

	@Inject
	SortOrderManagement sortOrderManagement;

	Context context;
	ActionBar aB;
	RecyclerView recyclerView;
	FastScroller fastScroller;

	ImageView emptyImageView;
	LinearLayout emptyTextView;
	TextView emptyTextViewFirst;

    MegaNodeAdapter adapter;

	public int pendingTransfers = 0;
	public int totalTransfers = 0;
	public long totalSizePendingTransfer=0;
	public long totalSizeTransfered=0;

	Stack<Integer> lastPositionStack;

	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;

	public NewHeaderItemDecoration headerItemDecoration;

	float density;
	DisplayMetrics outMetrics;
	Display display;

	DatabaseHandler dbH;
	MegaPreferences prefs;

	ArrayList<MegaNode> nodes;
	public ActionMode actionMode;

	LinearLayoutManager mLayoutManager;
	CustomizedGridLayoutManager gridLayoutManager;

	String downloadLocationDefaultPath;
    
    private int placeholderCount;

    private RelativeLayout transferOverQuotaBanner;
    private TextView transferOverQuotaBannerText;

    private static final String AD_SLOT = "and1";

	@Override
	protected MegaNodeAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void activateActionMode(){
		logDebug("activateActionMode");
		if (!adapter.isMultipleSelect()){
			adapter.setMultipleSelect(true);
			actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
		}
	}

	private class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			logDebug("onActionItemClicked");
			List<MegaNode> documents = adapter.getSelectedNodes();
			switch(item.getItemId()){
				case R.id.cab_menu_download:{
					((ManagerActivityLollipop) context).saveNodesToDevice(
							documents, false, false, false, false);

					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_rename:{

					if (documents.size()==1){
						((ManagerActivityLollipop) context).showRenameDialog(documents.get(0));
					}
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_copy:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToCopyNodes(handleList);
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_move:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToMoveNodes(handleList);
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_share_folder:{
					//Check that all the selected options are folders
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						if(documents.get(i).isFolder()){
							handleList.add(documents.get(i).getHandle());
						}
					}

					NodeController nC = new NodeController(context);
					nC.selectContactToShareFolders(handleList);

					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_share_out: {
					MegaNodeUtil.shareNodes(context, documents);
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_share_link:
				case R.id.cab_menu_edit_link: {

					logDebug("Public link option");
					if (documents.get(0) == null) {
						logWarning("The selected node is NULL");
						break;
					}
					((ManagerActivityLollipop) context).showGetLinkActivity(
							documents.get(0).getHandle());
					clearSelections();
					hideMultipleSelect();

					break;
				}
				case R.id.cab_menu_remove_link:{

					logDebug("Remove public link option");
					if(documents.get(0)==null){
						logWarning("The selected node is NULL");
						break;
					}
					((ManagerActivityLollipop) context).showConfirmationRemovePublicLink(documents.get(0));
					clearSelections();
					hideMultipleSelect();

					break;
				}
				case R.id.cab_menu_send_to_chat:{
					logDebug("Send files to chat");
					((ManagerActivityLollipop) context).attachNodesToChats(adapter.getArrayListSelectedNodes());
					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_trash:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					((ManagerActivityLollipop) context).askConfirmationMoveToRubbish(handleList);

					break;
				}
				case R.id.cab_menu_select_all:{
					selectAll();
					break;
				}
				case R.id.cab_menu_clear_selection:{
					clearSelections();
					hideMultipleSelect();
					break;
				}

				case R.id.cab_menu_remove_share:
					((ManagerActivityLollipop) context).showConfirmationRemoveAllSharingContacts(documents);
					break;

				case R.id.cab_menu_save_gallery:
					((ManagerActivityLollipop) context).saveNodesToGallery(adapter.getArrayListSelectedNodes());
					clearSelections();
					hideMultipleSelect();
					break;
			}
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			logDebug("onCreateActionMode");
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cloud_storage_action, menu);
			((ManagerActivityLollipop)context).hideFabButton();
			((ManagerActivityLollipop) context).showHideBottomNavigationView(true);
			checkScroll();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			logDebug("onDestroyActionMode");
			clearSelections();
			adapter.setMultipleSelect(false);
			((ManagerActivityLollipop)context).showFabButton();
			((ManagerActivityLollipop) context).showHideBottomNavigationView(false);
			checkScroll();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			logDebug("onPrepareActionMode");
			List<MegaNode> selected = adapter.getSelectedNodes();
			if (selected.isEmpty()) {
				return false;
			}

			CloudStorageOptionControlUtil.Control control =
					new CloudStorageOptionControlUtil.Control();

			if (selected.size() == 1) {
				if (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_OWNER).getErrorCode()
						== MegaError.API_OK) {
					if (selected.get(0).isExported()) {
						control.manageLink().setVisible(true)
								.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

						control.removeLink().setVisible(true);
					} else {
						control.getLink().setVisible(true)
								.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					}
				}

				if (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode()
						== MegaError.API_OK) {
					control.rename().setVisible(true);
				}
			}

			boolean showSendToChat = true;
			boolean showShareFolder = true;
			boolean showTrash = true;
			boolean showRemoveShare = true;
			int mediaCounter = 0;

			for (MegaNode node : selected) {
				if (!node.isFile()) {
					showSendToChat = false;
				} else {
					MimeTypeList nodeMime = MimeTypeList.typeForName(node.getName());
					if (nodeMime.isImage() || nodeMime.isVideo()) {
						mediaCounter++;
					}
				}
				if (!node.isFolder() || (MegaNodeUtil.isOutShare(node) && selected.size() > 1)) {
					showShareFolder = false;
				}
				if (megaApi.checkMove(node, megaApi.getRubbishNode()).getErrorCode()
						!= MegaError.API_OK) {
					showTrash = false;
				}

				if (!node.isFolder() ||  !MegaNodeUtil.isOutShare(node)) {
					showRemoveShare = false;
				}
			}

			if (showSendToChat) {
				control.sendToChat().setVisible(true)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			if (showShareFolder) {
				control.shareFolder().setVisible(true)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			if (showRemoveShare) {
				control.removeShare().setVisible(true);
			}

			if (mediaCounter == selected.size()) {
				control.saveToGallery().setVisible(true)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			} else {
				control.saveToGallery().setVisible(false);
			}

			control.trash().setVisible(showTrash);

			control.shareOut().setVisible(true)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			control.move().setVisible(true);
			control.copy().setVisible(true);
			if (selected.size() > 1) {
				control.move().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			if (control.alwaysActionCount() < CloudStorageOptionControlUtil.MAX_ACTION_COUNT) {
				control.copy().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			control.selectAll()
					.setVisible(selected.size()
							< adapter.getItemCount() - adapter.getPlaceholderCount());

			CloudStorageOptionControlUtil.applyControl(menu, control);

			return true;
		}
	}

	public static FileBrowserFragmentLollipop newInstance() {
		logDebug("newInstance");
		FileBrowserFragmentLollipop fragment = new FileBrowserFragmentLollipop();
		return fragment;
	}

	@Override
	public void onCreate (Bundle savedInstanceState){
		logDebug("onCreate");
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		dbH = DatabaseHandler.getDbHandler(context);
		downloadLocationDefaultPath = getDownloadLocation();
		lastPositionStack = new Stack<>();

		if (megaChatApi == null) {
			megaChatApi = ((MegaApplication) ((Activity) context).getApplication()).getMegaChatApi();
		}

		initAdsLoader(AD_SLOT, true);

		super.onCreate(savedInstanceState);
		logDebug("After onCreate called super");
	}

	public void checkScroll() {
		if (recyclerView == null) return;

		boolean visible = (adapter != null && adapter.isMultipleSelect())
				|| MegaApplication.getTransfersManagement().isTransferOverQuotaBannerShown()
				|| (recyclerView.canScrollVertically(-1) && recyclerView.getVisibility() == View.VISIBLE);

		((ManagerActivityLollipop) context).changeAppBarElevation(visible);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		logDebug("onCreateView");
		if (!isAdded()) {
			return null;
		}

		logDebug("Fragment ADDED");

		if (megaApi == null) {
			megaApi = ((MegaApplication) ((Activity) context).getApplication()).getMegaApi();
		}

		if (aB == null) {
			aB = ((AppCompatActivity) context).getSupportActionBar();
		}

		if (megaApi.getRootNode() == null) {
			return null;
		}

		display = ((Activity) context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		density = getResources().getDisplayMetrics().density;

		if (((ManagerActivityLollipop) context).getParentHandleBrowser() == -1 || ((ManagerActivityLollipop) context).getParentHandleBrowser() == megaApi.getRootNode().getHandle()) {
			logWarning("After consulting... the parent keeps -1 or ROOTNODE: " + ((ManagerActivityLollipop) context).getParentHandleBrowser());

			nodes = megaApi.getChildren(megaApi.getRootNode(), sortOrderManagement.getOrderCloud());
		} else {
			MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop) context).getParentHandleBrowser());

			nodes = megaApi.getChildren(parentNode, sortOrderManagement.getOrderCloud());
		}
		((ManagerActivityLollipop) context).setToolbarTitle();
		((ManagerActivityLollipop) context).supportInvalidateOptionsMenu();

		View v;

		if (((ManagerActivityLollipop) context).isList) {
			logDebug("isList");
			v = inflater.inflate(R.layout.fragment_filebrowserlist, container, false);

			recyclerView = v.findViewById(R.id.file_list_view_browser);
			fastScroller = v.findViewById(R.id.fastscroll);
			recyclerView.setPadding(0, 0, 0, scaleHeightPx(85, outMetrics));
			recyclerView.setClipToPadding(false);

			mLayoutManager = new LinearLayoutManager(context);
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			recyclerView.setLayoutManager(mLayoutManager);
			recyclerView.setHasFixedSize(true);
			recyclerView.setItemAnimator(noChangeRecyclerViewItemAnimator());
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					checkScroll();
				}
			});

			emptyImageView = v.findViewById(R.id.file_list_empty_image);
			emptyTextView = v.findViewById(R.id.file_list_empty_text);
			emptyTextViewFirst = v.findViewById(R.id.file_list_empty_text_first);

			if (adapter == null){
				adapter = new MegaNodeAdapter(context, this, nodes, ((ManagerActivityLollipop)context).getParentHandleBrowser(), recyclerView, aB, FILE_BROWSER_ADAPTER, MegaNodeAdapter.ITEM_VIEW_TYPE_LIST);
			}
			else{
				adapter.setParentHandle(((ManagerActivityLollipop)context).getParentHandleBrowser());
				adapter.setListFragment(recyclerView);
				adapter.setAdapterType(MegaNodeAdapter.ITEM_VIEW_TYPE_LIST);
            }

            adapter.setMultipleSelect(false);

            recyclerView.setAdapter(adapter);
            fastScroller.setRecyclerView(recyclerView);

            setNodes(nodes);

            if (adapter.getItemCount() == 0) {
				logDebug("itemCount is 0");
                recyclerView.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
				logDebug("itemCount is " + adapter.getItemCount());
                recyclerView.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
            }
        } else {
			logDebug("Grid View");
            v = inflater.inflate(R.layout.fragment_filebrowsergrid,container,false);
            recyclerView = (NewGridRecyclerView)v.findViewById(R.id.file_grid_view_browser);
            fastScroller = v.findViewById(R.id.fastscroll);
            
            recyclerView.setPadding(0,0,0,scaleHeightPx(80,outMetrics));
            
            recyclerView.setClipToPadding(false);
            recyclerView.setHasFixedSize(true);
            
            gridLayoutManager = (CustomizedGridLayoutManager)recyclerView.getLayoutManager();
            recyclerView.setItemAnimator(new DefaultItemAnimator());
			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					super.onScrolled(recyclerView, dx, dy);
					checkScroll();
				}
			});
            
            emptyImageView = v.findViewById(R.id.file_grid_empty_image);
            emptyTextView = v.findViewById(R.id.file_grid_empty_text);
            emptyTextViewFirst = v.findViewById(R.id.file_grid_empty_text_first);

            if (adapter == null) {
                adapter = new MegaNodeAdapter(context,this,nodes,((ManagerActivityLollipop)context).getParentHandleBrowser(),recyclerView,aB,FILE_BROWSER_ADAPTER,MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);
            } else {
                adapter.setParentHandle(((ManagerActivityLollipop)context).getParentHandleBrowser());
                adapter.setListFragment(recyclerView);
                adapter.setAdapterType(MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);
            }

            adapter.setMultipleSelect(false);
            
            recyclerView.setAdapter(adapter);
            fastScroller.setRecyclerView(recyclerView);
            setNodes(nodes);
            
            if (adapter.getItemCount() == 0) {
                recyclerView.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
            }
        }

		transferOverQuotaBanner = v.findViewById(R.id.transfer_over_quota_banner);
		transferOverQuotaBannerText = v.findViewById(R.id.banner_content_text);
		v.findViewById(R.id.banner_dismiss_button).setOnClickListener(v1 -> hideTransferOverQuotaBanner());
		v.findViewById(R.id.banner_upgrade_button).setOnClickListener(v12 -> {
			hideTransferOverQuotaBanner();
			((ManagerActivityLollipop) context).navigateToUpgradeAccount();
		});

		setTransferOverQuotaBannerVisibility();

		mAdsLoader.setAdViewContainer(v.findViewById(R.id.ad_view_container),
				((ManagerActivityLollipop) context).getOutMetrics());

		return v;
    }

	@Override
	public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		observeDragSupportEvents(getViewLifecycleOwner(), recyclerView, VIEWER_FROM_FILE_BROWSER);
	}

	@Override
    public void onAttach(Activity activity) {
		logDebug("onAttach");
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
		logDebug("onAttach");
        
        super.onAttach(context);
        this.context = context;
        aB = ((AppCompatActivity)context).getSupportActionBar();
    }

    @Override
	public void onDestroy() {
		if (adapter != null) {
			adapter.clearTakenDownDialog();
		}

		super.onDestroy();
	}

	public void openFile(MegaNode node, int position) {
		if (MimeTypeList.typeForName(node.getName()).isImage()) {
			Intent intent = new Intent(context, FullScreenImageViewerLollipop.class);
			//Put flag to notify FullScreenImageViewerLollipop.
			intent.putExtra("placeholder", placeholderCount);
			intent.putExtra("position", position);
			intent.putExtra("adapterType", FILE_BROWSER_ADAPTER);
			intent.putExtra("isFolderLink", false);
			if (megaApi.getParentNode(node).getType() == MegaNode.TYPE_ROOT) {
				intent.putExtra("parentNodeHandle", -1L);
			} else {
				intent.putExtra("parentNodeHandle", megaApi.getParentNode(node).getHandle());
			}

			intent.putExtra("orderGetChildren", sortOrderManagement.getOrderCloud());

			intent.putExtra(INTENT_EXTRA_KEY_HANDLE, node.getHandle());
			putThumbnailLocation(intent, recyclerView, position, VIEWER_FROM_FILE_BROWSER, adapter);

			context.startActivity(intent);
			((ManagerActivityLollipop) context).overridePendingTransition(0, 0);
		} else if (MimeTypeList.typeForName(node.getName()).isVideoReproducible() || MimeTypeList.typeForName(node.getName()).isAudio()) {
			MegaNode file = node;

			String mimeType = MimeTypeList.typeForName(file.getName()).getType();

			Intent mediaIntent;
			boolean internalIntent;
			boolean opusFile = false;
			if (MimeTypeList.typeForName(file.getName()).isVideoNotSupported() || MimeTypeList.typeForName(file.getName()).isAudioNotSupported()) {
				mediaIntent = new Intent(Intent.ACTION_VIEW);
				internalIntent = false;
				String[] s = file.getName().split("\\.");
				if (s != null && s.length > 1 && s[s.length - 1].equals("opus")) {
					opusFile = true;
				}
			} else {
				mediaIntent = getMediaIntent(context, node.getName());
				internalIntent = true;
			}
			mediaIntent.putExtra("position", position);
			mediaIntent.putExtra("placeholder", placeholderCount);
			if (megaApi.getParentNode(node).getType() == MegaNode.TYPE_ROOT) {
				mediaIntent.putExtra("parentNodeHandle", -1L);
			} else {
				mediaIntent.putExtra("parentNodeHandle", megaApi.getParentNode(node).getHandle());
			}
			mediaIntent.putExtra("orderGetChildren", sortOrderManagement.getOrderCloud());
			mediaIntent.putExtra("adapterType", FILE_BROWSER_ADAPTER);
			putThumbnailLocation(mediaIntent, recyclerView, position, VIEWER_FROM_FILE_BROWSER, adapter);

			mediaIntent.putExtra("FILENAME", file.getName());

			String localPath = getLocalFile(context, file.getName(), file.getSize());

			if (localPath != null) {
				File mediaFile = new File(localPath);
				//mediaIntent.setDataAndType(Uri.parse(localPath), mimeType);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().getPath())) {
					logDebug("itemClick:FileProviderOption");
					Uri mediaFileUri = FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile);
					if (mediaFileUri == null) {
						logDebug("itemClick:ERROR:NULLmediaFileUri");
						((ManagerActivityLollipop) context).showSnackbar(SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
					} else {
						mediaIntent.setDataAndType(mediaFileUri, MimeTypeList.typeForName(file.getName()).getType());
					}
				} else {
					Uri mediaFileUri = Uri.fromFile(mediaFile);
					if (mediaFileUri == null) {
						logError("itemClick:ERROR:NULLmediaFileUri");
						((ManagerActivityLollipop) context).showSnackbar(SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
					} else {
						mediaIntent.setDataAndType(mediaFileUri, MimeTypeList.typeForName(file.getName()).getType());
					}
				}
				mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			} else {
				logDebug("itemClick:localPathNULL");

				if (megaApi.httpServerIsRunning() == 0) {
					megaApi.httpServerStart();
					mediaIntent.putExtra(INTENT_EXTRA_KEY_NEED_STOP_HTTP_SERVER, true);
				} else {
					logWarning("itemClick:ERROR:httpServerAlreadyRunning");
				}

				ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
				ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				activityManager.getMemoryInfo(mi);

				if (mi.totalMem > BUFFER_COMP) {
					logDebug("itemClick:total mem: " + mi.totalMem + " allocate 32 MB");
					megaApi.httpServerSetMaxBufferSize(MAX_BUFFER_32MB);
				} else {
					logDebug("itemClick:total mem: " + mi.totalMem + " allocate 16 MB");
					megaApi.httpServerSetMaxBufferSize(MAX_BUFFER_16MB);
				}

				String url = megaApi.httpServerGetLocalLink(file);
				if (url != null) {
					Uri parsedUri = Uri.parse(url);
					if (parsedUri != null) {
						mediaIntent.setDataAndType(parsedUri, mimeType);
					} else {
						logError("itemClick:ERROR:httpServerGetLocalLink");
						((ManagerActivityLollipop) context).showSnackbar(SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
					}
				} else {
					logError("itemClick:ERROR:httpServerGetLocalLink");
					((ManagerActivityLollipop) context).showSnackbar(SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
				}
			}
			mediaIntent.putExtra("HANDLE", file.getHandle());
			if (opusFile) {
				mediaIntent.setDataAndType(mediaIntent.getData(), "audio/*");
			}
			if (internalIntent) {
				context.startActivity(mediaIntent);
			} else {
				logDebug("itemClick:externalIntent");
				if (isIntentAvailable(context, mediaIntent)) {
					context.startActivity(mediaIntent);
				} else {
					logWarning("itemClick:noAvailableIntent");
					((ManagerActivityLollipop) context).showSnackbar(SNACKBAR_TYPE, getString(R.string.intent_not_available), -1);

					((ManagerActivityLollipop) context).saveNodesToDevice(
							Collections.singletonList(node),
							true, false, false, false);
				}
			}
			((ManagerActivityLollipop) context).overridePendingTransition(0, 0);
		} else if (MimeTypeList.typeForName(node.getName()).isURL()) {
			logDebug("Is URL file");
			MegaNode file = node;

			String localPath = getLocalFile(context, file.getName(), file.getSize());

			if (localPath != null) {
				File mediaFile = new File(localPath);
				InputStream instream = null;

				try {
					// open the file for reading
					instream = new FileInputStream(mediaFile.getAbsolutePath());

					// if file the available for reading
					if (instream != null) {
						// prepare the file for reading
						InputStreamReader inputreader = new InputStreamReader(instream);
						BufferedReader buffreader = new BufferedReader(inputreader);

						String line1 = buffreader.readLine();
						if (line1 != null) {
							String line2 = buffreader.readLine();

							String url = line2.replace("URL=", "");

							logDebug("Is URL - launch browser intent");
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(url));
							startActivity(i);
						} else {
							logDebug("Not expected format: Exception on processing url file");
							Intent intent = new Intent(Intent.ACTION_VIEW);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								intent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), "text/plain");
							} else {
								intent.setDataAndType(Uri.fromFile(mediaFile), "text/plain");
							}
							intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							if (isIntentAvailable(context, intent)){
								startActivity(intent);
							} else {
								((ManagerActivityLollipop) context).saveNodesToDevice(
										Collections.singletonList(node),
										true, false, false, false);
							}
						}
					}
				} catch (Exception ex) {

					Intent intent = new Intent(Intent.ACTION_VIEW);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						intent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), "text/plain");
					} else {
						intent.setDataAndType(Uri.fromFile(mediaFile), "text/plain");
					}
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

					if (isIntentAvailable(context, intent)) {
						startActivity(intent);
					} else {
						((ManagerActivityLollipop) context).saveNodesToDevice(
								Collections.singletonList(node),
								true, false, false, false);
					}
				} finally {
					// close the file.
					try {
						instream.close();
					} catch (IOException e) {
						logError("EXCEPTION closing InputStream", e);
					}
				}
			} else {
				((ManagerActivityLollipop) context).saveNodesToDevice(
						Collections.singletonList(node),
						true, false, false, false);
			}
		} else if (MimeTypeList.typeForName(node.getName()).isPdf()) {
			logDebug("itemClick:isFile:isPdf");
			MegaNode file = node;

			String mimeType = MimeTypeList.typeForName(file.getName()).getType();

			Intent pdfIntent = new Intent(context, PdfViewerActivityLollipop.class);

			pdfIntent.putExtra("inside", true);
			pdfIntent.putExtra("adapterType", FILE_BROWSER_ADAPTER);

			String localPath = getLocalFile(context, file.getName(), file.getSize());

			if (localPath != null) {
				File mediaFile = new File(localPath);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().getPath())) {
					pdfIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), MimeTypeList.typeForName(file.getName()).getType());
				} else {
					pdfIntent.setDataAndType(Uri.fromFile(mediaFile), MimeTypeList.typeForName(file.getName()).getType());
				}
				pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			} else {
				if (megaApi.httpServerIsRunning() == 0) {
					megaApi.httpServerStart();
					pdfIntent.putExtra(INTENT_EXTRA_KEY_NEED_STOP_HTTP_SERVER, true);
				}

				ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
				ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				activityManager.getMemoryInfo(mi);

				if (mi.totalMem > BUFFER_COMP) {
					logDebug("Total mem: " + mi.totalMem + " allocate 32 MB");
					megaApi.httpServerSetMaxBufferSize(MAX_BUFFER_32MB);
				} else {
					logDebug("Total mem: " + mi.totalMem + " allocate 16 MB");
					megaApi.httpServerSetMaxBufferSize(MAX_BUFFER_16MB);
				}

				String url = megaApi.httpServerGetLocalLink(file);
				pdfIntent.setDataAndType(Uri.parse(url), mimeType);
			}
			pdfIntent.putExtra("HANDLE", file.getHandle());
			putThumbnailLocation(pdfIntent, recyclerView, position, VIEWER_FROM_FILE_BROWSER, adapter);
			if (isIntentAvailable(context, pdfIntent)) {
				context.startActivity(pdfIntent);
			} else {
				Toast.makeText(context, context.getResources().getString(R.string.intent_not_available), Toast.LENGTH_LONG).show();

				((ManagerActivityLollipop) context).saveNodesToDevice(
						Collections.singletonList(node),
						true, false, false, false);
			}
			((ManagerActivityLollipop) context).overridePendingTransition(0, 0);
		} else if (MimeTypeList.typeForName(node.getName()).isOpenableTextFile(node.getSize())) {
			manageTextFileIntent(context, node, FILE_BROWSER_ADAPTER);
		} else {
			logDebug("itemClick:isFile:otherOption");
			((ManagerActivityLollipop) context).saveNodesToDevice(
					Collections.singletonList(node),
					true, false, false, false);
		}
	}

    public void itemClick(int position) {
		logDebug("item click position: " + position);
        if (adapter.isMultipleSelect()) {
			logDebug("itemClick:multiselectON");
            adapter.toggleSelection(position);

            List<MegaNode> selectedNodes = adapter.getSelectedNodes();
            if (selectedNodes.size() > 0) {
                updateActionModeTitle();
            }
		}
		else{
			logDebug("itemClick:multiselectOFF");
			if (nodes.get(position).isFolder()){
				MegaNode n = nodes.get(position);

				int lastFirstVisiblePosition = 0;
				if(((ManagerActivityLollipop)context).isList){
					lastFirstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
					logDebug("lastFirstVisiblePosition: "+lastFirstVisiblePosition);
				}
				else{
					lastFirstVisiblePosition = ((NewGridRecyclerView) recyclerView).findFirstCompletelyVisibleItemPosition();
					if(lastFirstVisiblePosition==-1){
						logDebug("Completely -1 then find just visible position");
						lastFirstVisiblePosition = ((NewGridRecyclerView) recyclerView).findFirstVisibleItemPosition();
					}
				}

				logDebug("Push to stack "+lastFirstVisiblePosition+" position");
				lastPositionStack.push(lastFirstVisiblePosition);
				setFolderInfoNavigation(n);
			}
			else{
				//Is file
				openFile(nodes.get(position), position);
			}
		}
	}

	@Override
	public void multipleItemClick(int position) {
		adapter.toggleSelection(position);
	}

	@Override
	public void reselectUnHandledSingleItem(int position) {
		adapter.filClicked(position);
	}

	public void setFolderInfoNavigation(MegaNode n){
        ((ManagerActivityLollipop)context).setParentHandleBrowser(n.getHandle());
		((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
        ((ManagerActivityLollipop)context).setToolbarTitle();
        
        adapter.setParentHandle(((ManagerActivityLollipop)context).getParentHandleBrowser());
        nodes = megaApi.getChildren(n, sortOrderManagement.getOrderCloud());
        addSectionTitle(nodes,adapter.getAdapterType());
        adapter.setNodes(nodes);
        recyclerView.scrollToPosition(0);
        
        visibilityFastScroller();
        
        //If folder has no files
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyImageView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
            
            if (megaApi.getRootNode() != null && megaApi.getRootNode().getHandle() == n.getHandle()) {
                
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    emptyImageView.setImageResource(R.drawable.empty_cloud_drive_landscape);
                } else {
                    emptyImageView.setImageResource(R.drawable.empty_cloud_drive_portrait);
                }
                String textToShow = context.getString(R.string.context_empty_cloud_drive).toUpperCase();
                try {
                    textToShow = textToShow.replace("[A]","<font color=\'"
							+ ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
							+ "\'>");
                    textToShow = textToShow.replace("[/A]","</font>");
                    textToShow = textToShow.replace("[B]","<font color=\'"
							+ ColorUtils.getColorHexString(context, R.color.grey_300_grey_600)
							+ "\'>");
                    textToShow = textToShow.replace("[/B]","</font>");
                } catch (Exception e) {
                }
				Spanned result = HtmlCompat.fromHtml(textToShow, HtmlCompat.FROM_HTML_MODE_LEGACY);
                emptyTextViewFirst.setText(result);
                
            } else {
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    emptyImageView.setImageResource(R.drawable.empty_folder_landscape);
                } else {
                    emptyImageView.setImageResource(R.drawable.empty_folder_portrait);
                }
                String textToShow = context.getString(R.string.file_browser_empty_folder_new);
                try {
                    textToShow = textToShow.replace("[A]","<font color=\'"
							+ ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
							+ "\'>");
                    textToShow = textToShow.replace("[/A]","</font>");
                    textToShow = textToShow.replace("[B]","<font color=\'"
							+ ColorUtils.getColorHexString(context, R.color.grey_300_grey_600)
							+ "\'>");
                    textToShow = textToShow.replace("[/B]","</font>");
                } catch (Exception e) {
                }
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                emptyTextViewFirst.setText(result);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyImageView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
        }
        checkScroll();
    }
    
    public boolean showSelectMenuItem() {
		logDebug("showSelectMenuItem");
        if (adapter != null) {
            return adapter.isMultipleSelect();
        }
        
        return false;
    }
    
    public void selectAll() {
		logDebug("selectAll");
        if (adapter != null) {
            if (adapter.isMultipleSelect()) {
                adapter.selectAll();
            } else {
                adapter.setMultipleSelect(true);
                adapter.selectAll();
                
                actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
            }

			new Handler(Looper.getMainLooper()).post(() -> updateActionModeTitle());
        }
    }
    
    /*
     * Clear all selected items
     */
    private void clearSelections() {
        if (adapter.isMultipleSelect()) {
            adapter.clearSelections();
        }
    }

    @Override
    protected void updateActionModeTitle() {
		logDebug("updateActionModeTitle");
        if (actionMode == null || getActivity() == null) {
			logWarning("RETURN: null values");
            return;
        }
        
        List<MegaNode> documents = adapter.getSelectedNodes();
        int files = 0;
        int folders = 0;
        for (MegaNode document : documents) {
            if (document.isFile()) {
                files++;
            } else if (document.isFolder()) {
                folders++;
            }
        }
        
        String title;
        int sum = files + folders;
        
        if (files == 0 && folders == 0) {
            title = Integer.toString(sum);
        } else if (files == 0) {
            title = Integer.toString(folders);
        } else if (folders == 0) {
            title = Integer.toString(files);
        } else {
            title = Integer.toString(sum);
        }
        actionMode.setTitle(title);
        try {
            actionMode.invalidate();
        } catch (NullPointerException e) {
            e.printStackTrace();
			logError("Invalidate error", e);
        }
        
    }
    
    /*
     * Disable selection
     */
    public void hideMultipleSelect() {
		logDebug("hideMultipleSelect");
        adapter.setMultipleSelect(false);
        
        if (actionMode != null) {
            actionMode.finish();
        }
    }
    
    public int onBackPressed() {
		logDebug("onBackPressed");
        
        if (adapter != null) {
			logDebug("Parent Handle is: " + ((ManagerActivityLollipop)context).getParentHandleBrowser());

			if (((ManagerActivityLollipop) context).comesFromNotifications && ((ManagerActivityLollipop) context).comesFromNotificationHandle == (((ManagerActivityLollipop)context).getParentHandleBrowser())) {
				((ManagerActivityLollipop) context).comesFromNotifications = false;
				((ManagerActivityLollipop) context).comesFromNotificationHandle = -1;
				((ManagerActivityLollipop) context).selectDrawerItemLollipop(ManagerActivityLollipop.DrawerItem.NOTIFICATIONS);
				((ManagerActivityLollipop)context).setParentHandleBrowser(((ManagerActivityLollipop)context).comesFromNotificationHandleSaved);
				((ManagerActivityLollipop)context).comesFromNotificationHandleSaved = -1;
				((ManagerActivityLollipop) context).refreshCloudDrive();

				return 2;
			}
			else {
				MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(((ManagerActivityLollipop)context).getParentHandleBrowser()));
				if (parentNode != null) {
					recyclerView.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);

					((ManagerActivityLollipop)context).setParentHandleBrowser(parentNode.getHandle());
					((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

					((ManagerActivityLollipop)context).setToolbarTitle();

					nodes = megaApi.getChildren(parentNode, sortOrderManagement.getOrderCloud());
					addSectionTitle(nodes,adapter.getAdapterType());
					adapter.setNodes(nodes);

					visibilityFastScroller();

					int lastVisiblePosition = 0;
					if (!lastPositionStack.empty()) {
						lastVisiblePosition = lastPositionStack.pop();
						logDebug("Pop of the stack " + lastVisiblePosition + " position");
					}
					logDebug("Scroll to " + lastVisiblePosition + " position");

					if (lastVisiblePosition >= 0) {

						if (((ManagerActivityLollipop)context).isList) {
							mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition,0);
						} else {
							gridLayoutManager.scrollToPositionWithOffset(lastVisiblePosition,0);
						}
					}
					logDebug("return 2");
					return 2;
				} else {
					logWarning("ParentNode is NULL");
					return 0;
				}
			}
        }

        return 0;
    }

	public void scrollToFirstPosition () {
		if (((ManagerActivityLollipop)context).isList) {
			mLayoutManager.scrollToPositionWithOffset(0,0);
		}
		else {
			gridLayoutManager.scrollToPositionWithOffset(0,0);
		}
	}

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void addSectionTitle(List<MegaNode> nodes,int type) {
        Map<Integer, String> sections = new HashMap<>();
        int folderCount = 0;
        int fileCount = 0;
        for (MegaNode node : nodes) {
            if(node == null) {
                continue;
            }
            if (node.isFolder()) {
                folderCount++;
            }
            if (node.isFile()) {
                fileCount++;
            }
        }

        if (type == MegaNodeAdapter.ITEM_VIEW_TYPE_GRID) {
            int spanCount = 2;
            if (recyclerView instanceof NewGridRecyclerView) {
                spanCount = ((NewGridRecyclerView)recyclerView).getSpanCount();
            }
            if(folderCount > 0) {
                for (int i = 0;i < spanCount;i++) {
                    sections.put(i,getString(R.string.general_folders));
                }
            }

            if(fileCount > 0 ) {
                placeholderCount =  (folderCount % spanCount) == 0 ? 0 : spanCount - (folderCount % spanCount);
                if (placeholderCount == 0) {
                    for (int i = 0;i < spanCount;i++) {
                        sections.put(folderCount + i,getString(R.string.general_files));
                    }
                } else {
                    for (int i = 0;i < spanCount;i++) {
                        sections.put(folderCount + placeholderCount + i,getString(R.string.general_files));
                    }
                }
            }
        } else {
            placeholderCount = 0;
            sections.put(0,getString(R.string.general_folders));
            sections.put(folderCount,getString(R.string.general_files));
        }

		if (headerItemDecoration == null) {
			logDebug("Create new decoration");
			headerItemDecoration = new NewHeaderItemDecoration(context);
		} else {
			logDebug("Remove old decoration");
		    recyclerView.removeItemDecoration(headerItemDecoration);
        }
		headerItemDecoration.setType(type);
		headerItemDecoration.setKeys(sections);
        recyclerView.addItemDecoration(headerItemDecoration);
    }

    public void setNodes(ArrayList<MegaNode> nodes) {
		logDebug("Nodes size: " + nodes.size());

        visibilityFastScroller();
        this.nodes = nodes;
		if (((ManagerActivityLollipop)context).isList) {
			addSectionTitle(nodes,MegaNodeAdapter.ITEM_VIEW_TYPE_LIST);
		}
		else {
			addSectionTitle(nodes,MegaNodeAdapter.ITEM_VIEW_TYPE_GRID);
		}

		if (adapter != null) {
			adapter.setNodes(nodes);

			if (adapter.getItemCount() == 0) {
				recyclerView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);

				if (megaApi.getRootNode() != null && megaApi.getRootNode().getHandle() == ((ManagerActivityLollipop)context).getParentHandleBrowser() || ((ManagerActivityLollipop)context).getParentHandleBrowser() == -1) {

					if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						emptyImageView.setImageResource(R.drawable.empty_cloud_drive_landscape);
					} else {
						emptyImageView.setImageResource(R.drawable.empty_cloud_drive_portrait);
					}
					String textToShow = context.getString(R.string.context_empty_cloud_drive).toUpperCase();
					try {
						textToShow = textToShow.replace("[A]","<font color=\'"
								+ ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
								+ "\'>");
						textToShow = textToShow.replace("[/A]","</font>");
						textToShow = textToShow.replace("[B]","<font color=\'"
								+ ColorUtils.getColorHexString(context, R.color.grey_300_grey_600)
								+ "\'>");
						textToShow = textToShow.replace("[/B]","</font>");
					} catch (Exception e) {
					}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextViewFirst.setText(result);

				} else {
					if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						emptyImageView.setImageResource(R.drawable.empty_folder_landscape);
					} else {
						emptyImageView.setImageResource(R.drawable.empty_folder_portrait);
					}
					String textToShow = context.getString(R.string.file_browser_empty_folder_new);
					try {
						textToShow = textToShow.replace("[A]","<font color=\'"
								+ ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
								+ "\'>");
						textToShow = textToShow.replace("[/A]","</font>");
						textToShow = textToShow.replace("[B]","<font color=\'"
								+ ColorUtils.getColorHexString(context, R.color.grey_300_grey_600)
								+ "\'>");
						textToShow = textToShow.replace("[/B]","</font>");
					} catch (Exception e) {
					}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextViewFirst.setText(result);
				}
			} else {
				recyclerView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}
		} else {
			logWarning("Adapter is NULL");
		}
    }

    public boolean isMultipleselect() {
        if (adapter != null) {
            return adapter.isMultipleSelect();
        }
        return false;
    }
    
    public int getItemCount() {
        if (adapter != null) {
            return adapter.getItemCount();
        }
        return 0;
    }
    
    public void visibilityFastScroller() {
        if (adapter == null) {
            fastScroller.setVisibility(View.GONE);
        } else {
            if (adapter.getItemCount() < MIN_ITEMS_SCROLLBAR) {
                fastScroller.setVisibility(View.GONE);
            } else {
                fastScroller.setVisibility(View.VISIBLE);
            }
        }
    }

	//refresh list when item updated
	public void refresh(long handle) {
		if (handle == -1) {
			return;
		}
		updateNode(handle);
		adapter.notifyDataSetChanged();
	}

	private void updateNode(long handle) {
		for (int i = 0; i < nodes.size(); i++) {
			MegaNode node = nodes.get(i);
			//in grid view, we have to ignore the placholder.
			if(node == null) {
				continue;
			}
			if (node.getHandle() == handle) {
				MegaNode updated = megaApi.getNodeByHandle(handle);
				nodes.set(i, updated);
				break;
			}
		}
	}

	/**
	 * Sets the "transfer over quota" banner visibility.
	 */
	public void setTransferOverQuotaBannerVisibility() {
    	if (MegaApplication.getTransfersManagement().isTransferOverQuotaBannerShown()) {
    		transferOverQuotaBanner.setVisibility(View.VISIBLE);
    		transferOverQuotaBannerText.setText(context.getString(R.string.current_text_depleted_transfer_overquota, getHumanizedTime(megaApi.getBandwidthOverquotaDelay())));
			createAndShowCountDownTimer(R.string.current_text_depleted_transfer_overquota, transferOverQuotaBanner, transferOverQuotaBannerText);
		} else {
    		transferOverQuotaBanner.setVisibility(View.GONE);
		}
	}

	/**
	 * Hides the "transfer over quota" banner.
	 */
	private void hideTransferOverQuotaBanner() {
		MegaApplication.getTransfersManagement().setTransferOverQuotaBannerShown(false);
		setTransferOverQuotaBannerVisibility();
	}
}
