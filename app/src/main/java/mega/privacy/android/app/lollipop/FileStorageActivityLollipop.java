package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import kotlin.Unit;
import mega.privacy.android.app.FileDocument;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.activities.PasscodeActivity;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.interfaces.ActionNodeCallback;
import mega.privacy.android.app.lollipop.adapters.FileStorageLollipopAdapter;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.utils.RunOnUIThreadUtils;
import mega.privacy.android.app.utils.SDCardOperator;
import mega.privacy.android.app.utils.SDCardUtils;
import mega.privacy.android.app.utils.StringResourcesUtils;

import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.FileUtil.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.MegaApiUtils.*;
import static mega.privacy.android.app.utils.MegaNodeDialogUtil.showNewFolderDialog;
import static mega.privacy.android.app.utils.TextUtil.*;
import static mega.privacy.android.app.utils.Util.*;

public class FileStorageActivityLollipop extends PasscodeActivity implements OnClickListener,
		ActionNodeCallback {

	private static final String IS_SET_DOWNLOAD_LOCATION_SHOWN = "IS_SET_DOWNLOAD_LOCATION_SHOWN";
	private static final String IS_CONFIRMATION_CHECKED = "IS_CONFIRMATION_CHECKED";
	private static final String PATH = "PATH";
	public static final String PICK_FOLDER_TYPE = "PICK_FOLDER_TYPE";

	public enum PickFolderType {
		CU_FOLDER("CU_FOLDER"),
		MU_FOLDER("MU_FOLDER"),
		DOWNLOAD_FOLDER("DOWNLOAD_FOLDER"),
		NONE_ONLY_DOWNLOAD("NONE_ONLY_DOWNLOAD");

		private String folderType;

		PickFolderType(String folderType) {
			this.folderType = folderType;
		}

		public String getFolderType() {
			return folderType;
		}
	}

	public static final String EXTRA_URL = "fileurl";
	public static final String EXTRA_SIZE = "filesize";
	public static final String EXTRA_SERIALIZED_NODES = "serialized_nodes";
	public static final String EXTRA_DOCUMENT_HASHES = "document_hash";
	public static final String EXTRA_FROM_SETTINGS = "from_settings";
	public static final String EXTRA_SAVE_RECOVERY_KEY = "save_recovery_key";
	public static final String EXTRA_BUTTON_PREFIX = "button_prefix";
	public static final String EXTRA_PATH = "filepath";
	// Currently for exporting recovery key use.
	public static final String EXTRA_SD_URI = "sdcarduri";
	public static final String EXTRA_FILES = "fileslist";
    public static final String EXTRA_PROMPT = "prompt";

	// Pick modes
	public enum Mode {
		// Select single folder
		PICK_FOLDER("ACTION_PICK_FOLDER"),
		// Pick one or multiple files or folders
		PICK_FILE("ACTION_PICK_FILE"),
		//Browse files
		BROWSE_FILES("ACTION_BROWSE_FILES");

		private String action;

		Mode(String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}

		public static Mode getFromIntent(Intent intent) {
			if (intent.getAction().equals(PICK_FILE.getAction())) {
				return PICK_FILE;
			} else if (intent.getAction().equals(BROWSE_FILES.getAction())) {
				return BROWSE_FILES;
			} else {
				return PICK_FOLDER;
			}
		}
	}

	private MegaPreferences prefs;
	private Mode mode;
	
	private MenuItem newFolderMenuItem;
	
	private File path;
	private File root;
	private RelativeLayout viewContainer;
	private Button button;
	private TextView contentText;
	private RecyclerView listView;
	private LinearLayoutManager mLayoutManager;
	private Button cancelButton;
	private ImageView emptyImageView;
	private TextView emptyTextView;
	private LinearLayout buttonsContainer;

	private LinearLayout rootLevelLayout;
	private RelativeLayout internalStorageLayout;
	private RelativeLayout externalStorageLayout;
	
	private Boolean fromSettings, fromSaveRecoveryKey;
	private PickFolderType pickFolderType;
	private String sdRoot;
	private boolean hasSDCard;
    private String prompt;

	private Stack<Integer> lastPositionStack;
	
	private String url;
	private long size;
	private long[] documentHashes;
	private ArrayList<String> serializedNodes;

	private FileStorageLollipopAdapter adapter;
	private Toolbar tB;
	private ActionBar aB;
	
	private ActionMode actionMode;

	private AlertDialog setDownloadLocationDialog;
	private boolean isSetDownloadLocationShown;
	private boolean confirmationChecked;

	private Handler handler;

	private boolean pickingFromSDCard;

    /**
     * Pass to exporting recovery key operation.
     */
	private String sdCardUriString;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		logDebug("onOptionsItemSelected");

		// Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.cab_menu_create_folder:
				showNewFolderDialog(this, this);
                return true;

            case R.id.cab_menu_select_all:
                selectAll();
                return true;

            case R.id.cab_menu_unselect_all:
                clearSelections();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	private class ActionBarCallBack implements ActionMode.Callback {
		
		@Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.cab_menu_select_all:
                    selectAll();
                    break;

                case R.id.cab_menu_unselect_all:
                    clearSelections();
                    break;
            }

            return false;
        }

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.file_storage_action, menu);
			tB.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			clearSelections();
			adapter.setMultipleSelect(false);
			tB.setElevation(0);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			List<FileDocument> selected = adapter.getSelectedDocuments();
			
			if (selected.size() != 0) {				
				
				if(selected.size()==adapter.getItemCount()){
					menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);			
				}
				else{
					menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);	
				}	
			}
			else{
				menu.findItem(R.id.cab_menu_select_all).setVisible(true);
				menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
			}
			
			if (!(mode.equals(Mode.PICK_FOLDER))) {
				logDebug("Not Mode.PICK_FOLDER");
				menu.findItem(R.id.cab_menu_create_folder).setVisible(false);
			}
			
			return false;
		}
	}
	
	public void selectAll(){
		logDebug("selectAll");
		if (adapter != null){
			if(adapter.isMultipleSelect()){
				adapter.selectAll();
			}
			else{			
				adapter.setMultipleSelect(true);
				adapter.selectAll();
				
				actionMode = startSupportActionMode(new ActionBarCallBack());
			}

			new Handler(Looper.getMainLooper()).post(() -> updateActionModeTitle());
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		logDebug("onCreateOptionsMenuLollipop");
		
		
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.file_storage_action, menu);
	    getSupportActionBar().setDisplayShowCustomEnabled(true);
	    
	    newFolderMenuItem = menu.findItem(R.id.cab_menu_create_folder);
        newFolderMenuItem.setVisible(mode == Mode.PICK_FOLDER);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		logDebug("onPrepareOptionsMenu");

		menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
		menu.findItem(R.id.cab_menu_select_all).setVisible(mode == Mode.PICK_FILE);
		newFolderMenuItem.setVisible(mode == Mode.PICK_FOLDER);

		return super.onPrepareOptionsMenu(menu);
	}
	
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		logDebug("onCreate");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						REQUEST_WRITE_STORAGE);
			}
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

	    handler = new Handler();

		setContentView(R.layout.activity_filestorage);
		
		//Set toolbar
		tB = findViewById(R.id.toolbar_filestorage);
		setSupportActionBar(tB);
		aB = getSupportActionBar();
		aB.setDisplayHomeAsUpEnabled(true);
		aB.setDisplayShowHomeEnabled(true);

		Intent intent = getIntent();
		prompt = intent.getStringExtra(EXTRA_PROMPT);
		if (prompt != null) {
			showSnackbar(viewContainer, prompt);
		}
		fromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, true);
		fromSaveRecoveryKey = intent.getBooleanExtra(EXTRA_SAVE_RECOVERY_KEY, false);

		setPickFolderType(intent.getStringExtra(PICK_FOLDER_TYPE));

		File[] fs = getExternalFilesDirs(null);
		hasSDCard = fs.length > 1 && fs[1] != null;
		
		mode = Mode.getFromIntent(intent);
		if (mode == Mode.PICK_FOLDER) {
			documentHashes = intent.getExtras().getLongArray(EXTRA_DOCUMENT_HASHES);
			serializedNodes = intent.getStringArrayListExtra(EXTRA_SERIALIZED_NODES);
			url = intent.getExtras().getString(EXTRA_URL);
			size = intent.getExtras().getLong(EXTRA_SIZE);
			aB.setTitle(getString(R.string.general_select_to_download).toUpperCase());
		} else if (mode == Mode.BROWSE_FILES) {
			aB.setTitle(getString(R.string.browse_files_label).toUpperCase());
		} else{
			aB.setTitle(getString(R.string.general_select_to_upload).toUpperCase());
		}
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(PATH)) {
				path = new File(savedInstanceState.getString(PATH));
			}

			fromSaveRecoveryKey = savedInstanceState.getBoolean(EXTRA_SAVE_RECOVERY_KEY, false);

			isSetDownloadLocationShown = savedInstanceState.getBoolean(IS_SET_DOWNLOAD_LOCATION_SHOWN, false);
			confirmationChecked = savedInstanceState.getBoolean(IS_CONFIRMATION_CHECKED, false);
		}
		
        viewContainer = findViewById(R.id.file_storage_container);
		contentText = findViewById(R.id.file_storage_content_text);
		listView = findViewById(R.id.file_storage_list_view);

		buttonsContainer = findViewById(R.id.options_file_storage_layout);

		cancelButton = findViewById(R.id.file_storage_cancel_button);
		cancelButton.setOnClickListener(this);
		cancelButton.setText(getString(R.string.general_cancel).toUpperCase(Locale.getDefault()));

		button = findViewById(R.id.file_storage_button);
		button.setOnClickListener(this);

		if (fromSaveRecoveryKey) {
			button.setText(getString(R.string.save_action).toUpperCase(Locale.getDefault()));
		} else if (fromSettings) {
			button.setText(getString(R.string.general_select).toUpperCase(Locale.getDefault()));
		} else if (mode == Mode.PICK_FOLDER) {
			button.setText(getString(R.string.general_save_to_device).toUpperCase(Locale.getDefault()));
		} else if (mode == Mode.PICK_FILE){
			button.setText(getString(R.string.context_upload).toUpperCase(Locale.getDefault()));
		} else if (mode == Mode.BROWSE_FILES) {
			buttonsContainer.setVisibility(View.GONE);
		}

		rootLevelLayout = findViewById(R.id.root_level_layout);
		rootLevelLayout.setVisibility(View.GONE);
		internalStorageLayout = findViewById(R.id.internal_storage_layout);
		internalStorageLayout.setOnClickListener(this);
		externalStorageLayout = findViewById(R.id.external_storage_layout);
		externalStorageLayout.setOnClickListener(this);

		emptyImageView = findViewById(R.id.file_storage_empty_image);
		emptyTextView = findViewById(R.id.file_storage_empty_text);
		emptyImageView.setImageResource(isScreenInPortrait(this) ? R.drawable.empty_folder_portrait : R.drawable.empty_folder_landscape);

		String textToShow = getString(R.string.file_browser_empty_folder_new);
		try {
			textToShow = textToShow.replace("[A]", "<font color=\'"
					+ ColorUtils.getColorHexString(this, R.color.grey_900_grey_100)
					+ "\'>");
			textToShow = textToShow.replace("[/A]", "</font>");
			textToShow = textToShow.replace("[B]", "<font color=\'"
					+ ColorUtils.getColorHexString(this, R.color.grey_300_grey_600)
					+ "\'>");
			textToShow = textToShow.replace("[/B]", "</font>");
		} catch (Exception e) {
			logWarning("Exception formatting text, ", e);
		}
		emptyTextView.setText(HtmlCompat.fromHtml(textToShow, HtmlCompat.FROM_HTML_MODE_LEGACY));

		listView = findViewById(R.id.file_storage_list_view);
		listView.addItemDecoration(new SimpleDividerItemDecoration(this));
		mLayoutManager = new LinearLayoutManager(this);
		listView.setLayoutManager(mLayoutManager);
		listView.setItemAnimator(noChangeRecyclerViewItemAnimator());
		
		if (adapter == null){
			adapter = new FileStorageLollipopAdapter(this, listView, mode);
			listView.setAdapter(adapter);
		}

        lastPositionStack = new Stack<>();

		prefs = dbH.getPreferences();

		if (mode == Mode.BROWSE_FILES) {
			if (intent.getExtras() != null) {
				String extraPath = intent.getExtras().getString(EXTRA_PATH);
				if (!isTextEmpty(extraPath)) {
					root = path = new File(extraPath);
				}
			}
		    checkPath();
		} else if (hasSDCard) {
			showRootWithSDView(true);
		} else {
			openPickFromInternalStorage();
		}

		if (isSetDownloadLocationShown) {
			showConfirmationSaveInSameLocation();
		}
	}

	/**
	 * Sets the type of pick folder action.
	 *
	 * @param pickFolderString	the type of pick folder action.
	 */
	private void setPickFolderType(String pickFolderString) {
		if (isTextEmpty(pickFolderString)) {
			pickFolderType = PickFolderType.NONE_ONLY_DOWNLOAD;
		} else if (pickFolderString.equals(PickFolderType.CU_FOLDER.getFolderType())) {
			pickFolderType = PickFolderType.CU_FOLDER;
			dbH.setCameraFolderExternalSDCard(false);
		} else if (pickFolderString.equals(PickFolderType.MU_FOLDER.getFolderType())) {
			pickFolderType = PickFolderType.MU_FOLDER;
			dbH.setMediaFolderExternalSdCard(false);
		} else if (pickFolderString.equals(PickFolderType.DOWNLOAD_FOLDER.getFolderType())) {
			pickFolderType = PickFolderType.DOWNLOAD_FOLDER;
		}
	}

	/**
	 * Sets the view to pick from Internal Storage.
	 */
	private void openPickFromInternalStorage() {
		pickingFromSDCard = false;
		root = buildExternalStorageFile("");

		if (pickFolderType.equals(PickFolderType.CU_FOLDER) && Environment.getExternalStorageDirectory() != null) {
			path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		} else if (pickFolderType.equals(PickFolderType.MU_FOLDER) && Environment.getExternalStorageDirectory() != null) {
			path = Environment.getExternalStorageDirectory();
		} else if (prefs != null && prefs.getLastFolderUpload() != null) {
			path = new File(prefs.getLastFolderUpload());
		}

		if (path == null) {
			path = buildDefaultDownloadDir(this);
		}

		path.mkdirs();

		checkPath();
	}

	/**
	 * Sets the view to pick from SD card.
	 */
	private void openPickFromSDCard() {
		pickingFromSDCard = true;

		SDCardOperator sdCardOperator;
		try {
			sdCardOperator = new SDCardOperator(this);
		} catch (SDCardOperator.SDCardException e) {
			e.printStackTrace();
			logError("Initialize SDCardOperator failed", e);
			//sd card is not available, choose internal storage location
			showRootWithSDView(false);
			openPickFromInternalStorage();
			return;
		}

		String sdCardRoot = sdCardOperator.getSDCardRoot();

		if (mode.equals(Mode.PICK_FILE)) {
			sdRoot = sdCardRoot;
		} else if (isBasedOnFileStorage()) {
			try {
				sdCardOperator.initDocumentFileRoot(dbH.getSDCardUri());
				sdRoot = sdCardRoot;
			} catch (SDCardOperator.SDCardException e) {
				e.printStackTrace();
				logError("SDCardOperator initDocumentFileRoot failed, requestSDCardPermission", e);
				requestSDCardPermission(sdCardRoot);
			}
		} else {
			requestSDCardPermission(sdCardRoot);
		}

		if (sdRoot != null) {
			openSDCardPath();
		}
	}

	/**
	 * Opens a SD card path.
	 */
	private void openSDCardPath() {
		path = new File(sdRoot);
		showRootWithSDView(false);
		checkPath();
	}

	/**
	 * Requests SD card permissions.
	 *
	 * @param sdCardRoot	the root path of the SD card.¡
	 */
	private void requestSDCardPermission(String sdCardRoot) {
		Intent intent = null;
		if (isBasedOnFileStorage()) {
			StorageManager sm = getSystemService(StorageManager.class);
			if (sm != null) {
				StorageVolume volume = sm.getStorageVolume(new File(sdCardRoot));
				if (volume != null) {
					intent = volume.createAccessIntent(null);
				}
			}
		}

        //for below N or above P, open SAF
        if (intent == null) intent = openSAFIntent();

        logDebug("Request SD card write permission with intent: " + intent);

        try {
            /*
                A small number of devices(the device's OS version should be >= N and < Q) cannot handle
                Intent { act=android.os.storage.action.OPEN_EXTERNAL_DIRECTORY launchParam=MultiScreenLaunchParams { mDisplayId=0 mBaseDisplayId=0 mFlags=0 }}.
             */
            startActivityForResult(intent, REQUEST_CODE_TREE);
        } catch (Exception e) {
            logError("Start request SD card uri activity error. Current OS version: " + Build.VERSION.SDK_INT, e);
            if (isBasedOnFileStorage()) {
                // Try to request SD card uri with SAF.
				// The toast is showing on SAF.
                Toast.makeText(this,StringResourcesUtils.getString(R.string.ask_for_select_sdcard_root),Toast.LENGTH_LONG).show();
                startActivityForResult(openSAFIntent(), REQUEST_CODE_TREE);
            } else {
                // Should never happen.
                e.printStackTrace();
            }
        }
    }

    /**
     * Get an Intent which access SD card via SAF and grant the uri.
     *
     * @return The Intent launches SAF.
     */
    private Intent openSAFIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        return intent;
    }

	/**
	 * Shows or hides the root view with Internal storage and External storage.
	 *
	 * @param show	true if the root with both storages has to be shown, false otherwise
	 */
	private void showRootWithSDView(boolean show) {
		if (show) {
			contentText.setText(String.format("%s%s", SEPARATOR, getString(R.string.storage_root_label)));
			rootLevelLayout.setVisibility(View.VISIBLE);
			buttonsContainer.setVisibility(View.GONE);
			showEmptyState();
		} else {
			rootLevelLayout.setVisibility(View.GONE);
			buttonsContainer.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Shows the empty view or the list view depending on if there are items in the adapter.
	 * Hides both if the root view with Internal storage and External storage is shown.
	 */
	private void showEmptyState() {
		if (rootLevelLayout.getVisibility() == View.VISIBLE) {
			listView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
		} else if (adapter != null && adapter.getItemCount() > 0) {
			listView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Changes the path shown in the screen or finish the activity it the current one is not valid.
	 */
	private void checkPath() {
		if (path == null){
			finish();
		}

		changeFolder(path);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		emptyImageView.setImageResource(isScreenInPortrait(this) ? R.drawable.empty_folder_portrait : R.drawable.empty_folder_landscape);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		if (path != null) {
			state.putString(PATH, path.getAbsolutePath());
		}
		state.putBoolean(EXTRA_SAVE_RECOVERY_KEY, fromSaveRecoveryKey);
		state.putBoolean(IS_SET_DOWNLOAD_LOCATION_SHOWN, isSetDownloadLocationShown);
		state.putBoolean(IS_CONFIRMATION_CHECKED, confirmationChecked);

		super.onSaveInstanceState(state);
	}
	
	/*
	 * Open new folder
	 * @param newPath New folder path
	 */
	@SuppressLint("NewApi")
	private void changeFolder(File newPath) {
		logDebug("New path: " + newPath);
		
		setFiles(newPath);
		path = newPath;
		contentText.setText(path.getAbsolutePath());
		invalidateOptionsMenu();
        if (mode == Mode.PICK_FILE) {
			clearSelections();
		}
	}
	
	/*
	 * Update file list for new folder
	 */
	private void setFiles(File path) {
		logDebug("setFiles");
		List<FileDocument> documents = new ArrayList<FileDocument>();

		if (path == null || !path.canRead()) {
			showErrorAlertDialog(getString(R.string.error_io_problem),
					true, this);
			return;
		}

		File[] files = path.listFiles();

		if (files != null) {
			logDebug("Number of files: " + files.length);

			for (File file : files) {
				FileDocument document = new FileDocument(file);
				if (document.isHidden()) {
					continue;
				}

				documents.add(document);
			}

			Collections.sort(documents, new CustomComparator());
		}

		adapter.setFiles(documents);
		showEmptyState();
	}

	private void updateActionModeTitle() {
		logDebug("updateActionModeTitle");
		if (actionMode == null) {
			logWarning("RETURN");
			return;
		}
		
		List<FileDocument> documents = adapter.getSelectedDocuments();
		int files = 0;
		int folders = 0;
		for (FileDocument document : documents) {
			if (document.isFolder()) {
				folders++;
			}
			else{
				files++;
			}
		}

		actionMode.setTitle(getFolderInfo(folders, files));

		try {
			actionMode.invalidate();
		} catch (NullPointerException e) {
			logError("Invalidate error", e);
			e.printStackTrace();
		}
	}

	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		logDebug("clearSelections");
		if(adapter.isMultipleSelect()){
			adapter.clearSelections();
		}
	}

	/*
	 * Comparator to sort the files
	 */
	public class CustomComparator implements Comparator<FileDocument> {
		@Override
		public int compare(FileDocument o1, FileDocument o2) {
			if (o1.isFolder() != o2.isFolder()) {
				return o1.isFolder() ? -1 : 1;
			}
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	@Override
	public void onClick(View v) {
		logDebug("onClick");

		switch (v.getId()) {
			case R.id.file_storage_button:
                //don't record last upload folder for SD card upload
                if(!hasSDCard) {
                    dbH.setLastUploadFolder(path.getAbsolutePath());
                }
				if (mode == Mode.PICK_FOLDER) {
					boolean isCUOrMUFolder = pickFolderType.equals(PickFolderType.CU_FOLDER) || pickFolderType.equals(PickFolderType.MU_FOLDER);

					if (!isCUOrMUFolder && (prefs == null || prefs.getStorageAskAlways() == null || Boolean.parseBoolean(prefs.getStorageAskAlways()))
							&& dbH.getAskSetDownloadLocation()) {
						showConfirmationSaveInSameLocation();
					} else {
						finishPickFolder();
					}
				}
				else {
					logDebug("Mode.PICK_FILE");
					if(adapter.getSelectedCount()<=0){
						showSnackbar(viewContainer, getString(R.string.error_no_selection));
						break;
					}
					new AsyncTask<Void, Void, Void>()
					{
						ArrayList<String> files = new ArrayList<String>();

						@Override
						protected Void doInBackground(Void... params) {
							List<FileDocument> selectedDocuments= adapter.getSelectedDocuments();
							for (int i = 0; i < selectedDocuments.size(); i++) {
								FileDocument document = selectedDocuments.get(i);
								if(document != null)
								{
									File file = document.getFile();
									logDebug("Add to files selected: " + file.getAbsolutePath());
									files.add(file.getAbsolutePath());
								}
								
							}
							return null;	
						}

						@Override
						public void onPostExecute(Void a)
						{

							setResultFiles(files);
						}
					}.execute();			
				}
				break;

			case R.id.file_storage_cancel_button:
				finish();
				break;

			case R.id.internal_storage_layout:
				showRootWithSDView(false);
				openPickFromInternalStorage();
				break;

			case R.id.external_storage_layout:
				openPickFromSDCard();
				break;
		}
	}

	/**
	 * This dialog is shown when the user is selecting the download location.
	 * It asks if they want to set the current chosen location as default.
	 * It the user enables the checkbox, the dialog should not appear again.
	 *
	 */
	private void showConfirmationSaveInSameLocation(){
		if (setDownloadLocationDialog != null && setDownloadLocationDialog.isShowing()) {
			return;
		}

		if (prefs == null) {
			prefs = dbH.getPreferences();
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Mega_MaterialAlertDialog);
		View v = getLayoutInflater().inflate(R.layout.dialog_general_confirmation, null);
		builder.setView(v);

		TextView text = v.findViewById(R.id.confirmation_text);
		text.setText(R.string.confirmation_download_location);

		Button cancelButton = v.findViewById(R.id.negative_button);
		cancelButton.setText(R.string.general_negative_button);
		cancelButton.setOnClickListener(v2 -> setDownloadLocationDialog.dismiss());

		Button confirmationButton = v.findViewById(R.id.positive_button);
		confirmationButton.setText(R.string.general_yes);
		confirmationButton.setOnClickListener(v3 -> {
			setDownloadLocationDialog.dismiss();
			dbH.setStorageAskAlways(false);
			dbH.setStorageDownloadLocation(path.getAbsolutePath());
			prefs.setStorageDownloadLocation(path.getAbsolutePath());
		});

		CheckBox checkBox = v.findViewById(R.id.confirmation_checkbox);
		checkBox.setChecked(confirmationChecked);

		LinearLayout checkBoxLayout = v.findViewById(R.id.confirmation_checkbox_layout);
		checkBoxLayout.setOnClickListener(v1 -> checkBox.setChecked(!checkBox.isChecked()));

		builder.setCancelable(false);
		builder.setOnDismissListener(dialog -> {
			isSetDownloadLocationShown = false;
			dbH.setAskSetDownloadLocation(!checkBox.isChecked());
			finishPickFolder();
		});
		setDownloadLocationDialog = builder.create();
		setDownloadLocationDialog.show();
		isSetDownloadLocationShown = true;
	}

	private void finishPickFolder() {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_PATH, path.getAbsolutePath());
		intent.putExtra(EXTRA_SD_URI, sdCardUriString);
		intent.putExtra(EXTRA_DOCUMENT_HASHES, documentHashes);
		intent.putStringArrayListExtra(EXTRA_SERIALIZED_NODES, serializedNodes);
		intent.putExtra(EXTRA_URL, url);
		intent.putExtra(EXTRA_SIZE, size);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        // do nothing
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	public void itemClick(int position) {
		logDebug("Position: " + position);

		FileDocument document = adapter.getDocumentAt(position);
		if (document == null) {
			return;
		}

		if (adapter.isMultipleSelect()) {
			adapter.toggleSelection(position);
			List<FileDocument> selected = adapter.getSelectedDocuments();
			if (selected.size() > 0) {
				updateActionModeTitle();
			}
		} else if (document.isFolder()) {
			lastPositionStack.push(mLayoutManager.findFirstCompletelyVisibleItemPosition());
			changeFolder(document.getFile());
		} else if (mode == Mode.PICK_FILE) {
			//Multiselect on to select several files if desired
			checkActionMode();
			adapter.toggleSelection(position);
			updateActionModeTitle();
			adapter.notifyDataSetChanged();
		} else if (mode == Mode.BROWSE_FILES) {
			File f = adapter.getItem(position).getFile();

			if (isFileAvailable(f)) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				Uri uri = isAndroidNougatOrUpper() ? FileProvider.getUriForFile(this, AUTHORITY_STRING_FILE_PROVIDER, f)
						: Uri.fromFile(f);

				if (uri != null) {
					intent.setDataAndType(uri, MimeTypeList.typeForName(f.getName()).getType());
				} else {
					logWarning("The file cannot be opened, uri is null");
					return;
				}

				if (isIntentAvailable(this, intent)) {
					startActivity(intent);
				}
			} else {
				showSnackbar(viewContainer, getString(R.string.corrupt_video_dialog_text));
			}
		}
	}
	
	/*
	 * Set selected files to pass to the caller activity and finish this
	 * activity
	 */
	private void setResultFiles(ArrayList<String> files) {
		logDebug(files.size() + "files selected");
		Intent intent = new Intent();
		intent.putStringArrayListExtra(EXTRA_FILES, files);
		intent.putExtra(EXTRA_PATH, path.getAbsolutePath());
		setResult(RESULT_OK, intent);
		finish();
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
	
	@Override
	public void onBackPressed() {
		if (psaWebBrowser.consumeBack()) return;
		retryConnectionsAndSignalPresence();

		// Finish activity if at the root
		boolean isRoot;
		if (hasSDCard && !mode.equals(Mode.BROWSE_FILES)) {
			isRoot = rootLevelLayout.getVisibility() == View.VISIBLE;
		} else {
			isRoot = path.equals(root);
		}

		if (isRoot) {
			super.onBackPressed();
			// Go one level higher otherwise
		} else if (hasSDCard && ((pickingFromSDCard && path.equals(new File(sdRoot)) || !pickingFromSDCard && path.equals(root)))) {
			path = null;
			showRootWithSDView(true);
		} else {
			changeFolder(path.getParentFile());
			int lastVisiblePosition = 0;
			if(!lastPositionStack.empty()){
				lastVisiblePosition = lastPositionStack.pop();
			}

			if(lastVisiblePosition>=0){
				mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
			}
		}
	}

	@Override
	public void finishRenameActionWithSuccess(@NonNull String newName) {
		//No action needed
	}

	@Override
	public void actionConfirmed() {
		//No update needed
	}

	@Override
	public void createFolder(@NotNull String value) {
		logDebug(value + " Of value");
        SDCardOperator sdCardOperator = null;
        try {
            sdCardOperator = new SDCardOperator(this);
        } catch (SDCardOperator.SDCardException e) {
            e.printStackTrace();
            logError("Initialize SDCardOperator failed", e);
        }

        if (sdCardOperator != null && SDCardOperator.isSDCardPath(path.getAbsolutePath())) {
            try {
                sdCardOperator.initDocumentFileRoot(dbH.getSDCardUri());
                sdCardOperator.createFolder(path.getAbsolutePath(), value);
            } catch (SDCardOperator.SDCardException e) {
                e.printStackTrace();
                logError("SDCardOperator initDocumentFileRoot failed", e);
                showErrorAlertDialog(getString(R.string.error_io_problem), true, this);
            }
        } else {
            createFolderWithFile(value);
        }

        setFiles(path);
    }

    private void createFolderWithFile(String value) {
        File newFolder = new File(path, value);
        newFolder.mkdir();
        newFolder.setReadable(true, false);
        newFolder.setExecutable(true, false);
    }

	/**
	 * Starts the action mode if needed.
	 */
	public void checkActionMode() {
		if (mode != Mode.PICK_FILE) return;

		if (adapter != null && !adapter.isMultipleSelect()){
			adapter.setMultipleSelect(true);
			actionMode = startSupportActionMode(new ActionBarCallBack());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == REQUEST_CODE_TREE) {
			if (intent == null) {
				logWarning("intent NULL");
				if (resultCode != Activity.RESULT_OK) {
					if (isBasedOnFileStorage()) {
						showSnackbar(viewContainer, getString(R.string.download_requires_permission));
					}
				} else {
					onCannotWriteOnSDCard();
				}
				return;
			}

			Uri treeUri = intent.getData();
			if (treeUri == null) {
				logWarning("tree uri is null!");
				onCannotWriteOnSDCard();
				return;
			}

            /*
                If a device's OS version is >= N and < Q, the granted SD card uri MUST be root uri,
                then user can select any location on SD card as donwload location.

                But if the uri is not root(it may happen when user request uri from SAF,
                because user can select any folder and just grant the uri for the selected folder),
                need to force the user to select SD card root on SAF.
             */
            if (isBasedOnFileStorage() && SDCardUtils.isNotRootUri(treeUri)) {
				showSnackbar(viewContainer, StringResourcesUtils.getString(R.string.ask_for_select_sdcard_root));
				RunOnUIThreadUtils.INSTANCE.runDelay(1500, () -> {
                    startActivityForResult(openSAFIntent(), REQUEST_CODE_TREE);
                    return Unit.INSTANCE;
                });
                return;
            }

            ContentResolver contentResolver = getContentResolver();
			contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
			if (pickedDir == null || !pickedDir.canWrite()) {
				logWarning("PickedDir null or cannot write.");
				return;
			}

			SDCardOperator sdCardOperator = null;
			try {
				sdCardOperator = new SDCardOperator(this);
			} catch (SDCardOperator.SDCardException e) {
				e.printStackTrace();
				logError("SDCardOperator initialize failed", e);
			}

			if (sdCardOperator == null) {
				onCannotWriteOnSDCard();
				return;
			}

            String uriString = treeUri.toString();
            if (isBasedOnFileStorage()) {
                // The uri is SD card root uri.
                dbH.setSDCardUri(uriString);
                sdRoot = sdCardOperator.getSDCardRoot();
                openSDCardPath();
            } else {
                String pathString = getFullPathFromTreeUri(treeUri, this);

                if (isTextEmpty(pathString)) {
                    logWarning("getFullPathFromTreeUri is Null.");
                    return;
                }

                path = new File(pathString);

                if (pickFolderType.equals(PickFolderType.CU_FOLDER)) {
                    dbH.setCameraFolderExternalSDCard(true);
                    dbH.setUriExternalSDCard(uriString);
                } else if (pickFolderType.equals(PickFolderType.MU_FOLDER)) {
                    dbH.setMediaFolderExternalSdCard(true);
                    dbH.setUriMediaExternalSdCard(uriString);
                } else if (fromSaveRecoveryKey) {
                    // For temporary use, don't store the uri to database.
                    sdCardUriString = uriString;
                } else {
                    dbH.setSDCardUri(uriString);
                }

                finishPickFolder();
            }
        }
    }

	/**
	 * Shows a warning indicating no SD card was detected.
	 */
	private void onCannotWriteOnSDCard() {
		showSnackbar(viewContainer, getString(R.string.no_external_SD_card_detected));
		new Handler().postDelayed(this::openPickFromInternalStorage, 2000);
	}
}
