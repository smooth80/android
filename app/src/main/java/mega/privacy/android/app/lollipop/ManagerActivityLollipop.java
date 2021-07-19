package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.NavOptions;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Lifecycle;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.jeremyliao.liveeventbus.LiveEventBus;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import mega.privacy.android.app.AndroidCompletedTransfer;
import mega.privacy.android.app.BusinessExpiredAlertActivity;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaAttributes;
import mega.privacy.android.app.MegaContactAdapter;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.OpenPasswordLinkActivity;
import mega.privacy.android.app.Product;
import mega.privacy.android.app.R;
import mega.privacy.android.app.SMSVerificationActivity;
import mega.privacy.android.app.ShareInfo;
import mega.privacy.android.app.TransfersManagementActivity;
import mega.privacy.android.app.UploadService;
import mega.privacy.android.app.UserCredentials;
import mega.privacy.android.app.fragments.managerFragments.cu.CustomHideBottomViewOnScrollBehaviour;
import mega.privacy.android.app.mediaplayer.miniplayer.MiniAudioPlayerController;
import mega.privacy.android.app.activities.WebViewActivity;
import mega.privacy.android.app.components.CustomViewPager;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.components.attacher.MegaAttacher;
import mega.privacy.android.app.components.saver.NodeSaver;
import mega.privacy.android.app.components.transferWidget.TransfersManagement;
import mega.privacy.android.app.components.twemoji.EmojiEditText;
import mega.privacy.android.app.components.twemoji.EmojiTextView;
import mega.privacy.android.app.fcm.ContactsAdvancedNotificationBuilder;
import mega.privacy.android.app.fragments.homepage.HomepageSearchable;
import mega.privacy.android.app.fragments.homepage.main.HomepageFragment;
import mega.privacy.android.app.fragments.homepage.main.HomepageFragmentDirections;
import mega.privacy.android.app.fragments.managerFragments.LinksFragment;
import mega.privacy.android.app.activities.OfflineFileInfoActivity;
import mega.privacy.android.app.fragments.offline.OfflineFragment;
import mega.privacy.android.app.globalmanagement.SortOrderManagement;
import mega.privacy.android.app.interfaces.ActionNodeCallback;
import mega.privacy.android.app.fragments.managerFragments.cu.CameraUploadsFragment;
import mega.privacy.android.app.interfaces.SnackbarShower;
import mega.privacy.android.app.interfaces.ChatManagementCallback;
import mega.privacy.android.app.fragments.settingsFragments.cookie.CookieDialogHandler;
import mega.privacy.android.app.interfaces.UploadBottomSheetDialogActionListener;
import mega.privacy.android.app.listeners.CancelTransferListener;
import mega.privacy.android.app.listeners.ExportListener;
import mega.privacy.android.app.listeners.GetAttrUserListener;
import mega.privacy.android.app.listeners.RemoveFromChatRoomListener;
import mega.privacy.android.app.lollipop.adapters.ContactsPageAdapter;
import mega.privacy.android.app.lollipop.adapters.MyAccountPageAdapter;
import mega.privacy.android.app.lollipop.adapters.SharesPageAdapter;
import mega.privacy.android.app.lollipop.adapters.TransfersPageAdapter;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.listeners.CreateGroupChatWithPublicLink;
import mega.privacy.android.app.lollipop.listeners.FabButtonListener;
import mega.privacy.android.app.lollipop.managerSections.CompletedTransfersFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.ContactsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.ExportRecoveryKeyFragment;
import mega.privacy.android.app.lollipop.managerSections.FileBrowserFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.InboxFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.IncomingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.MyAccountFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.MyStorageFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.NotificationsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.OutgoingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.ReceivedRequestsFragmentLollipop;
import mega.privacy.android.app.fragments.recent.RecentsFragment;
import mega.privacy.android.app.lollipop.managerSections.RubbishBinFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SearchFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SentRequestsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SettingsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.TransfersFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.TurnOnNotificationsFragment;
import mega.privacy.android.app.lollipop.managerSections.UpgradeAccountFragmentLollipop;
import mega.privacy.android.app.lollipop.megaachievements.AchievementsActivity;
import mega.privacy.android.app.lollipop.megachat.BadgeDrawerArrowDrawable;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.RecentChatsFragmentLollipop;
import mega.privacy.android.app.lollipop.qrcode.QRCodeActivity;
import mega.privacy.android.app.lollipop.qrcode.ScanCodeFragment;
import mega.privacy.android.app.lollipop.tasks.CheckOfflineNodesTask;
import mega.privacy.android.app.lollipop.tasks.FilePrepareTask;
import mega.privacy.android.app.lollipop.tasks.FillDBContactsTask;
import mega.privacy.android.app.middlelayer.iab.BillingManager;
import mega.privacy.android.app.middlelayer.iab.BillingUpdatesListener;
import mega.privacy.android.app.middlelayer.iab.MegaPurchase;
import mega.privacy.android.app.middlelayer.iab.MegaSku;
import mega.privacy.android.app.modalbottomsheet.ContactsBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.ManageTransferBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.MyAccountBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.NodeOptionsBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.OfflineOptionsBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.ReceivedRequestBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.SentRequestBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.SortByBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.UploadBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.ChatBottomSheetDialogFragment;
import mega.privacy.android.app.utils.AlertsAndWarnings;
import mega.privacy.android.app.utils.ChatUtil;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.utils.AvatarUtil;
import mega.privacy.android.app.utils.CameraUploadUtil;
import mega.privacy.android.app.modalbottomsheet.nodelabel.NodeLabelBottomSheetDialogFragment;
import mega.privacy.android.app.psa.Psa;
import mega.privacy.android.app.psa.PsaViewHolder;
import mega.privacy.android.app.psa.PsaManager;
import mega.privacy.android.app.service.iab.BillingManagerImpl;
import mega.privacy.android.app.service.push.MegaMessageService;
import mega.privacy.android.app.sync.cusync.CuSyncManager;
import mega.privacy.android.app.utils.LastShowSMSDialogTimeChecker;
import mega.privacy.android.app.utils.MegaNodeDialogUtil;
import mega.privacy.android.app.utils.LinksUtil;
import mega.privacy.android.app.utils.StringResourcesUtils;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import mega.privacy.android.app.utils.TimeUtils;
import mega.privacy.android.app.utils.contacts.MegaContactGetter;
import nz.mega.documentscanner.DocumentScannerActivity;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaAchievementsDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatPeerList;
import nz.mega.sdk.MegaChatPresenceConfig;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaFolderInfo;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferData;
import nz.mega.sdk.MegaTransferListenerInterface;
import nz.mega.sdk.MegaUser;
import nz.mega.sdk.MegaUserAlert;
import nz.mega.sdk.MegaUtilsAndroid;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static mega.privacy.android.app.modalbottomsheet.UploadBottomSheetDialogFragment.GENERAL_UPLOAD;
import static mega.privacy.android.app.modalbottomsheet.UploadBottomSheetDialogFragment.HOMEPAGE_UPLOAD;
import static mega.privacy.android.app.utils.MegaNodeDialogUtil.IS_NEW_TEXT_FILE_SHOWN;
import static mega.privacy.android.app.utils.MegaNodeDialogUtil.NEW_TEXT_FILE_TEXT;
import static mega.privacy.android.app.utils.MegaNodeDialogUtil.checkNewTextFileDialogState;
import static mega.privacy.android.app.utils.ConstantsUrl.RECOVERY_URL;
import static mega.privacy.android.app.utils.MegaNodeDialogUtil.showRenameNodeDialog;
import static mega.privacy.android.app.utils.OfflineUtils.*;
import static mega.privacy.android.app.constants.BroadcastConstants.*;
import static mega.privacy.android.app.constants.IntentConstants.*;
import static mega.privacy.android.app.utils.AlertsAndWarnings.showOverDiskQuotaPaywallWarning;
import static mega.privacy.android.app.utils.ChatUtil.*;
import static mega.privacy.android.app.utils.ColorUtils.tintIcon;
import static mega.privacy.android.app.utils.PermissionUtils.*;
import static mega.privacy.android.app.utils.StringResourcesUtils.getQuantityString;
import static mega.privacy.android.app.utils.TextUtil.isTextEmpty;
import static mega.privacy.android.app.utils.billing.PaymentUtils.*;
import static mega.privacy.android.app.lollipop.FileInfoActivityLollipop.NODE_HANDLE;
import static mega.privacy.android.app.lollipop.qrcode.MyCodeFragment.QR_IMAGE_FILE_NAME;
import static mega.privacy.android.app.middlelayer.iab.BillingManager.RequestCode.REQ_CODE_BUY;
import static mega.privacy.android.app.modalbottomsheet.ModalBottomSheetUtil.isBottomSheetDialogShown;
import static mega.privacy.android.app.service.iab.BillingManagerImpl.PAYMENT_GATEWAY;
import static mega.privacy.android.app.utils.AvatarUtil.*;
import static mega.privacy.android.app.utils.CacheFolderManager.TEMPORAL_FOLDER;
import static mega.privacy.android.app.utils.CacheFolderManager.*;
import static mega.privacy.android.app.utils.CallUtil.*;
import static mega.privacy.android.app.utils.CameraUploadUtil.*;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.DBUtil.resetAccountDetailsTimeStamp;
import static mega.privacy.android.app.utils.FileUtil.*;
import static mega.privacy.android.app.utils.JobUtil.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.MegaApiUtils.calculateDeepBrowserTreeIncoming;
import static mega.privacy.android.app.utils.MegaNodeUtil.*;
import static mega.privacy.android.app.utils.ProgressDialogUtil.*;
import static mega.privacy.android.app.utils.TimeUtils.getHumanizedTime;
import static mega.privacy.android.app.utils.UploadUtil.*;
import static mega.privacy.android.app.utils.Util.*;
import static nz.mega.sdk.MegaApiJava.*;
import static nz.mega.sdk.MegaChatApiJava.MEGACHAT_INVALID_HANDLE;

@AndroidEntryPoint
public class ManagerActivityLollipop extends TransfersManagementActivity
		implements MegaRequestListenerInterface, MegaChatListenerInterface,
		MegaChatRequestListenerInterface, OnNavigationItemSelectedListener,
		MegaGlobalListenerInterface, MegaTransferListenerInterface, OnClickListener,
		BottomNavigationView.OnNavigationItemSelectedListener, UploadBottomSheetDialogActionListener,
		BillingUpdatesListener, ChatManagementCallback, ActionNodeCallback, SnackbarShower {

	private static final String TRANSFER_OVER_QUOTA_SHOWN = "TRANSFER_OVER_QUOTA_SHOWN";

	public static final String TRANSFERS_TAB = "TRANSFERS_TAB";
	private static final String SEARCH_SHARED_TAB = "SEARCH_SHARED_TAB";
	private static final String SEARCH_DRAWER_ITEM = "SEARCH_DRAWER_ITEM";
	private static final String DRAWER_ITEM_BEFORE_OPEN_FULLSCREEN_OFFLINE = "DRAWER_ITEM_BEFORE_OPEN_FULLSCREEN_OFFLINE";
	public static final String OFFLINE_SEARCH_QUERY = "OFFLINE_SEARCH_QUERY:";
	private static final String MK_LAYOUT_VISIBLE = "MK_LAYOUT_VISIBLE";

    private static final String BUSINESS_GRACE_ALERT_SHOWN = "BUSINESS_GRACE_ALERT_SHOWN";
	private static final String BUSINESS_CU_ALERT_SHOWN = "BUSINESS_CU_ALERT_SHOWN";
	public static final String BUSINESS_CU_FRAGMENT_SETTINGS = "BUSINESS_CU_FRAGMENT_SETTINGS";
	public static final String BUSINESS_CU_FRAGMENT_CU = "BUSINESS_CU_FRAGMENT_CU";

	private static final String DEEP_BROWSER_TREE_LINKS = "DEEP_BROWSER_TREE_LINKS";
    private static final String PARENT_HANDLE_LINKS = "PARENT_HANDLE_LINKS";
    public static final String NEW_CREATION_ACCOUNT = "NEW_CREATION_ACCOUNT";
    public static final String JOINING_CHAT_LINK = "JOINING_CHAT_LINK";
    public static final String LINK_JOINING_CHAT_LINK = "LINK_JOINING_CHAT_LINK";
    public static final String CONNECTED = "CONNECTED";

    private static final String SMALL_GRID = "SMALL_GRID";

	public static final int ERROR_TAB = -1;
	public static final int INCOMING_TAB = 0;
	public static final int OUTGOING_TAB = 1;
  	public static final int LINKS_TAB = 2;
	public static final int CONTACTS_TAB = 0;
	public static final int SENT_REQUESTS_TAB = 1;
	public static final int RECEIVED_REQUESTS_TAB = 2;
	public static final int GENERAL_TAB = 0;
	public static final int STORAGE_TAB = 1;
	public static final int PENDING_TAB = 0;
	public static final int COMPLETED_TAB = 1;

	private static final int CLOUD_DRIVE_BNV = 0;
	private static final int CAMERA_UPLOADS_BNV = 1;
	private static final int HOMEPAGE_BNV = 2;
	private static final int CHAT_BNV = 3;
	private static final int SHARED_BNV = 4;
	private static final int HIDDEN_BNV = 5;
	private static final int MEDIA_UPLOADS_BNV = 6;
	// 8dp + 56dp(Fab's size) + 8dp
    public static final int TRANSFER_WIDGET_MARGIN_BOTTOM = 72;

	/** The causes of elevating the app bar */
	public static final int ELEVATION_SCROLL = 0x01;
    public static final int ELEVATION_CALL_IN_PROGRESS = 0x02;
    /** The cause bitmap of elevating the app bar */
    private int mElevationCause;
    /** True if any TabLayout is visible */
    private boolean mShowAnyTabLayout;

    private LastShowSMSDialogTimeChecker smsDialogTimeChecker;

	@Inject
	CookieDialogHandler cookieDialogHandler;
	@Inject
	SortOrderManagement sortOrderManagement;

	public int accountFragment;

	private long handleInviteContact = -1;

	public ArrayList<Integer> transfersInProgress;
	public MegaTransferData transferData;

	public long transferCallback = 0;

	//GET PRO ACCOUNT PANEL
	LinearLayout getProLayout=null;
	TextView getProText;
	TextView leftCancelButton;
	TextView rightUpgradeButton;
	Button addPhoneNumberButton;
	TextView addPhoneNumberLabel;
	FloatingActionButton fabButton;

	AlertDialog evaluateAppDialog;

	MegaNode inboxNode = null;

	private boolean mkLayoutVisible = false;

	MegaNode rootNode = null;

	NodeController nC;
	ContactController cC;
	AccountController aC;

	private final MegaAttacher nodeAttacher = new MegaAttacher(this);
	private final NodeSaver nodeSaver = new NodeSaver(this, this, this,
			AlertsAndWarnings.showSaveToDeviceConfirmDialog(this));

	private AndroidCompletedTransfer selectedTransfer;
	MegaNode selectedNode;
	MegaOffline selectedOfflineNode;
	MegaContactAdapter selectedUser;
	MegaContactRequest selectedRequest;

	public long selectedChatItemId;

	private BadgeDrawerArrowDrawable badgeDrawable;

	MegaPreferences prefs = null;
	MegaAttributes attr = null;
	static ManagerActivityLollipop managerActivity = null;
	MegaApplication app = null;
	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;
	Handler handler;
	DisplayMetrics outMetrics;
    float scaleText;
	FragmentContainerView fragmentContainer;
    ActionBar aB;
    MaterialToolbar toolbar;
    AppBarLayout abL;

	int selectedAccountType;
	int displayedAccountType;

	int countUserAttributes=0;
	int errorUserAttibutes=0;

	ShareInfo infoManager;
	MegaNode parentNodeManager;

	boolean firstNavigationLevel = true;
    public DrawerLayout drawerLayout;
    ArrayList<MegaUser> contacts = new ArrayList<>();
    ArrayList<MegaUser> visibleContacts = new ArrayList<>();

    public boolean openFolderRefresh = false;

    public boolean openSettingsStorage = false;
    public boolean openSettingsQR = false;
	boolean newAccount = false;
	public boolean newCreationAccount;

	private int storageState = MegaApiJava.STORAGE_STATE_UNKNOWN; //Default value
	private int storageStateFromBroadcast = MegaApiJava.STORAGE_STATE_UNKNOWN; //Default value
    private boolean showStorageAlertWithDelay;

	private boolean isStorageStatusDialogShown = false;

	private boolean isTransferOverQuotaWarningShown;
	private AlertDialog transferOverQuotaWarning;
	private AlertDialog confirmationTransfersDialog;

	private boolean userNameChanged;
	private boolean userEmailChanged;

	private AlertDialog reconnectDialog;

	private RelativeLayout navigationDrawerAddPhoneContainer;
    int orientationSaved;

    private boolean isSMSDialogShowing;
    private String bonusStorageSMS = "GB";
    private final static String STATE_KEY_SMS_DIALOG =  "isSMSDialogShowing";
    private final static String STATE_KEY_SMS_BONUS =  "bonusStorageSMS";
	private BillingManager mBillingManager;
	private List<MegaSku> mSkuDetailsList;

	public enum FragmentTag {
		CLOUD_DRIVE, HOMEPAGE, CAMERA_UPLOADS, INBOX, INCOMING_SHARES, OUTGOING_SHARES, CONTACTS, RECEIVED_REQUESTS, SENT_REQUESTS, SETTINGS, MY_ACCOUNT, MY_STORAGE, SEARCH, TRANSFERS, COMPLETED_TRANSFERS, RECENT_CHAT, RUBBISH_BIN, NOTIFICATIONS, UPGRADE_ACCOUNT, TURN_ON_NOTIFICATIONS, EXPORT_RECOVERY_KEY, PERMISSIONS, SMS_VERIFICATION, LINKS;

		public String getTag () {
			switch (this) {
				case CLOUD_DRIVE: return "fbFLol";
				case HOMEPAGE: return "fragmentHomepage";
				case RUBBISH_BIN: return "rubbishBinFLol";
				case CAMERA_UPLOADS: return "cuFLol";
				case INBOX: return "iFLol";
				case INCOMING_SHARES: return "isF";
				case OUTGOING_SHARES: return "osF";
				case CONTACTS: return "android:switcher:" + R.id.contact_tabs_pager + ":" + 0;
				case SENT_REQUESTS: return "android:switcher:" + R.id.contact_tabs_pager + ":" + 1;
				case RECEIVED_REQUESTS: return "android:switcher:" + R.id.contact_tabs_pager + ":" + 2;
				case SETTINGS: return "sttF";
				case MY_ACCOUNT: return "android:switcher:" + R.id.my_account_tabs_pager + ":" + 0;
				case MY_STORAGE: return "android:switcher:" + R.id.my_account_tabs_pager + ":" + 1;
				case SEARCH: return "sFLol";
				case TRANSFERS: return "android:switcher:" + R.id.transfers_tabs_pager + ":" + 0;
				case COMPLETED_TRANSFERS: return "android:switcher:" + R.id.transfers_tabs_pager + ":" + 1;
				case RECENT_CHAT: return "rChat";
				case NOTIFICATIONS: return "notificFragment";
				case UPGRADE_ACCOUNT: return "upAFL";
				case TURN_ON_NOTIFICATIONS: return "tonF";
				case EXPORT_RECOVERY_KEY: return "eRKeyF";
				case PERMISSIONS: return "pF";
                case SMS_VERIFICATION: return "svF";
				case LINKS: return "lF";
			}
			return null;
		}
	}

	public enum DrawerItem {
		CLOUD_DRIVE, CAMERA_UPLOADS, HOMEPAGE, CHAT, SHARED_ITEMS,
		ACCOUNT, CONTACTS, NOTIFICATIONS, SETTINGS,
		INBOX, SEARCH, TRANSFERS, RUBBISH_BIN,
		ASK_PERMISSIONS;

		public String getTitle(Context context) {
			switch(this)
			{
				case CLOUD_DRIVE: return context.getString(R.string.section_cloud_drive);
				case CAMERA_UPLOADS: return context.getString(R.string.section_photo_sync);
				case INBOX: return context.getString(R.string.section_inbox);
				case SHARED_ITEMS: return context.getString(R.string.title_shared_items);
				case CONTACTS: {
					context.getString(R.string.section_contacts);
				}
				case SETTINGS: return context.getString(R.string.action_settings);
				case ACCOUNT: return context.getString(R.string.section_account);
				case SEARCH: return context.getString(R.string.action_search);
				case TRANSFERS: return context.getString(R.string.section_transfers);
				case CHAT: return context.getString(R.string.section_chat);
				case RUBBISH_BIN: return context.getString(R.string.section_rubbish_bin);
				case NOTIFICATIONS: return context.getString(R.string.title_properties_chat_contact_notifications);
			}
			return null;
		}
	}

	public boolean turnOnNotifications = false;

	private int searchSharedTab = -1;
	private DrawerItem searchDrawerItem = null;
	private DrawerItem drawerItem;
	DrawerItem drawerItemPreUpgradeAccount = null;
	int accountFragmentPreUpgradeAccount = -1;
	static MenuItem drawerMenuItem = null;
	LinearLayout fragmentLayout;
	BottomNavigationViewEx bNV;
	NavigationView nV;
	RelativeLayout usedSpaceLayout;
	private EmojiTextView nVDisplayName;
	TextView nVEmail;
	TextView businessLabel;
	RoundedImageView nVPictureProfile;
	TextView spaceTV;
	ProgressBar usedSpacePB;

	private MiniAudioPlayerController miniAudioPlayerController;

	private LinearLayout cuViewTypes;
	private TextView cuYearsButton;
	private TextView cuMonthsButton;
	private TextView cuDaysButton;
	private TextView cuAllButton;
	private LinearLayout cuLayout;
	private Button enableCUButton;
	private ProgressBar cuProgressBar;

	//Tabs in Shares
	private TabLayout tabLayoutShares;
	private SharesPageAdapter sharesPageAdapter;
	private CustomViewPager viewPagerShares;

	//Tabs in Contacts
	private TabLayout tabLayoutContacts;
	private ContactsPageAdapter contactsPageAdapter;
	private CustomViewPager viewPagerContacts;

	//Tabs in My Account
	private TabLayout tabLayoutMyAccount;
	private MyAccountPageAdapter mTabsAdapterMyAccount;
	private ViewPager viewPagerMyAccount;

	//Tabs in Transfers
	private TabLayout tabLayoutTransfers;
	private TransfersPageAdapter mTabsAdapterTransfers;
	private CustomViewPager viewPagerTransfers;

	private RelativeLayout callInProgressLayout;
	private Chronometer callInProgressChrono;
	private TextView callInProgressText;
	private LinearLayout microOffLayout;
	private LinearLayout videoOnLayout;

	boolean firstTimeAfterInstallation = true;
	SearchView searchView;
	public boolean searchExpand = false;
	private String searchQuery = "";
	public boolean textSubmitted = false;
	public boolean textsearchQuery = false;
	boolean isSearching = false;
	public int levelsSearch = -1;
	boolean openLink = false;

	long lastTimeOnTransferUpdate = Calendar.getInstance().getTimeInMillis();

	boolean firstLogin = false;
	private boolean askPermissions = false;
	private boolean isClearRubbishBin = false;
	private boolean moveToRubbish = false;
	private boolean restoreFromRubbish = false;

	boolean megaContacts = true;
	String feedback;

	private HomepageScreen mHomepageScreen = HomepageScreen.HOMEPAGE;

	private enum HomepageScreen {
       	HOMEPAGE, PHOTOS, DOCUMENTS, AUDIO, VIDEO,
       	FULLSCREEN_OFFLINE, OFFLINE_FILE_INFO, RECENT_BUCKET
	}

	public boolean isSmallGridCameraUploads = false;

	public boolean passwordReminderFromMyAccount = false;

	public boolean isList = true;

	private long parentHandleBrowser;
	private long parentHandleRubbish;
	private long parentHandleIncoming;
	private long parentHandleLinks;
	private long parentHandleOutgoing;
	private long parentHandleSearch;
	private long parentHandleInbox;
	private String pathNavigationOffline;
	public int deepBrowserTreeIncoming = 0;
	public int deepBrowserTreeOutgoing = 0;
	private int deepBrowserTreeLinks;

	int indexShares = -1;
	int indexContacts = -1;
	int indexAccount = -1;
	int indexTransfers = -1;

	//LOLLIPOP FRAGMENTS
    private FileBrowserFragmentLollipop fbFLol;
    private RubbishBinFragmentLollipop rubbishBinFLol;
    private InboxFragmentLollipop iFLol;
    private IncomingSharesFragmentLollipop inSFLol;
	private OutgoingSharesFragmentLollipop outSFLol;
	private LinksFragment lF;
	private ContactsFragmentLollipop cFLol;
	private ReceivedRequestsFragmentLollipop rRFLol;
	private SentRequestsFragmentLollipop sRFLol;
	private MyAccountFragmentLollipop maFLol;
	private MyStorageFragmentLollipop mStorageFLol;
	private TransfersFragmentLollipop tFLol;
	private CompletedTransfersFragmentLollipop completedTFLol;
	private SearchFragmentLollipop sFLol;
	private SettingsFragmentLollipop sttFLol;
	private UpgradeAccountFragmentLollipop upAFL;
	private CameraUploadsFragment cuFragment;
	private RecentChatsFragmentLollipop rChatFL;
	private NotificationsFragmentLollipop notificFragment;
	private TurnOnNotificationsFragment tonF;
	private ExportRecoveryKeyFragment eRKeyF;
	private PermissionsFragment pF;
	private SMSVerificationFragment svF;

	private boolean mStopped = true;
	private DrawerItem drawerItemBeforeOpenFullscreenOffline = null;
	private OfflineFragment fullscreenOfflineFragment;
	private OfflineFragment pagerOfflineFragment;
	private RecentsFragment pagerRecentsFragment;

	ProgressDialog statusDialog;

	private AlertDialog permissionsDialog;
	private AlertDialog presenceStatusDialog;
	private AlertDialog openLinkDialog;
	private boolean openLinkDialogIsShown = false;
	private boolean openLinkDialogIsErrorShown = false;
	private AlertDialog alertNotPermissionsUpload;
	private AlertDialog clearRubbishBinDialog;
	private AlertDialog insertPassDialog;
	private AlertDialog changeUserAttributeDialog;
	private AlertDialog alertDialogStorageStatus;
	private AlertDialog alertDialogSMSVerification;
	private AlertDialog newTextFileDialog;

	private MenuItem searchMenuItem;
	private MenuItem gridSmallLargeMenuItem;
	private MenuItem addContactMenuItem;
	private MenuItem addMenuItem;
	private MenuItem createFolderMenuItem;
	private MenuItem importLinkMenuItem;
	private MenuItem enableSelectMenuItem;
	private MenuItem selectMenuItem;
	private MenuItem unSelectMenuItem;
	private MenuItem thumbViewMenuItem;
	private MenuItem refreshMenuItem;
	private MenuItem sortByMenuItem;
	private MenuItem helpMenuItem;
	private MenuItem doNotDisturbMenuItem;
	private MenuItem upgradeAccountMenuItem;
	private MenuItem clearRubbishBinMenuitem;
	private MenuItem changePass;
	private MenuItem exportMK;
	private MenuItem takePicture;
	private MenuItem cancelSubscription;
	private MenuItem killAllSessions;
	private MenuItem cancelAllTransfersMenuItem;
	private MenuItem playTransfersMenuIcon;
	private MenuItem pauseTransfersMenuIcon;
	private MenuItem logoutMenuItem;
	private MenuItem forgotPassMenuItem;
	private MenuItem inviteMenuItem;
	private MenuItem retryTransfers;
	private MenuItem clearCompletedTransfers;
	private MenuItem scanQRcodeMenuItem;
	private MenuItem returnCallMenuItem;

	private Chronometer chronometerMenuItem;
	private LinearLayout layoutCallMenuItem;

	private int typesCameraPermission = INVALID_TYPE_PERMISSIONS;
	AlertDialog enable2FADialog;
	boolean isEnable2FADialogShown = false;
	Button enable2FAButton;
	Button skip2FAButton;

	private boolean is2FAEnabled = false;

	public boolean comesFromNotifications = false;
	public int comesFromNotificationsLevel = 0;
	public long comesFromNotificationHandle = -1;
	public long comesFromNotificationHandleSaved = -1;
	public int comesFromNotificationDeepBrowserTreeIncoming = -1;

	RelativeLayout myAccountHeader;
	ImageView contactStatus;
	RelativeLayout myAccountSection;
	RelativeLayout inboxSection;
	RelativeLayout contactsSection;
	RelativeLayout notificationsSection;
	private RelativeLayout transfersSection;
	RelativeLayout settingsSection;
	Button upgradeAccount;
	TextView contactsSectionText;
	TextView notificationsSectionText;
	int bottomNavigationCurrentItem = -1;
	View chatBadge;
	View callBadge;

	private boolean connected;

	private boolean joiningToChatLink;
	private String linkJoinToChatLink;

	private boolean onAskingPermissionsFragment = false;
	public boolean onAskingSMSVerificationFragment = false;

	private View mNavHostView;
	private NavController mNavController;
	private HomepageSearchable mHomepageSearchable;

	private BroadcastReceiver refreshAddPhoneNumberButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,Intent intent) {
            if (intent != null && intent.getAction() == BROADCAST_ACTION_INTENT_REFRESH_ADD_PHONE_NUMBER) {
                if(drawerLayout != null) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }
                refreshAddPhoneNumberButton();
            }
        }
    };
	private EditText openLinkText;
	private RelativeLayout openLinkError;
	private TextView openLinkErrorText;
	private Button openLinkOpenButton;

	private boolean isBusinessGraceAlertShown;
	private AlertDialog businessGraceAlert;
	private boolean isBusinessCUAlertShown;
	private AlertDialog businessCUAlert;

	private BottomSheetDialogFragment bottomSheetDialogFragment;
	private PsaViewHolder psaViewHolder;

	/**
	 * Broadcast to update the completed transfers tab.
	 */
	private BroadcastReceiver transferFinishReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isTransfersCompletedAdded()) {
				return;
			}

			if (intent == null || intent.getAction() == null
					|| !intent.getAction().equals(BROADCAST_ACTION_TRANSFER_FINISH)) {
				return;
			}

			AndroidCompletedTransfer completedTransfer = intent.getParcelableExtra(COMPLETED_TRANSFER);
			if (completedTransfer == null) {
				return;
			}

			completedTFLol.transferFinish(completedTransfer);
		}
	};


	/**
	 * Broadcast to show a "transfer over quota" warning if it is on Transfers section.
	 */
	private BroadcastReceiver transferOverQuotaUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateWidget(intent);

			if (intent == null) return;

			if (intent.getAction() != null && intent.getAction().equals(ACTION_TRANSFER_OVER_QUOTA) && drawerItem == DrawerItem.TRANSFERS && isActivityInForeground()) {
				showTransfersTransferOverQuotaWarning();
			}

			if (MegaApplication.getTransfersManagement().thereAreFailedTransfers() && drawerItem == DrawerItem.TRANSFERS && getTabItemTransfers() == COMPLETED_TAB && !retryTransfers.isVisible()) {
				retryTransfers.setVisible(true);
			}
		}
	};

	private BroadcastReceiver chatArchivedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) return;

			String title = intent.getStringExtra(CHAT_TITLE);
			if (title != null) {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.success_archive_chat, title), -1);
			}
		}
	};

	private BroadcastReceiver updateMyAccountReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null){
                if (ACTION_STORAGE_STATE_CHANGED.equals(intent.getAction())) {
                    storageStateFromBroadcast = intent.getIntExtra(EXTRA_STORAGE_STATE, MegaApiJava.STORAGE_STATE_UNKNOWN);
                    if (!showStorageAlertWithDelay) {
                        checkStorageStatus(storageStateFromBroadcast != MegaApiJava.STORAGE_STATE_UNKNOWN ?
								storageStateFromBroadcast : app.getStorageState(), false);
                    }
                    updateAccountDetailsVisibleInfo();
                    return;
				}

				int actionType = intent.getIntExtra(ACTION_TYPE, INVALID_ACTION);

				if(actionType == UPDATE_GET_PRICING){
					logDebug("BROADCAST TO UPDATE AFTER GET PRICING");
					//UPGRADE_ACCOUNT_FRAGMENT
					upAFL = (UpgradeAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.UPGRADE_ACCOUNT.getTag());
					if(upAFL!=null){
						upAFL.setPricingInfo();
					}
				}
				else if(actionType == UPDATE_ACCOUNT_DETAILS){
					logDebug("BROADCAST TO UPDATE AFTER UPDATE_ACCOUNT_DETAILS");
					if (isFinishing()) {
						return;
					}

					updateAccountDetailsVisibleInfo();

					//Check if myAccount section is visible
					if (getMyAccountFragment() != null) {
						logDebug("Update the account fragment");
						maFLol.setAccountDetails();
					}

					if (getMyStorageFragment() != null) {
						logDebug("Update the account fragment");
						mStorageFLol.setAccountDetails();
					}

					if (getUpgradeAccountFragment() != null) {
						if (drawerItem == DrawerItem.ACCOUNT && accountFragment == UPGRADE_ACCOUNT_FRAGMENT && megaApi.isBusinessAccount()) {
							closeUpgradeAccountFragment();
						} else {
							upAFL.showAvailableAccount();
						}
					}

					if (megaApi.isBusinessAccount()) {
						supportInvalidateOptionsMenu();
					}
				}
				else if(actionType == UPDATE_CREDIT_CARD_SUBSCRIPTION){
					logDebug("BROADCAST TO UPDATE AFTER UPDATE_CREDIT_CARD_SUBSCRIPTION");
					updateCancelSubscriptions();
				}
				else if(actionType == UPDATE_PAYMENT_METHODS){
					logDebug("BROADCAST TO UPDATE AFTER UPDATE_PAYMENT_METHODS");
				}
			}
		}
	};

	private BroadcastReceiver receiverUpdate2FA = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				boolean enabled = intent.getBooleanExtra(INTENT_EXTRA_KEY_ENABLED, false);
				is2FAEnabled = enabled;
				if (getSettingsFragment() != null) {
					sttFLol.update2FAPreference(enabled);
				}
			}
		}
	};

	private final BroadcastReceiver receiverUpdateOrder = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || !BROADCAST_ACTION_INTENT_UPDATE_ORDER.equals(intent.getAction())) {
				return;
			}

			if (intent.getBooleanExtra(IS_CLOUD_ORDER, true)) {
				refreshCloudOrder(intent.getIntExtra(NEW_ORDER, ORDER_DEFAULT_ASC));
			} else {
				refreshOthersOrder();
			}
		}
	};

    private BroadcastReceiver receiverUpdateView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                updateView(intent.getBooleanExtra("isList", true));
				supportInvalidateOptionsMenu();
            }
        }
    };

	private BroadcastReceiver receiverCUAttrChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			synchronized (this){
				if (drawerItem == DrawerItem.CAMERA_UPLOADS) {
					cameraUploadsClicked();
				}

				//update folder icon
				onNodesCloudDriveUpdate();
			}
		}
	};

	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			logDebug("Network broadcast received!");
			int actionType;

			if (intent != null){
				actionType = intent.getIntExtra(ACTION_TYPE, INVALID_ACTION);

				if(actionType == GO_OFFLINE){
				    //stop cu process
                    stopRunningCameraUploadService(ManagerActivityLollipop.this);
					showOfflineMode();
				}
				else if(actionType == GO_ONLINE){
					showOnlineMode();
				}
				else if(actionType == START_RECONNECTION){
					startConnection();
				}
			}
		}
	};

	private BroadcastReceiver cameraUploadLauncherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,Intent intent) {
            try {
				logDebug("cameraUploadLauncherReceiver: Start service here");
                startCameraUploadServiceIgnoreAttr(ManagerActivityLollipop.this);
            } catch (Exception e) {
				logError("cameraUploadLauncherReceiver: Exception", e);
            }
        }
    };

	private BroadcastReceiver contactUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null)
				return;


			long userHandle = intent.getLongExtra(EXTRA_USER_HANDLE, INVALID_HANDLE);

			if (intent.getAction().equals(ACTION_UPDATE_NICKNAME)
					|| intent.getAction().equals(ACTION_UPDATE_FIRST_NAME)
					|| intent.getAction().equals(ACTION_UPDATE_LAST_NAME)) {
				if (getContactsFragment() != null) {
					cFLol.updateContact(userHandle);
				}

				if (isIncomingAdded() && inSFLol.getItemCount() > 0) {
					inSFLol.updateContact(userHandle);
				}

				if (isOutgoingAdded() && outSFLol.getItemCount() > 0) {
					outSFLol.updateContact(userHandle);
				}
			} else if (intent.getAction().equals(ACTION_UPDATE_CREDENTIALS) && getContactsFragment() != null) {
				cFLol.updateContact(userHandle);
			}
		}
	};

	private BroadcastReceiver chatCallUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null)
				return;

			long chatIdReceived = intent.getLongExtra(UPDATE_CHAT_CALL_ID, MEGACHAT_INVALID_HANDLE);

			if (chatIdReceived == MEGACHAT_INVALID_HANDLE)
				return;

			if (intent.getAction().equals(ACTION_CALL_STATUS_UPDATE)) {
				int callStatus = intent.getIntExtra(UPDATE_CALL_STATUS, INVALID_CALL_STATUS);
				switch (callStatus) {
					case MegaChatCall.CALL_STATUS_REQUEST_SENT:
					case MegaChatCall.CALL_STATUS_RING_IN:
					case MegaChatCall.CALL_STATUS_IN_PROGRESS:
					case MegaChatCall.CALL_STATUS_RECONNECTING:
					case MegaChatCall.CALL_STATUS_JOINING:
					case MegaChatCall.CALL_STATUS_DESTROYED:
					case MegaChatCall.CALL_STATUS_USER_NO_PRESENT:
						updateVisibleCallElements(chatIdReceived);
						break;
				}
			}

			if (intent.getAction().equals(ACTION_CHANGE_CALL_ON_HOLD)) {
				updateVisibleCallElements(chatIdReceived);
			}

			if (intent.getAction().equals(ACTION_CHANGE_LOCAL_AVFLAGS)) {
				MegaChatCall callInProgress = getCallInProgress();
				if (callInProgress != null && callInProgress.getChatid() == chatIdReceived) {
					showHideMicroAndVideoIcons(callInProgress, microOffLayout, videoOnLayout);
				}
			}
		}
	};

	private BroadcastReceiver chatSessionUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null)
				return;

			long chatIdReceived = intent.getLongExtra(UPDATE_CHAT_CALL_ID, MEGACHAT_INVALID_HANDLE);

			if (chatIdReceived == MEGACHAT_INVALID_HANDLE)
				return;

			if (intent.getAction().equals(ACTION_CHANGE_SESSION_ON_HOLD)) {
				updateVisibleCallElements(chatIdReceived);
			}
		}
	};

	private BroadcastReceiver chatRoomMuteUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null)
				return;

			if(intent.getAction().equals(ACTION_UPDATE_PUSH_NOTIFICATION_SETTING)){
				if (getChatsFragment() != null) {
					rChatFL.notifyPushChanged();
				}
			}
		}
	};

	private BroadcastReceiver updateCUSettingsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction() != null && getSettingsFragment() != null
					&& (intent.getAction().equals(ACTION_REFRESH_CAMERA_UPLOADS_SETTING)
					|| intent.getAction().equals(ACTION_REFRESH_CAMERA_UPLOADS_SETTING_SUBTITLE))) {
				sttFLol.refreshCameraUploadsSettings();
			}
		}
	};

	private final BroadcastReceiver cuUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || !ACTION_UPDATE_CU.equals(intent.getAction())) {
				return;
			}

			updateCUProgress(intent.getIntExtra(PROGRESS, 0),
					intent.getIntExtra(PENDING_TRANSFERS, 0));
		}
	};

    public void launchPayment(String productId) {
        //start purchase/subscription flow
        MegaSku skuDetails = getSkuDetails(mSkuDetailsList, productId);
        MegaPurchase purchase = app.getMyAccountInfo().getActiveSubscription();
        String oldSku = purchase == null ? null : purchase.getSku();
        String token = purchase == null ? null : purchase.getToken();
        if (mBillingManager != null) {
            mBillingManager.initiatePurchaseFlow(oldSku, token, skuDetails);
        }
    }

	private MegaSku getSkuDetails(List<MegaSku> list, String key) {
		if (list == null || list.isEmpty()) {
			return null;
		}

		for (MegaSku details : list) {
			if (details.getSku().equals(key)) {
				return details;
			}
		}
		return null;
	}

    public void initGooglePlayPayments() {
        mBillingManager = new BillingManagerImpl(this, this);
    }

	@Override
    public void onBillingClientSetupFinished() {
        logInfo("Billing client setup finished");
        mBillingManager.getInventory(skuList -> {
            mSkuDetailsList = skuList;
            app.getMyAccountInfo().setAvailableSkus(skuList);

            upAFL = (UpgradeAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.UPGRADE_ACCOUNT.getTag());
            if (upAFL != null) {
                upAFL.setPricingInfo();
            }
        });
    }

	@Override
	public void onQueryPurchasesFinished(boolean isFailed, int resultCode, List<MegaPurchase> purchases) {
		if (isFailed || purchases == null) {
			logWarning("Query of purchases failed, result code is " + resultCode + ", is purchase null: " + (purchases == null));
			return;
		}

		updateAccountInfo(purchases);
		updateSubscriptionLevel(app.getMyAccountInfo());
	}

	@Override
	public void onPurchasesUpdated(boolean isFailed, int resultCode, List<MegaPurchase> purchases) {
        if (!isFailed) {
            String message;
            if (purchases != null && !purchases.isEmpty()) {
                MegaPurchase purchase = purchases.get(0);
                //payment may take time to process, we will not give privilege until it has been fully processed
                String sku = purchase.getSku();
                String subscriptionType = getSubscriptionType(this, sku);
                String subscriptionRenewalType = getSubscriptionRenewalType(this, sku);
                if (mBillingManager.isPurchased(purchase)) {
                    //payment has been processed
                    updateAccountInfo(purchases);
                    logDebug("Purchase " + sku + " successfully, subscription type is: " + subscriptionType + ", subscription renewal type is: " + subscriptionRenewalType);
                    message = getString(R.string.message_user_purchased_subscription, subscriptionType, subscriptionRenewalType);
                    updateSubscriptionLevel(app.getMyAccountInfo());
                } else {
                    //payment is being processed or in unknown state
                    logDebug("Purchase " + sku + " is being processed or in unknown state.");
                    message = getString(R.string.message_user_payment_pending);
                }
            } else {
                //down grade case
                logDebug("Downgrade, the new subscription takes effect when the old one expires.");
                message = getString(R.string.message_user_purchased_subscription_down_grade);
            }
            showAlert(this, message, null);
            drawerItem = DrawerItem.CLOUD_DRIVE;
            selectDrawerItemLollipop(drawerItem);
        } else {
            logWarning("Update purchase failed, with result code: " + resultCode);
        }
	}

	private void updateAccountInfo(List<MegaPurchase> purchases) {
		MyAccountInfo myAccountInfo = app.getMyAccountInfo();
		int highest = -1;
		int temp = -1;
		MegaPurchase max = null;
		for (MegaPurchase purchase : purchases) {
			switch (purchase.getSku()) {
				case SKU_PRO_LITE_MONTH:
				case SKU_PRO_LITE_YEAR:
					temp = 0;
					break;
				case SKU_PRO_I_MONTH:
				case SKU_PRO_I_YEAR:
                    temp = 1;
					break;
				case SKU_PRO_II_MONTH:
				case SKU_PRO_II_YEAR:
                    temp = 2;
					break;
				case SKU_PRO_III_MONTH:
				case SKU_PRO_III_YEAR:
                    temp = 3;
					break;
			}

			if(temp >= highest){
			    highest = temp;
			    max = purchase;
            }
		}

        if(max != null ){
            logDebug("Set current max subscription: " + max);
            myAccountInfo.setActiveSubscription(max);
        } else {
            myAccountInfo.setActiveSubscription(null);
        }

		myAccountInfo.setLevelInventory(highest);
		myAccountInfo.setInventoryFinished(true);

		upAFL = (UpgradeAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.UPGRADE_ACCOUNT.getTag());
		if (upAFL != null) {
			upAFL.setPricingInfo();
		}
	}

	/**
	 * Method for updating the visible elements related to a call.
	 *
	 * @param chatIdReceived The chat ID of a call.
	 */
	private void updateVisibleCallElements(long chatIdReceived) {
		if (getChatsFragment() != null && rChatFL.isVisible()) {
			rChatFL.refreshNode(megaChatApi.getChatListItem(chatIdReceived));
		}

		if (isScreenInPortrait(ManagerActivityLollipop.this)) {
			setCallWidget();
		} else {
			supportInvalidateOptionsMenu();
		}
	}

	private void updateSubscriptionLevel(MyAccountInfo myAccountInfo) {
		MegaPurchase highestGooglePlaySubscription = myAccountInfo.getActiveSubscription();
		if (!myAccountInfo.isAccountDetailsFinished() || highestGooglePlaySubscription == null) {
			return;
		}

		String json = highestGooglePlaySubscription.getReceipt();
		logDebug("ORIGINAL JSON:" + json); //Print JSON in logs to help debug possible payments issues

		MegaAttributes attributes = dbH.getAttributes();
		long lastPublicHandle = attributes.getLastPublicHandle();

		if (myAccountInfo.getLevelInventory() > myAccountInfo.getLevelAccountDetails()) {
			if (lastPublicHandle == INVALID_HANDLE) {
				megaApi.submitPurchaseReceipt(PAYMENT_GATEWAY, json, this);
			} else {
				megaApi.submitPurchaseReceipt(PAYMENT_GATEWAY, json, lastPublicHandle,
						attributes.getLastPublicHandleType(), attributes.getLastPublicHandleTimeStamp(), this);
			}
		}
	}

	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch(requestCode){
			case REQUEST_UPLOAD_CONTACT:{
				uploadContactInfo(infoManager, parentNodeManager);
				break;
			}
	        case REQUEST_CAMERA:{
				if (typesCameraPermission == TAKE_PICTURE_OPTION) {
					logDebug("TAKE_PICTURE_OPTION");
		        	if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
		        		if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
		        			ActivityCompat.requestPermissions(this,
					                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
									REQUEST_WRITE_STORAGE);
		        		}
		        		else{
							checkTakePicture(this, TAKE_PHOTO_CODE);
							typesCameraPermission = INVALID_TYPE_PERMISSIONS;
						}
		        	}
	        	} else if (typesCameraPermission == TAKE_PROFILE_PICTURE) {
					logDebug("TAKE_PROFILE_PICTURE");
					if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
						if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
									REQUEST_WRITE_STORAGE);
						}
						else{
							this.takeProfilePicture();
							typesCameraPermission = INVALID_TYPE_PERMISSIONS;
						}
					}

				} else if ((typesCameraPermission == RETURN_CALL_PERMISSIONS || typesCameraPermission == START_CALL_PERMISSIONS) &&
						grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					controlCallPermissions();
				}
				break;
	        }
			case REQUEST_READ_WRITE_STORAGE:{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					showUploadPanel();
				}
				break;
			}
	        case REQUEST_WRITE_STORAGE:{
	        	if (firstLogin){
					logDebug("The first time");
	        		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
						if (typesCameraPermission==TAKE_PICTURE_OPTION){
							logDebug("TAKE_PICTURE_OPTION");
							if (!checkPermission(Manifest.permission.CAMERA)){
								ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
							}
							else{
								checkTakePicture(this, TAKE_PHOTO_CODE);
								typesCameraPermission = INVALID_TYPE_PERMISSIONS;
							}

							break;
						}
						else if (typesCameraPermission==TAKE_PROFILE_PICTURE){
							logDebug("TAKE_PROFILE_PICTURE");
							if (!checkPermission(Manifest.permission.CAMERA)){
								ActivityCompat.requestPermissions(this,
										new String[]{Manifest.permission.CAMERA},
										REQUEST_CAMERA);
							}
							else{
								this.takeProfilePicture();
								typesCameraPermission = INVALID_TYPE_PERMISSIONS;
							}

							break;
						}
		        	}
	        	}
	        	else{
					if (typesCameraPermission==TAKE_PICTURE_OPTION){
						logDebug("TAKE_PICTURE_OPTION");
						if (!checkPermission(Manifest.permission.CAMERA)){
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.CAMERA},
									REQUEST_CAMERA);
						}
						else{
							checkTakePicture(this, TAKE_PHOTO_CODE);
							typesCameraPermission = INVALID_TYPE_PERMISSIONS;
						}
					}
					else if (typesCameraPermission==TAKE_PROFILE_PICTURE){
						logDebug("TAKE_PROFILE_PICTURE");
						if (!checkPermission(Manifest.permission.CAMERA)){
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.CAMERA},
									REQUEST_CAMERA);
						}
						else{
							this.takeProfilePicture();
							typesCameraPermission = INVALID_TYPE_PERMISSIONS;
						}
					} else {
						refreshOfflineNodes();
					}

					break;
				}

				nodeSaver.handleRequestPermissionsResult(requestCode);
	        	break;
	        }

            case REQUEST_CAMERA_UPLOAD:
			case REQUEST_CAMERA_ON_OFF:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkIfShouldShowBusinessCUAlert();
                } else {
                    showSnackbar(SNACKBAR_TYPE, getString(R.string.on_refuse_storage_permission), INVALID_HANDLE);
                }

                break;

			case REQUEST_CAMERA_ON_OFF_FIRST_TIME:
                if(permissions.length == 0) {
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkIfShouldShowBusinessCUAlert();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[0])) {
                        if (getCameraUploadFragment() != null) {
							cuFragment.onStoragePermissionRefused();
                        }
                    } else {
                        showSnackbar(SNACKBAR_TYPE, getString(R.string.on_refuse_storage_permission), INVALID_HANDLE);
                    }
                }

                break;

			case PermissionsFragment.PERMISSIONS_FRAGMENT: {
				pF = (PermissionsFragment) getSupportFragmentManager().findFragmentByTag(FragmentTag.PERMISSIONS.getTag());
				if (pF != null) {
					pF.setNextPermission();
				}
				break;
			}

			case REQUEST_RECORD_AUDIO:
				if ((typesCameraPermission == RETURN_CALL_PERMISSIONS || typesCameraPermission == START_CALL_PERMISSIONS) &&
						grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					controlCallPermissions();
				}
				break;
        }
    }

	/**
	 * Method for checking the necessary actions when you have permission to start a call or return to one in progress.
	 */
	private void controlCallPermissions() {
		if (checkPermissionsCall(this, typesCameraPermission)) {
			switch (typesCameraPermission) {
				case RETURN_CALL_PERMISSIONS:
					returnActiveCall(this);
					break;

				case START_CALL_PERMISSIONS:
					MegaChatRoom chat = megaChatApi.getChatRoomByUser(MegaApplication.getUserWaitingForCall());
					if (chat != null) {
						startCallWithChatOnline(this, chat);
					}
					break;
			}
			typesCameraPermission = INVALID_TYPE_PERMISSIONS;
		}
	}

	public void setTypesCameraPermission(int typesCameraPermission) {
		this.typesCameraPermission = typesCameraPermission;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		logDebug("onSaveInstanceState");
		if (drawerItem != null){
			logDebug("DrawerItem = " + drawerItem);
		}
		else{
			logWarning("DrawerItem is null");
		}
		super.onSaveInstanceState(outState);
		outState.putLong("parentHandleBrowser", parentHandleBrowser);
		outState.putLong("parentHandleRubbish", parentHandleRubbish);
		outState.putLong("parentHandleIncoming", parentHandleIncoming);
		logDebug("IN BUNDLE -> parentHandleOutgoing: " + parentHandleOutgoing);
		outState.putLong(PARENT_HANDLE_LINKS, parentHandleLinks);
		outState.putLong("parentHandleOutgoing", parentHandleOutgoing);
		outState.putLong("parentHandleSearch", parentHandleSearch);
		outState.putLong("parentHandleInbox", parentHandleInbox);
		outState.putSerializable("drawerItem", drawerItem);
		outState.putSerializable(DRAWER_ITEM_BEFORE_OPEN_FULLSCREEN_OFFLINE,
				drawerItemBeforeOpenFullscreenOffline);
		outState.putSerializable(SEARCH_DRAWER_ITEM, searchDrawerItem);
		outState.putSerializable(SEARCH_SHARED_TAB, searchSharedTab);
		outState.putBoolean(EXTRA_FIRST_LOGIN, firstLogin);
		outState.putBoolean(STATE_KEY_SMS_DIALOG, isSMSDialogShowing);
		outState.putString(STATE_KEY_SMS_BONUS, bonusStorageSMS);

		if (parentHandleIncoming != INVALID_HANDLE) {
			outState.putInt("deepBrowserTreeIncoming", deepBrowserTreeIncoming);
		}

		if (parentHandleOutgoing != INVALID_HANDLE) {
			outState.putInt("deepBrowserTreeOutgoing", deepBrowserTreeOutgoing);
		}

		if (parentHandleLinks != INVALID_HANDLE) {
			outState.putInt(DEEP_BROWSER_TREE_LINKS, deepBrowserTreeLinks);
		}

		if (viewPagerShares != null) {
			indexShares = viewPagerShares.getCurrentItem();
		}
		outState.putInt("indexShares", indexShares);

		if (viewPagerContacts != null) {
			indexContacts = viewPagerContacts.getCurrentItem();
		}
		outState.putInt("indexContacts", indexContacts);
		outState.putString("pathNavigationOffline", pathNavigationOffline);
		if(drawerItem==DrawerItem.ACCOUNT){
			outState.putInt("accountFragment", accountFragment);
		}
		outState.putBoolean(MK_LAYOUT_VISIBLE, mkLayoutVisible);

		if(searchQuery!=null){
			outState.putInt("levelsSearch", levelsSearch);
			outState.putString("searchQuery", searchQuery);
			textsearchQuery = true;
			outState.putBoolean("textsearchQuery", textsearchQuery);
		}else {
			textsearchQuery = false;
		}
		if (passwordReminderFromMyAccount){
			outState.putBoolean("passwordReminderFromMyAccount", true);
		}
		if (turnOnNotifications){
			outState.putBoolean("turnOnNotifications", turnOnNotifications);
		}

		outState.putInt("orientationSaved", orientationSaved);
		outState.putBoolean("isEnable2FADialogShown", isEnable2FADialogShown);
		outState.putInt("bottomNavigationCurrentItem", bottomNavigationCurrentItem);
		outState.putBoolean("searchExpand", searchExpand);
		outState.putBoolean("comesFromNotifications", comesFromNotifications);
		outState.putInt("comesFromNotificationsLevel", comesFromNotificationsLevel);
		outState.putLong("comesFromNotificationHandle", comesFromNotificationHandle);
		outState.putLong("comesFromNotificationHandleSaved", comesFromNotificationHandleSaved);
		outState.putBoolean("onAskingPermissionsFragment", onAskingPermissionsFragment);
		pF = (PermissionsFragment) getSupportFragmentManager().findFragmentByTag(FragmentTag.PERMISSIONS.getTag());
		if (onAskingPermissionsFragment && pF != null) {
			getSupportFragmentManager().putFragment(outState, FragmentTag.PERMISSIONS.getTag(), pF);
		}
        outState.putBoolean("onAskingSMSVerificationFragment", onAskingSMSVerificationFragment);
        svF = (SMSVerificationFragment) getSupportFragmentManager().findFragmentByTag(FragmentTag.SMS_VERIFICATION.getTag());
        if (onAskingSMSVerificationFragment && svF != null) {
            getSupportFragmentManager().putFragment(outState, FragmentTag.SMS_VERIFICATION.getTag(), svF);
        }
		outState.putInt("elevation", mElevationCause);
		outState.putInt("storageState", storageState);
		outState.putBoolean("isStorageStatusDialogShown", isStorageStatusDialogShown);
		outState.putSerializable("drawerItemPreUpgradeAccount", drawerItemPreUpgradeAccount);
		outState.putInt("accountFragmentPreUpgradeAccount", accountFragmentPreUpgradeAccount);
		outState.putInt("comesFromNotificationDeepBrowserTreeIncoming", comesFromNotificationDeepBrowserTreeIncoming);
		outState.putBoolean("openLinkDialogIsShown", openLinkDialogIsShown);
		if (openLinkDialogIsShown) {
			if (openLinkText != null && openLinkText.getText() != null) {
				outState.putString("openLinkText", openLinkText.getText().toString());
			}
			else {
				outState.putString("openLinkText", "");
			}
			outState.putBoolean("openLinkDialogIsErrorShown", openLinkDialogIsErrorShown);
		}

		outState.putBoolean(BUSINESS_GRACE_ALERT_SHOWN, isBusinessGraceAlertShown);
		if (isBusinessCUAlertShown) {
			outState.putBoolean(BUSINESS_CU_ALERT_SHOWN, isBusinessCUAlertShown);
		}

		outState.putBoolean(TRANSFER_OVER_QUOTA_SHOWN, isTransferOverQuotaWarningShown);
		outState.putInt(TYPE_CALL_PERMISSION, typesCameraPermission);
		outState.putBoolean(JOINING_CHAT_LINK, joiningToChatLink);
		outState.putString(LINK_JOINING_CHAT_LINK, linkJoinToChatLink);
		outState.putBoolean(CONNECTED, connected);
		outState.putBoolean(SMALL_GRID, isSmallGridCameraUploads);

		if (getCameraUploadFragment() != null) {
			getSupportFragmentManager().putFragment(outState, FragmentTag.CAMERA_UPLOADS.getTag(), cuFragment);
		}

		checkNewTextFileDialogState(newTextFileDialog, outState);

		nodeAttacher.saveState(outState);
		nodeSaver.saveState(outState);
	}

	@Override
	public void onStart() {
		logDebug("onStart");

		mStopped = false;

		super.onStart();
	}

	@SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
		logDebug("onCreate");
//		Fragments are restored during the Activity's onCreate().
//		Importantly though, they are restored in the base Activity class's onCreate().
//		Thus if you call super.onCreate() first, all of the rest of your onCreate() method will execute after your Fragments have been restored.
		super.onCreate(savedInstanceState);
		logDebug("onCreate after call super");

		// This block for solving the issue below:
		// Android is installed for the first time. Press the “Open” button on the system installation dialog, press the home button to switch the app to background,
		// and then switch the app to foreground, causing the app to create a new instantiation.
		if (!isTaskRoot()) {
			Intent intent = getIntent();
			if (intent != null) {
				String action = intent.getAction();
				if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
					finish();
					return;
				}
			}
		}

		boolean selectDrawerItemPending = true;
		//upload from device, progress dialog should show when screen orientation changes.
        if (shouldShowDialog) {
            showProcessFileDialog(this,null);
        }

		getLifecycle().addObserver(cookieDialogHandler);

		if(savedInstanceState!=null){
			logDebug("Bundle is NOT NULL");
			parentHandleBrowser = savedInstanceState.getLong("parentHandleBrowser", -1);
			logDebug("savedInstanceState -> parentHandleBrowser: " + parentHandleBrowser);
			parentHandleRubbish = savedInstanceState.getLong("parentHandleRubbish", -1);
			parentHandleIncoming = savedInstanceState.getLong("parentHandleIncoming", -1);
			logDebug("savedInstanceState -> parentHandleIncoming: " + parentHandleIncoming);
			parentHandleOutgoing = savedInstanceState.getLong("parentHandleOutgoing", -1);
			logDebug("savedInstanceState -> parentHandleOutgoing: " + parentHandleOutgoing);
			parentHandleLinks = savedInstanceState.getLong(PARENT_HANDLE_LINKS, INVALID_HANDLE);
			parentHandleSearch = savedInstanceState.getLong("parentHandleSearch", -1);
			parentHandleInbox = savedInstanceState.getLong("parentHandleInbox", -1);
			deepBrowserTreeIncoming = savedInstanceState.getInt("deepBrowserTreeIncoming", 0);
			deepBrowserTreeOutgoing = savedInstanceState.getInt("deepBrowserTreeOutgoing", 0);
			deepBrowserTreeLinks = savedInstanceState.getInt(DEEP_BROWSER_TREE_LINKS, 0);
			isSMSDialogShowing = savedInstanceState.getBoolean(STATE_KEY_SMS_DIALOG, false);
			bonusStorageSMS = savedInstanceState.getString(STATE_KEY_SMS_BONUS);
			firstLogin = savedInstanceState.getBoolean(EXTRA_FIRST_LOGIN);
			askPermissions = savedInstanceState.getBoolean(EXTRA_ASK_PERMISSIONS);
			drawerItem = (DrawerItem) savedInstanceState.getSerializable("drawerItem");
			drawerItemBeforeOpenFullscreenOffline
					= (DrawerItem) savedInstanceState.getSerializable(DRAWER_ITEM_BEFORE_OPEN_FULLSCREEN_OFFLINE);
			searchDrawerItem = (DrawerItem) savedInstanceState.getSerializable(SEARCH_DRAWER_ITEM);
			searchSharedTab = savedInstanceState.getInt(SEARCH_SHARED_TAB);
			indexShares = savedInstanceState.getInt("indexShares", indexShares);
			logDebug("savedInstanceState -> indexShares: " + indexShares);
			indexContacts = savedInstanceState.getInt("indexContacts", 0);
			pathNavigationOffline = savedInstanceState.getString("pathNavigationOffline", pathNavigationOffline);
			logDebug("savedInstanceState -> pathNavigationOffline: " + pathNavigationOffline);
			accountFragment = savedInstanceState.getInt("accountFragment", -1);
			mkLayoutVisible = savedInstanceState.getBoolean(MK_LAYOUT_VISIBLE, false);
			selectedAccountType = savedInstanceState.getInt("selectedAccountType", -1);
			searchQuery = savedInstanceState.getString("searchQuery");
			textsearchQuery = savedInstanceState.getBoolean("textsearchQuery");
			levelsSearch = savedInstanceState.getInt("levelsSearch");
			passwordReminderFromMyAccount = savedInstanceState.getBoolean("passwordReminderFromaMyAccount", false);
			turnOnNotifications = savedInstanceState.getBoolean("turnOnNotifications", false);
			orientationSaved = savedInstanceState.getInt("orientationSaved");
			isEnable2FADialogShown = savedInstanceState.getBoolean("isEnable2FADialogShown", false);
			bottomNavigationCurrentItem = savedInstanceState.getInt("bottomNavigationCurrentItem", -1);
			searchExpand = savedInstanceState.getBoolean("searchExpand", false);
			comesFromNotifications = savedInstanceState.getBoolean("comesFromNotifications", false);
			comesFromNotificationsLevel = savedInstanceState.getInt("comesFromNotificationsLevel", 0);
			comesFromNotificationHandle = savedInstanceState.getLong("comesFromNotificationHandle", -1);
			comesFromNotificationHandleSaved = savedInstanceState.getLong("comesFromNotificationHandleSaved", -1);
			onAskingPermissionsFragment = savedInstanceState.getBoolean("onAskingPermissionsFragment", false);
			if (onAskingPermissionsFragment) {
				pF = (PermissionsFragment) getSupportFragmentManager().getFragment(savedInstanceState, FragmentTag.PERMISSIONS.getTag());
			}
            onAskingSMSVerificationFragment = savedInstanceState.getBoolean("onAskingSMSVerificationFragment", false);
            if (onAskingSMSVerificationFragment) {
                svF = (SMSVerificationFragment) getSupportFragmentManager().getFragment(savedInstanceState, FragmentTag.SMS_VERIFICATION.getTag());
            }
			mElevationCause = savedInstanceState.getInt("elevation", 0);
			storageState = savedInstanceState.getInt("storageState", MegaApiJava.STORAGE_STATE_UNKNOWN);
			isStorageStatusDialogShown = savedInstanceState.getBoolean("isStorageStatusDialogShown", false);
			drawerItemPreUpgradeAccount = (DrawerItem) savedInstanceState.getSerializable("drawerItemPreUpgradeAccount");
			accountFragmentPreUpgradeAccount = savedInstanceState.getInt("accountFragmentPreUpgradeAccount", -1);
			comesFromNotificationDeepBrowserTreeIncoming = savedInstanceState.getInt("comesFromNotificationDeepBrowserTreeIncoming", -1);
			openLinkDialogIsShown = savedInstanceState.getBoolean("openLinkDialogIsShown", false);
			isBusinessGraceAlertShown = savedInstanceState.getBoolean(BUSINESS_GRACE_ALERT_SHOWN, false);
			isBusinessCUAlertShown = savedInstanceState.getBoolean(BUSINESS_CU_ALERT_SHOWN, false);
			isTransferOverQuotaWarningShown = savedInstanceState.getBoolean(TRANSFER_OVER_QUOTA_SHOWN, false);
			typesCameraPermission = savedInstanceState.getInt(TYPE_CALL_PERMISSION, INVALID_TYPE_PERMISSIONS);
			joiningToChatLink = savedInstanceState.getBoolean(JOINING_CHAT_LINK, false);
			linkJoinToChatLink = savedInstanceState.getString(LINK_JOINING_CHAT_LINK);
			connected = savedInstanceState.getBoolean(CONNECTED, false);
			isSmallGridCameraUploads = savedInstanceState.getBoolean(SMALL_GRID, false);

			nodeAttacher.restoreState(savedInstanceState);
			nodeSaver.restoreState(savedInstanceState);
		}
		else{
			logDebug("Bundle is NULL");
			parentHandleBrowser = -1;
			parentHandleRubbish = -1;
			parentHandleIncoming = -1;
			parentHandleOutgoing = -1;
			parentHandleLinks = INVALID_HANDLE;
			parentHandleSearch = -1;
			parentHandleInbox = -1;
			indexContacts = -1;
			deepBrowserTreeIncoming = 0;
			deepBrowserTreeOutgoing = 0;
			deepBrowserTreeLinks = 0;
			this.setPathNavigationOffline(OFFLINE_ROOT);
		}

		IntentFilter contactUpdateFilter = new IntentFilter(BROADCAST_ACTION_INTENT_FILTER_CONTACT_UPDATE);
		contactUpdateFilter.addAction(ACTION_UPDATE_NICKNAME);
		contactUpdateFilter.addAction(ACTION_UPDATE_FIRST_NAME);
		contactUpdateFilter.addAction(ACTION_UPDATE_LAST_NAME);
		contactUpdateFilter.addAction(ACTION_UPDATE_CREDENTIALS);
		registerReceiver(contactUpdateReceiver, contactUpdateFilter);

		IntentFilter filter = new IntentFilter(BROADCAST_ACTION_INTENT_UPDATE_ACCOUNT_DETAILS);
		filter.addAction(ACTION_STORAGE_STATE_CHANGED);
		registerReceiver(updateMyAccountReceiver, filter);

		registerReceiver(receiverUpdate2FA,
				new IntentFilter(BROADCAST_ACTION_INTENT_UPDATE_2FA_SETTINGS));

		registerReceiver(networkReceiver,
				new IntentFilter(BROADCAST_ACTION_INTENT_CONNECTIVITY_CHANGE));

		registerReceiver(receiverCUAttrChanged,
				new IntentFilter(BROADCAST_ACTION_INTENT_CU_ATTR_CHANGE));

		registerReceiver(receiverUpdateOrder, new IntentFilter(BROADCAST_ACTION_INTENT_UPDATE_ORDER));
		registerReceiver(receiverUpdateView, new IntentFilter(BROADCAST_ACTION_INTENT_UPDATE_VIEW));
		registerReceiver(chatArchivedReceiver, new IntentFilter(BROADCAST_ACTION_INTENT_CHAT_ARCHIVED));

		registerReceiver(refreshAddPhoneNumberButtonReceiver,
				new IntentFilter(BROADCAST_ACTION_INTENT_REFRESH_ADD_PHONE_NUMBER));

		IntentFilter filterTransfers = new IntentFilter(BROADCAST_ACTION_INTENT_TRANSFER_UPDATE);
		filterTransfers.addAction(ACTION_TRANSFER_OVER_QUOTA);
		registerReceiver(transferOverQuotaUpdateReceiver, filterTransfers);

		registerReceiver(transferFinishReceiver, new IntentFilter(BROADCAST_ACTION_TRANSFER_FINISH));

		registerReceiver(chatSessionUpdateReceiver, new IntentFilter(ACTION_CHANGE_SESSION_ON_HOLD));
		IntentFilter filterCall = new IntentFilter(ACTION_CALL_STATUS_UPDATE);
		filterCall.addAction(ACTION_CHANGE_CALL_ON_HOLD);
		filterCall.addAction(ACTION_CHANGE_LOCAL_AVFLAGS);
		registerReceiver(chatCallUpdateReceiver, filterCall);

		registerReceiver(chatRoomMuteUpdateReceiver, new IntentFilter(ACTION_UPDATE_PUSH_NOTIFICATION_SETTING));
        registerReceiver(cameraUploadLauncherReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));

		registerTransfersReceiver();

        IntentFilter filterUpdateCUSettings = new IntentFilter(BROADCAST_ACTION_INTENT_SETTINGS_UPDATED);
		filterUpdateCUSettings.addAction(ACTION_REFRESH_CAMERA_UPLOADS_SETTING);
		filterUpdateCUSettings.addAction(ACTION_REFRESH_CAMERA_UPLOADS_SETTING_SUBTITLE);
        registerReceiver(updateCUSettingsReceiver, filterUpdateCUSettings);

        registerReceiver(cuUpdateReceiver, new IntentFilter(ACTION_UPDATE_CU));

        smsDialogTimeChecker = new LastShowSMSDialogTimeChecker(this);
        nC = new NodeController(this);
		cC = new ContactController(this);
		aC = new AccountController(this);

        createCacheFolders(this);

        dbH = DatabaseHandler.getDbHandler(getApplicationContext());

		managerActivity = this;
		app = (MegaApplication)getApplication();
		megaApi = app.getMegaApi();

		megaChatApi = app.getMegaChatApi();
		logDebug("addChatListener");
		megaChatApi.addChatListener(this);

		if (megaChatApi != null){
			logDebug("retryChatPendingConnections()");
			megaChatApi.retryPendingConnections(false, null);
		}

		MegaApplication.getPushNotificationSettingManagement().getPushNotificationSetting();

		transfersInProgress = new ArrayList<Integer>();

		//sync local contacts to see who's on mega.
		if (hasPermissions(this, Manifest.permission.READ_CONTACTS) && app.getStorageState() != STORAGE_STATE_PAYWALL) {
		    logDebug("sync mega contacts");
			MegaContactGetter getter = new MegaContactGetter(this);
			getter.getMegaContacts(megaApi, TimeUtils.WEEK);
		}

		Display display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = getResources().getDisplayMetrics().density;

	    if (dbH.getEphemeral() != null){
            Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
            intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
		}

	    if (dbH.getCredentials() == null){
	    	Intent newIntent = getIntent();

	    	if (newIntent != null){
		    	if (newIntent.getAction() != null){
		    		if (newIntent.getAction().equals(ACTION_EXPORT_MASTER_KEY) || newIntent.getAction().equals(ACTION_OPEN_MEGA_LINK) || newIntent.getAction().equals(ACTION_OPEN_MEGA_FOLDER_LINK)){
		    			openLink = true;
		    		}
		    		else if (newIntent.getAction().equals(ACTION_CANCEL_CAM_SYNC)){
                        stopRunningCameraUploadService(getApplicationContext());
		    			finish();
		    			return;
		    		}
		    	}
		    }

	    	if (!openLink){
//				megaApi.localLogout();
//				AccountController aC = new AccountController(this);
//				aC.logout(this, megaApi, megaChatApi, false);
				Intent intent = new Intent(this, LoginActivityLollipop.class);
				intent.putExtra(VISIBLE_FRAGMENT,  TOUR_FRAGMENT);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
					finish();
				}

		    }

	    	return;
	    }

	    prefs = dbH.getPreferences();
		if (prefs == null){
			firstTimeAfterInstallation = true;
			isList=true;

			if (savedInstanceState == null) {
				isSmallGridCameraUploads = false;
			}
		}
		else{
			if (prefs.getFirstTime() == null){
				firstTimeAfterInstallation = true;
			}else{
				firstTimeAfterInstallation = Boolean.parseBoolean(prefs.getFirstTime());
			}
			if (prefs.getPreferredViewList() == null){
				isList = true;
			}
			else{
				isList = Boolean.parseBoolean(prefs.getPreferredViewList());
			}

			if (savedInstanceState == null) {
				isSmallGridCameraUploads = dbH.isSmallGridCamera();
			}
		}
		logDebug("Preferred View List: " + isList);

		LiveEventBus.get(EVENT_LIST_GRID_CHANGE, Boolean.class).post(isList);

		LiveEventBus.get(EVENT_ORDER_CHANGE, Integer.class).post(sortOrderManagement.getOrderCloud());

		handler = new Handler();

		logDebug("Set view");
		setContentView(R.layout.activity_manager);

		observePsa();

		//Set toolbar
		abL = (AppBarLayout) findViewById(R.id.app_bar_layout);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		aB = getSupportActionBar();

		aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);

        fragmentLayout = (LinearLayout) findViewById(R.id.fragment_layout);

        bNV = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_view);
		bNV.setOnNavigationItemSelectedListener(this);
		bNV.enableAnimation(false);
		bNV.enableItemShiftingMode(false);
		bNV.enableShiftingMode(false);
		bNV.setTextVisibility(false);

		miniAudioPlayerController = new MiniAudioPlayerController(
				findViewById(R.id.mini_audio_player),
				() -> {
					// we need update fragmentLayout's layout params when player view is closed.
					if (bNV.getVisibility() == View.VISIBLE) {
						showBNVImmediate();
					}

					return Unit.INSTANCE;
				});

        //Set navigation view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
			@Override
			public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
				refreshDrawerInfo(false);
			}

			@Override
			public void onDrawerOpened(@NonNull View drawerView) {
				refreshDrawerInfo(storageState == MegaApiAndroid.STORAGE_STATE_UNKNOWN);
			}

			@Override
			public void onDrawerClosed(@NonNull View drawerView) {

			}

			@Override
			public void onDrawerStateChanged(int newState) {

			}

			/**
			 * Method to refresh the info displayed in the drawer menu.
			 *
			 * @param refreshStorageInfo Parameter to indicate if refresh the storage info.
			 */
			private void refreshDrawerInfo(boolean refreshStorageInfo) {
				if (!isOnline(managerActivity) || megaApi==null || megaApi.getRootNode()==null) {
					disableNavigationViewLayout();
				}
				else {
					resetNavigationViewLayout();
				}

				setContactStatus();

				if (!refreshStorageInfo) return;
                showAddPhoneNumberInMenu();
				refreshAccountInfo();
			}
		});
        nV = (NavigationView) findViewById(R.id.navigation_view);

		myAccountHeader = findViewById(R.id.navigation_drawer_account_section);
		myAccountHeader.setOnClickListener(this);
		contactStatus = (ImageView) findViewById(R.id.contact_state);
        myAccountSection = findViewById(R.id.my_account_section);
        myAccountSection.setOnClickListener(this);
        inboxSection = findViewById(R.id.inbox_section);
        inboxSection.setOnClickListener(this);
        contactsSection = findViewById(R.id.contacts_section);
        contactsSection.setOnClickListener(this);
		notificationsSection = findViewById(R.id.notifications_section);
		notificationsSection.setOnClickListener(this);
		notificationsSectionText = (TextView) findViewById(R.id.notification_section_text);
        contactsSectionText = (TextView) findViewById(R.id.contacts_section_text);
		findViewById(R.id.offline_section).setOnClickListener(this);
		transfersSection = findViewById(R.id.transfers_section);
		transfersSection.setOnClickListener(this);
		findViewById(R.id.rubbish_bin_section).setOnClickListener(this);
        settingsSection = findViewById(R.id.settings_section);
        settingsSection.setOnClickListener(this);
        upgradeAccount = (Button) findViewById(R.id.upgrade_navigation_view);
        upgradeAccount.setOnClickListener(this);

        navigationDrawerAddPhoneContainer = findViewById(R.id.navigation_drawer_add_phone_number_container);

        addPhoneNumberButton = findViewById(R.id.navigation_drawer_add_phone_number_button);
        addPhoneNumberButton.setOnClickListener(this);

        addPhoneNumberLabel = findViewById(R.id.navigation_drawer_add_phone_number_label);
        megaApi.getAccountAchievements(this);

		badgeDrawable = new BadgeDrawerArrowDrawable(managerActivity, R.color.red_600_red_300,
				R.color.white_dark_grey, R.color.white_dark_grey);

		BottomNavigationMenuView menuView = (BottomNavigationMenuView) bNV.getChildAt(0);
		// Navi button Chat
		BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(3);
		chatBadge = LayoutInflater.from(this).inflate(R.layout.bottom_chat_badge, menuView, false);
		itemView.addView(chatBadge);
		setChatBadge();

		callBadge = LayoutInflater.from(this).inflate(R.layout.bottom_call_badge, menuView, false);
		itemView.addView(callBadge);
		callBadge.setVisibility(View.GONE);
		setCallBadge();

		usedSpaceLayout = findViewById(R.id.nv_used_space_layout);

		//FAB buttonaB.
		fabButton = (FloatingActionButton) findViewById(R.id.floating_button);
		fabButton.setOnClickListener(new FabButtonListener(this));

		//PRO PANEL
		getProLayout=(LinearLayout) findViewById(R.id.get_pro_account);
		getProLayout.setBackgroundColor(Util.isDarkMode(this)
				? ColorUtils.getColorForElevation(this, 8f) : Color.WHITE);
		String getProTextString = getString(R.string.get_pro_account);
		try {
			getProTextString = getProTextString.replace("[A]", "\n");
		}
		catch(Exception e){
			logError("Formatted string: " + getProTextString, e);
		}

		getProText= (TextView) findViewById(R.id.get_pro_account_text);
		getProText.setText(getProTextString);
		rightUpgradeButton = (TextView) findViewById(R.id.btnRight_upgrade);
		leftCancelButton = (TextView) findViewById(R.id.btnLeft_cancel);

        nVDisplayName = findViewById(R.id.navigation_drawer_account_information_display_name);
        nVDisplayName.setMaxWidthEmojis(dp2px(MAX_WIDTH_BOTTOM_SHEET_DIALOG_PORT, outMetrics));

		nVEmail = (TextView) findViewById(R.id.navigation_drawer_account_information_email);
        nVPictureProfile = (RoundedImageView) findViewById(R.id.navigation_drawer_user_account_picture_profile);

		businessLabel = findViewById(R.id.business_label);
		businessLabel.setVisibility(View.GONE);

        fragmentContainer = findViewById(R.id.fragment_container);
        spaceTV = (TextView) findViewById(R.id.navigation_drawer_space);
        usedSpacePB = (ProgressBar) findViewById(R.id.manager_used_space_bar);
        //TABS section Contacts
		tabLayoutContacts =  findViewById(R.id.sliding_tabs_contacts);
		viewPagerContacts = findViewById(R.id.contact_tabs_pager);
		viewPagerContacts.setOffscreenPageLimit(3);

		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			tabLayoutContacts.setTabMode(TabLayout.MODE_FIXED);
		}
		else {
			tabLayoutContacts.setTabMode(TabLayout.MODE_SCROLLABLE);
		}

		cuViewTypes = findViewById(R.id.cu_view_type);
		cuYearsButton = findViewById(R.id.years_button);
		cuMonthsButton = findViewById(R.id.months_button);
		cuDaysButton = findViewById(R.id.days_button);
		cuAllButton = findViewById(R.id.all_button);
		cuLayout = findViewById(R.id.cu_layout);
		cuProgressBar = findViewById(R.id.cu_progress_bar);
		enableCUButton = findViewById(R.id.enable_cu_button);
		enableCUButton.setOnClickListener(v -> {
			if (getCameraUploadFragment() != null) {
				cuFragment.enableCUClick();
			}
		});

		//TABS section Shared Items
		tabLayoutShares =  findViewById(R.id.sliding_tabs_shares);
		viewPagerShares = findViewById(R.id.shares_tabs_pager);
		viewPagerShares.setOffscreenPageLimit(3);

		viewPagerShares.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				logDebug("selectDrawerItemSharedItems - TabId: " +  position);
				supportInvalidateOptionsMenu();
				checkScrollElevation();
				setSharesTabIcons(position);
				switch (position) {
					case INCOMING_TAB:
						if (isOutgoingAdded() && outSFLol.isMultipleSelect()) {
							outSFLol.getActionMode().finish();
						} else if (isLinksAdded() && lF.isMultipleSelect()) {
							lF.getActionMode().finish();
						}
						break;
					case OUTGOING_TAB:
						if (isIncomingAdded() && inSFLol.isMultipleSelect()) {
							inSFLol.getActionMode().finish();
						}  else if (isLinksAdded() && lF.isMultipleSelect()) {
							lF.getActionMode().finish();
						}
						break;
					case LINKS_TAB:
						if (isIncomingAdded() && inSFLol.isMultipleSelect()) {
							inSFLol.getActionMode().finish();
						} else if (isOutgoingAdded() && outSFLol.isMultipleSelect()) {
							outSFLol.getActionMode().finish();
						}
						break;
				}
				setToolbarTitle();
				showFabButton();
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		//Tab section MyAccount
		tabLayoutMyAccount =  (TabLayout) findViewById(R.id.sliding_tabs_my_account);
		viewPagerMyAccount = (ViewPager) findViewById(R.id.my_account_tabs_pager);

		//Tab section Transfers
		tabLayoutTransfers =  (TabLayout) findViewById(R.id.sliding_tabs_transfers);
		viewPagerTransfers = findViewById(R.id.transfers_tabs_pager);
		viewPagerTransfers.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				supportInvalidateOptionsMenu();
				checkScrollElevation();

				if (position == PENDING_TAB && isTransfersInProgressAdded()) {
					tFLol.setGetMoreQuotaViewVisibility();
				} else if (position == COMPLETED_TAB) {
					if (isTransfersCompletedAdded()) {
						completedTFLol.setGetMoreQuotaViewVisibility();
					}

					if (isTransfersInProgressAdded()) {
						tFLol.checkSelectModeAfterChangeTabOrDrawerItem();
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		callInProgressLayout = findViewById(R.id.call_in_progress_layout);
		callInProgressLayout.setOnClickListener(this);
		callInProgressChrono = findViewById(R.id.call_in_progress_chrono);
		callInProgressText = findViewById(R.id.call_in_progress_text);
		microOffLayout = findViewById(R.id.micro_off_layout);
		videoOnLayout = findViewById(R.id.video_on_layout);
		callInProgressLayout.setVisibility(View.GONE);

		if (mElevationCause > 0) {
			// A work around: mElevationCause will be changed unexpectedly shortly
			int elevationCause = mElevationCause;
			// Apply the previous Appbar elevation(e.g. before rotation) after all views have been created
			handler.postDelayed(()-> changeAppBarElevation(true, elevationCause), 100);
		}

		mNavHostView = findViewById(R.id.nav_host_fragment);
		setupNavDestListener();

		setTransfersWidgetLayout(findViewById(R.id.transfers_widget_layout), this);

		transferData = megaApi.getTransferData(this);
		if (transferData != null) {
			for (int i = 0; i < transferData.getNumDownloads(); i++) {
				int tag = transferData.getDownloadTag(i);
				transfersInProgress.add(tag);
				MegaApplication.getTransfersManagement().checkIfTransferIsPaused(tag);
			}

			for (int i = 0; i < transferData.getNumUploads(); i++) {
				int tag = transferData.getUploadTag(i);
				transfersInProgress.add(transferData.getUploadTag(i));
				MegaApplication.getTransfersManagement().checkIfTransferIsPaused(tag);
			}
		}

        if (!isOnline(this)){
			logDebug("No network -> SHOW OFFLINE MODE");

			if(drawerItem==null){
				drawerItem = DrawerItem.HOMEPAGE;
			}

			selectDrawerItemLollipop(drawerItem);
			showOfflineMode();

			UserCredentials credentials = dbH.getCredentials();
			if (credentials != null) {
				String gSession = credentials.getSession();
				int ret = megaChatApi.getInitState();
				logDebug("In Offline mode - Init chat is: " + ret);
				if (ret == 0 || ret == MegaChatApi.INIT_ERROR) {
					ret = megaChatApi.init(gSession);
					logDebug("After init: " + ret);
					if (ret == MegaChatApi.INIT_NO_CACHE) {
						logDebug("condition ret == MegaChatApi.INIT_NO_CACHE");
					} else if (ret == MegaChatApi.INIT_ERROR) {
						logWarning("condition ret == MegaChatApi.INIT_ERROR");
					} else {
						logDebug("Chat correctly initialized");
					}
				} else {
					logDebug("Offline mode: Do not init, chat already initialized");
				}
			}

			return;
        }

		///Check the MK or RK file
		logInfo("App version: " + getVersion());
		final File fMKOld = buildExternalStorageFile(OLD_MK_FILE);
		final File fRKOld = buildExternalStorageFile(OLD_RK_FILE);
		if (isFileAvailable(fMKOld)) {
			logDebug("Old MK file need to be renamed!");
			aC.renameRK(fMKOld);
		} else if (isFileAvailable(fRKOld)) {
			logDebug("Old RK file need to be renamed!");
			aC.renameRK(fRKOld);
		}

		rootNode = megaApi.getRootNode();
		if (rootNode == null || LoginActivityLollipop.isBackFromLoginPage){
			 if (getIntent() != null){
				 logDebug("Action: " + getIntent().getAction());
				if (getIntent().getAction() != null){
					if (getIntent().getAction().equals(ACTION_IMPORT_LINK_FETCH_NODES)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_MEGA_LINK)){
						Intent intent = new Intent(managerActivity, FileLinkActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_MEGA_FOLDER_LINK)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_OPEN_MEGA_FOLDER_LINK);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if(getIntent().getAction().equals(ACTION_OPEN_CHAT_LINK)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_OPEN_CHAT_LINK);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_CANCEL_CAM_SYNC)){
                        stopRunningCameraUploadService(getApplicationContext());
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_EXPORT_MASTER_KEY)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(getIntent().getAction());
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_SHOW_TRANSFERS)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_SHOW_TRANSFERS);
						intent.putExtra(TRANSFERS_TAB, getIntent().getIntExtra(TRANSFERS_TAB, ERROR_TAB));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_IPC)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_IPC);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_CHAT_NOTIFICATION_MESSAGE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_CHAT_NOTIFICATION_MESSAGE);
						startActivity(intent);
						finish();
						return;
					}
                    else if(getIntent().getAction().equals(ACTION_CHAT_SUMMARY)) {
                        Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
                        intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setAction(ACTION_CHAT_SUMMARY);
                        startActivity(intent);
                        finish();
                        return;
                    }
					else if (getIntent().getAction().equals(ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_HANDLE_NODE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT, LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_OPEN_HANDLE_NODE);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_OVERQUOTA_TRANSFER)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT, LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_OVERQUOTA_TRANSFER);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_OVERQUOTA_STORAGE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT, LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_OVERQUOTA_STORAGE);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_CONTACTS_SECTION)){
						logDebug("Login");
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(CONTACT_HANDLE, getIntent().getLongExtra(CONTACT_HANDLE, -1));
						intent.putExtra(VISIBLE_FRAGMENT, LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_OPEN_CONTACTS_SECTION);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(ACTION_SHOW_SNACKBAR_SENT_AS_MESSAGE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_SHOW_SNACKBAR_SENT_AS_MESSAGE);
						startActivity(intent);
						finish();
						return;
					} else if (getIntent().getAction().equals(ACTION_SHOW_UPGRADE_ACCOUNT)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ACTION_SHOW_UPGRADE_ACCOUNT);
						startActivity(intent);
						finish();
						return;
					}
				}
			}
			Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
			intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}
		else{
			inboxNode = megaApi.getInboxNode();
			attr = dbH.getAttributes();
			if (attr != null){
				if (attr.getInvalidateSdkCache() != null){
					if (attr.getInvalidateSdkCache().compareTo("") != 0) {
						try {
							if (Boolean.parseBoolean(attr.getInvalidateSdkCache())){
								logDebug("megaApi.invalidateCache();");
								megaApi.invalidateCache();
							}
						}
						catch(Exception e){}
					}
				}
			}

			dbH.setInvalidateSdkCache(false);
            MegaMessageService.getToken(this);
			nVEmail.setVisibility(View.VISIBLE);
			nVEmail.setText(megaApi.getMyEmail());
//				megaApi.getUserData(this);
			megaApi.getUserAttribute(MegaApiJava.USER_ATTR_FIRSTNAME, this);
			megaApi.getUserAttribute(MegaApiJava.USER_ATTR_LASTNAME, this);

			this.setDefaultAvatar();

			this.setProfileAvatar();

			initGooglePlayPayments();

			megaApi.addGlobalListener(this);
			megaApi.isGeolocationEnabled(this);

			if(savedInstanceState==null) {
				logDebug("Run async task to check offline files");
				//Check the consistency of the offline nodes in the DB
				CheckOfflineNodesTask checkOfflineNodesTask = new CheckOfflineNodesTask(this);
				checkOfflineNodesTask.execute();
			}

	        if (getIntent() != null){
				if (getIntent().getAction() != null){
			        if (getIntent().getAction().equals(ACTION_EXPORT_MASTER_KEY)){
						logDebug("Intent to export Master Key - im logged in!");
						drawerItem=DrawerItem.ACCOUNT;
						showMKLayout();
						selectDrawerItemLollipop(drawerItem);
						return;
					}
					else if(getIntent().getAction().equals(ACTION_CANCEL_ACCOUNT)){
						String link = getIntent().getDataString();
						if(link!=null){
							logDebug("Link to cancel: " + link);
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							megaApi.queryCancelLink(link, this);
						}
					}
					else if(getIntent().getAction().equals(ACTION_CHANGE_MAIL)){
						String link = getIntent().getDataString();
						if(link!=null){
							logDebug("Link to change mail: " + link);
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							showDialogInsertPassword(link, false);
						}
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_FOLDER)) {
						logDebug("Open after LauncherFileExplorerActivityLollipop ");
						boolean locationFileInfo = getIntent().getBooleanExtra("locationFileInfo", false);
						long handleIntent = getIntent().getLongExtra("PARENT_HANDLE", -1);

						if (getIntent().getBooleanExtra(SHOW_MESSAGE_UPLOAD_STARTED, false)) {
							int numberUploads = getIntent().getIntExtra(NUMBER_UPLOADS, 1);
							showSnackbar(SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.upload_began, numberUploads, numberUploads), -1);
						}

						if (locationFileInfo){
							boolean offlineAdapter = getIntent().getBooleanExtra("offline_adapter", false);
							if (offlineAdapter){
								drawerItem = DrawerItem.HOMEPAGE;
								selectDrawerItemLollipop(drawerItem);
								selectDrawerItemPending=false;
								openFullscreenOfflineFragment(
										getIntent().getStringExtra(INTENT_EXTRA_KEY_PATH_NAVIGATION));
							}
							else {
								long fragmentHandle = getIntent().getLongExtra("fragmentHandle", -1);

								if (fragmentHandle == megaApi.getRootNode().getHandle()){
									drawerItem = DrawerItem.CLOUD_DRIVE;
									setParentHandleBrowser(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
								else if (fragmentHandle == megaApi.getRubbishNode().getHandle()){
									drawerItem = DrawerItem.RUBBISH_BIN;
									setParentHandleRubbish(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
								else if (fragmentHandle == megaApi.getInboxNode().getHandle()){
									drawerItem = DrawerItem.INBOX;
									setParentHandleInbox(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
								else {
									//Incoming
									drawerItem = DrawerItem.SHARED_ITEMS;
									indexShares = 0;
									MegaNode parentIntentN = megaApi.getNodeByHandle(handleIntent);
									if (parentIntentN != null){
										deepBrowserTreeIncoming = calculateDeepBrowserTreeIncoming(parentIntentN, this);
									}
									setParentHandleIncoming(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
							}
						}
						else {
							actionOpenFolder(handleIntent);
						}

						setIntent(null);
					}
					else if(getIntent().getAction().equals(ACTION_PASS_CHANGED)){
						int result = getIntent().getIntExtra(RESULT, MegaError.API_OK);
						if (result == MegaError.API_OK) {
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							logDebug("Show success mesage");
							showAlert(this, getString(R.string.pass_changed_alert), null);
						}
						else if(result==MegaError.API_EARGS){
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							logWarning("Error when changing pass - the current password is not correct");
							showAlert(this,getString(R.string.old_password_provided_incorrect), getString(R.string.general_error_word));
						}
						else{
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							logError("Error when changing pass - show error message");
							showAlert(this,getString(R.string.general_text_error), getString(R.string.general_error_word));
						}
					}
					else if(getIntent().getAction().equals(ACTION_RESET_PASS)){
						String link = getIntent().getDataString();
						if(link!=null){
							logDebug("Link to resetPass: " + link);
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							showConfirmationResetPassword(link);
						}
					}
					else if(getIntent().getAction().equals(ACTION_IPC)){
						logDebug("IPC link - go to received request in Contacts");
						markNotificationsSeen(true);
						drawerItem=DrawerItem.CONTACTS;
						indexContacts=2;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(ACTION_CHAT_NOTIFICATION_MESSAGE)){
						logDebug("Chat notitificacion received");
						drawerItem=DrawerItem.CHAT;
						selectDrawerItemLollipop(drawerItem);
						long chatId = getIntent().getLongExtra(CHAT_ID, MEGACHAT_INVALID_HANDLE);
						if (getIntent().getBooleanExtra(EXTRA_MOVE_TO_CHAT_SECTION, false)){
							moveToChatSection(chatId);
						}
						else {
							String text = getIntent().getStringExtra(SHOW_SNACKBAR);
							if (chatId != -1) {
								openChat(chatId, text);
							}
						}
						selectDrawerItemPending=false;
						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(ACTION_CHAT_SUMMARY)) {
						logDebug("Chat notification: ACTION_CHAT_SUMMARY");
						drawerItem=DrawerItem.CHAT;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(ACTION_OPEN_CHAT_LINK)){
						logDebug("ACTION_OPEN_CHAT_LINK: " + getIntent().getDataString());
						drawerItem=DrawerItem.CHAT;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
						megaChatApi.checkChatLink(getIntent().getDataString(), this);
						getIntent().setAction(null);
						setIntent(null);
					}
					else if (getIntent().getAction().equals(ACTION_JOIN_OPEN_CHAT_LINK)) {
						linkJoinToChatLink = getIntent().getDataString();
						joiningToChatLink = true;

						if (connected) {
							megaChatApi.checkChatLink(linkJoinToChatLink, this);
						}

						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(ACTION_SHOW_SETTINGS)) {
						logDebug("Chat notification: SHOW_SETTINGS");
						selectDrawerItemPending=false;
						moveToSettingsSection();
						getIntent().setAction(null);
						setIntent(null);
					}
					else if (getIntent().getAction().equals(ACTION_SHOW_SETTINGS_STORAGE)) {
						logDebug("ACTION_SHOW_SETTINGS_STORAGE");
						selectDrawerItemPending=false;
						moveToSettingsSectionStorage();
						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION)){
						logDebug("ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION");
						markNotificationsSeen(true);

						drawerItem=DrawerItem.SHARED_ITEMS;
						indexShares=0;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(ACTION_SHOW_MY_ACCOUNT)){
						logDebug("Intent from chat - show my account");

						drawerItem=DrawerItem.ACCOUNT;
						accountFragment=MY_ACCOUNT_FRAGMENT;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(ACTION_SHOW_UPGRADE_ACCOUNT)){
						navigateToUpgradeAccount();
						selectDrawerItemPending=false;
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_HANDLE_NODE)){
						String link = getIntent().getDataString();
						String [] s = link.split("#");
						if (s.length > 1){
							String nodeHandleLink = s[1];
							String [] sSlash = s[1].split("/");
							if (sSlash.length > 0){
								nodeHandleLink = sSlash[0];
							}
							long nodeHandleLinkLong = MegaApiAndroid.base64ToHandle(nodeHandleLink);
							MegaNode nodeLink = megaApi.getNodeByHandle(nodeHandleLinkLong);
							if (nodeLink == null){
								showSnackbar(SNACKBAR_TYPE, getString(R.string.general_error_file_not_found), -1);
							}
							else{
								MegaNode pN = megaApi.getParentNode(nodeLink);
								if (pN == null){
									pN = megaApi.getRootNode();
								}
								parentHandleBrowser = pN.getHandle();
								drawerItem = DrawerItem.CLOUD_DRIVE;
								selectDrawerItemLollipop(drawerItem);
								selectDrawerItemPending = false;

								Intent i = new Intent(this, FileInfoActivityLollipop.class);
								i.putExtra("handle", nodeLink.getHandle());
								i.putExtra(NAME, nodeLink.getName());
								startActivity(i);
							}
						}
						else{
							drawerItem = DrawerItem.CLOUD_DRIVE;
							selectDrawerItemLollipop(drawerItem);
						}
					}
					else if (getIntent().getAction().equals(ACTION_IMPORT_LINK_FETCH_NODES)){
						getIntent().setAction(null);
						setIntent(null);
					}
					else if (getIntent().getAction().equals(ACTION_OPEN_CONTACTS_SECTION)){
						markNotificationsSeen(true);
						openContactLink(getIntent().getLongExtra(CONTACT_HANDLE, -1));
					}
					else if (getIntent().getAction().equals(ACTION_REFRESH_STAGING)){
						update2FASetting();
					}
					else if(getIntent().getAction().equals(ACTION_SHOW_SNACKBAR_SENT_AS_MESSAGE)){
						long chatId = getIntent().getLongExtra(CHAT_ID, MEGACHAT_INVALID_HANDLE);
						showSnackbar(MESSAGE_SNACKBAR_TYPE, null, chatId);
						getIntent().setAction(null);
						setIntent(null);
					}
				}
	        }

			logDebug("Check if there any unread chat");
			if (megaChatApi != null) {
				logDebug("Connect to chat!: " + megaChatApi.getInitState());
				if ((megaChatApi.getInitState() != MegaChatApi.INIT_ERROR)) {
					logDebug("Connection goes!!!");
					megaChatApi.connect(this);
				} else {
					logWarning("Not launch connect: " + megaChatApi.getInitState());
				}
			} else {
				logError("megaChatApi is NULL");
			}
			setChatBadge();

			logDebug("Check if there any INCOMING pendingRequest contacts");
			setContactTitleSection();

			setNotificationsTitleSection();

			if (drawerItem == null) {
	        	drawerItem = DrawerItem.HOMEPAGE;
	        	Intent intent = getIntent();
	        	if (intent != null){
	        		boolean upgradeAccount = getIntent().getBooleanExtra(EXTRA_UPGRADE_ACCOUNT, false);
					newAccount = getIntent().getBooleanExtra(EXTRA_NEW_ACCOUNT, false);
					newCreationAccount = getIntent().getBooleanExtra(NEW_CREATION_ACCOUNT, false);
					firstLogin = getIntent().getBooleanExtra(EXTRA_FIRST_LOGIN, firstLogin);
					askPermissions = getIntent().getBooleanExtra(EXTRA_ASK_PERMISSIONS, askPermissions);

                    //reset flag to fix incorrect view loaded when orientation changes
                    getIntent().removeExtra(EXTRA_NEW_ACCOUNT);
                    getIntent().removeExtra(EXTRA_UPGRADE_ACCOUNT);
					getIntent().removeExtra(EXTRA_FIRST_LOGIN);
					getIntent().removeExtra(EXTRA_ASK_PERMISSIONS);
	        		if(upgradeAccount){
	        			drawerLayout.closeDrawer(Gravity.LEFT);
						int accountType = getIntent().getIntExtra(EXTRA_ACCOUNT_TYPE, 0);

						switch (accountType){
							case FREE:{
								if (firstLogin && app.getStorageState() != STORAGE_STATE_PAYWALL) {
									logDebug("First login. Go to Camera Uploads configuration.");
									drawerItem = DrawerItem.CAMERA_UPLOADS;
								} else {
									drawerItem = DrawerItem.ACCOUNT;
									accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
									displayedAccountType = -1;
								}
								setIntent(null);
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_I:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								displayedAccountType = PRO_I;
								selectDrawerItemLollipop(drawerItem);
								selectDrawerItemPending=false;
								return;
							}
							case PRO_II:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_II;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_III:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_III;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_LITE:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_LITE;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
						}
	        		}
	        		else{
						if (firstLogin && app.getStorageState() != STORAGE_STATE_PAYWALL) {
							logDebug("First login. Go to Camera Uploads configuration.");
							drawerItem = DrawerItem.CAMERA_UPLOADS;
							setIntent(null);
						}
					}
	        	}
	        }
	        else{
				logDebug("DRAWERITEM NOT NULL: " + drawerItem);
				Intent intentRec = getIntent();
	        	if (intentRec != null){
					boolean upgradeAccount = getIntent().getBooleanExtra(EXTRA_UPGRADE_ACCOUNT, false);
					newAccount = getIntent().getBooleanExtra(EXTRA_NEW_ACCOUNT, false);
                    newCreationAccount = getIntent().getBooleanExtra(NEW_CREATION_ACCOUNT, false);
					//reset flag to fix incorrect view loaded when orientation changes
                    getIntent().removeExtra(EXTRA_NEW_ACCOUNT);
                    getIntent().removeExtra(EXTRA_UPGRADE_ACCOUNT);
					firstLogin = intentRec.getBooleanExtra(EXTRA_FIRST_LOGIN, firstLogin);
					askPermissions = intentRec.getBooleanExtra(EXTRA_ASK_PERMISSIONS, askPermissions);
                    if(upgradeAccount){
						drawerLayout.closeDrawer(Gravity.LEFT);
						int accountType = getIntent().getIntExtra(EXTRA_ACCOUNT_TYPE, 0);

						switch (accountType){
							case FREE:{
								if (firstLogin && app.getStorageState() != STORAGE_STATE_PAYWALL) {
									logDebug("First login. Go to Camera Uploads configuration.");
									drawerItem = DrawerItem.CAMERA_UPLOADS;
								} else {
									drawerItem = DrawerItem.ACCOUNT;
									accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
									displayedAccountType = -1;
								}
								setIntent(null);
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_I:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_I;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_II:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_II;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_III:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_III;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case PRO_LITE:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = PRO_LITE;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case BUSINESS:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = BUSINESS;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
						}
					}
					else{
						if (firstLogin && !joiningToChatLink) {
							logDebug("Intent firstTimeCam==true");
							if (prefs != null && prefs.getCamSyncEnabled() != null) {
								firstLogin = false;
							} else {
								firstLogin = true;
								if (app.getStorageState() != STORAGE_STATE_PAYWALL) {
									drawerItem = DrawerItem.CAMERA_UPLOADS;
								}
							}
							setIntent(null);
						}
					}

	        		if (intentRec.getAction() != null){
	        			if (intentRec.getAction().equals(ACTION_SHOW_TRANSFERS)){
							if (intentRec.getBooleanExtra(OPENED_FROM_CHAT, false)) {
								sendBroadcast(new Intent(ACTION_CLOSE_CHAT_AFTER_OPEN_TRANSFERS));
							}

	        				drawerItem = DrawerItem.TRANSFERS;
	        				indexTransfers = intentRec.getIntExtra(TRANSFERS_TAB, ERROR_TAB);
							setIntent(null);
	        			} else if (intentRec.getAction().equals(ACTION_REFRESH_AFTER_BLOCKED)) {
							drawerItem = DrawerItem.CLOUD_DRIVE;
	        				setIntent(null);
						}
	        		}
	        	}
				drawerLayout.closeDrawer(Gravity.LEFT);
			}

			checkCurrentStorageStatus(true);

	        //INITIAL FRAGMENT
			if(selectDrawerItemPending){
				selectDrawerItemLollipop(drawerItem);
			}
		}

		megaApi.shouldShowPasswordReminderDialog(false, this);

		updateAccountDetailsVisibleInfo();

		setContactStatus();

		checkInitialScreens();

		if (openLinkDialogIsShown) {
			showOpenLinkDialog();
			String text = savedInstanceState.getString("openLinkText", "");
			openLinkText.setText(text);
			openLinkText.setSelection(text.length());
			boolean openLinkDialogIsErrorShown = savedInstanceState.getBoolean("openLinkDialogIsErrorShown", false);
			if (openLinkDialogIsErrorShown) {
				openLink(text);
			}
		}

		if (mkLayoutVisible) {
			showMKLayout();
		}

		if (drawerItem == DrawerItem.TRANSFERS && isTransferOverQuotaWarningShown) {
            showTransfersTransferOverQuotaWarning();
        }

		PsaManager.INSTANCE.startChecking();

		if (savedInstanceState != null && savedInstanceState.getBoolean(IS_NEW_TEXT_FILE_SHOWN, false)) {
			showNewTextFileDialog(savedInstanceState.getString(NEW_TEXT_FILE_TEXT));
		}

		logDebug("END onCreate");
	}

	/**
	 * Checks which screen should be shown when an user is logins.
	 * There are three different screens or warnings:
	 * - Business warning: it takes priority over the other two
	 * - SMS verification screen: it takes priority over the other one
	 * - Onboarding permissions screens: it has to be only shown when account is logged in after the installation,
	 * 		some of the permissions required have not been granted
	 * 		and the business warnings and SMS verification have not to be shown.
	 */
	private void checkInitialScreens() {
		if (checkBusinessStatus()) {
			setBusinessAlertShown(true);
			return;
		}

		if (firstTimeAfterInstallation || askPermissions) {
			//haven't verified phone number
			if (canVoluntaryVerifyPhoneNumber() && !onAskingPermissionsFragment && !newCreationAccount) {
				askForSMSVerification();
			} else {
				drawerItem = DrawerItem.ASK_PERMISSIONS;
				askForAccess();
			}
		} else if (firstLogin && !newCreationAccount && canVoluntaryVerifyPhoneNumber() && !onAskingPermissionsFragment) {
			askForSMSVerification();
		}
	}

	/**
	 * Updates the state of the flag indicating if there is a business alert shown.
	 *
	 * @param shown	true if there is any business alert shown, false otherwise.
	 */
	private void setBusinessAlertShown(boolean shown) {
		MyAccountInfo myAccountInfo = MegaApplication.getInstance().getMyAccountInfo();
		if (myAccountInfo != null) {
			myAccountInfo.setBusinessAlertShown(shown);
		}
	}

	/**
	 * Checks if some business warning has to be shown due to the status of the account.
	 *
	 * @return True if some warning has been shown, false otherwise.
	 */
	private boolean checkBusinessStatus() {
		if (!megaApi.isBusinessAccount()) {
			return false;
		}

		if (isBusinessGraceAlertShown) {
			showBusinessGraceAlert();
			return true;
		}

		if (isBusinessCUAlertShown) {
			showBusinessCUAlert();
			return true;
		}

		MyAccountInfo myAccountInfo = MegaApplication.getInstance().getMyAccountInfo();
		if (myAccountInfo == null || myAccountInfo.isBusinessAlertShown()) {
			return false;
		}

		if (firstLogin && myAccountInfo.wasNotBusinessAlertShownYet()) {
			int status = megaApi.getBusinessStatus();

			if (status == BUSINESS_STATUS_EXPIRED) {
				myAccountInfo.setBusinessAlertAlreadyShown();
				startActivity(new Intent(this, BusinessExpiredAlertActivity.class));
				return true;
			} else if (megaApi.isMasterBusinessAccount() && status == BUSINESS_STATUS_GRACE_PERIOD) {
				myAccountInfo.setBusinessAlertAlreadyShown();
				showBusinessGraceAlert();
				return true;
			}
		}

		return false;
	}

    private void showBusinessGraceAlert() {
    	logDebug("showBusinessGraceAlert");
    	if (businessGraceAlert != null && businessGraceAlert.isShowing()) {
    		return;
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_business_grace_alert, null);

		businessGraceAlert = builder.setView(v)
				.setPositiveButton(R.string.general_dismiss, (dialog, which) -> {
					setBusinessAlertShown(isBusinessGraceAlertShown = false);
					try {
						businessGraceAlert.dismiss();
					} catch (Exception e) {
						logWarning("Exception dismissing businessGraceAlert", e);
					}
				})
				.create();

        businessGraceAlert.setCanceledOnTouchOutside(false);
        try {
            businessGraceAlert.show();
        }catch (Exception e){
            logWarning("Exception showing businessGraceAlert", e);
        }
        isBusinessGraceAlertShown = true;
    }

	public void checkIfShouldShowBusinessCUAlert() {
		if (megaApi.isBusinessAccount() && !megaApi.isMasterBusinessAccount()) {
			showBusinessCUAlert();
		} else if (getCameraUploadFragment() != null){
			if (cuFragment.isEnableCUFragmentShown()) {
				cuFragment.enableCu();
			} else {
				cuFragment.enableCUClick();
			}
		}
	}

    private void showBusinessCUAlert() {
        if (businessCUAlert != null && businessCUAlert.isShowing()) {
            return;
        }

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(R.string.section_photo_sync)
				.setMessage(R.string.camera_uploads_business_alert)
				.setNegativeButton(R.string.general_cancel, (dialog, which) -> { })
				.setPositiveButton(R.string.general_enable, (dialog, which) -> {
					if (getCameraUploadFragment() != null) {
						cuFragment.enableCUClick();
					}
				})
				.setCancelable(false)
				.setOnDismissListener(dialog -> setBusinessAlertShown(isBusinessCUAlertShown = false));
		businessCUAlert = builder.create();
		businessCUAlert.show();
		isBusinessCUAlertShown = true;
    }

	private void openContactLink (long handle) {
    	if (handle == -1) {
			logWarning("Not valid contact handle");
    		return;
		}

		handleInviteContact = handle;
    	dismissOpenLinkDialog();
		logDebug("Handle to invite a contact: " + handle);
		drawerItem = DrawerItem.CONTACTS;
		indexContacts = 0;
		selectDrawerItemLollipop(drawerItem);
	}

	private void askForSMSVerification() {
        if(!smsDialogTimeChecker.shouldShow()) return;
        showStorageAlertWithDelay = true;
        //If mobile device, only portrait mode is allowed
        if (!isTablet(this)) {
            logDebug("mobile only portrait mode");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        smsDialogTimeChecker.update();
        onAskingSMSVerificationFragment = true;
        if (svF == null) {
            svF = new SMSVerificationFragment();
        }
        replaceFragment(svF, FragmentTag.SMS_VERIFICATION.getTag());
        tabLayoutContacts.setVisibility(View.GONE);
        viewPagerContacts.setVisibility(View.GONE);
        tabLayoutShares.setVisibility(View.GONE);
        viewPagerShares.setVisibility(View.GONE);
        tabLayoutMyAccount.setVisibility(View.GONE);
        viewPagerMyAccount.setVisibility(View.GONE);
        tabLayoutTransfers.setVisibility(View.GONE);
        viewPagerTransfers.setVisibility(View.GONE);
        abL.setVisibility(View.GONE);

        fragmentContainer.setVisibility(View.VISIBLE);
        drawerLayout.closeDrawer(Gravity.LEFT);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        supportInvalidateOptionsMenu();
        hideFabButton();
        showHideBottomNavigationView(true);
    }

	public void askForAccess () {
        askPermissions = false;
    	showStorageAlertWithDelay = true;
    	//If mobile device, only portrait mode is allowed
		if (!isTablet(this)) {
			logDebug("Mobile only portrait mode");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    	boolean writeStorageGranted = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		boolean readStorageGranted = checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    	boolean cameraGranted = checkPermission(Manifest.permission.CAMERA);
		boolean microphoneGranted = checkPermission(Manifest.permission.RECORD_AUDIO);
//		boolean writeCallsGranted = checkPermission(Manifest.permission.WRITE_CALL_LOG);

		if (!writeStorageGranted || !readStorageGranted || !cameraGranted || !microphoneGranted/* || !writeCallsGranted*/) {
			deleteCurrentFragment();

			if (pF == null) {
				pF = new PermissionsFragment();
			}

			replaceFragment(pF, FragmentTag.PERMISSIONS.getTag());

			onAskingPermissionsFragment = true;

			abL.setVisibility(View.GONE);
			setTabsVisibility();
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			supportInvalidateOptionsMenu();
			hideFabButton();
			showHideBottomNavigationView(true);
		}
	}

	public void destroySMSVerificationFragment() {
        if (!isTablet(this)) {
            logDebug("mobile, all orientation");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        onAskingSMSVerificationFragment = false;
        svF = null;
        // For Android devices which have Android below 6, no need to go to request permission fragment.
        if(!firstTimeAfterInstallation || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            abL.setVisibility(View.VISIBLE);

            deleteCurrentFragment();

            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            supportInvalidateOptionsMenu();
            selectDrawerItemLollipop(drawerItem);
        }
    }

	public void destroyPermissionsFragment () {
		//In mobile, allow all orientation after permission screen
		if (!isTablet(this)) {
			logDebug("Mobile, all orientation");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}

		turnOnNotifications = false;

		abL.setVisibility(View.VISIBLE);

		deleteCurrentFragment();

		onAskingPermissionsFragment = false;

		pF = null;

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		supportInvalidateOptionsMenu();

		if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
			drawerItem = DrawerItem.CLOUD_DRIVE;
		} else {
			firstLogin = true;
			drawerItem = DrawerItem.CAMERA_UPLOADS;
		}

		selectDrawerItemLollipop(drawerItem);
	}

	void setContactStatus() {
		if (megaChatApi == null) {
			megaChatApi = app.getMegaChatApi();
			megaChatApi.addChatListener(this);
		}

		int chatStatus = megaChatApi.getOnlineStatus();
		if (contactStatus != null) {
			ChatUtil.setContactStatus(chatStatus, contactStatus, StatusIconLocation.DRAWER);
		}
	}

	void passwordReminderDialogBlocked(){
		megaApi.passwordReminderDialogBlocked(this);
	}

	void passwordReminderDialogSkiped(){
		megaApi.passwordReminderDialogSkipped(this);
	}

	@Override
	protected void onResume(){
		if (drawerItem == DrawerItem.SEARCH && getSearchFragment() != null) {
			sFLol.setWaitingForSearchedNodes(true);
		}

		super.onResume();

//		dbH.setShowNotifOff(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			queryIfNotificationsAreOn();
		}

		if (getResources().getConfiguration().orientation != orientationSaved) {
			orientationSaved = getResources().getConfiguration().orientation;
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}

        checkScrollElevation();

		if (drawerItem == DrawerItem.ACCOUNT ) {
			app.refreshAccountInfo();
		}

		checkTransferOverQuotaOnResume();

		if (miniAudioPlayerController != null) {
			miniAudioPlayerController.onResume();
		}
	}

	void queryIfNotificationsAreOn(){
		logDebug("queryIfNotificationsAreOn");

		if (dbH == null){
			dbH = DatabaseHandler.getDbHandler(getApplicationContext());
		}

		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if (turnOnNotifications){
			setTurnOnNotificationsFragment();
		}
		else {
			NotificationManagerCompat nf = NotificationManagerCompat.from(this);
			logDebug ("Notifications Enabled: " + nf.areNotificationsEnabled());
			if (!nf.areNotificationsEnabled()){
				logDebug("OFF");
				if (dbH.getShowNotifOff() == null || dbH.getShowNotifOff().equals("true")) {
					if (megaChatApi == null) {
						megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
					}
					if ((megaApi.getContacts().size() >= 1) || (megaChatApi.getChatListItems().size() >= 1)) {
						setTurnOnNotificationsFragment();
					}
				}
			}
		}
	}

	public void deleteTurnOnNotificationsFragment(){
		logDebug("deleteTurnOnNotificationsFragment");
		turnOnNotifications = false;

		abL.setVisibility(View.VISIBLE);

		tonF = null;

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		supportInvalidateOptionsMenu();
		selectDrawerItemLollipop(drawerItem);

		setStatusBarColor(this, android.R.color.transparent);
	}

	void deleteCurrentFragment () {
		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment != null){
			getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNowAllowingStateLoss();
		}
	}

	void setTurnOnNotificationsFragment(){
		logDebug("setTurnOnNotificationsFragment");
		aB.setSubtitle(null);
		abL.setVisibility(View.GONE);

		deleteCurrentFragment();

		if (tonF == null){
			tonF = new TurnOnNotificationsFragment();
		}
		replaceFragment(tonF, FragmentTag.TURN_ON_NOTIFICATIONS.getTag());

		setTabsVisibility();
		abL.setVisibility(View.GONE);

		drawerLayout.closeDrawer(Gravity.LEFT);
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		supportInvalidateOptionsMenu();
		hideFabButton();
		showHideBottomNavigationView(true);

		setStatusBarColor(this, R.color.teal_500_teal_400);
	}

	void actionOpenFolder(long handleIntent) {
		if (handleIntent == INVALID_HANDLE) {
			logWarning("handleIntent is not valid");
			return;
		}

		MegaNode parentIntentN = megaApi.getNodeByHandle(handleIntent);
		if (parentIntentN == null) {
			logWarning("parentIntentN is null");
			return;
		}

		switch (megaApi.getAccess(parentIntentN)) {
			case MegaShare.ACCESS_READ:
			case MegaShare.ACCESS_READWRITE:
			case MegaShare.ACCESS_FULL:
				parentHandleIncoming = handleIntent;
				deepBrowserTreeIncoming = calculateDeepBrowserTreeIncoming(parentIntentN, this);
				drawerItem = DrawerItem.SHARED_ITEMS;
				break;

			default:
				if (megaApi.isInRubbish(parentIntentN)) {
					parentHandleRubbish = handleIntent;
					drawerItem = DrawerItem.RUBBISH_BIN;
				} else if (megaApi.isInInbox(parentIntentN)) {
					parentHandleInbox = handleIntent;
					drawerItem = DrawerItem.INBOX;
				} else {
					parentHandleBrowser = handleIntent;
					drawerItem = DrawerItem.CLOUD_DRIVE;
				}
				break;
		}
	}

	@Override
	protected void onPostResume() {
		logDebug("onPostResume");
    	super.onPostResume();

		if (isSearching){
			selectDrawerItemLollipop(DrawerItem.SEARCH);
			isSearching = false;
			return;
		}

    	managerActivity = this;

    	Intent intent = getIntent();

//    	dbH = new DatabaseHandler(getApplicationContext());
    	dbH = DatabaseHandler.getDbHandler(getApplicationContext());
    	if(dbH.getCredentials() == null){
    		if (!openLink){
//				megaApi.localLogout();
//				AccountController aC = new AccountController(this);
//				aC.logout(this, megaApi, megaChatApi, false);
    			return;
    		}
    		else{
				logDebug("Not credentials");
    			if (intent != null) {
					logDebug("Not credentials -> INTENT");
    				if (intent.getAction() != null){
						logDebug("Intent with ACTION: " + intent.getAction());

    					if (getIntent().getAction().equals(ACTION_EXPORT_MASTER_KEY)){
    						Intent exportIntent = new Intent(managerActivity, LoginActivityLollipop.class);
							intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
							exportIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    						exportIntent.setAction(getIntent().getAction());
    						startActivity(exportIntent);
    						finish();
    						return;
    					}
    				}
    			}
    		}
		}

    	if (intent != null) {
			logDebug("Intent not null! " + intent.getAction());
    		// Open folder from the intent
			if (intent.hasExtra(EXTRA_OPEN_FOLDER)) {
				logDebug("INTENT: EXTRA_OPEN_FOLDER");

				parentHandleBrowser = intent.getLongExtra(EXTRA_OPEN_FOLDER, -1);
				intent.removeExtra(EXTRA_OPEN_FOLDER);
				setIntent(null);
			}

    		if (intent.getAction() != null){
				logDebug("Intent action");

    			if(getIntent().getAction().equals(ACTION_EXPLORE_ZIP)){
					logDebug("Open zip browser");

    				String pathZip=intent.getExtras().getString(EXTRA_PATH_ZIP);

    				Intent intentZip = new Intent(managerActivity, ZipBrowserActivityLollipop.class);
    				intentZip.putExtra(ZipBrowserActivityLollipop.EXTRA_PATH_ZIP, pathZip);
    			    startActivity(intentZip);
    			}
//    			else if(getIntent().getAction().equals(ManagerActivityLollipop.ACTION_OPEN_PDF)){
//
//    				String pathPdf=intent.getExtras().getString(EXTRA_PATH_PDF);
//
//    			    File pdfFile = new File(pathPdf);
//
//    			    Intent intentPdf = new Intent();
//    			    intentPdf.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
//    			    intentPdf.setClass(this, OpenPDFActivity.class);
//    			    intentPdf.setAction("android.intent.action.VIEW");
//    				this.startActivity(intentPdf);
//
//    			}
    			if (getIntent().getAction().equals(ACTION_IMPORT_LINK_FETCH_NODES)){
					logDebug("ACTION_IMPORT_LINK_FETCH_NODES");

					Intent loginIntent = new Intent(managerActivity, LoginActivityLollipop.class);
					intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					loginIntent.setAction(ACTION_IMPORT_LINK_FETCH_NODES);
					loginIntent.setData(Uri.parse(getIntent().getDataString()));
					startActivity(loginIntent);
					finish();
					return;
				}
				else if (getIntent().getAction().equals(ACTION_OPEN_MEGA_LINK)){
					logDebug("ACTION_OPEN_MEGA_LINK");

					Intent fileLinkIntent = new Intent(managerActivity, FileLinkActivityLollipop.class);
					fileLinkIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					fileLinkIntent.setAction(ACTION_IMPORT_LINK_FETCH_NODES);
					String data = getIntent().getDataString();
					if(data!=null){
						fileLinkIntent.setData(Uri.parse(data));
						startActivity(fileLinkIntent);
					}
					else{
						logWarning("getDataString is NULL");
					}
					finish();
					return;
				}
    			else if (intent.getAction().equals(ACTION_OPEN_MEGA_FOLDER_LINK)){
					logDebug("ACTION_OPEN_MEGA_FOLDER_LINK");

    				Intent intentFolderLink = new Intent(managerActivity, FolderLinkActivityLollipop.class);
    				intentFolderLink.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				intentFolderLink.setAction(ACTION_OPEN_MEGA_FOLDER_LINK);

					String data = getIntent().getDataString();
					if(data!=null){
						intentFolderLink.setData(Uri.parse(data));
						startActivity(intentFolderLink);
					}
					else{
						logWarning("getDataString is NULL");
					}
					finish();
    			}
    			else if (intent.getAction().equals(ACTION_REFRESH_PARENTHANDLE_BROWSER)){

    				parentHandleBrowser = intent.getLongExtra("parentHandle", -1);
    				intent.removeExtra("parentHandle");

					//Refresh Cloud Fragment
					refreshCloudDrive();

					//Refresh Rubbish Fragment
					refreshRubbishBin();
    			}
    			else if(intent.getAction().equals(ACTION_OVERQUOTA_STORAGE)){
	    			showOverquotaAlert(false);
	    		}
				else if(intent.getAction().equals(ACTION_PRE_OVERQUOTA_STORAGE)){
					showOverquotaAlert(true);
				}
				else if (intent.getAction().equals(ACTION_CHANGE_AVATAR)){
					logDebug("Intent CHANGE AVATAR");

					String path = intent.getStringExtra("IMAGE_PATH");
					megaApi.setAvatar(path, this);
				} else if (intent.getAction().equals(ACTION_CANCEL_CAM_SYNC)) {
					logDebug("ACTION_CANCEL_UPLOAD or ACTION_CANCEL_DOWNLOAD or ACTION_CANCEL_CAM_SYNC");
					drawerItem = DrawerItem.TRANSFERS;
					indexTransfers = intent.getIntExtra(TRANSFERS_TAB, ERROR_TAB);
					selectDrawerItemLollipop(drawerItem);

                    String text = getString(R.string.cam_sync_cancel_sync);

					MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                    builder.setMessage(text);

                    builder.setPositiveButton(getString(R.string.general_yes),
							(dialog, whichButton) -> {
								stopRunningCameraUploadService(ManagerActivityLollipop.this);
								dbH.setCamSyncEnabled(false);
								sendBroadcast(new Intent(ACTION_UPDATE_DISABLE_CU_SETTING));

								if (drawerItem == DrawerItem.CAMERA_UPLOADS) {
									cuLayout.setVisibility(View.VISIBLE);
								}
							});

                    builder.setNegativeButton(getString(R.string.general_no), null);
                    final AlertDialog dialog = builder.create();
                    try {
                        dialog.show();
                    } catch (Exception ex) {
						logError("EXCEPTION", ex);
                    }
				}
    			else if (intent.getAction().equals(ACTION_SHOW_TRANSFERS)){
					if (intent.getBooleanExtra(OPENED_FROM_CHAT, false)) {
						sendBroadcast(new Intent(ACTION_CLOSE_CHAT_AFTER_OPEN_TRANSFERS));
					}

    				drawerItem = DrawerItem.TRANSFERS;
					indexTransfers = intent.getIntExtra(TRANSFERS_TAB, ERROR_TAB);
    				selectDrawerItemLollipop(drawerItem);
    			}
    			else if (intent.getAction().equals(ACTION_TAKE_SELFIE)){
					logDebug("Intent take selfie");
					checkTakePicture(this, TAKE_PHOTO_CODE);
    			}
				else if (intent.getAction().equals(SHOW_REPEATED_UPLOAD)){
					logDebug("Intent SHOW_REPEATED_UPLOAD");
					String message = intent.getStringExtra("MESSAGE");
					showSnackbar(SNACKBAR_TYPE, message, -1);
				}
				else if(getIntent().getAction().equals(ACTION_IPC)){
					logDebug("IPC - go to received request in Contacts");
					markNotificationsSeen(true);
					drawerItem=DrawerItem.CONTACTS;
					indexContacts=2;
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(ACTION_CHAT_NOTIFICATION_MESSAGE)){
					logDebug("ACTION_CHAT_NOTIFICATION_MESSAGE");

					long chatId = getIntent().getLongExtra(CHAT_ID, MEGACHAT_INVALID_HANDLE);
					if (getIntent().getBooleanExtra(EXTRA_MOVE_TO_CHAT_SECTION, false)){
						moveToChatSection(chatId);
					}
					else {
						String text = getIntent().getStringExtra(SHOW_SNACKBAR);
						if (chatId != -1) {
							openChat(chatId, text);
						}
					}
				}
				else if(getIntent().getAction().equals(ACTION_CHAT_SUMMARY)) {
					logDebug("ACTION_CHAT_SUMMARY");
					drawerItem=DrawerItem.CHAT;
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION)){
					logDebug("ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION");
					markNotificationsSeen(true);

					drawerItem=DrawerItem.SHARED_ITEMS;
					indexShares = 0;
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(ACTION_OPEN_CONTACTS_SECTION)){
					logDebug("ACTION_OPEN_CONTACTS_SECTION");
					markNotificationsSeen(true);
					openContactLink(getIntent().getLongExtra(CONTACT_HANDLE, -1));
				}
				else if (getIntent().getAction().equals(ACTION_RECOVERY_KEY_EXPORTED)){
					logDebug("ACTION_RECOVERY_KEY_EXPORTED");
					exportRecoveryKey();
				}
				else if (getIntent().getAction().equals(ACTION_REQUEST_DOWNLOAD_FOLDER_LOGOUT)){
					String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);

					if (parentPath != null){
						String sdCardUriString = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_SD_URI);
						AccountController ac = new AccountController(this);
						ac.exportMK(parentPath, sdCardUriString);
					}
				}
				else  if (getIntent().getAction().equals(ACTION_RECOVERY_KEY_COPY_TO_CLIPBOARD)){
					AccountController ac = new AccountController(this);
					if (getIntent().getBooleanExtra("logout", false)) {
						ac.copyMK(true);
					}
					else {
						ac.copyMK(false);
					}
				}
				else if (getIntent().getAction().equals(ACTION_REFRESH_STAGING)){
					update2FASetting();
				}
				else if (getIntent().getAction().equals(ACTION_OPEN_FOLDER)) {
					logDebug("Open after LauncherFileExplorerActivityLollipop ");
					long handleIntent = getIntent().getLongExtra("PARENT_HANDLE", -1);

					if (getIntent().getBooleanExtra(SHOW_MESSAGE_UPLOAD_STARTED, false)) {
						int numberUploads = getIntent().getIntExtra(NUMBER_UPLOADS, 1);
						showSnackbar(SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.upload_began, numberUploads, numberUploads), -1);
					}

					actionOpenFolder(handleIntent);
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(ACTION_SHOW_SNACKBAR_SENT_AS_MESSAGE)){
					long chatId = getIntent().getLongExtra(CHAT_ID, MEGACHAT_INVALID_HANDLE);
					showSnackbar(MESSAGE_SNACKBAR_TYPE, null, chatId);
				}

    			intent.setAction(null);
				setIntent(null);
    		}
    	}


    	if (bNV != null){
            Menu nVMenu = bNV.getMenu();
            resetNavigationViewMenu(nVMenu);

    		switch(drawerItem){
	    		case CLOUD_DRIVE:{
					logDebug("Case CLOUD DRIVE");
					//Check the tab to shown and the title of the actionBar
					setToolbarTitle();
					setBottomNavigationMenuItemChecked(CLOUD_DRIVE_BNV);
	    			break;
	    		}
	    		case SHARED_ITEMS:{
					logDebug("Case SHARED ITEMS");
					setBottomNavigationMenuItemChecked(SHARED_BNV);
					try {
						NotificationManager notificationManager =
								(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

						notificationManager.cancel(NOTIFICATION_PUSH_CLOUD_DRIVE);
					}
					catch (Exception e){
						logError("Exception NotificationManager - remove contact notification", e);
					}
					setToolbarTitle();
		    		break;
	    		}
				case SETTINGS:{
					setToolbarTitle();
					setBottomNavigationMenuItemChecked(HIDDEN_BNV);
					break;
				}
				case CONTACTS:{
					setBottomNavigationMenuItemChecked(HIDDEN_BNV);
					try {
						ContactsAdvancedNotificationBuilder notificationBuilder;
						notificationBuilder =  ContactsAdvancedNotificationBuilder.newInstance(this, megaApi);

						notificationBuilder.removeAllIncomingContactNotifications();
						notificationBuilder.removeAllAcceptanceContactNotifications();
					}
					catch (Exception e){
						logError("Exception NotificationManager - remove all CONTACT notifications", e);
					}

					setToolbarTitle();
					break;
				}
				case SEARCH:{
					setBottomNavigationMenuItemChecked(HIDDEN_BNV);
					setToolbarTitle();
					break;
				}
				case CHAT:
					setBottomNavigationMenuItemChecked(CHAT_BNV);
					if (getChatsFragment() != null && rChatFL.isVisible()) {
						rChatFL.setChats();
						rChatFL.setStatus();
					}
					MegaApplication.setRecentChatVisible(true);
					break;
				case ACCOUNT:{
					setBottomNavigationMenuItemChecked(HIDDEN_BNV);
					setToolbarTitle();
					try {
						NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						notificationManager.cancel(NOTIFICATION_STORAGE_OVERQUOTA);
					}
					catch (Exception e){
						logError("Exception NotificationManager - remove all notifications", e);
					}

					break;
				}
				case CAMERA_UPLOADS: {
					setBottomNavigationMenuItemChecked(CAMERA_UPLOADS_BNV);
					break;
				}
				case NOTIFICATIONS: {
					notificFragment = (NotificationsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.NOTIFICATIONS.getTag());
					if(notificFragment!=null){
						notificFragment.setNotifications();
					}
					break;
				}
				case HOMEPAGE:
				default:
					setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);
					break;
    		}
    	}
	}

	public void openChat(long chatId, String text){
		logDebug("Chat ID: " + chatId);
//		drawerItem=DrawerItem.CHAT;
//		selectDrawerItemLollipop(drawerItem);

		if(chatId!=-1){
			MegaChatRoom chat = megaChatApi.getChatRoom(chatId);
			if(chat!=null){
				logDebug("Open chat with id: " + chatId);
				Intent intentToChat = new Intent(this, ChatActivityLollipop.class);
				intentToChat.setAction(ACTION_CHAT_SHOW_MESSAGES);
				intentToChat.putExtra(CHAT_ID, chatId);
				intentToChat.putExtra(SHOW_SNACKBAR, text);
				this.startActivity(intentToChat);
			}
			else{
				logError("Error, chat is NULL");
			}
		}
		else{
			logError("Error, chat id is -1");
		}
	}

	public void setProfileAvatar() {
		logDebug("setProfileAvatar");
		Pair<Boolean, Bitmap> circleAvatar = AvatarUtil.getCircleAvatar(this, megaApi.getMyEmail());
		if (circleAvatar.first) {
			nVPictureProfile.setImageBitmap(circleAvatar.second);
		} else {
			megaApi.getUserAvatar(megaApi.getMyUser(),
					buildAvatarFile(this, megaApi.getMyEmail() + JPG_EXTENSION).getAbsolutePath(),
					this);
		}
	}

	public void setDefaultAvatar(){
		logDebug("setDefaultAvatar");
		nVPictureProfile.setImageBitmap(getDefaultAvatar(getColorAvatar(megaApi.getMyUser()), MegaApplication.getInstance().getMyAccountInfo().getFullName(), AVATAR_SIZE, true));
	}

	public void setOfflineAvatar(String email, long myHandle, String name){
		logDebug("setOfflineAvatar");
		if (nVPictureProfile == null) {
			return;
		}

		Pair<Boolean, Bitmap> circleAvatar = AvatarUtil.getCircleAvatar(this, email);
		if (circleAvatar.first) {
			nVPictureProfile.setImageBitmap(circleAvatar.second);
		} else {
			nVPictureProfile.setImageBitmap(
					getDefaultAvatar(getColorAvatar(myHandle), name, AVATAR_SIZE, true));
		}
	}

	public void showDialogChangeUserAttribute(){
		userNameChanged = false;
		userEmailChanged = false;

		megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);

		ScrollView scrollView = new ScrollView(this);

		LinearLayout layout = new LinearLayout(this);

		scrollView.addView(layout);

		layout.setOrientation(LinearLayout.VERTICAL);
//        layout.setNestedScrollingEnabled(true);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(scaleWidthPx(20, outMetrics), scaleHeightPx(20, outMetrics), scaleWidthPx(17, outMetrics), 0);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.setMargins(scaleWidthPx(20, outMetrics), 0, scaleWidthPx(17, outMetrics), 0);

		final EmojiEditText inputFirstName = new EmojiEditText(this);
		inputFirstName.getBackground().mutate().clearColorFilter();
		inputFirstName.getBackground().mutate().setColorFilter(ColorUtils.getThemeColor(this, R.attr.colorSecondary), PorterDuff.Mode.SRC_ATOP);
		layout.addView(inputFirstName, params);

		final RelativeLayout error_layout_firstName = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_firstName, params1);

		final ImageView error_icon_firstName = new ImageView(ManagerActivityLollipop.this);
		error_icon_firstName.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_firstName.addView(error_icon_firstName);
		RelativeLayout.LayoutParams params_icon_firstName = (RelativeLayout.LayoutParams) error_icon_firstName.getLayoutParams();

		params_icon_firstName.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_firstName.setLayoutParams(params_icon_firstName);

		error_icon_firstName.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.red_600_red_300));

		final TextView textError_firstName = new TextView(ManagerActivityLollipop.this);
		error_layout_firstName.addView(textError_firstName);
		RelativeLayout.LayoutParams params_text_error_firstName = (RelativeLayout.LayoutParams) textError_firstName.getLayoutParams();
		params_text_error_firstName.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error_firstName.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error_firstName.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error_firstName.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error_firstName.setMargins(scaleWidthPx(3, outMetrics), 0,0,0);
		textError_firstName.setLayoutParams(params_text_error_firstName);

		textError_firstName.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.red_600_red_300));

		error_layout_firstName.setVisibility(View.GONE);

		final EmojiEditText inputLastName = new EmojiEditText(this);
		inputLastName.getBackground().mutate().clearColorFilter();
		inputLastName.getBackground().mutate().setColorFilter(ColorUtils.getThemeColor(this, R.attr.colorSecondary), PorterDuff.Mode.SRC_ATOP);
		layout.addView(inputLastName, params);

		final RelativeLayout error_layout_lastName = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_lastName, params1);

		final ImageView error_icon_lastName = new ImageView(ManagerActivityLollipop.this);
		error_icon_lastName.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_lastName.addView(error_icon_lastName);
		RelativeLayout.LayoutParams params_icon_lastName = (RelativeLayout.LayoutParams) error_icon_lastName.getLayoutParams();


		params_icon_lastName.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_lastName.setLayoutParams(params_icon_lastName);

		error_icon_lastName.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.red_600_red_300));

		final TextView textError_lastName = new TextView(ManagerActivityLollipop.this);
		error_layout_lastName.addView(textError_lastName);
		RelativeLayout.LayoutParams params_text_error_lastName = (RelativeLayout.LayoutParams) textError_lastName.getLayoutParams();
		params_text_error_lastName.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error_lastName.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error_lastName.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error_lastName.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error_lastName.setMargins(scaleWidthPx(3, outMetrics), 0,0,0);
		textError_lastName.setLayoutParams(params_text_error_lastName);

		textError_lastName.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.red_600_red_300));

		error_layout_lastName.setVisibility(View.GONE);

		final EditText inputMail = new EditText(this);
		inputMail.getBackground().mutate().clearColorFilter();
		inputMail.getBackground().mutate().setColorFilter(ColorUtils.getThemeColor(this, R.attr.colorSecondary), PorterDuff.Mode.SRC_ATOP);
		layout.addView(inputMail, params);

		final RelativeLayout error_layout_email = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_email, params1);

		final ImageView error_icon_email = new ImageView(ManagerActivityLollipop.this);
		error_icon_email.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_email.addView(error_icon_email);
		RelativeLayout.LayoutParams params_icon_email = (RelativeLayout.LayoutParams) error_icon_email.getLayoutParams();


		params_icon_email.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_email.setLayoutParams(params_icon_email);

		error_icon_email.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.red_600_red_300));

		final TextView textError_email = new TextView(ManagerActivityLollipop.this);
		error_layout_email.addView(textError_email);
		RelativeLayout.LayoutParams params_text_error_email = (RelativeLayout.LayoutParams) textError_email.getLayoutParams();
		params_text_error_email.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error_email.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error_email.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error_email.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error_email.setMargins(scaleWidthPx(3, outMetrics), 0,scaleWidthPx(20, outMetrics),0);
		textError_email.setLayoutParams(params_text_error_email);

		textError_email.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.red_600_red_300));

		error_layout_email.setVisibility(View.GONE);

		final OnEditorActionListener editorActionListener = new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String valueFirstName = inputFirstName.getText().toString().trim();
					String valueLastName = inputLastName.getText().toString().trim();
					String value = inputMail.getText().toString().trim();
					String emailError = getEmailError(value, managerActivity);
					if (emailError == null && userEmailChanged && !userNameChanged) {
						emailError = comparedToCurrentEmail(value, managerActivity);
					}
					if (emailError != null) {
						inputMail.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.red_600_red_300), PorterDuff.Mode.SRC_ATOP);
						textError_email.setText(emailError);
						error_layout_email.setVisibility(View.VISIBLE);
						inputMail.requestFocus();
					} else if (valueFirstName.equals("") || valueFirstName.isEmpty()) {
						logWarning("First name input is empty");
						inputFirstName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.red_600_red_300), PorterDuff.Mode.SRC_ATOP);
						textError_firstName.setText(R.string.error_enter_username);
						error_layout_firstName.setVisibility(View.VISIBLE);
						inputFirstName.requestFocus();
					} else if (valueLastName.equals("") || valueLastName.isEmpty()) {
						logWarning("Last name input is empty");
						inputLastName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.red_600_red_300), PorterDuff.Mode.SRC_ATOP);
                        textError_lastName.setText(R.string.error_enter_userlastname);
						error_layout_lastName.setVisibility(View.VISIBLE);
						inputLastName.requestFocus();
					} else {
						logDebug("Positive button pressed - change user attribute(s)");
						countUserAttributes = aC.updateUserAttributes(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText(), valueFirstName, ((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText(), valueLastName, megaApi.getMyEmail(), value);
						changeUserAttributeDialog.dismiss();
					}
				} else {
					logDebug("Other IME" + actionId);
				}
				return false;
			}
		};

		inputFirstName.setSingleLine();
		inputFirstName.setHint(R.string.first_name_text);
		inputFirstName.setText(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText());
		inputFirstName.setTextColor(ColorUtils.getThemeColor(this, android.R.attr.textColorSecondary));
		inputFirstName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		inputFirstName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		inputFirstName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				userNameChanged = true;
				if(error_layout_firstName.getVisibility() == View.VISIBLE){
					error_layout_firstName.setVisibility(View.GONE);
					inputFirstName.getBackground().mutate().clearColorFilter();
					inputFirstName.getBackground().mutate().setColorFilter(ColorUtils.getThemeColor(
							ManagerActivityLollipop.this, R.attr.colorSecondary), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});
		inputFirstName.setOnEditorActionListener(editorActionListener);
		inputFirstName.setImeActionLabel(getString(R.string.save_action),EditorInfo.IME_ACTION_DONE);
		inputFirstName.requestFocus();

		inputLastName.setSingleLine();
		inputLastName.setHint(R.string.lastname_text);
		inputLastName.setText(((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText());
		inputLastName.setTextColor(ColorUtils.getThemeColor(this, android.R.attr.textColorSecondary));
		inputLastName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		inputLastName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		inputLastName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				userNameChanged = true;
				if(error_layout_lastName.getVisibility() == View.VISIBLE){
					error_layout_lastName.setVisibility(View.GONE);
					inputLastName.getBackground().mutate().clearColorFilter();
					inputLastName.getBackground().mutate().setColorFilter(ColorUtils.getThemeColor(
							ManagerActivityLollipop.this, R.attr.colorSecondary), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});
		inputLastName.setOnEditorActionListener(editorActionListener);
		inputLastName.setImeActionLabel(getString(R.string.save_action),EditorInfo.IME_ACTION_DONE);

		inputMail.getBackground().mutate().clearColorFilter();
		inputMail.setSingleLine();
		inputMail.setHint(R.string.email_text);
		inputMail.setText(megaApi.getMyUser().getEmail());
		inputMail.setTextColor(ColorUtils.getThemeColor(this, android.R.attr.textColorSecondary));
		inputMail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		inputMail.setImeOptions(EditorInfo.IME_ACTION_DONE);
		inputMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		inputMail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				userEmailChanged = true;
				if(error_layout_email.getVisibility() == View.VISIBLE){
					error_layout_email.setVisibility(View.GONE);
					inputMail.getBackground().mutate().clearColorFilter();
					inputMail.getBackground().mutate().setColorFilter(ColorUtils.getThemeColor(
							ManagerActivityLollipop.this, R.attr.colorSecondary), PorterDuff.Mode.SRC_ATOP);

				}
			}
		});
		inputMail.setOnEditorActionListener(editorActionListener);
		inputMail.setImeActionLabel(getString(R.string.save_action),EditorInfo.IME_ACTION_DONE);

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(getString(R.string.title_edit_profile_info));

		builder.setPositiveButton(getString(R.string.save_action), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				inputFirstName.getBackground().clearColorFilter();
				inputLastName.getBackground().clearColorFilter();
				inputMail.getBackground().clearColorFilter();
			}
		});
		builder.setView(scrollView);

		changeUserAttributeDialog = builder.create();
		changeUserAttributeDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		changeUserAttributeDialog.show();
		changeUserAttributeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				logDebug("OK BTTN PASSWORD");
				String valueFirstName = inputFirstName.getText().toString().trim();
				String valueLastName = inputLastName.getText().toString().trim();
				String value = inputMail.getText().toString().trim();
				String emailError = getEmailError(value, managerActivity);
				if (emailError == null && userEmailChanged && !userNameChanged) {
					emailError = comparedToCurrentEmail(value, managerActivity);
				}
				if (emailError != null) {
					inputMail.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.red_600_red_300), PorterDuff.Mode.SRC_ATOP);
					textError_email.setText(emailError);
					error_layout_email.setVisibility(View.VISIBLE);
					inputMail.requestFocus();
				}
				else if(valueFirstName.equals("")||valueFirstName.isEmpty()){
					logWarning("Input is empty");
					inputFirstName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.red_600_red_300), PorterDuff.Mode.SRC_ATOP);
                    textError_firstName.setText(R.string.error_enter_username);
					error_layout_firstName.setVisibility(View.VISIBLE);
					inputFirstName.requestFocus();
				}
				else if(valueLastName.equals("")||valueLastName.isEmpty()){
					logWarning("Input is empty");
					inputLastName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.red_600_red_300), PorterDuff.Mode.SRC_ATOP);
                    textError_lastName.setText(R.string.error_enter_userlastname);
					error_layout_lastName.setVisibility(View.VISIBLE);
					inputLastName.requestFocus();
				}
				else {
					logDebug("Positive button pressed - change user attribute");
					countUserAttributes = aC.updateUserAttributes(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText(), valueFirstName, ((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText(), valueLastName, megaApi.getMyEmail(), value);
					changeUserAttributeDialog.dismiss();
				}
			}
		});
	}

	@Override
	protected void onStop(){
		logDebug("onStop");

		mStopped = true;

		super.onStop();
	}

	@Override
	protected void onPause() {
		logDebug("onPause");
    	managerActivity = null;
    	MegaApplication.getTransfersManagement().setIsOnTransfersSection(false);
    	super.onPause();
    }

	@Override
    protected void onDestroy(){
		logDebug("onDestroy()");

		dbH.removeSentPendingMessages();

    	if (megaApi != null && megaApi.getRootNode() != null){
    		megaApi.removeGlobalListener(this);
    		megaApi.removeTransferListener(this);
    		megaApi.removeRequestListener(this);
    	}

		if (megaChatApi != null){
			megaChatApi.removeChatListener(this);
		}
        if (alertDialogSMSVerification != null) {
            alertDialogSMSVerification.dismiss();
        }
		isStorageStatusDialogShown = false;

		unregisterReceiver(chatCallUpdateReceiver);
		unregisterReceiver(chatSessionUpdateReceiver);
		unregisterReceiver(chatRoomMuteUpdateReceiver);
		unregisterReceiver(contactUpdateReceiver);
		unregisterReceiver(updateMyAccountReceiver);
		unregisterReceiver(receiverUpdate2FA);
		unregisterReceiver(networkReceiver);
		unregisterReceiver(receiverUpdateOrder);
		unregisterReceiver(receiverUpdateView);
		unregisterReceiver(chatArchivedReceiver);
        unregisterReceiver(refreshAddPhoneNumberButtonReceiver);
		unregisterReceiver(receiverCUAttrChanged);
		unregisterReceiver(transferOverQuotaUpdateReceiver);
		unregisterReceiver(transferFinishReceiver);
        unregisterReceiver(cameraUploadLauncherReceiver);
        unregisterReceiver(updateCUSettingsReceiver);
		unregisterReceiver(cuUpdateReceiver);

		if (mBillingManager != null) {
			mBillingManager.destroy();
		}
		cancelSearch();
        if(reconnectDialog != null) {
            reconnectDialog.cancel();
        }

        if (confirmationTransfersDialog != null) {
            confirmationTransfersDialog.dismiss();
        }

        if (newTextFileDialog != null) {
        	newTextFileDialog.dismiss();
		}

        if (miniAudioPlayerController != null) {
			miniAudioPlayerController.onDestroy();
			miniAudioPlayerController = null;
		}

		nodeSaver.destroy();

    	super.onDestroy();
	}

	private void cancelSearch() {
		if (getSearchFragment() != null) {
			sFLol.cancelPreviousAsyncTask();
		}
	}

	void replaceFragment (Fragment f, String fTag) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_container, f, fTag);
		ft.commitNowAllowingStateLoss();
		// refresh manually
		if (f instanceof RecentChatsFragmentLollipop) {
			RecentChatsFragmentLollipop rcf = (RecentChatsFragmentLollipop) f;
			if (rcf.isResumed()) {
				rcf.refreshMegaContactsList();
				rcf.setCustomisedActionBar();
			}
		}
	}

	private void refreshFragment (String fTag) {
		Fragment f = getSupportFragmentManager().findFragmentByTag(fTag);
		if (f != null) {
			logDebug("Fragment " + fTag + " refreshing");
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.detach(f);
			if (fTag.equals(FragmentTag.CLOUD_DRIVE.getTag())) {
				((FileBrowserFragmentLollipop) f).headerItemDecoration = null;
			}
			else if (fTag.equals(FragmentTag.RUBBISH_BIN.getTag())) {
				((RubbishBinFragmentLollipop) f).headerItemDecoration = null;
			}
			else if (fTag.equals(FragmentTag.INCOMING_SHARES.getTag())) {
				((IncomingSharesFragmentLollipop) f).headerItemDecoration = null;
			}
			else if (fTag.equals(FragmentTag.OUTGOING_SHARES.getTag())) {
				((OutgoingSharesFragmentLollipop) f).headerItemDecoration = null;
			}
            else if (fTag.equals(FragmentTag.LINKS.getTag())) {
                ((LinksFragment) f).headerItemDecoration = null;
            }
			else if (fTag.equals(FragmentTag.INBOX.getTag())) {
				((InboxFragmentLollipop) f).headerItemDecoration = null;
			}
			else if (fTag.equals(FragmentTag.SEARCH.getTag())) {
				((SearchFragmentLollipop) f).setHeaderItemDecoration(null);
			}

			ft.attach(f);
			ft.commitNowAllowingStateLoss();
		}
		else {
			logWarning("Fragment == NULL. Not refresh");
		}
	}

	public boolean isFirstLogin() {
        return firstLogin;
    }

	public void selectDrawerItemCloudDrive(){
		logDebug("selectDrawerItemCloudDrive");
		abL.setVisibility(View.VISIBLE);

        tabLayoutContacts.setVisibility(View.GONE);
        viewPagerContacts.setVisibility(View.GONE);
        tabLayoutShares.setVisibility(View.GONE);
        viewPagerShares.setVisibility(View.GONE);
        tabLayoutMyAccount.setVisibility(View.GONE);
        viewPagerMyAccount.setVisibility(View.GONE);
        tabLayoutTransfers.setVisibility(View.GONE);
        viewPagerTransfers.setVisibility(View.GONE);

        fragmentContainer.setVisibility(View.VISIBLE);

        fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CLOUD_DRIVE.getTag());
        if (fbFLol == null) {
            fbFLol = FileBrowserFragmentLollipop.newInstance();
        }
        replaceFragment(fbFLol, FragmentTag.CLOUD_DRIVE.getTag());
    }

    private void showGlobalAlertDialogsIfNeeded() {
		if (showStorageAlertWithDelay) {
			showStorageAlertWithDelay = false;
			checkStorageStatus(storageStateFromBroadcast != MegaApiJava.STORAGE_STATE_UNKNOWN ?
					storageStateFromBroadcast : app.getStorageState(), false);
		}

		if (!firstTimeAfterInstallation){
			logDebug("Its NOT first time");
			int dbContactsSize = dbH.getContactsSize();
			int sdkContactsSize = megaApi.getContacts().size();
			if (dbContactsSize != sdkContactsSize){
				logDebug("Contacts TABLE != CONTACTS SDK "+ dbContactsSize + " vs " +sdkContactsSize);
				dbH.clearContacts();
				FillDBContactsTask fillDBContactsTask = new FillDBContactsTask(this);
				fillDBContactsTask.execute();
			}
		}
		else{
			logDebug("Its first time");

			//Fill the contacts DB
			FillDBContactsTask fillDBContactsTask = new FillDBContactsTask(this);
			fillDBContactsTask.execute();
			firstTimeAfterInstallation = false;
			dbH.setFirstTime(false);
		}

		checkBeforeShowSMSVerificationDialog();

		cookieDialogHandler.showDialogIfNeeded(this);
    }

    /**
	 * Observe LiveData for PSA, and show PSA view when get it.
	 */
    private void observePsa() {
        psaViewHolder = new PsaViewHolder(findViewById(R.id.psa_layout), PsaManager.INSTANCE);

		LiveEventBus.get(EVENT_PSA, Psa.class).observe(this, psa -> {
			if (psa.getUrl() == null) {
				showPsa(psa);
			}
		});
    }

	/**
	 * Show PSA view for old PSA type.
	 *
	 * @param psa the PSA to show
	 */
    private void showPsa(Psa psa) {
        if (psa == null || drawerItem != DrawerItem.HOMEPAGE
				|| mHomepageScreen != HomepageScreen.HOMEPAGE) {
			updateHomepageFabPosition();
            return;
        }

        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED
				&& getProLayout.getVisibility() == View.GONE
				&& TextUtils.isEmpty(psa.getUrl())) {
			psaViewHolder.bind(psa);
			handler.post(this::updateHomepageFabPosition);
        }
    }

    public void checkBeforeShowSMSVerificationDialog() {
        //This account hasn't verified a phone number and first login.

		MyAccountInfo myAccountInfo = MegaApplication.getInstance().getMyAccountInfo();
		if (myAccountInfo != null && myAccountInfo.isBusinessAlertShown()) {
			//The business alerts has priority over SMS verification
			return;
		}

        if (canVoluntaryVerifyPhoneNumber() && (smsDialogTimeChecker.shouldShow() || isSMSDialogShowing) && !newCreationAccount) {
            showSMSVerificationDialog();
        }
    }

    public void setToolbarTitle(String title) {
        aB.setTitle(title);
    }

	public void setToolbarTitle(){
		logDebug("setToolbarTitle");
		if(drawerItem==null){
			return;
		}

		switch (drawerItem){
			case CLOUD_DRIVE:{

                aB.setSubtitle(null);
                logDebug("Cloud Drive SECTION");
                MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
                if (parentNode != null) {
                    if (megaApi.getRootNode() != null) {
                        if (parentNode.getHandle() == megaApi.getRootNode().getHandle() || parentHandleBrowser == -1) {
                            aB.setTitle(getString(R.string.section_cloud_drive).toUpperCase());
                            firstNavigationLevel = true;
                        }
                        else {
                            aB.setTitle(parentNode.getName());
                            firstNavigationLevel = false;
                        }
                    }
                    else {
                        parentHandleBrowser = -1;
                    }
                }
                else {
                    if (megaApi.getRootNode() != null) {
                        parentHandleBrowser = megaApi.getRootNode().getHandle();
                        aB.setTitle(getString(R.string.title_mega_info_empty_screen).toUpperCase());
                        firstNavigationLevel = true;
                    }
                    else {
                        parentHandleBrowser = -1;
                        firstNavigationLevel = true;
                    }
                }
				break;
			}
			case RUBBISH_BIN: {
				aB.setSubtitle(null);
				if(parentHandleRubbish == megaApi.getRubbishNode().getHandle() || parentHandleRubbish == -1){
					aB.setTitle(getResources().getString(R.string.section_rubbish_bin).toUpperCase());
					firstNavigationLevel = true;
				}
				else{
					MegaNode node = megaApi.getNodeByHandle(parentHandleRubbish);
					if(node==null){
						logWarning("Node NULL - cannot be recovered");
						aB.setTitle(getResources().getString(R.string.section_rubbish_bin).toUpperCase());
					}
					else{
						aB.setTitle(node.getName());
					}

					firstNavigationLevel = false;
				}
				break;
			}
			case SHARED_ITEMS:{
				logDebug("Shared Items SECTION");
				aB.setSubtitle(null);
				int indexShares = getTabItemShares();
				if (indexShares == ERROR_TAB) break;
				switch(indexShares){
					case INCOMING_TAB:{
						if (isIncomingAdded()) {
							if (parentHandleIncoming != -1) {
								MegaNode node = megaApi.getNodeByHandle(parentHandleIncoming);
								if (node == null) {
									aB.setTitle(getResources().getString(R.string.title_shared_items).toUpperCase());
								}
								else {
									aB.setTitle(node.getName());
								}

								firstNavigationLevel = false;
							}
							else {
								aB.setTitle(getResources().getString(R.string.title_shared_items).toUpperCase());
								firstNavigationLevel = true;
							}
						}
						else {
							logDebug("selectDrawerItemSharedItems: inSFLol == null");
							}
						break;
					}
					case OUTGOING_TAB:{
						logDebug("setToolbarTitle: OUTGOING TAB");
						if (isOutgoingAdded()) {
							if (parentHandleOutgoing != -1) {
								MegaNode node = megaApi.getNodeByHandle(parentHandleOutgoing);
								aB.setTitle(node.getName());
								firstNavigationLevel = false;
							} else {
								aB.setTitle(getResources().getString(R.string.title_shared_items).toUpperCase());
								firstNavigationLevel = true;
							}
						}
						break;
					}
                    case LINKS_TAB:
                        if (isLinksAdded()) {
                            if (parentHandleLinks == INVALID_HANDLE) {
                                aB.setTitle(getResources().getString(R.string.title_shared_items).toUpperCase());
                                firstNavigationLevel = true;
                            } else {
                                MegaNode node = megaApi.getNodeByHandle(parentHandleLinks);
                                aB.setTitle(node.getName());
                                firstNavigationLevel = false;
                            }
                        }
                        break;
					default: {
						aB.setTitle(getResources().getString(R.string.title_shared_items).toUpperCase());
						firstNavigationLevel = true;
						break;
					}
				}
				break;
			}
			case INBOX:{
				aB.setSubtitle(null);
				if(parentHandleInbox==megaApi.getInboxNode().getHandle()||parentHandleInbox==-1){
					aB.setTitle(getResources().getString(R.string.section_inbox).toUpperCase());
					firstNavigationLevel = true;
				}
				else{
					MegaNode node = megaApi.getNodeByHandle(parentHandleInbox);
					aB.setTitle(node.getName());
					firstNavigationLevel = false;
				}
				break;
			}
			case CONTACTS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.section_contacts).toUpperCase());
				firstNavigationLevel = true;
				break;
			}
			case NOTIFICATIONS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.title_properties_chat_contact_notifications).toUpperCase());
				firstNavigationLevel = true;
				break;
			}
			case CHAT:{
				abL.setVisibility(View.VISIBLE);
				aB.setTitle(getString(R.string.section_chat).toUpperCase());

				firstNavigationLevel = true;
				break;
			}
			case SEARCH:{
				aB.setSubtitle(null);
				if(parentHandleSearch==-1){
					firstNavigationLevel = true;
					if(searchQuery!=null){
						textSubmitted = true;
						if(!searchQuery.isEmpty()){
							aB.setTitle(getString(R.string.action_search).toUpperCase()+": "+searchQuery);
						}else{
							aB.setTitle(getString(R.string.action_search).toUpperCase()+": "+"");
						}
					}else{
						aB.setTitle(getString(R.string.action_search).toUpperCase()+": "+"");
					}

				}else{
					MegaNode parentNode = megaApi.getNodeByHandle(parentHandleSearch);
					if (parentNode != null){
						aB.setTitle(parentNode.getName());
						firstNavigationLevel = false;
					}
				}
				break;
			}
			case SETTINGS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.action_settings).toUpperCase());
				firstNavigationLevel = true;
				break;
			}
			case ACCOUNT:{
				aB.setSubtitle(null);
				if(accountFragment==MY_ACCOUNT_FRAGMENT){
					aB.setTitle(getString(R.string.section_account).toUpperCase());
					setFirstNavigationLevel(true);
				}
				else if(accountFragment==UPGRADE_ACCOUNT_FRAGMENT){
					aB.setTitle(getString(R.string.action_upgrade_account).toUpperCase());
					setFirstNavigationLevel(false);
				}
				else{
					aB.setTitle(getString(R.string.section_account).toUpperCase());
					setFirstNavigationLevel(true);
				}
				break;
			}
			case TRANSFERS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.section_transfers).toUpperCase());
				setFirstNavigationLevel(true);
				break;
			}
			case CAMERA_UPLOADS:{
				aB.setSubtitle(null);
				if (getCameraUploadFragment() != null && cuFragment.isEnableCUFragmentShown()) {
					setFirstNavigationLevel(false);
					aB.setTitle(getString(R.string.settings_camera_upload_on).toUpperCase());
				} else {
					setFirstNavigationLevel(true);
					aB.setTitle(getString(R.string.section_photo_sync).toUpperCase());
				}
				break;
			}
			case HOMEPAGE: {
				setFirstNavigationLevel(false);
				int titleId = -1;

				switch (mHomepageScreen) {
					case PHOTOS:
						titleId = R.string.sortby_type_photo_first;
						break;
					case DOCUMENTS:
						titleId = R.string.section_documents;
						break;
					case AUDIO:
						titleId = R.string.upload_to_audio;
						break;
                    case VIDEO:
                        titleId = R.string.sortby_type_video_first;
                        break;
				}

				if (titleId != -1) {
					aB.setTitle(getString(titleId).toUpperCase(Locale.getDefault()));
				}
			}
			default:{
				logDebug("Default GONE");

				break;
			}
		}

		updateNavigationToolbarIcon();
	}

	public void setToolbarTitleFromFullscreenOfflineFragment(String title,
			boolean firstNavigationLevel, boolean showSearch) {
		aB.setSubtitle(null);
		aB.setTitle(title);
		this.firstNavigationLevel = firstNavigationLevel;
		updateNavigationToolbarIcon();
		textSubmitted = true;
		if (searchMenuItem != null) {
			searchMenuItem.setVisible(showSearch);
		}
	}

	public void updateNavigationToolbarIcon(){
		int totalHistoric = megaApi.getNumUnreadUserAlerts();
		int totalIpc = 0;
		ArrayList<MegaContactRequest> requests = megaApi.getIncomingContactRequests();
		if(requests!=null) {
			totalIpc = requests.size();
		}

		int totalNotifications = totalHistoric + totalIpc;

		if(totalNotifications==0){
			if(isFirstNavigationLevel()){
				if (drawerItem == DrawerItem.SEARCH || drawerItem == DrawerItem.ACCOUNT || drawerItem == DrawerItem.INBOX || drawerItem == DrawerItem.CONTACTS || drawerItem == DrawerItem.NOTIFICATIONS
						|| drawerItem == DrawerItem.SETTINGS || drawerItem == DrawerItem.RUBBISH_BIN || drawerItem == DrawerItem.TRANSFERS){
					aB.setHomeAsUpIndicator(tintIcon(this, R.drawable.ic_arrow_back_white));
				}
				else {
					aB.setHomeAsUpIndicator(tintIcon(this, R.drawable.ic_menu_white));
				}
			}
			else{
				aB.setHomeAsUpIndicator(tintIcon(this, R.drawable.ic_arrow_back_white));
			}
		}
		else{
			if(isFirstNavigationLevel()){
				if (drawerItem == DrawerItem.SEARCH || drawerItem == DrawerItem.ACCOUNT || drawerItem == DrawerItem.INBOX || drawerItem == DrawerItem.CONTACTS || drawerItem == DrawerItem.NOTIFICATIONS
						|| drawerItem == DrawerItem.SETTINGS || drawerItem == DrawerItem.RUBBISH_BIN || drawerItem == DrawerItem.TRANSFERS){
					badgeDrawable.setProgress(1.0f);
				}
				else {
					badgeDrawable.setProgress(0.0f);
				}
			}
			else{
				badgeDrawable.setProgress(1.0f);
			}

			if(totalNotifications>9){
				badgeDrawable.setText("9+");
			}
			else{
				badgeDrawable.setText(totalNotifications+"");
			}

			aB.setHomeAsUpIndicator(badgeDrawable);
		}
	}

	public void showOnlineMode(){
		logDebug("showOnlineMode");

		try {
			if (usedSpaceLayout != null) {

				if (rootNode != null) {
					Menu bNVMenu = bNV.getMenu();
					if (bNVMenu != null) {
						resetNavigationViewMenu(bNVMenu);
					}
					clickDrawerItemLollipop(drawerItem);

					if (getSettingsFragment() != null) {
						sttFLol.setOnlineOptions(true);
					}

					supportInvalidateOptionsMenu();

//					if (rChatFL != null) {
//						if (rChatFL.isAdded()) {
//							logDebug("ONLINE: Update screen RecentChats");
//							if (!isChatEnabled()) {
//								rChatFL.showDisableChatScreen();
//							}
//						}
//					}		updateAccountDetailsVisibleInfo();

					updateAccountDetailsVisibleInfo();
					checkCurrentStorageStatus(false);
				} else {
					logWarning("showOnlineMode - Root is NULL");
					if (getApplicationContext() != null) {
						if(((MegaApplication) getApplication()).getOpenChatId()!=-1){
							Intent intent = new Intent(BROADCAST_ACTION_INTENT_CONNECTIVITY_CHANGE_DIALOG);
							sendBroadcast(intent);
						}
						else{
							showConfirmationConnect();
						}
					}
				}
			}
		}catch (Exception e){}
	}

	public void showConfirmationConnect(){
		logDebug("showConfirmationConnect");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						startConnection();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						logDebug("showConfirmationConnect: BUTTON_NEGATIVE");
                        setToolbarTitle();
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		try {
			builder.setMessage(R.string.confirmation_to_reconnect).setPositiveButton(R.string.general_ok, dialogClickListener)
					.setNegativeButton(R.string.general_cancel, dialogClickListener);
            reconnectDialog = builder.create();
            reconnectDialog.setCanceledOnTouchOutside(false);
            reconnectDialog.show();
		}
		catch (Exception e){}
	}

	public void startConnection(){
		logDebug("startConnection");
		Intent intent = new Intent(this, LoginActivityLollipop.class);
		intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void showOfflineMode() {
		logDebug("showOfflineMode");

		try{
			if (megaApi == null) {
				logWarning("megaApi is Null in Offline mode");
			}

			if (usedSpaceLayout != null) {
				usedSpaceLayout.setVisibility(View.GONE);
			}
			if (nVEmail != null) {
				nVEmail.setText(megaChatApi.getMyEmail());
			}
			if (nVDisplayName != null) {
				nVDisplayName.setText(megaChatApi.getMyFullname());
			}

			setOfflineAvatar(megaChatApi.getMyEmail(), megaChatApi.getMyUserHandle(),
					megaChatApi.getMyFullname());

			if (getSettingsFragment() != null) {
				sttFLol.setOnlineOptions(false);
			}

			logDebug("DrawerItem on start offline: " + drawerItem);
			if (drawerItem == null) {
				logWarning("drawerItem == null --> On start OFFLINE MODE");
				drawerItem = DrawerItem.HOMEPAGE;
				if (bNV != null) {
					Menu bNVMenu = bNV.getMenu();
					if (bNVMenu != null) {
						disableNavigationViewMenu(bNVMenu);
					}
				}
				setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);
				selectDrawerItemLollipop(drawerItem);
			} else {
				if (bNV != null) {
					Menu bNVMenu = bNV.getMenu();
					if (bNVMenu != null) {
						disableNavigationViewMenu(bNVMenu);
					}
				}
				logDebug("Change to OFFLINE MODE");
				clickDrawerItemLollipop(drawerItem);
			}

			supportInvalidateOptionsMenu();
		}catch(Exception e){}
	}

	public void clickDrawerItemLollipop(DrawerItem item){
		logDebug("Item: " + item);
		Menu bNVMenu = bNV.getMenu();
		if (bNVMenu != null){
			if(item==null){
				drawerMenuItem = bNVMenu.findItem(R.id.bottom_navigation_item_cloud_drive);
				onNavigationItemSelected(drawerMenuItem);
				return;
			}

			drawerLayout.closeDrawer(Gravity.LEFT);

			switch (item){
				case CLOUD_DRIVE:{
					setBottomNavigationMenuItemChecked(CLOUD_DRIVE_BNV);
					break;
				}
				case HOMEPAGE: {
					setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);
					break;
				}
				case CAMERA_UPLOADS:{
					setBottomNavigationMenuItemChecked(CAMERA_UPLOADS_BNV);
					break;
				}
				case SHARED_ITEMS:{
					setBottomNavigationMenuItemChecked(SHARED_BNV);
					break;
				}
				case CHAT:{
					setBottomNavigationMenuItemChecked(CHAT_BNV);
					break;
				}
				case CONTACTS:
				case SETTINGS:
				case SEARCH:
				case ACCOUNT:
				case TRANSFERS:
				case NOTIFICATIONS:
				case INBOX:{
					setBottomNavigationMenuItemChecked(HIDDEN_BNV);
					break;
				}
			}
		}
	}

	public void selectDrawerItemSharedItems(){
		logDebug("selectDrawerItemSharedItems");
		abL.setVisibility(View.VISIBLE);

		try {
			NotificationManager notificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.cancel(NOTIFICATION_PUSH_CLOUD_DRIVE);
		}
		catch (Exception e){
			logError("Exception NotificationManager - remove contact notification", e);
		}

		if (sharesPageAdapter == null){
			logWarning("sharesPageAdapter is NULL");
			sharesPageAdapter = new SharesPageAdapter(getSupportFragmentManager(),this);
			viewPagerShares.setAdapter(sharesPageAdapter);
			tabLayoutShares.setupWithViewPager(viewPagerShares);
			setSharesTabIcons(indexShares);

			//Force on CreateView, addTab do not execute onCreateView
			if (indexShares != ERROR_TAB) {
				logDebug("The index of the TAB Shares is: " + indexShares);
				if (viewPagerShares != null){
					switch (indexShares) {
						case INCOMING_TAB:
						case OUTGOING_TAB:
						case LINKS_TAB:
							viewPagerShares.setCurrentItem(indexShares);
							break;
					}
				}
				indexShares = ERROR_TAB;
			}
			else {
				//No bundle, no change of orientation
				logDebug("indexShares is NOT -1");
			}

		}

		setToolbarTitle();

		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	private void setSharesTabIcons(int tabSelected) {
    	if (tabLayoutShares == null
				|| tabLayoutShares.getTabAt(INCOMING_TAB) == null
				|| tabLayoutShares.getTabAt(OUTGOING_TAB) == null
				|| tabLayoutShares.getTabAt(LINKS_TAB) == null) {
    		return;
		}

    	// The TabLayout style sets the default icon tint
		switch (tabSelected) {
			case OUTGOING_TAB:
				tabLayoutShares.getTabAt(INCOMING_TAB).setIcon(R.drawable.ic_incoming_shares);
				tabLayoutShares.getTabAt(OUTGOING_TAB).setIcon(mutateIconSecondary(this, R.drawable.ic_outgoing_shares, R.color.red_600_red_300));
				tabLayoutShares.getTabAt(LINKS_TAB).setIcon(R.drawable.link_ic);
				break;
			case LINKS_TAB:
				tabLayoutShares.getTabAt(INCOMING_TAB).setIcon(R.drawable.ic_incoming_shares);
				tabLayoutShares.getTabAt(OUTGOING_TAB).setIcon(R.drawable.ic_outgoing_shares);
				tabLayoutShares.getTabAt(LINKS_TAB).setIcon(mutateIconSecondary(this, R.drawable.link_ic, R.color.red_600_red_300));
				break;
			default:
				tabLayoutShares.getTabAt(INCOMING_TAB).setIcon(mutateIconSecondary(this, R.drawable.ic_incoming_shares, R.color.red_600_red_300));
				tabLayoutShares.getTabAt(OUTGOING_TAB).setIcon(R.drawable.ic_outgoing_shares);
				tabLayoutShares.getTabAt(LINKS_TAB).setIcon(R.drawable.link_ic);
		}
	}

	public void selectDrawerItemContacts (){
		logDebug("selectDrawerItemContacts");
		abL.setVisibility(View.VISIBLE);

		try {
			ContactsAdvancedNotificationBuilder notificationBuilder;
			notificationBuilder =  ContactsAdvancedNotificationBuilder.newInstance(this, megaApi);

			notificationBuilder.removeAllIncomingContactNotifications();
			notificationBuilder.removeAllAcceptanceContactNotifications();
		}
		catch (Exception e){
			logError("Exception NotificationManager - remove all CONTACT notifications", e);
		}

		if (aB == null){
			aB = getSupportActionBar();
		}
		setToolbarTitle();

		if (contactsPageAdapter == null){
			logWarning("contactsPageAdapter == null");
			contactsPageAdapter = new ContactsPageAdapter(getSupportFragmentManager(),this);
			viewPagerContacts.setAdapter(contactsPageAdapter);
			tabLayoutContacts.setupWithViewPager(viewPagerContacts);

			logDebug("The index of the TAB CONTACTS is: " + indexContacts);
			if(indexContacts==-1) {
				logWarning("The index os contacts is -1");
				ArrayList<MegaContactRequest> requests = megaApi.getIncomingContactRequests();
				if(requests!=null) {
					int pendingRequest = requests.size();
					if (pendingRequest != 0) {
						indexContacts = 2;
					}
				}
			}

			if (viewPagerContacts != null) {
				switch (indexContacts){
					case SENT_REQUESTS_TAB:{
						viewPagerContacts.setCurrentItem(SENT_REQUESTS_TAB);
						logDebug("Select Sent Requests TAB");
						break;
					}
					case RECEIVED_REQUESTS_TAB:{
						viewPagerContacts.setCurrentItem(RECEIVED_REQUESTS_TAB);
						logDebug("Select Received Request TAB");
						break;
					}
					default:{
						viewPagerContacts.setCurrentItem(CONTACTS_TAB);
						logDebug("Select Contacts TAB");
						break;
					}
				}
			}
		}
		else {
			logDebug("contactsPageAdapter NOT null");
			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
			sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SENT_REQUESTS.getTag());
			rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECEIVED_REQUESTS.getTag());

			logDebug("The index of the TAB CONTACTS is: " + indexContacts);
			if (viewPagerContacts != null) {
				switch (indexContacts) {
					case SENT_REQUESTS_TAB: {
						viewPagerContacts.setCurrentItem(SENT_REQUESTS_TAB);
						logDebug("Select Sent Requests TAB");
						break;
					}
					case RECEIVED_REQUESTS_TAB: {
						viewPagerContacts.setCurrentItem(RECEIVED_REQUESTS_TAB);
						logDebug("Select Received Request TAB");
						break;
					}
					default: {
						viewPagerContacts.setCurrentItem(CONTACTS_TAB);
						logDebug("Select Contacts TAB");
						break;
					}
				}
			}
		}

		viewPagerContacts.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				indexContacts = position;
			}

			@Override
			public void onPageSelected(int position) {
				logDebug("onPageSelected");
				checkScrollElevation();
				indexContacts = position;
				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
				if(cFLol!=null){
					cFLol.hideMultipleSelect();
					cFLol.clearSelectionsNoAnimations();
				}
				sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SENT_REQUESTS.getTag());
				if(sRFLol!=null){
					sRFLol.clearSelections();
					sRFLol.hideMultipleSelect();
				}
				rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECEIVED_REQUESTS.getTag());
				if(rRFLol!=null){
					rRFLol.clearSelections();
					rRFLol.hideMultipleSelect();
				}
				supportInvalidateOptionsMenu();
				showFabButton();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		supportInvalidateOptionsMenu();
		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void selectDrawerItemAccount(){

		if(((MegaApplication) getApplication()).getMyAccountInfo()!=null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumVersions() == -1){
			megaApi.getFolderInfo(megaApi.getRootNode(), this);
		}

		switch(accountFragment){
			case UPGRADE_ACCOUNT_FRAGMENT:{
				showUpAF();
				break;
			}
			default:{
				app.refreshAccountInfo();
				accountFragment=MY_ACCOUNT_FRAGMENT;

				if (mTabsAdapterMyAccount == null){
					mTabsAdapterMyAccount = new MyAccountPageAdapter(getSupportFragmentManager(), this);
					viewPagerMyAccount.setAdapter(mTabsAdapterMyAccount);
					tabLayoutMyAccount.setupWithViewPager(viewPagerMyAccount);
				} else{
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_STORAGE.getTag());
				}

				if(viewPagerMyAccount != null) {
					switch (indexAccount){
						case STORAGE_TAB:{
							viewPagerMyAccount.setCurrentItem(STORAGE_TAB);
							break;
						}
						default:{
							indexAccount = GENERAL_TAB;
							viewPagerMyAccount.setCurrentItem(GENERAL_TAB);
							updateLogoutWarnings();
						}
					}
				}

				viewPagerMyAccount.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					}

					@Override
					public void onPageSelected(int position) {
						supportInvalidateOptionsMenu();
						checkScrollElevation();
					}

					@Override
					public void onPageScrollStateChanged(int state) {
					}
				});
				setToolbarTitle();
				supportInvalidateOptionsMenu();
				showFabButton();
				break;
			}
		}
	}

	public void selectDrawerItemNotifications(){
		logDebug("selectDrawerItemNotifications");

		abL.setVisibility(View.VISIBLE);

		drawerItem = DrawerItem.NOTIFICATIONS;

		setBottomNavigationMenuItemChecked(HIDDEN_BNV);

		notificFragment = (NotificationsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.NOTIFICATIONS.getTag());
		if (notificFragment == null){
			logWarning("New NotificationsFragment");
			notificFragment = NotificationsFragmentLollipop.newInstance();
		}
		else {
			refreshFragment(FragmentTag.NOTIFICATIONS.getTag());
		}
        replaceFragment(notificFragment, FragmentTag.NOTIFICATIONS.getTag());

		setToolbarTitle();

		showFabButton();
	}

	public void selectDrawerItemTransfers(){
		logDebug("selectDrawerItemTransfers");

		abL.setVisibility(View.VISIBLE);
        transfersWidget.hide();

		drawerItem = DrawerItem.TRANSFERS;

		setBottomNavigationMenuItemChecked(HIDDEN_BNV);

		if (mTabsAdapterTransfers == null) {
			mTabsAdapterTransfers = new TransfersPageAdapter(getSupportFragmentManager(), this);
			viewPagerTransfers.setAdapter(mTabsAdapterTransfers);
			tabLayoutTransfers.setupWithViewPager(viewPagerTransfers);
		}

		boolean showCompleted = !dbH.getCompletedTransfers().isEmpty() && transfersWidget.getPendingTransfers() <= 0;

		TransfersManagement transfersManagement = MegaApplication.getTransfersManagement();
		indexTransfers = transfersManagement.thereAreFailedTransfers() || showCompleted ? COMPLETED_TAB : PENDING_TAB;

		if (viewPagerTransfers != null) {
			switch (indexTransfers) {
				case COMPLETED_TAB:
					refreshFragment(FragmentTag.COMPLETED_TRANSFERS.getTag());
					viewPagerTransfers.setCurrentItem(COMPLETED_TAB);
					break;

				default:
					refreshFragment(FragmentTag.TRANSFERS.getTag());
					viewPagerTransfers.setCurrentItem(PENDING_TAB);

					if (transfersManagement.shouldShowNetWorkWarning()) {
						showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), MEGACHAT_INVALID_HANDLE);
					}

					break;
			}

			if(mTabsAdapterTransfers != null) {
				mTabsAdapterTransfers.notifyDataSetChanged();
			}

			indexTransfers = viewPagerTransfers.getCurrentItem();
		}

		setToolbarTitle();
		showFabButton();
		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void selectDrawerItemChat(){
		((MegaApplication)getApplication()).setRecentChatVisible(true);
		setToolbarTitle();

		rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
		if (rChatFL == null) {
			rChatFL = RecentChatsFragmentLollipop.newInstance();
		} else {
			refreshFragment(FragmentTag.RECENT_CHAT.getTag());
		}

		replaceFragment(rChatFL, FragmentTag.RECENT_CHAT.getTag());

		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void setBottomNavigationMenuItemChecked (int item) {
		if (bNV != null && bNV.getMenu() != null) {
			if(item == HIDDEN_BNV) {
				showHideBottomNavigationView(true);
			}
			else if (bNV.getMenu().getItem(item) != null) {
				if (!bNV.getMenu().getItem(item).isChecked()) {
					bNV.getMenu().getItem(item).setChecked(true);
				}
			}
		}
	}

	private void setTabsVisibility() {
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);
		mShowAnyTabLayout = false;

    	fragmentContainer.setVisibility(View.GONE);
    	mNavHostView.setVisibility(View.GONE);

		updatePsaViewVisibility();

    	if (turnOnNotifications) {
			fragmentContainer.setVisibility(View.VISIBLE);
			drawerLayout.closeDrawer(Gravity.LEFT);
			return;
		}

    	switch (drawerItem) {
			case SHARED_ITEMS: {
				int tabItemShares = getTabItemShares();

				if ((tabItemShares == INCOMING_TAB && parentHandleIncoming != INVALID_HANDLE)
						|| (tabItemShares == OUTGOING_TAB && parentHandleOutgoing != INVALID_HANDLE)
						|| (tabItemShares == LINKS_TAB && parentHandleLinks != INVALID_HANDLE)) {
					tabLayoutShares.setVisibility(View.GONE);
					viewPagerShares.disableSwipe(true);
				} else {
					tabLayoutShares.setVisibility(View.VISIBLE);
					viewPagerShares.disableSwipe(false);
				}

				viewPagerShares.setVisibility(View.VISIBLE);
				mShowAnyTabLayout = true;
				break;
			}
			case CONTACTS: {
				tabLayoutContacts.setVisibility(View.VISIBLE);
				viewPagerContacts.setVisibility(View.VISIBLE);
				mShowAnyTabLayout = true;
				break;
			}
			case ACCOUNT: {
				switch(accountFragment){
					case UPGRADE_ACCOUNT_FRAGMENT:
					case BACKUP_RECOVERY_KEY_FRAGMENT:{
						fragmentContainer.setVisibility(View.VISIBLE);
						break;
					}
					default:{
						tabLayoutMyAccount.setVisibility(View.VISIBLE);
						viewPagerMyAccount.setVisibility(View.VISIBLE);
						mShowAnyTabLayout = true;
						break;
					}
				}
				break;
			}
			case TRANSFERS: {
				tabLayoutTransfers.setVisibility(View.VISIBLE);
				viewPagerTransfers.setVisibility(View.VISIBLE);
				mShowAnyTabLayout = true;
				break;
			}
			case HOMEPAGE:
				mNavHostView.setVisibility(View.VISIBLE);
				break;
			default: {
				fragmentContainer.setVisibility(View.VISIBLE);
				break;
			}
		}

		LiveEventBus.get(EVENT_HOMEPAGE_VISIBILITY, Boolean.class)
				.post(drawerItem == DrawerItem.HOMEPAGE);

		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	/**
	 * Hides or shows tabs of a section depending on the navigation level
	 * and if select mode is enabled or not.
	 *
	 * @param hide       If true, hides the tabs, else shows them.
	 * @param currentTab The current tab where the action happens.
	 */
	public void hideTabs(boolean hide, int currentTab) {
		int visibility = hide ? View.GONE : View.VISIBLE;

		switch (drawerItem) {
			case SHARED_ITEMS:
				switch (currentTab) {
					case INCOMING_TAB:
						if (!isIncomingAdded() || (!hide && parentHandleIncoming != INVALID_HANDLE)) {
							return;
						}

						break;

					case OUTGOING_TAB:
						if (!isOutgoingAdded() || (!hide && parentHandleOutgoing != INVALID_HANDLE)) {
							return;
						}

						break;

					case LINKS_TAB:
						if (!isLinksAdded() || (!hide && parentHandleLinks != INVALID_HANDLE)) {
							return;
						}

						break;
				}

				tabLayoutShares.setVisibility(visibility);
				viewPagerShares.disableSwipe(hide);
				break;

			case CONTACTS:
				switch (currentTab) {
					case CONTACTS_TAB:
						if (!isContactsAdded()) return;
						else break;

					case SENT_REQUESTS_TAB:
						if (!isSentRequestAdded()) return;
						else break;

					case RECEIVED_REQUESTS_TAB:
						if (!isReceivedRequestAdded()) return;
						else break;
				}

				tabLayoutContacts.setVisibility(visibility);
				viewPagerContacts.disableSwipe(hide);
				break;

			case TRANSFERS:
				if (currentTab == PENDING_TAB && !isTransfersInProgressAdded()) {
					return;
				}

				tabLayoutTransfers.setVisibility(visibility);
				viewPagerTransfers.disableSwipe(hide);
				break;
		}
	}

	private void removeFragment(Fragment fragment) {
		if (fragment != null && fragment.isAdded()) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.remove(fragment);
			ft.commitAllowingStateLoss();
		}
	}

	/**
	 * Set up a listener for navigating to a new destination (screen)
	 * This only for Homepage for the time being since it is the only module to
	 * which Jetpack Navigation applies.
	 * It updates the status variable such as mHomepageScreen, as well as updating
	 * BNV, Toolbar title, etc.
	 */
	private void setupNavDestListener() {
		mNavController = Navigation.findNavController(mNavHostView);

		mNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
			int destinationId = destination.getId();
			mHomepageSearchable = null;

			if (destinationId == R.id.homepageFragment) {
				mHomepageScreen = HomepageScreen.HOMEPAGE;
				updatePsaViewVisibility();
				// Showing the bottom navigation view immediately because the initial dimension
				// of Homepage bottom sheet is calculated based on it
				showBNVImmediate();
				if (bottomNavigationCurrentItem == HOMEPAGE_BNV) {
					abL.setVisibility(View.GONE);
				}
				setDrawerLockMode(false);
				return;
			} else if (destinationId == R.id.photosFragment) {
				mHomepageScreen = HomepageScreen.PHOTOS;
			} else if (destinationId == R.id.documentsFragment) {
				mHomepageScreen = HomepageScreen.DOCUMENTS;
			} else if (destinationId == R.id.audioFragment) {
				mHomepageScreen = HomepageScreen.AUDIO;
			} else if (destinationId == R.id.videoFragment) {
				mHomepageScreen = HomepageScreen.VIDEO;
			} else if (destinationId == R.id.fullscreen_offline) {
				mHomepageScreen = HomepageScreen.FULLSCREEN_OFFLINE;
			} else if (destinationId == R.id.offline_file_info) {
				mHomepageScreen = HomepageScreen.OFFLINE_FILE_INFO;
				updatePsaViewVisibility();
				abL.setVisibility(View.GONE);
				showHideBottomNavigationView(true);
				return;
			} else if (destinationId == R.id.recentBucketFragment) {
				mHomepageScreen = HomepageScreen.RECENT_BUCKET;
			}

			updatePsaViewVisibility();
			abL.setVisibility(View.VISIBLE);
			showHideBottomNavigationView(true);
			supportInvalidateOptionsMenu();
			setToolbarTitle();
			setDrawerLockMode(true);
		});
	}

	/**
	 * Hides all views only related to CU section and sets the CU default view.
	 */
	private void resetCUFragment() {
		cuLayout.setVisibility(View.GONE);
		cuViewTypes.setVisibility(View.GONE);

		if (getCameraUploadFragment() != null) {
			cuFragment.setDefaultView();
			showBottomView();
		}
	}

	@SuppressLint("NewApi")
	public void selectDrawerItemLollipop(DrawerItem item) {
    	if (item == null) {
    		logWarning("The selected DrawerItem is NULL. Using latest or default value.");
    		item = drawerItem != null ? drawerItem : DrawerItem.CLOUD_DRIVE;
		}

    	logDebug("Selected DrawerItem: " + item.name());

    	// Homepage may hide the Appbar before
		abL.setVisibility(View.VISIBLE);

    	drawerItem = item;
		MegaApplication.setRecentChatVisible(false);
		resetActionBar(aB);
		transfersWidget.update();
		setCallWidget();

		if (item != DrawerItem.CHAT) {
			//remove recent chat fragment as its life cycle get triggered unexpectedly, e.g. rotate device while not on recent chat page
			removeFragment(getChatsFragment());
		}

		if (item != DrawerItem.CAMERA_UPLOADS) {
			resetCUFragment();
		}

		if (item != DrawerItem.TRANSFERS && isTransfersInProgressAdded()) {
			tFLol.checkSelectModeAfterChangeTabOrDrawerItem();
		}

		MegaApplication.getTransfersManagement().setIsOnTransfersSection(item == DrawerItem.TRANSFERS);

    	switch (item){
			case CLOUD_DRIVE:{
				selectDrawerItemCloudDrive();
				if (openFolderRefresh){
					onNodesCloudDriveUpdate();
					openFolderRefresh = false;
				}
    			supportInvalidateOptionsMenu();
				setToolbarTitle();
				showFabButton();
				showHideBottomNavigationView(false);
				if (!comesFromNotifications) {
					bottomNavigationCurrentItem = CLOUD_DRIVE_BNV;
				}
				setBottomNavigationMenuItemChecked(CLOUD_DRIVE_BNV);
				logDebug("END for Cloud Drive");
    			break;
    		}
			case RUBBISH_BIN:{
				showHideBottomNavigationView(true);
				abL.setVisibility(View.VISIBLE);
				rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
				if (rubbishBinFLol == null) {
					rubbishBinFLol = RubbishBinFragmentLollipop.newInstance();
				}

				setBottomNavigationMenuItemChecked(HIDDEN_BNV);

				replaceFragment(rubbishBinFLol, FragmentTag.RUBBISH_BIN.getTag());

				if (openFolderRefresh){
					onNodesCloudDriveUpdate();
					openFolderRefresh = false;
				}
				supportInvalidateOptionsMenu();
				setToolbarTitle();
				showFabButton();
				break;
			}
			case HOMEPAGE: {
			    // Don't use fabButton.hide() here.
			    fabButton.setVisibility(View.GONE);
				if (mHomepageScreen == HomepageScreen.HOMEPAGE) {
					showBNVImmediate();
					abL.setVisibility(View.GONE);
                    showHideBottomNavigationView(false);
				} else {
					// For example, back from Rubbish Bin to Photos
					setToolbarTitle();
					invalidateOptionsMenu();
                    showHideBottomNavigationView(true);
				}

				setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);

				if (!comesFromNotifications) {
					bottomNavigationCurrentItem = HOMEPAGE_BNV;
				}

				showGlobalAlertDialogsIfNeeded();
				break;
			}
    		case CAMERA_UPLOADS: {
				abL.setVisibility(View.VISIBLE);

				if (getCameraUploadFragment() == null) {
					cuFragment = new CameraUploadsFragment();
				} else {
					refreshFragment(FragmentTag.CAMERA_UPLOADS.getTag());
				}

				cuFragment.setViewTypes(cuViewTypes, cuYearsButton, cuMonthsButton, cuDaysButton, cuAllButton);
				replaceFragment(cuFragment, FragmentTag.CAMERA_UPLOADS.getTag());
				setToolbarTitle();
				supportInvalidateOptionsMenu();
				showFabButton();
				showHideBottomNavigationView(false);
				if (!comesFromNotifications) {
					bottomNavigationCurrentItem = CAMERA_UPLOADS_BNV;
				}
				setBottomNavigationMenuItemChecked(CAMERA_UPLOADS_BNV);

				break;
    		}
    		case INBOX:{
				showHideBottomNavigationView(true);
				abL.setVisibility(View.VISIBLE);
				iFLol = (InboxFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.INBOX.getTag());
				if (iFLol == null) {
					iFLol = InboxFragmentLollipop.newInstance();
				}

				replaceFragment(iFLol, FragmentTag.INBOX.getTag());

				if (openFolderRefresh){
					onNodesInboxUpdate();
					openFolderRefresh = false;
				}
    			supportInvalidateOptionsMenu();
				setToolbarTitle();
				showFabButton();
    			break;
    		}
    		case SHARED_ITEMS:{
				selectDrawerItemSharedItems();
				if (openFolderRefresh){
					onNodesSharedUpdate();
					openFolderRefresh = false;
				}
    			supportInvalidateOptionsMenu();

				showFabButton();
				showHideBottomNavigationView(false);
				if (!comesFromNotifications) {
					bottomNavigationCurrentItem = SHARED_BNV;
				}
				setBottomNavigationMenuItemChecked(SHARED_BNV);
    			break;
    		}
    		case CONTACTS:{
				showHideBottomNavigationView(true);
				selectDrawerItemContacts();
				showFabButton();
    			break;
    		}
			case NOTIFICATIONS:{
				showHideBottomNavigationView(true);
				selectDrawerItemNotifications();
				supportInvalidateOptionsMenu();
				showFabButton();
				break;
			}
    		case SETTINGS:{
				showHideBottomNavigationView(true);
				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumVersions() == -1){
					megaApi.getFolderInfo(megaApi.getRootNode(), this);
				}

				aB.setSubtitle(null);
				abL.setVisibility(View.VISIBLE);

    			supportInvalidateOptionsMenu();

    			if (getSettingsFragment() != null){
					if (openSettingsStorage){
						sttFLol.goToCategoryStorage();
					}
					else if (openSettingsQR){
						logDebug ("goToCategoryQR");
						sttFLol.goToCategoryQR();
					}
				}
				else {
					sttFLol = new SettingsFragmentLollipop();
				}

				replaceFragment(sttFLol, FragmentTag.SETTINGS.getTag());

				setToolbarTitle();
				supportInvalidateOptionsMenu();
				showFabButton();

				if (sttFLol != null){
					sttFLol.update2FAVisibility();
				}
				break;
    		}
    		case SEARCH:{
				showHideBottomNavigationView(true);

				setBottomNavigationMenuItemChecked(HIDDEN_BNV);

    			drawerItem = DrawerItem.SEARCH;
				if (getSearchFragment() == null) {
					sFLol = SearchFragmentLollipop.newInstance();
				}

				replaceFragment(sFLol, FragmentTag.SEARCH.getTag());
				showFabButton();

    			break;
    		}
			case ACCOUNT:{
				showHideBottomNavigationView(true);
				logDebug("Case ACCOUNT: " + accountFragment);
//    			tB.setVisibility(View.GONE);
				aB.setSubtitle(null);
				selectDrawerItemAccount();
				supportInvalidateOptionsMenu();
				break;
			}
    		case TRANSFERS:{
				showHideBottomNavigationView(true);
				aB.setSubtitle(null);
				selectDrawerItemTransfers();
    			supportInvalidateOptionsMenu();
				showFabButton();
    			break;
    		}
			case CHAT:{
				logDebug("Chat selected");
				if (megaApi != null) {
					contacts = megaApi.getContacts();
					for (int i=0;i<contacts.size();i++){
						if (contacts.get(i).getVisibility() == MegaUser.VISIBILITY_VISIBLE){
							visibleContacts.add(contacts.get(i));
						}
					}
				}
				selectDrawerItemChat();
				supportInvalidateOptionsMenu();
				showHideBottomNavigationView(false);
				if (!comesFromNotifications) {
					bottomNavigationCurrentItem = CHAT_BNV;
				}
				setBottomNavigationMenuItemChecked(CHAT_BNV);
				break;
			}
    	}

    	setTabsVisibility();
    	checkScrollElevation();

		if (megaApi.multiFactorAuthAvailable()) {
			if (newAccount || isEnable2FADialogShown) {
				showEnable2FADialog();
			}
		}
	}

	public void openFullscreenOfflineFragment(String path) {
		drawerItem = DrawerItem.HOMEPAGE;
    	mNavController.navigate(
    			HomepageFragmentDirections.Companion.actionHomepageToFullscreenOffline(path, false),
				new NavOptions.Builder().setLaunchSingleTop(true).build());
	}

	public void fullscreenOfflineFragmentOpened(OfflineFragment fragment) {
    	fullscreenOfflineFragment = fragment;

		showFabButton();
		setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);
		abL.setVisibility(View.VISIBLE);
		setToolbarTitle();
		supportInvalidateOptionsMenu();
	}

	public void fullscreenOfflineFragmentClosed(OfflineFragment fragment) {
		if (fragment == fullscreenOfflineFragment) {
			if (drawerItemBeforeOpenFullscreenOffline != null && !mStopped) {
				if (drawerItem != drawerItemBeforeOpenFullscreenOffline) {
					selectDrawerItemLollipop(drawerItemBeforeOpenFullscreenOffline);
				}
				drawerItemBeforeOpenFullscreenOffline = null;
			}

			setPathNavigationOffline("/");
			fullscreenOfflineFragment = null;
			// workaround for flicker of AppBarLayout: if we go back to homepage from fullscreen
			// offline, and hide AppBarLayout when immediately on go back, we will see the flicker
			// of AppBarLayout, hide AppBarLayout when fullscreen offline is closed is better.
			if (bottomNavigationCurrentItem == HOMEPAGE_BNV
                    && mHomepageScreen == HomepageScreen.HOMEPAGE) {
				abL.setVisibility(View.GONE);
			}
		}
	}

	public void pagerOfflineFragmentOpened(OfflineFragment fragment) {
    	pagerOfflineFragment = fragment;
	}

	public void pagerOfflineFragmentClosed(OfflineFragment fragment) {
    	if (fragment == pagerOfflineFragment) {
			pagerOfflineFragment = null;
		}
	}

	public void pagerRecentsFragmentOpened(RecentsFragment fragment) {
    	pagerRecentsFragment = fragment;
	}

	public void pagerRecentsFragmentClosed(RecentsFragment fragment) {
    	if (fragment == pagerRecentsFragment) {
			pagerRecentsFragment = null;
		}
	}

	private void showBNVImmediate() {
		updateMiniAudioPlayerVisibility(true);

		bNV.setTranslationY(0);
		bNV.setVisibility(View.VISIBLE);
		final CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.setMargins(0, 0, 0,
				getResources().getDimensionPixelSize(R.dimen.bottom_navigation_view_height));
		fragmentLayout.setLayoutParams(params);
	}

	/**
	 * Update whether we should display the mini audio player. It should only
	 * be visible when BNV is visible.
	 *
	 * @param shouldVisible whether we should display the mini audio player
	 * @return is the mini player visible after this update
	 */
	private boolean updateMiniAudioPlayerVisibility(boolean shouldVisible) {
		if (miniAudioPlayerController != null) {
			miniAudioPlayerController.setShouldVisible(shouldVisible);

			handler.post(this::updateHomepageFabPosition);

			return miniAudioPlayerController.visible();
		}

		return false;
	}

	/**
	 * Update homepage FAB position, considering the visibility of PSA layout and mini audio player.
	 */
	private void updateHomepageFabPosition() {
		HomepageFragment fragment = getFragmentByType(HomepageFragment.class);
		if (drawerItem == DrawerItem.HOMEPAGE && mHomepageScreen == HomepageScreen.HOMEPAGE && fragment != null) {
			fragment.updateFabPosition(psaViewHolder.visible() ? psaViewHolder.psaLayoutHeight() : 0,
					miniAudioPlayerController.visible() ? miniAudioPlayerController.playerHeight() : 0);
		}
	}

	private boolean isCloudAdded () {
        fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CLOUD_DRIVE.getTag());
        return fbFLol != null && fbFLol.isAdded();
    }

	private boolean isContactsAdded() {
		return getContactsFragment() != null && cFLol.isAdded();
	}

	private boolean isSentRequestAdded() {
		return getSentRequestFragment() != null && sRFLol.isAdded();
	}

	private boolean isReceivedRequestAdded() {
		return getReceivedRequestFragment() != null && rRFLol.isAdded();
	}

	private boolean isIncomingAdded () {
    	if (sharesPageAdapter == null) return false;

		inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, INCOMING_TAB);

    	return inSFLol != null && inSFLol.isAdded();
	}

	private boolean isOutgoingAdded() {
    	if (sharesPageAdapter == null) return false;

		outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, OUTGOING_TAB);

		return outSFLol != null && outSFLol.isAdded();
	}

	private boolean isLinksAdded() {
    	if (sharesPageAdapter == null) return false;

    	lF = (LinksFragment) sharesPageAdapter.instantiateItem(viewPagerShares, LINKS_TAB);

    	return lF != null && lF.isAdded();
	}

	private boolean isTransfersInProgressAdded() {
		if (mTabsAdapterTransfers == null) return false;

		tFLol = (TransfersFragmentLollipop) mTabsAdapterTransfers.instantiateItem(viewPagerTransfers, PENDING_TAB);

		return tFLol.isAdded();
	}

	private boolean isTransfersCompletedAdded() {
		if (mTabsAdapterTransfers == null) return false;

		completedTFLol = (CompletedTransfersFragmentLollipop) mTabsAdapterTransfers.instantiateItem(viewPagerTransfers, COMPLETED_TAB);

		return completedTFLol.isAdded();
	}

    public void checkScrollElevation() {
    	if(drawerItem==null){
    		return;
		}

        switch (drawerItem) {
            case CLOUD_DRIVE: {
            	fbFLol.checkScroll();
                break;
            }
			case HOMEPAGE: {
				if (fullscreenOfflineFragment != null) {
					fullscreenOfflineFragment.checkScroll();
				}
				break;
			}
            case CAMERA_UPLOADS: {
                if (getCameraUploadFragment() != null) {
					cuFragment.checkScroll();
                }
                break;
            }
            case INBOX: {
            	iFLol = (InboxFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.INBOX.getTag());
                if (iFLol != null) {
                    iFLol.checkScroll();
                }
                break;
            }
            case SHARED_ITEMS: {
            	if (getTabItemShares() == INCOMING_TAB && isIncomingAdded()) inSFLol.checkScroll();
            	else if (getTabItemShares() == OUTGOING_TAB && isOutgoingAdded()) outSFLol.checkScroll();
            	else if (getTabItemShares() == LINKS_TAB && isLinksAdded()) lF.checkScroll();
                break;
            }
            case CONTACTS: {
				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
				rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECEIVED_REQUESTS.getTag());
				sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SENT_REQUESTS.getTag());
                if (getTabItemContacts() == CONTACTS_TAB && cFLol != null) {
                    cFLol.checkScroll();
                }
                else if (getTabItemContacts() == SENT_REQUESTS_TAB && sRFLol != null) {
                    sRFLol.checkScroll();
                }
                else if (getTabItemContacts() == RECEIVED_REQUESTS_TAB && rRFLol != null) {
                    rRFLol.checkScroll();
                }
                break;
            }
            case SETTINGS: {
                if (getSettingsFragment() != null) {
                    sttFLol.checkScroll();
                }
                break;
            }
            case ACCOUNT: {
				mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_STORAGE.getTag());
				maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
                if (getTabItemMyAccount() == GENERAL_TAB && maFLol != null) {
                    maFLol.checkScroll();
                }
                else if (getTabItemMyAccount() == STORAGE_TAB && mStorageFLol != null) {
                    mStorageFLol.checkScroll();
                }
                break;
            }
            case SEARCH: {
				if (getSearchFragment() != null) {
                    sFLol.checkScroll();
                }
                break;
            }
            case CHAT: {
				rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
                if (rChatFL != null) {
                    rChatFL.checkScroll();
                }
                break;
            }
            case RUBBISH_BIN: {
				rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
                if (rubbishBinFLol != null) {
                    rubbishBinFLol.checkScroll();
                }
                break;
            }

			case TRANSFERS: {
				if (getTabItemTransfers() == PENDING_TAB && isTransfersInProgressAdded()) {
					tFLol.checkScroll();
				} else  if (getTabItemTransfers() == COMPLETED_TAB && isTransfersCompletedAdded()) {
					completedTFLol.checkScroll();
				}
			}
        }
    }


    void showEnable2FADialog () {
		logDebug ("newAccount: "+newAccount);
		newAccount = false;

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_enable_2fa_create_account, null);
		builder.setView(v);

		enable2FAButton = (Button) v.findViewById(R.id.enable_2fa_button);
		enable2FAButton.setOnClickListener(this);
		skip2FAButton = (Button) v.findViewById(R.id.skip_enable_2fa_button);
		skip2FAButton.setOnClickListener(this);

		enable2FADialog = builder.create();
		enable2FADialog.setCanceledOnTouchOutside(false);
		try {
			enable2FADialog.show();
		}catch (Exception e){};
		isEnable2FADialogShown = true;
	}

	public void moveToSettingsSection(){
		drawerItem=DrawerItem.SETTINGS;
		selectDrawerItemLollipop(drawerItem);
	}

	public void moveToSettingsSectionStorage(){
		openSettingsStorage = true;
		drawerItem=DrawerItem.SETTINGS;
		selectDrawerItemLollipop(drawerItem);
	}

	public void moveToSettingsSectionQR(){
		openSettingsQR = true;
		drawerItem=DrawerItem.SETTINGS;
		selectDrawerItemLollipop(drawerItem);
	}

	/**
	 * Resets the scroll of settings page
	 */
	public void resetSettingsScrollIfNecessary() {
		openSettingsStorage = false;
		openSettingsQR = false;

		if (getSettingsFragment() != null) {
			sttFLol.goToFirstCategory();
		}
	}

	public void moveToChatSection (long idChat) {
		if (idChat != -1) {
			Intent intent = new Intent(this, ChatActivityLollipop.class);
			intent.setAction(ACTION_CHAT_SHOW_MESSAGES);
			intent.putExtra(CHAT_ID, idChat);
			this.startActivity(intent);
		}
    	drawerItem = DrawerItem.CHAT;
    	selectDrawerItemLollipop(drawerItem);
	}

	public void showMyAccount(){
		drawerItem = DrawerItem.ACCOUNT;
		selectDrawerItemLollipop(drawerItem);
	}

	public void updateInfoNumberOfSubscriptions(){
        if (cancelSubscription != null){
            cancelSubscription.setVisible(false);
        }
        if (((MegaApplication) getApplication()).getMyAccountInfo()!= null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumberOfSubscriptions() > 0){
            if (cancelSubscription != null){
                if (drawerItem == DrawerItem.ACCOUNT){
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
                    if (maFLol != null){
                        cancelSubscription.setVisible(true);
                    }
                }
            }
        }
    }

	public void showUpAF() {
		logDebug("showUpAF");
		accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
		setToolbarTitle();
		upAFL = new UpgradeAccountFragmentLollipop();
		replaceFragment(upAFL, FragmentTag.UPGRADE_ACCOUNT.getTag());
		setTabsVisibility();
		supportInvalidateOptionsMenu();
		showFabButton();
	}

	private void closeSearchSection() {
    	searchQuery = "";
		drawerItem = searchDrawerItem;
		selectDrawerItemLollipop(drawerItem);
		searchDrawerItem = null;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		logDebug("onCreateOptionsMenuLollipop");
		// Force update the toolbar title to make the the tile length to be updated
		setToolbarTitle();
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_manager, menu);

		searchMenuItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) searchMenuItem.getActionView();

		SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
		searchAutoComplete.setHint(getString(R.string.hint_action_search));
		View v = searchView.findViewById(androidx.appcompat.R.id.search_plate);
		v.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

		if (searchView != null) {
			searchView.setIconifiedByDefault(true);
		}

		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				logDebug("onMenuItemActionExpand");
                searchQuery = "";
                searchExpand = true;
                if (drawerItem == DrawerItem.HOMEPAGE) {
                    if (mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE) {
                        setFullscreenOfflineFragmentSearchQuery(searchQuery);
                    } else if (mHomepageSearchable != null) {
                        mHomepageSearchable.searchReady();
                    } else {
                        openSearchOnHomepage();
                    }
                } else if (drawerItem != DrawerItem.CHAT) {
                    textsearchQuery = false;
                    firstNavigationLevel = true;
                    parentHandleSearch = -1;
                    levelsSearch = -1;
                    setSearchDrawerItem();
                    selectDrawerItemLollipop(drawerItem);
                } else {
                    resetActionBar(aB);
                }
				hideCallMenuItem(chronometerMenuItem, returnCallMenuItem);
				hideCallWidget(ManagerActivityLollipop.this, callInProgressChrono, callInProgressLayout);
                return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				logDebug("onMenuItemActionCollapse()");
				searchExpand = false;
				setCallWidget();
				setCallMenuItem(returnCallMenuItem, layoutCallMenuItem, chronometerMenuItem);
				if (drawerItem == DrawerItem.CHAT) {
					if (getChatsFragment() != null) {
						rChatFL.closeSearch();
						rChatFL.setCustomisedActionBar();
						supportInvalidateOptionsMenu();
					}
				} else if (drawerItem == DrawerItem.HOMEPAGE) {
					if (mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE) {
						if (!textSubmitted) {
							setFullscreenOfflineFragmentSearchQuery(null);
							textSubmitted = true;
						}
						supportInvalidateOptionsMenu();
					} else if (mHomepageSearchable != null) {
						mHomepageSearchable.exitSearch();
						searchQuery = "";
						supportInvalidateOptionsMenu();
					}
				} else {
						cancelSearch();
						textSubmitted = true;
						closeSearchSection();
				}
				return true;
			}
		});

		searchView.setMaxWidth(Integer.MAX_VALUE);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (drawerItem == DrawerItem.CHAT) {
					hideKeyboard(managerActivity, 0);
				} else if (drawerItem == DrawerItem.HOMEPAGE) {
					if (mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE) {
						searchExpand = false;
						textSubmitted = true;
						hideKeyboard(managerActivity, 0);
						if (fullscreenOfflineFragment != null) {
							fullscreenOfflineFragment.onSearchQuerySubmitted();
						}
						setToolbarTitle();
						supportInvalidateOptionsMenu();
					} else {
						hideKeyboard(ManagerActivityLollipop.this);
					}
				} else {
					searchExpand = false;
					searchQuery = "" + query;
					setToolbarTitle();
					logDebug("Search query: " + query);
					textSubmitted = true;
					supportInvalidateOptionsMenu();
				}
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				logDebug("onQueryTextChange");
				if (drawerItem == DrawerItem.CHAT) {
					searchQuery = newText;
					rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
					if (rChatFL != null) {
						rChatFL.filterChats(newText);
					}
				} else if (drawerItem == DrawerItem.HOMEPAGE) {
					if (mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE) {
						if (textSubmitted) {
							textSubmitted = false;
							return true;
						}

						searchQuery = newText;
						setFullscreenOfflineFragmentSearchQuery(searchQuery);
					} else if (mHomepageSearchable != null) {
						searchQuery = newText;
						mHomepageSearchable.searchQuery(searchQuery);
					}
				} else {
					if (textSubmitted) {
						textSubmitted = false;
					} else {
						if (!textsearchQuery) {
							searchQuery = newText;
						}
						if (getSearchFragment() != null) {
							sFLol.newSearchNodesTask();
						}
					}
				}
				return true;
			}
		});

		gridSmallLargeMenuItem = menu.findItem(R.id.action_grid_view_large_small);
		addContactMenuItem = menu.findItem(R.id.action_add_contact);
		addMenuItem = menu.findItem(R.id.action_add);
		createFolderMenuItem = menu.findItem(R.id.action_new_folder);
		importLinkMenuItem = menu.findItem(R.id.action_import_link);
		enableSelectMenuItem = menu.findItem(R.id.action_enable_select);
		selectMenuItem = menu.findItem(R.id.action_select);
		unSelectMenuItem = menu.findItem(R.id.action_unselect);
		thumbViewMenuItem = menu.findItem(R.id.action_grid);
		refreshMenuItem = menu.findItem(R.id.action_menu_refresh);
		sortByMenuItem = menu.findItem(R.id.action_menu_sort_by);
		helpMenuItem = menu.findItem(R.id.action_menu_help);
		doNotDisturbMenuItem = menu.findItem(R.id.action_menu_do_not_disturb);
		upgradeAccountMenuItem = menu.findItem(R.id.action_menu_upgrade_account);
		clearRubbishBinMenuitem = menu.findItem(R.id.action_menu_clear_rubbish_bin);
		cancelAllTransfersMenuItem = menu.findItem(R.id.action_menu_cancel_all_transfers);
		clearCompletedTransfers = menu.findItem(R.id.action_menu_clear_completed_transfers);
		retryTransfers = menu.findItem(R.id.action_menu_retry_transfers);
		playTransfersMenuIcon = menu.findItem(R.id.action_play);
		pauseTransfersMenuIcon = menu.findItem(R.id.action_pause);
		scanQRcodeMenuItem = menu.findItem(R.id.action_scan_qr);
		takePicture = menu.findItem(R.id.action_take_picture);
		cancelSubscription = menu.findItem(R.id.action_menu_cancel_subscriptions);
		exportMK = menu.findItem(R.id.action_menu_export_MK);
		killAllSessions = menu.findItem(R.id.action_menu_kill_all_sessions);
		logoutMenuItem = menu.findItem(R.id.action_menu_logout);
		forgotPassMenuItem = menu.findItem(R.id.action_menu_forgot_pass);
		inviteMenuItem = menu.findItem(R.id.action_menu_invite);
		returnCallMenuItem = menu.findItem(R.id.action_return_call);
		RelativeLayout rootView = (RelativeLayout) returnCallMenuItem.getActionView();
		layoutCallMenuItem = rootView.findViewById(R.id.layout_menu_call);
		chronometerMenuItem = rootView.findViewById(R.id.chrono_menu);
		chronometerMenuItem.setVisibility(View.GONE);

		rootView.setOnClickListener(v1 -> onOptionsItemSelected(returnCallMenuItem));

		changePass = menu.findItem(R.id.action_menu_change_pass);

		if (bNV != null) {
			Menu bNVMenu = bNV.getMenu();
			if (bNVMenu != null) {
				if (drawerItem == null) {
					drawerItem = DrawerItem.CLOUD_DRIVE;
				}

				if (drawerItem == DrawerItem.CLOUD_DRIVE) {
					setBottomNavigationMenuItemChecked(CLOUD_DRIVE_BNV);
				}
			}
		}

		setCallMenuItem(returnCallMenuItem, layoutCallMenuItem, chronometerMenuItem);

		if (isOnline(this)) {
			switch (drawerItem) {
				case CLOUD_DRIVE:
					upgradeAccountMenuItem.setVisible(true);
					importLinkMenuItem.setVisible(true);
					addMenuItem.setEnabled(true);
					addMenuItem.setVisible(true);
					takePicture.setVisible(true);

					createFolderMenuItem.setVisible(true);
                    if (isCloudAdded() && fbFLol.getItemCount() > 0) {
                        thumbViewMenuItem.setVisible(true);
                        setGridListIcon();
                        searchMenuItem.setVisible(true);
                        sortByMenuItem.setVisible(true);
                    }
					break;
				case HOMEPAGE:
					if (mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE) {
						updateFullscreenOfflineFragmentOptionMenu(true);
					}
					break;
				case RUBBISH_BIN:
					if (getRubbishBinFragment() != null && rubbishBinFLol.getItemCount() > 0) {
						thumbViewMenuItem.setVisible(true);
						setGridListIcon();
						clearRubbishBinMenuitem.setVisible(true);
						sortByMenuItem.setVisible(true);
						searchMenuItem.setVisible(true);
					}
					break;
				case CAMERA_UPLOADS:
					gridSmallLargeMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

					updateCuFragmentOptionsMenu();
					break;

				case INBOX:
					if (getInboxFragment() != null && iFLol.getItemCount() > 0) {
						selectMenuItem.setVisible(true);
						sortByMenuItem.setVisible(true);
						searchMenuItem.setVisible(true);
						thumbViewMenuItem.setVisible(true);
						setGridListIcon();
					}
					break;

				case SHARED_ITEMS:
					if (getTabItemShares() == INCOMING_TAB && isIncomingAdded()) {
						addMenuItem.setEnabled(true);

						if (isIncomingAdded() && inSFLol.getItemCount() > 0) {
							thumbViewMenuItem.setVisible(true);
							setGridListIcon();
							sortByMenuItem.setVisible(true);
							searchMenuItem.setVisible(true);

							if (parentHandleIncoming != INVALID_HANDLE) {
								MegaNode node = megaApi.getNodeByHandle(parentHandleIncoming);
								if (node != null) {
									int accessLevel = megaApi.getAccess(node);

									switch (accessLevel) {
										case MegaShare.ACCESS_OWNER:
										case MegaShare.ACCESS_READWRITE:
										case MegaShare.ACCESS_FULL: {
											addMenuItem.setVisible(true);
											createFolderMenuItem.setVisible(true);
											break;
										}
									}
								}
							}
						}
					} else if (getTabItemShares() == OUTGOING_TAB && isOutgoingAdded()) {
						if (parentHandleOutgoing != INVALID_HANDLE) {
							addMenuItem.setVisible(true);
							createFolderMenuItem.setVisible(true);
						}

						if (isOutgoingAdded() && outSFLol.getItemCount() > 0) {
							thumbViewMenuItem.setVisible(true);
							setGridListIcon();
							sortByMenuItem.setVisible(true);
							searchMenuItem.setVisible(true);
						}
					} else if (getTabItemShares() == LINKS_TAB && isLinksAdded()) {
						if (isLinksAdded() && lF.getItemCount() > 0) {
							sortByMenuItem.setVisible(true);
							searchMenuItem.setVisible(true);
						}
					}
					break;

				case CONTACTS:
					if (getTabItemContacts() == CONTACTS_TAB) {
						scanQRcodeMenuItem.setVisible(true);
						addContactMenuItem.setVisible(true);

						if (getContactsFragment() != null && cFLol.getItemCount() > 0) {
							thumbViewMenuItem.setVisible(true);
							setGridListIcon();
							sortByMenuItem.setVisible(true);

						}

						if (handleInviteContact != -1 && cFLol != null) {
							cFLol.invite(handleInviteContact);
						}
					} else if (getTabItemContacts() == SENT_REQUESTS_TAB) {
						addContactMenuItem.setVisible(true);
						upgradeAccountMenuItem.setVisible(true);
						scanQRcodeMenuItem.setVisible(true);
					}
					break;

				case SEARCH:
					if (searchExpand) {
						openSearchView();
						sFLol.checkSelectMode();
					} else if (getSearchFragment() != null
							&& getSearchFragment().getNodes() != null
							&& getSearchFragment().getNodes().size() > 0) {
						sortByMenuItem.setVisible(true);
						thumbViewMenuItem.setVisible(true);
						setGridListIcon();
					}
					break;

				case ACCOUNT:
					if (accountFragment == MY_ACCOUNT_FRAGMENT) {
						refreshMenuItem.setVisible(true);
						killAllSessions.setVisible(true);
						upgradeAccountMenuItem.setVisible(true);
						changePass.setVisible(true);
						logoutMenuItem.setVisible(true);

						if (getTabItemMyAccount() == GENERAL_TAB) {
							exportMK.setVisible(true);
						}

						if (app.getMyAccountInfo() != null && app.getMyAccountInfo().getNumberOfSubscriptions() > 0) {
							cancelSubscription.setVisible(true);
						}
					} else {
						refreshMenuItem.setVisible(true);
						logoutMenuItem.setVisible(true);
					}
					break;

				case TRANSFERS:
					if (getTabItemTransfers() == PENDING_TAB && isTransfersInProgressAdded() && transfersInProgress.size() > 0) {
					    if (megaApi.areTransfersPaused(MegaTransfer.TYPE_DOWNLOAD) || megaApi.areTransfersPaused(MegaTransfer.TYPE_UPLOAD)) {
                            playTransfersMenuIcon.setVisible(true);
                        } else {
                            pauseTransfersMenuIcon.setVisible(true);
                        }

						cancelAllTransfersMenuItem.setVisible(true);
					    enableSelectMenuItem.setVisible(true);
					} else if (getTabItemTransfers() == COMPLETED_TAB && isTransfersInProgressAdded() && completedTFLol.isAnyTransferCompleted()) {
						clearCompletedTransfers.setVisible(true);
						retryTransfers.setVisible(thereAreFailedOrCancelledTransfers());
					}

					break;

				case SETTINGS:
					upgradeAccountMenuItem.setVisible(true);
					break;

				case CHAT:
					if (searchExpand) {
						openSearchView();
					} else {
						doNotDisturbMenuItem.setVisible(true);
						inviteMenuItem.setVisible(true);
						if (getChatsFragment() != null && rChatFL.getItemCount() > 0) {
							searchMenuItem.setVisible(true);
						}
						importLinkMenuItem.setVisible(true);
						importLinkMenuItem.setTitle(getString(R.string.action_open_chat_link));
					}
					break;

				case NOTIFICATIONS:
					break;
			}
		}

		if (drawerItem == DrawerItem.HOMEPAGE) {
			// Get the Searchable again at onCreateOptionsMenu() after screen rotation
			mHomepageSearchable = findHomepageSearchable();

			if (searchExpand) {
				openSearchView();
			} else {
				if (mHomepageSearchable != null) {
					searchMenuItem.setVisible(mHomepageSearchable.shouldShowSearchMenu());
				}
			}
		}

		if (megaApi.isBusinessAccount()) {
			upgradeAccountMenuItem.setVisible(false);
		}

		logDebug("Call to super onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}

    private void openSearchOnHomepage() {
        textsearchQuery = false;
        firstNavigationLevel = true;
        parentHandleSearch = -1;
        levelsSearch = -1;
        setSearchDrawerItem();
        selectDrawerItemLollipop(drawerItem);
        resetActionBar(aB);

        if (sFLol != null) {
            sFLol.newSearchNodesTask();
        }
    }

    private void setFullscreenOfflineFragmentSearchQuery(String searchQuery) {
		if (fullscreenOfflineFragment != null) {
			fullscreenOfflineFragment.setSearchQuery(searchQuery);
		}
	}

	public void updateFullscreenOfflineFragmentOptionMenu(boolean openSearchView) {
    	if (fullscreenOfflineFragment == null) {
    		return;
		}

		if (searchExpand && openSearchView) {
			openSearchView();
		} else if (!searchExpand) {
			if (isOnline(this)) {
				if (fullscreenOfflineFragment.getItemCount() > 0
						&& !fullscreenOfflineFragment.searchMode() && searchMenuItem != null) {
					searchMenuItem.setVisible(true);
				}
			} else {
				supportInvalidateOptionsMenu();
			}

			fullscreenOfflineFragment.refreshActionBarTitle();
		}
	}

	private HomepageSearchable findHomepageSearchable() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment);
		for (Fragment fragment : navHostFragment.getChildFragmentManager().getFragments()) {
			if (fragment instanceof HomepageSearchable) {
				return (HomepageSearchable) fragment;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
    public <F extends Fragment> F getFragmentByType(Class<F> fragmentClass) {
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
        	return null;
		}

        for (Fragment fragment : navHostFragment.getChildFragmentManager().getFragments()) {
            if (fragment.getClass() == fragmentClass) {
                return (F) fragment;
            }
        }

        return null;
    }

	public void updateCuFragmentOptionsMenu() {
		if (selectMenuItem == null || sortByMenuItem == null || gridSmallLargeMenuItem == null) {
			return;
		}

		if (drawerItem == DrawerItem.CAMERA_UPLOADS
				&& getCameraUploadFragment() != null
				&& cuFragment.getItemCount() > 0) {
			boolean visible = cuFragment.shouldShowFullInfoAndOptions();
			sortByMenuItem.setVisible(visible);
			setCuThumbnailTypeIcon();
			gridSmallLargeMenuItem.setVisible(visible);
		}
	}

	private void setCuThumbnailTypeIcon() {
		if (isSmallGridCameraUploads) {
			gridSmallLargeMenuItem.setIcon(R.drawable.ic_thumbnail_view);
		} else {
			gridSmallLargeMenuItem.setIcon(R.drawable.ic_menu_gridview_small);
		}
	}

	private void setGridListIcon() {
		if (isList){
			thumbViewMenuItem.setTitle(getString(R.string.action_grid));
			thumbViewMenuItem.setIcon(R.drawable.ic_thumbnail_view);
		}
		else{
			thumbViewMenuItem.setIcon(R.drawable.ic_list_view);
		}
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		logDebug("onOptionsItemSelected");
		typesCameraPermission = INVALID_TYPE_PERMISSIONS;

		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if (megaApi != null){
			logDebug("retryPendingConnections");
			megaApi.retryPendingConnections();
		}

		if (megaChatApi != null){
			megaChatApi.retryPendingConnections(false, null);
		}

		int id = item.getItemId();
		switch(id){
			case android.R.id.home:{
				if (firstNavigationLevel && drawerItem != DrawerItem.SEARCH){
					if (drawerItem == DrawerItem.RUBBISH_BIN || drawerItem == DrawerItem.ACCOUNT || drawerItem == DrawerItem.INBOX || drawerItem == DrawerItem.CONTACTS
							|| drawerItem == DrawerItem.NOTIFICATIONS|| drawerItem == DrawerItem.SETTINGS || drawerItem == DrawerItem.TRANSFERS) {
						if (drawerItem == DrawerItem.ACCOUNT && comesFromNotifications) {
							comesFromNotifications = false;
							selectDrawerItemLollipop(DrawerItem.NOTIFICATIONS);
						}
						else {
							if (drawerItem == DrawerItem.SETTINGS) {
								resetSettingsScrollIfNecessary();
							}

							backToDrawerItem(bottomNavigationCurrentItem);
						}
					} else {
						drawerLayout.openDrawer(nV);
					}
				}
				else{
					logDebug("NOT firstNavigationLevel");
		    		if (drawerItem == DrawerItem.CLOUD_DRIVE){
						//Cloud Drive
						if (isCloudAdded()) {
							fbFLol.onBackPressed();
						}
		    		}
					else if (drawerItem == DrawerItem.RUBBISH_BIN) {
		    			rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
						if (rubbishBinFLol != null){
							rubbishBinFLol.onBackPressed();
						}
					}
		    		else if (drawerItem == DrawerItem.SHARED_ITEMS){
						if (getTabItemShares() == INCOMING_TAB && isIncomingAdded()) {
							inSFLol.onBackPressed();
						} else if (getTabItemShares() == OUTGOING_TAB && isOutgoingAdded()) {
							outSFLol.onBackPressed();
						} else if (getTabItemShares() == LINKS_TAB && isLinksAdded()) {
							lF.onBackPressed();
						}
		    		}
					else if (drawerItem == DrawerItem.CAMERA_UPLOADS) {
						if (getCameraUploadFragment() != null) {
							if (cuFragment.isEnableCUFragmentShown()) {
								cuFragment.onBackPressed();
								return true;
							}

							setToolbarTitle();
							invalidateOptionsMenu();
							return true;
						}
					} else if (drawerItem == DrawerItem.INBOX) {
						iFLol = (InboxFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.INBOX.getTag());
						if (iFLol != null){
							iFLol.onBackPressed();
							return true;
						}
					}
		    		else if (drawerItem == DrawerItem.SEARCH){
		    			if (getSearchFragment() != null){
//		    				sFLol.onBackPressed();
		    				onBackPressed();
		    				return true;
		    			}
		    		}
		    		else if (drawerItem == DrawerItem.TRANSFERS){

						drawerItem = DrawerItem.CLOUD_DRIVE;
						setBottomNavigationMenuItemChecked(CLOUD_DRIVE_BNV);
						selectDrawerItemLollipop(drawerItem);
						return true;
		    		}
					else if (drawerItem == DrawerItem.ACCOUNT){
						if (accountFragment == UPGRADE_ACCOUNT_FRAGMENT) {
							closeUpgradeAccountFragment();
							return true;
						}
					} else if (drawerItem == DrawerItem.HOMEPAGE) {
						if (mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE) {
							handleBackPressIfFullscreenOfflineFragmentOpened();
						} else {
							mNavController.navigateUp();
						}
					} else {
						super.onBackPressed();
					}
				}
		    	return true;
		    }
			case R.id.action_search:{
				logDebug("Action search selected");
                hideItemsWhenSearchSelected();
                return true;
			}
		    case R.id.action_import_link:{
				showOpenLinkDialog();
		    	return true;
		    }
		    case R.id.action_take_picture:{
		    	typesCameraPermission = TAKE_PICTURE_OPTION;

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					boolean hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
					if (!hasStoragePermission) {
						ActivityCompat.requestPermissions(this,
				                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								REQUEST_WRITE_STORAGE);
					}

					boolean hasCameraPermission = checkPermission(Manifest.permission.CAMERA);
					if (!hasCameraPermission) {
						ActivityCompat.requestPermissions(this,
				                new String[]{Manifest.permission.CAMERA},
								REQUEST_CAMERA);
					}

					if (hasStoragePermission && hasCameraPermission){
						checkTakePicture(this, TAKE_PHOTO_CODE);
					}
				}
		    	else{
					checkTakePicture(this, TAKE_PHOTO_CODE);
		    	}

		    	return true;
		    }
		    case R.id.action_menu_cancel_all_transfers:{
		    	showConfirmationCancelAllTransfers();
		    	return true;
		    }
			case R.id.action_menu_clear_completed_transfers:{
				showConfirmationClearCompletedTransfers();
				return true;
			}
	        case R.id.action_pause:{
	        	if (drawerItem == DrawerItem.TRANSFERS){
					logDebug("Click on action_pause - play visible");
	        		megaApi.pauseTransfers(true, this);
	        		pauseTransfersMenuIcon.setVisible(false);
	        		playTransfersMenuIcon.setVisible(true);
	        	}

	        	return true;
	        }
	        case R.id.action_play:{
				logDebug("Click on action_play - pause visible");
				pauseTransfersMenuIcon.setVisible(true);
				playTransfersMenuIcon.setVisible(false);
    			megaApi.pauseTransfers(false, this);

	        	return true;
	        }
	        case R.id.action_add_contact:{
	        	if (drawerItem == DrawerItem.CONTACTS||drawerItem == DrawerItem.CHAT){
					chooseAddContactDialog(false);
	        	}
	        	return true;
	        }
			case R.id.action_menu_invite:{
				if (drawerItem == DrawerItem.CHAT){
                    logDebug("to InviteContactActivity");
                    startActivityForResult(new Intent(getApplicationContext(), InviteContactActivity.class), REQUEST_INVITE_CONTACT_FROM_DEVICE);
				}

				return true;
			}
			case R.id.action_menu_do_not_disturb:
				if (drawerItem == DrawerItem.CHAT) {
					if (getGeneralNotification().equals(NOTIFICATIONS_ENABLED)) {
						createMuteNotificationsChatAlertDialog(this, null);
					} else {
						showSnackbar(MUTE_NOTIFICATIONS_SNACKBAR_TYPE, null, -1);
					}
				}
				return true;

	        case R.id.action_menu_kill_all_sessions:{
				showConfirmationCloseAllSessions();
	        	return true;
	        }
	        case R.id.action_new_folder:{
	        	if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        		showNewFolderDialog();
	        	}
	        	else if(drawerItem == DrawerItem.SHARED_ITEMS){
	        		showNewFolderDialog();
	        	}
	        	else if (drawerItem == DrawerItem.CONTACTS){
					chooseAddContactDialog(false);
	        	}
	        	return true;
	        }
	        case R.id.action_add:{
	        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	    			if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
	    				ActivityCompat.requestPermissions(this,
	    		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								REQUEST_WRITE_STORAGE);
	    			}
	    		}

	        	if (drawerItem == DrawerItem.SHARED_ITEMS){
	        		if (viewPagerShares.getCurrentItem()==0){

						MegaNode checkNode = megaApi.getNodeByHandle(parentHandleIncoming);

						if((megaApi.checkAccess(checkNode, MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK)){
							this.showUploadPanel();
						}
						else if(megaApi.checkAccess(checkNode, MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK){
							this.showUploadPanel();
						}
						else if(megaApi.checkAccess(checkNode, MegaShare.ACCESS_READ).getErrorCode() == MegaError.API_OK){
							logWarning("Not permissions to upload");
							MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
							builder.setMessage(getString(R.string.no_permissions_upload));
							builder.setCancelable(false).setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
								   public void onClick(DialogInterface dialog, int id) {
										//do things
									   alertNotPermissionsUpload.dismiss();
								   }
							   });

							alertNotPermissionsUpload = builder.create();
							alertNotPermissionsUpload.show();
						}

	        		}
	        		else if(viewPagerShares.getCurrentItem()==1){
						this.showUploadPanel();
					}
	        	}
	        	else {
        			this.showUploadPanel();
	        	}

	        	return true;
	        }

	        case R.id.action_select:{
	        	switch (drawerItem) {
					case CLOUD_DRIVE:
						if (isCloudAdded()){
							fbFLol.selectAll();
						}
						break;

					case RUBBISH_BIN:
						if (getRubbishBinFragment() != null){
							rubbishBinFLol.selectAll();
						}
						break;

					case CONTACTS:
						switch(getTabItemContacts()){
							case CONTACTS_TAB:{
								if (getContactsFragment() != null){
									cFLol.selectAll();
								}
								break;
							}
							case SENT_REQUESTS_TAB:{
								if (getSentRequestFragment() != null){
									sRFLol.selectAll();
								}
								break;
							}
							case RECEIVED_REQUESTS_TAB:{
								if (getReceivedRequestFragment() != null){
									rRFLol.selectAll();
								}
								break;
							}
						}
						break;

					case SHARED_ITEMS:
						switch (getTabItemShares()) {
							case INCOMING_TAB:
								if (isIncomingAdded()) {
									inSFLol.selectAll();
								}
								break;

							case OUTGOING_TAB:
								if (isOutgoingAdded()) {
									outSFLol.selectAll();
								}
								break;

							case LINKS_TAB:
								if (isLinksAdded()) {
									lF.selectAll();
								}
								break;
						}
						break;
					case HOMEPAGE:
						if (fullscreenOfflineFragment != null) {
							fullscreenOfflineFragment.selectAll();
						}
						break;
					case CHAT:
						if (getChatsFragment() != null) {
							rChatFL.selectAll();
						}
						break;

					case INBOX:
						if (getInboxFragment() != null) {
							iFLol.selectAll();
						}
						break;

					case SEARCH:
						if (getSearchFragment() != null) {
							sFLol.selectAll();
						}
						break;

					case CAMERA_UPLOADS:
						if (getCameraUploadFragment() != null) {
							cuFragment.selectAll();
						}
						break;
				}

	        	return true;
	        }
	        case R.id.action_grid_view_large_small:{
				if (drawerItem == DrawerItem.CAMERA_UPLOADS){
					isSmallGridCameraUploads = !isSmallGridCameraUploads;
					dbH.setSmallGridCamera(isSmallGridCameraUploads);

					setCuThumbnailTypeIcon();

					refreshFragment(FragmentTag.CAMERA_UPLOADS.getTag());
	        	}
	        	return true;
	        }
	        case R.id.action_grid:{
				logDebug("action_grid selected");
	        	if (drawerItem == DrawerItem.CAMERA_UPLOADS){
					logDebug("action_grid_list in CameraUploads");
					if(!firstLogin) {
						gridSmallLargeMenuItem.setVisible(true);
					}else{
						gridSmallLargeMenuItem.setVisible(false);
					}
					searchMenuItem.setVisible(false);
					refreshFragment(FragmentTag.CAMERA_UPLOADS.getTag());
				} else {
					updateView(!isList);
				}
	        	supportInvalidateOptionsMenu();

	        	return true;
	        }
	        case R.id.action_menu_clear_rubbish_bin:{
	        	showClearRubbishBinDialog();
	        	return true;
	        }
	        case R.id.action_menu_refresh:{
	        	switch(drawerItem){
		        	case ACCOUNT:{
						//Refresh all the info of My Account
		        		Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra(VISIBLE_FRAGMENT,  LOGIN_FRAGMENT);
			    		intent.setAction(ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
			    		break;
		        	}
	        	}
	        	return true;
	        }
	        case R.id.action_menu_sort_by:{
	        	int orderType;

	        	switch (drawerItem) {
					case CONTACTS:
						orderType = ORDER_CONTACTS;
						break;

					case CAMERA_UPLOADS:
						orderType = ORDER_CAMERA;
						break;

					default:
						if (drawerItem == DrawerItem.SHARED_ITEMS
								&& getTabItemShares() == INCOMING_TAB && deepBrowserTreeIncoming == 0) {
							showNewSortByPanel(ORDER_OTHERS, true);
							return true;
						}

						if (drawerItem == DrawerItem.SHARED_ITEMS
								&& getTabItemShares() == OUTGOING_TAB && deepBrowserTreeOutgoing == 0) {
							orderType = ORDER_OTHERS;
						} else {
							orderType = ORDER_CLOUD;
						}
				}

				showNewSortByPanel(orderType);
	        	return true;
	        }
	        case R.id.action_menu_help:{
	        	Intent intent = new Intent();
	            intent.setAction(Intent.ACTION_VIEW);
	            intent.addCategory(Intent.CATEGORY_BROWSABLE);
	            intent.setData(Uri.parse("https://mega.co.nz/#help/android"));
	            startActivity(intent);

	    		return true;
	    	}
	        case R.id.action_menu_upgrade_account:{
	        	accountFragmentPreUpgradeAccount = accountFragment;
	        	drawerItemPreUpgradeAccount = drawerItem;
	        	drawerItem = DrawerItem.ACCOUNT;
	        	setBottomNavigationMenuItemChecked(HIDDEN_BNV);
				accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
				displayedAccountType = -1;
				selectDrawerItemLollipop(drawerItem);
				return true;
	        }

	        case R.id.action_menu_change_pass:{
	        	Intent intent = new Intent(this, ChangePasswordActivityLollipop.class);
				startActivity(intent);
				return true;
	        }
	        case R.id.action_menu_export_MK:{
				logDebug("Export MK option selected");

				showMKLayout();
	        	return true;
	        }
	        case R.id.action_menu_logout:{
				logDebug("Action menu logout pressed");
				passwordReminderFromMyAccount = true;
				megaApi.shouldShowPasswordReminderDialog(true, this);
	        	return true;
	        }
	        case R.id.action_menu_cancel_subscriptions:{
				logDebug("Action menu cancel subscriptions pressed");
	        	if (megaApi != null){
	        		//Show the message
	        		showCancelMessage();
	        	}
	        	return true;
	        }
			case R.id.action_menu_forgot_pass:{
				logDebug("Action menu forgot pass pressed");
				maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
				if(maFLol!=null){
					showConfirmationResetPasswordFromMyAccount();
				}
				return true;
			}
			case R.id.action_scan_qr: {
				logDebug("Action menu scan QR code pressed");
                //Check if there is a in progress call:
				checkBeforeOpeningQR(true);
				return true;
			}
			case R.id.action_return_call:{
				logDebug("Action menu return to call in progress pressed");
				returnCallWithPermissions();
				return true;
			}
			case R.id.action_menu_retry_transfers:
				retryAllTransfers();
				return true;

			case R.id.action_enable_select:
				if (isTransfersInProgressAdded()) {
					tFLol.activateActionMode();
				}
				return true;

            default:{
	            return super.onOptionsItemSelected(item);
            }
		}
	}

    private void hideItemsWhenSearchSelected() {
        textSubmitted = false;
        if (createFolderMenuItem != null) {
			doNotDisturbMenuItem.setVisible(false);
			upgradeAccountMenuItem.setVisible(false);
            cancelAllTransfersMenuItem.setVisible(false);
            clearCompletedTransfers.setVisible(false);
            pauseTransfersMenuIcon.setVisible(false);
            playTransfersMenuIcon.setVisible(false);
            createFolderMenuItem.setVisible(false);
            addContactMenuItem.setVisible(false);
            addMenuItem.setVisible(false);
            refreshMenuItem.setVisible(false);
            sortByMenuItem.setVisible(false);
            unSelectMenuItem.setVisible(false);
            changePass.setVisible(false);
            clearRubbishBinMenuitem.setVisible(false);
            importLinkMenuItem.setVisible(false);
            takePicture.setVisible(false);
            refreshMenuItem.setVisible(false);
            helpMenuItem.setVisible(false);
            gridSmallLargeMenuItem.setVisible(false);
            logoutMenuItem.setVisible(false);
            forgotPassMenuItem.setVisible(false);
            inviteMenuItem.setVisible(false);
            selectMenuItem.setVisible(false);
            thumbViewMenuItem.setVisible(false);
            searchMenuItem.setVisible(false);
            killAllSessions.setVisible(false);
            exportMK.setVisible(false);
        }
    }

	private void returnCallWithPermissions() {
		if (checkPermissionsCall(this, RETURN_CALL_PERMISSIONS)) {
			returnActiveCall(this);
		}
	}

	public void checkBeforeOpeningQR(boolean openScanQR){
		if (isNecessaryDisableLocalCamera() != -1) {
			showConfirmationOpenCamera(this, ACTION_OPEN_QR, openScanQR);
			return;
		}
		openQR(openScanQR);
	}

	public void openQR(boolean openScanQr){
		ScanCodeFragment fragment = new ScanCodeFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitNowAllowingStateLoss();
		Intent intent = new Intent(this, QRCodeActivity.class);
		intent.putExtra(OPEN_SCAN_QR, openScanQr);
		startActivity(intent);
	}

	private void updateView (boolean isList) {
        if (this.isList != isList) {
            this.isList = isList;
            dbH.setPreferredViewList(isList);
        }

		LiveEventBus.get(EVENT_LIST_GRID_CHANGE, Boolean.class).post(isList);

        //Refresh Cloud Fragment
        refreshFragment(FragmentTag.CLOUD_DRIVE.getTag());

        //Refresh Rubbish Fragment
        refreshFragment(FragmentTag.RUBBISH_BIN.getTag());

        //Refresh ContactsFragmentLollipop layout even current fragment isn't ContactsFragmentLollipop.
        refreshFragment(FragmentTag.CONTACTS.getTag());

        if (contactsPageAdapter != null) {
            contactsPageAdapter.notifyDataSetChanged();
        }

        //Refresh shares section
        refreshFragment(FragmentTag.INCOMING_SHARES.getTag());

        //Refresh shares section
        refreshFragment(FragmentTag.OUTGOING_SHARES.getTag());

		refreshSharesPageAdapter();

        //Refresh search section
        refreshFragment(FragmentTag.SEARCH.getTag());

        //Refresh inbox section
        refreshFragment(FragmentTag.INBOX.getTag());
    }

	public void hideMKLayout(){
		logDebug("hideMKLayout");
		mkLayoutVisible= false;

		abL.setVisibility(View.VISIBLE);

		eRKeyF = null;

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		supportInvalidateOptionsMenu();
		selectDrawerItemLollipop(drawerItem);
	}

	public void showMKLayout(){
		logDebug("showMKLayout");

		accountFragment = BACKUP_RECOVERY_KEY_FRAGMENT;
		mkLayoutVisible=true;

		aB.setSubtitle(null);
		abL.setVisibility(View.GONE);

		deleteCurrentFragment();

		if (eRKeyF == null){
			eRKeyF = new ExportRecoveryKeyFragment();
		}
		replaceFragment(eRKeyF, FragmentTag.EXPORT_RECOVERY_KEY.getTag());

		abL.setVisibility(View.GONE);

		setTabsVisibility();
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		supportInvalidateOptionsMenu();
		hideFabButton();
	}

	public void refreshAfterMovingToRubbish(){
		logDebug("refreshAfterMovingToRubbish");

		if (drawerItem == DrawerItem.CLOUD_DRIVE) {
			refreshCloudDrive();
		} else if (drawerItem == DrawerItem.INBOX) {
			onNodesInboxUpdate();
		} else if (drawerItem == DrawerItem.SHARED_ITEMS) {
			onNodesSharedUpdate();
		} else if (drawerItem == DrawerItem.SEARCH) {
			refreshSearch();
		} else if (drawerItem == DrawerItem.HOMEPAGE) {
			LiveEventBus.get(EVENT_NODES_CHANGE).post(false);
		}

        checkCameraUploadFolder(true,null);
		refreshRubbishBin();
		setToolbarTitle();
	}

    /**
     * After nodes on Cloud Drive changed or some nodes are moved to rubbish bin,
     * need to check CU and MU folders' status.
     *
     * @param shouldDisable If CU or MU folder is deleted by current client, then CU should be disabled. Otherwise not.
     * @param updatedNodes Nodes which have changed.
     */
    private void checkCameraUploadFolder(boolean shouldDisable, ArrayList<MegaNode> updatedNodes) {
        // Get CU and MU folder hanlde from local setting.
        long primaryHandle = getPrimaryFolderHandle();
        long secondaryHandle = getSecondaryFolderHandle();

        if (updatedNodes != null) {
            List<Long> handles = new ArrayList<>();
            for (MegaNode node : updatedNodes) {
                handles.add(node.getHandle());
            }
            // If CU and MU folder don't change then return.
            if (!handles.contains(primaryHandle) && !handles.contains(secondaryHandle)) {
                logDebug("Updated nodes don't include CU/MU, return.");
                return;
            }
        }

        MegaPreferences prefs = dbH.getPreferences();
        boolean isSecondaryEnabled = false;
        if (prefs != null) {
            isSecondaryEnabled = Boolean.parseBoolean(prefs.getSecondaryMediaFolderEnabled());
        }

        // Check if CU and MU folder are moved to rubbish bin.
        boolean isPrimaryFolderInRubbish = isNodeInRubbish(primaryHandle);
        boolean isSecondaryFolderInRubbish = isSecondaryEnabled && isNodeInRubbish(secondaryHandle);

        // If only MU folder is in rubbish bin.
        if (isSecondaryFolderInRubbish && !isPrimaryFolderInRubbish) {
            logDebug("MU folder is deleted, backup settings and disable MU.");
            if (shouldDisable) {
                // Back up timestamps and disabled MU upload.
                backupTimestampsAndFolderHandle();
                disableMediaUploadProcess();
            } else {
                // Just stop the upload process.
                stopRunningCameraUploadService(app);
            }
        } else if (isPrimaryFolderInRubbish) {
            // If CU folder is in rubbish bin.
            logDebug("CU folder is deleted, backup settings and disable CU.");
            if (shouldDisable) {
                // Disable both CU and MU.
                backupTimestampsAndFolderHandle();
                disableCameraUploadSettingProcess(false);
				sendBroadcast(new Intent(ACTION_UPDATE_DISABLE_CU_UI_SETTING));
			} else {
                // Just stop the upload process.
                stopRunningCameraUploadService(app);
            }
        }
    }

	public void refreshRubbishBin () {
		rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
		if (rubbishBinFLol != null){
			ArrayList<MegaNode> nodes;
			if(parentHandleRubbish == -1){
				nodes = megaApi.getChildren(megaApi.getRubbishNode(), sortOrderManagement.getOrderCloud());
			}
			else{
				nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish),
						sortOrderManagement.getOrderCloud());
			}

			rubbishBinFLol.hideMultipleSelect();
			rubbishBinFLol.setNodes(nodes);
			rubbishBinFLol.getRecyclerView().invalidate();
		}
	}

	public void refreshAfterMoving() {
		logDebug("refreshAfterMoving");
		if (drawerItem == DrawerItem.CLOUD_DRIVE) {

			//Refresh Cloud Fragment
			refreshCloudDrive();

			//Refresh Rubbish Fragment
			refreshRubbishBin();
		}
		else if(drawerItem == DrawerItem.RUBBISH_BIN){
			//Refresh Rubbish Fragment
			refreshRubbishBin();
		}
		else if (drawerItem == DrawerItem.INBOX) {
			onNodesInboxUpdate();

			refreshCloudDrive();
		}
		else if(drawerItem == DrawerItem.SHARED_ITEMS) {
			onNodesSharedUpdate();

			//Refresh Cloud Fragment
			refreshCloudDrive();

			//Refresh Rubbish Fragment
			refreshRubbishBin();
		}else if(drawerItem == DrawerItem.SEARCH){
			refreshSearch();
		}

		setToolbarTitle();
	}

	public void refreshSearch() {
		if (getSearchFragment() != null){
			sFLol.hideMultipleSelect();
			sFLol.refresh();
		}
	}

	public void refreshAfterRemoving(){
		logDebug("refreshAfterRemoving");

		rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
		if (rubbishBinFLol != null){
			rubbishBinFLol.hideMultipleSelect();

			if (isClearRubbishBin){
				isClearRubbishBin = false;
				parentHandleRubbish = megaApi.getRubbishNode().getHandle();
				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(),
						sortOrderManagement.getOrderCloud());
				rubbishBinFLol.setNodes(nodes);
				rubbishBinFLol.getRecyclerView().invalidate();
			}
			else{
				refreshRubbishBin();
			}
		}

		onNodesInboxUpdate();

		refreshSearch();
	}

	@Override
	public void onBackPressed() {
		logDebug("onBackPressed");

		// Let the PSA web browser fragment(if visible) to consume the back key event
		if (psaWebBrowser.consumeBack()) return;

		retryConnectionsAndSignalPresence();

		if (drawerLayout.isDrawerOpen(nV)) {
    		drawerLayout.closeDrawer(Gravity.LEFT);
    		return;
    	}

		try {
			statusDialog.dismiss();
		} catch (Exception ignored) {}

		logDebug("DRAWERITEM: " + drawerItem);

		if (turnOnNotifications) {
			deleteTurnOnNotificationsFragment();
			return;
		}
		if (onAskingPermissionsFragment || onAskingSMSVerificationFragment) {
			return;
		}
		if (mkLayoutVisible) {
			hideMKLayout();
			return;
		}

		if (drawerItem == DrawerItem.CLOUD_DRIVE) {
		    if (!isCloudAdded() || fbFLol.onBackPressed() == 0) {
				backToDrawerItem(-1);
			}
		} else if (drawerItem == DrawerItem.RUBBISH_BIN) {
			rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
			if (rubbishBinFLol == null || rubbishBinFLol.onBackPressed() == 0) {
				backToDrawerItem(bottomNavigationCurrentItem);
			}
		} else if (drawerItem == DrawerItem.TRANSFERS) {
			backToDrawerItem(bottomNavigationCurrentItem);

    	} else if (drawerItem == DrawerItem.INBOX) {
			iFLol = (InboxFragmentLollipop) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.INBOX.getTag());
			if (iFLol == null || iFLol.onBackPressed() == 0) {
				backToDrawerItem(bottomNavigationCurrentItem);
			}
		} else if (drawerItem == DrawerItem.NOTIFICATIONS) {
			backToDrawerItem(bottomNavigationCurrentItem);
		} else if (drawerItem == DrawerItem.SETTINGS) {
			if (!isOnline(this)) {
				showOfflineMode();
			}

			resetSettingsScrollIfNecessary();
			backToDrawerItem(bottomNavigationCurrentItem);
		} else if (drawerItem == DrawerItem.SHARED_ITEMS) {
			switch (getTabItemShares()) {
				case INCOMING_TAB:
					if (!isIncomingAdded() || inSFLol.onBackPressed() == 0) {
						backToDrawerItem(-1);
					}
					break;
				case OUTGOING_TAB:
					if (!isOutgoingAdded() || outSFLol.onBackPressed() == 0) {
						backToDrawerItem(-1);
					}
					break;
				case LINKS_TAB:
					if (!isLinksAdded() || lF.onBackPressed() == 0) {
						backToDrawerItem(-1);
					}
					break;
				default:
					backToDrawerItem(-1);
					break;
			}
		} else if (drawerItem == DrawerItem.CHAT) {
			backToDrawerItem(-1);
		} else if (drawerItem == DrawerItem.CONTACTS) {
			switch (getTabItemContacts()) {
				case CONTACTS_TAB:
		    		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager()
							.findFragmentByTag(FragmentTag.CONTACTS.getTag());
		    		if (cFLol == null || cFLol.onBackPressed() == 0) {
						backToDrawerItem(bottomNavigationCurrentItem);
		    		}
					break;
				case SENT_REQUESTS_TAB:
				case RECEIVED_REQUESTS_TAB:
				default:
					backToDrawerItem(bottomNavigationCurrentItem);
					break;
			}
		} else if (drawerItem == DrawerItem.ACCOUNT) {
			logDebug("MyAccountSection");
			logDebug("The accountFragment is: " + accountFragment);
    		switch(accountFragment) {
	    		case MY_ACCOUNT_FRAGMENT:
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager()
							.findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
	    			if (maFLol == null || maFLol.onBackPressed() == 0){
						if (comesFromNotifications) {
							comesFromNotifications = false;
							selectDrawerItemLollipop(DrawerItem.NOTIFICATIONS);
						} else {
							backToDrawerItem(bottomNavigationCurrentItem);
						}
	    			}
	    			break;
	    		case UPGRADE_ACCOUNT_FRAGMENT:
					logDebug("Back to MyAccountFragment -> drawerItemPreUpgradeAccount");
					closeUpgradeAccountFragment();
	    			break;
	    		case OVERQUOTA_ALERT:
				default:
	    			backToDrawerItem(bottomNavigationCurrentItem);
	    			break;
    		}
    	} else if (drawerItem == DrawerItem.CAMERA_UPLOADS) {
			if (getCameraUploadFragment() == null || cuFragment.onBackPressed() == 0){
				backToDrawerItem(-1);
			}
    	} else if (drawerItem == DrawerItem.SEARCH) {
			if (getSearchFragment() == null || sFLol.onBackPressed() == 0) {
    			closeSearchSection();
    		}
        } else if (drawerItem == DrawerItem.HOMEPAGE && mHomepageScreen == HomepageScreen.HOMEPAGE) {
            HomepageFragment fragment = getFragmentByType(HomepageFragment.class);
            if(fragment != null && fragment.isFabExpanded()) {
                fragment.collapseFab();
            } else {
            	// The Psa requires the activity to load the new PSA even though the app
				// is on the background. So don't call super.onBackPressed() since it will destroy
				// this activity and its embedded web browser fragment.
				moveTaskToBack(false);
            }
        } else {
			handleBackPressIfFullscreenOfflineFragmentOpened();
		}
	}

	private void handleBackPressIfFullscreenOfflineFragmentOpened() {
		if (fullscreenOfflineFragment == null || fullscreenOfflineFragment.onBackPressed() == 0) {
			// workaround for flicker of AppBarLayout: if we go back to homepage from fullscreen
			// offline, and hide AppBarLayout when immediately on go back, we will see the flicker
			// of AppBarLayout, hide AppBarLayout when fullscreen offline is closed is better.
			if (bottomNavigationCurrentItem != HOMEPAGE_BNV) {
				backToDrawerItem(bottomNavigationCurrentItem);
			} else {
				drawerItem = DrawerItem.HOMEPAGE;
			}
			super.onBackPressed();
		}
	}

    public void adjustTransferWidgetPositionInHomepage() {
        if (drawerItem == DrawerItem.HOMEPAGE && mHomepageScreen == HomepageScreen.HOMEPAGE) {
            RelativeLayout transfersWidgetLayout = findViewById(R.id.transfers_widget_layout);
            if (transfersWidgetLayout == null) return;

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) transfersWidgetLayout.getLayoutParams();
            params.bottomMargin = Util.dp2px(TRANSFER_WIDGET_MARGIN_BOTTOM, outMetrics);
            params.gravity = Gravity.END;
			transfersWidgetLayout.setLayoutParams(params);
        }
    }

    /**
	 * Update the PSA view visibility. It should only visible in root homepage tab.
	 */
    private void updatePsaViewVisibility() {
		psaViewHolder.toggleVisible(drawerItem == DrawerItem.HOMEPAGE
				&& mHomepageScreen == HomepageScreen.HOMEPAGE);
		if (psaViewHolder.visible()) {
			handler.post(this::updateHomepageFabPosition);
		} else {
			updateHomepageFabPosition();
		}
	}

	private void closeUpgradeAccountFragment() {
		setFirstNavigationLevel(true);
		displayedAccountType = -1;

		if (drawerItemPreUpgradeAccount != null) {
			if (drawerItemPreUpgradeAccount == DrawerItem.ACCOUNT) {
				if (accountFragmentPreUpgradeAccount == -1) {
					accountFragment = MY_ACCOUNT_FRAGMENT;
				} else {
					accountFragment = accountFragmentPreUpgradeAccount;
				}
			}

			drawerItem = drawerItemPreUpgradeAccount;
		} else {
			accountFragment = MY_ACCOUNT_FRAGMENT;
			drawerItem = DrawerItem.ACCOUNT;
		}

		selectDrawerItemLollipop(drawerItem);

        // Hide fragment (required to check if show ODQ Paywall)
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(upAFL);
        ft.commitNow();
	}

	public void backToDrawerItem(int item) {
    	if (item == CLOUD_DRIVE_BNV) {
    		drawerItem = DrawerItem.CLOUD_DRIVE;
    		if (isCloudAdded()) {
    			fbFLol.setTransferOverQuotaBannerVisibility();
			}
		}
		else if (item == CAMERA_UPLOADS_BNV) {
			drawerItem = DrawerItem.CAMERA_UPLOADS;
		}
		else if (item == CHAT_BNV) {
			drawerItem = DrawerItem.CHAT;
		}
		else if (item == SHARED_BNV) {
			drawerItem = DrawerItem.SHARED_ITEMS;
		}
		else if (item == HOMEPAGE_BNV || item == -1) {
			drawerItem = DrawerItem.HOMEPAGE;
		}

		selectDrawerItemLollipop(drawerItem);
	}

	void isFirstTimeCam() {
		if(firstLogin){
			firstLogin = false;
			dbH.setCamSyncEnabled(false);
			bottomNavigationCurrentItem = CLOUD_DRIVE_BNV;
		}
	}

	private void checkIfShouldCloseSearchView(DrawerItem oldDrawerItem) {
    	if (!searchExpand) return;

		if (oldDrawerItem == DrawerItem.CHAT
				|| (oldDrawerItem == DrawerItem.HOMEPAGE
				&& mHomepageScreen == HomepageScreen.FULLSCREEN_OFFLINE)) {
			searchExpand = false;
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		logDebug("onNavigationItemSelected");

		if (nV != null){
			Menu nVMenu = nV.getMenu();
			resetNavigationViewMenu(nVMenu);
		}

		DrawerItem oldDrawerItem = drawerItem;

		switch (menuItem.getItemId()){
			case R.id.bottom_navigation_item_cloud_drive: {
				if (drawerItem == DrawerItem.CLOUD_DRIVE) {
					MegaNode rootNode = megaApi.getRootNode();
					if (rootNode == null) {
						logError("Root node is null");
					}

                    if (parentHandleBrowser != INVALID_HANDLE
							&& rootNode != null && parentHandleBrowser != rootNode.getHandle()) {
                        parentHandleBrowser = rootNode.getHandle();
                        refreshFragment(FragmentTag.CLOUD_DRIVE.getTag());
                        if (isCloudAdded()) {
                            fbFLol.scrollToFirstPosition();
                        }
                    }
				}
				else {
					drawerItem = DrawerItem.CLOUD_DRIVE;
					setBottomNavigationMenuItemChecked(CLOUD_DRIVE_BNV);
				}
				break;
			}
			case R.id.bottom_navigation_item_homepage: {
				drawerItem = DrawerItem.HOMEPAGE;
				if (fullscreenOfflineFragment != null) {
					super.onBackPressed();
					return true;
				} else {
					setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);
				}
				break;
			}
			case R.id.bottom_navigation_item_camera_uploads: {
				// if pre fragment is the same one, do nothing.
				if(oldDrawerItem != DrawerItem.CAMERA_UPLOADS) {
					drawerItem = DrawerItem.CAMERA_UPLOADS;
					setBottomNavigationMenuItemChecked(CAMERA_UPLOADS_BNV);
				}
				break;
			}
			case R.id.bottom_navigation_item_shared_items: {
				if (drawerItem == DrawerItem.SHARED_ITEMS) {
					if (getTabItemShares() == INCOMING_TAB && parentHandleIncoming != INVALID_HANDLE) {
						parentHandleIncoming = INVALID_HANDLE;
						refreshFragment(FragmentTag.INCOMING_SHARES.getTag());
					} else if (getTabItemShares() == OUTGOING_TAB && parentHandleOutgoing != INVALID_HANDLE){
						parentHandleOutgoing = INVALID_HANDLE;
						refreshFragment(FragmentTag.OUTGOING_SHARES.getTag());
					} else if (getTabItemShares() == LINKS_TAB && parentHandleLinks != INVALID_HANDLE) {
						parentHandleLinks = INVALID_HANDLE;
						refreshFragment(FragmentTag.LINKS.getTag());
					}

					refreshSharesPageAdapter();
				} else {
					drawerItem = DrawerItem.SHARED_ITEMS;
					setBottomNavigationMenuItemChecked(SHARED_BNV);
				}
				break;
			}
			case R.id.bottom_navigation_item_chat: {
				drawerItem = DrawerItem.CHAT;
				setBottomNavigationMenuItemChecked(CHAT_BNV);
				break;
			}
		}

		checkIfShouldCloseSearchView(oldDrawerItem);
		selectDrawerItemLollipop(drawerItem);
		drawerLayout.closeDrawer(Gravity.LEFT);

		return true;
	}

	@Override
	public void showSnackbar(int type, String content, long chatId) {
    	showSnackbar(type, fragmentContainer, content, chatId);
	}

	public void restoreFromRubbish(final MegaNode node) {
		logDebug("Node Handle: " + node.getHandle());

		restoreFromRubbish = true;

		MegaNode newParent = megaApi.getNodeByHandle(node.getRestoreHandle());
		if(newParent !=null){
			megaApi.moveNode(node, newParent, this);
		}
		else{
			logDebug("The restore folder no longer exists");
		}
	}

	public void showRenameDialog(final MegaNode document){
		showRenameNodeDialog(this, document, this, this);
	}

	public void showGetLinkActivity(long handle){
		logDebug("Handle: " + handle);
		MegaNode node = megaApi.getNodeByHandle(handle);
		if (node == null) {
			showSnackbar(SNACKBAR_TYPE, getString(R.string.warning_node_not_exists_in_cloud), MEGACHAT_INVALID_HANDLE);
			return;
		}


		if (showTakenDownNodeActionNotAvailableDialog(node, this)) {
			return;
		}

		LinksUtil.showGetLinkActivity(this, handle);

		refreshAfterMovingToRubbish();
	}

	/*
	 * Display keyboard
	 */
	private void showKeyboardDelayed(final View view) {
		logDebug("showKeyboardDelayed");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 50);
	}

	public void setIsClearRubbishBin(boolean value){
		this.isClearRubbishBin = value;
	}

	public void setMoveToRubbish(boolean value){
		this.moveToRubbish = value;
	}

	public void askConfirmationMoveToRubbish(final ArrayList<Long> handleList){
		logDebug("askConfirmationMoveToRubbish");
		isClearRubbishBin=false;

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						//TODO remove the outgoing shares
						nC.moveToTrash(handleList, moveToRubbish);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		if(handleList!=null){

			if (handleList.size() > 0){
				Long handle = handleList.get(0);
				MegaNode p = megaApi.getNodeByHandle(handle);
				while (megaApi.getParentNode(p) != null){
					p = megaApi.getParentNode(p);
				}
				if (p.getHandle() != megaApi.getRubbishNode().getHandle()){

					setMoveToRubbish(true);

					MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
					if (getPrimaryFolderHandle() == handle && CameraUploadUtil.isPrimaryEnabled()) {
						builder.setMessage(getResources().getString(R.string.confirmation_move_cu_folder_to_rubbish));
					} else if (getSecondaryFolderHandle() == handle && CameraUploadUtil.isSecondaryEnabled()) {
						builder.setMessage(R.string.confirmation_move_mu_folder_to_rubbish);
					} else {
						builder.setMessage(getResources().getString(R.string.confirmation_move_to_rubbish));
					}

					builder.setPositiveButton(R.string.general_move, dialogClickListener);
					builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
					builder.show();
				}
				else{

					setMoveToRubbish(false);

					MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
					builder.setMessage(getResources().getString(R.string.confirmation_delete_from_mega));

					builder.setPositiveButton(R.string.context_remove, dialogClickListener);
					builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
					builder.show();
				}
			}
		}
		else{
			logWarning("handleList NULL");
			return;
		}

	}

	public void showDialogInsertPassword(String link, boolean cancelAccount){
		logDebug("showDialogInsertPassword");

		final String confirmationLink = link;
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(scaleWidthPx(20, outMetrics), scaleHeightPx(20, outMetrics), scaleWidthPx(17, outMetrics), 0);

		final EditText input = new EditText(this);
		layout.addView(input, params);

//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setHint(getString(R.string.edit_text_insert_pass));
		input.setTextColor(ColorUtils.getThemeColor(this, android.R.attr.textColorSecondary));
		input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		if(cancelAccount){
			logDebug("cancelAccount action");
			input.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						String pass = input.getText().toString().trim();
						if(pass.equals("")||pass.isEmpty()){
							logWarning("Input is empty");
							input.setError(getString(R.string.invalid_string));
							input.requestFocus();
						}
						else {
							logDebug("Action DONE ime - cancel account");
							aC.confirmDeleteAccount(confirmationLink, pass);
							insertPassDialog.dismiss();
						}
					}
					else{
						logDebug("Other IME" + actionId);
					}
					return false;
				}
			});
			input.setImeActionLabel(getString(R.string.delete_account),EditorInfo.IME_ACTION_DONE);
			builder.setTitle(getString(R.string.delete_account));
			builder.setMessage(getString(R.string.delete_account_text_last_step));
			builder.setNegativeButton(getString(R.string.general_dismiss), null);
			builder.setPositiveButton(getString(R.string.delete_account),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					});
		}
		else{
			logDebug("changeMail action");
			input.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						String pass = input.getText().toString().trim();
						if(pass.equals("")||pass.isEmpty()){
							logWarning("Input is empty");
							input.setError(getString(R.string.invalid_string));
							input.requestFocus();
						}
						else {
							logDebug("Action DONE ime - change mail");
							aC.confirmChangeMail(confirmationLink, pass);
							insertPassDialog.dismiss();
						}
					}
					else{
						logDebug("Other IME" + actionId);
					}
					return false;
				}
			});
			input.setImeActionLabel(getString(R.string.change_pass),EditorInfo.IME_ACTION_DONE);
			builder.setTitle(getString(R.string.change_mail_title_last_step));
			builder.setMessage(getString(R.string.change_mail_text_last_step));
			builder.setNegativeButton(getString(android.R.string.cancel), null);
			builder.setPositiveButton(getString(R.string.change_pass),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					});
		}

		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				hideKeyboard(managerActivity, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});

		builder.setView(layout);
		insertPassDialog = builder.create();
		insertPassDialog.show();
		if(cancelAccount){
			builder.setNegativeButton(getString(R.string.general_dismiss), null);
			insertPassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					logDebug("OK BTTN PASSWORD");
					String pass = input.getText().toString().trim();
					if(pass.equals("")||pass.isEmpty()){
						logWarning("Input is empty");
						input.setError(getString(R.string.invalid_string));
						input.requestFocus();
					}
					else {
						logDebug("Positive button pressed - cancel account");
						aC.confirmDeleteAccount(confirmationLink, pass);
						insertPassDialog.dismiss();
					}
				}
			});
		}
		else{
			builder.setNegativeButton(getString(android.R.string.cancel), null);
			insertPassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					logDebug("OK BTTN PASSWORD");
					String pass = input.getText().toString().trim();
					if(pass.equals("")||pass.isEmpty()){
						logWarning("Input is empty");
						input.setError(getString(R.string.invalid_string));
						input.requestFocus();
					}
					else {
						logDebug("Positive button pressed - change mail");
						aC.confirmChangeMail(confirmationLink, pass);
						insertPassDialog.dismiss();
					}
				}
			});
		}
	}

	public void askConfirmationDeleteAccount(){
		logDebug("askConfirmationDeleteAccount");
		megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						aC.deleteAccount();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(getString(R.string.delete_account));

		builder.setMessage(getResources().getString(R.string.delete_account_text));

		builder.setPositiveButton(R.string.delete_account, dialogClickListener);
		builder.setNegativeButton(R.string.general_dismiss, dialogClickListener);
		builder.show();
	}

	private void showOpenLinkError(boolean show, int error) {
		if (openLinkDialog != null) {
			if (show) {
				openLinkDialogIsErrorShown = true;
				ColorUtils.setErrorAwareInputAppearance(openLinkText, true);
				openLinkError.setVisibility(View.VISIBLE);
				if (drawerItem == DrawerItem.CLOUD_DRIVE) {
					if (openLinkText.getText().toString().isEmpty()) {
						openLinkErrorText.setText(R.string.invalid_file_folder_link_empty);
						return;
					}
                    switch (error) {
                        case CHAT_LINK: {
							openLinkText.setTextColor(ColorUtils.getThemeColor(this,
									android.R.attr.textColorPrimary));
                            openLinkErrorText.setText(R.string.valid_chat_link);
                            openLinkOpenButton.setText(R.string.action_open_chat_link);
                            break;
                        }
                        case CONTACT_LINK: {
							openLinkText.setTextColor(ColorUtils.getThemeColor(this,
									android.R.attr.textColorPrimary));
                            openLinkErrorText.setText(R.string.valid_contact_link);
                            openLinkOpenButton.setText(R.string.action_open_contact_link);
                            break;
                        }
                        case ERROR_LINK: {
                            openLinkErrorText.setText(R.string.invalid_file_folder_link);
                            break;
                        }
                    }
                }
                else if (drawerItem == DrawerItem.CHAT) {
					if (openLinkText.getText().toString().isEmpty()) {
						openLinkErrorText.setText(R.string.invalid_chat_link_empty);
						return;
					}
                    openLinkErrorText.setText(R.string.invalid_chat_link_args);
                }
			}
			else {
				openLinkDialogIsErrorShown = false;
				if (openLinkError.getVisibility() == View.VISIBLE) {
					ColorUtils.setErrorAwareInputAppearance(openLinkText, false);
					openLinkError.setVisibility(View.GONE);
					openLinkOpenButton.setText(R.string.context_open_link);
				}
			}
		}
	}

	private void dismissOpenLinkDialog() {
		try {
			openLinkDialog.dismiss();
			openLinkDialogIsShown = false;
		} catch (Exception e) {}
	}

	private void openLink (String link) {
		// Password link
		if (matchRegexs(link, PASSWORD_LINK_REGEXS)) {
			dismissOpenLinkDialog();
			Intent openLinkIntent = new Intent(this, OpenPasswordLinkActivity.class);
			openLinkIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			openLinkIntent.setData(Uri.parse(link));
			startActivity(openLinkIntent);
			return;
		}

		if (drawerItem == DrawerItem.CLOUD_DRIVE) {
			int error = nC.importLink(link);
			if (openLinkError.getVisibility() == View.VISIBLE) {
                switch (error) {
                    case CHAT_LINK: {
						logDebug("Open chat link: correct chat link");
                        showChatLink(link);
                        dismissOpenLinkDialog();
                        break;
                    }
                    case CONTACT_LINK: {
						logDebug("Open contact link: correct contact link");
                        String[] s = link.split("C!");
                        if (s!= null && s.length>1) {
                            long handle = MegaApiAndroid.base64ToHandle(s[1].trim());
                            openContactLink(handle);
                            dismissOpenLinkDialog();
                        }
                        break;
                    }
                }
            }
            else {
                switch (error) {
                    case FILE_LINK:
                    case FOLDER_LINK: {
						logDebug("Do nothing: correct file or folder link");
                        dismissOpenLinkDialog();
                        break;
                    }
                    case CHAT_LINK:
                    case CONTACT_LINK:
                    case ERROR_LINK: {
						logWarning("Show error: invalid link or correct chat or contact link");
                        showOpenLinkError(true, error);
                        break;
                    }
                }
            }
		}
		else if (drawerItem == DrawerItem.CHAT) {
			megaChatApi.checkChatLink(link, managerActivity);
		}
	}

	private void showOpenLinkDialog() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_error_hint, null);
		builder.setView(v).setPositiveButton(R.string.context_open_link, null)
				.setNegativeButton(R.string.general_cancel, null);

		openLinkText = v.findViewById(R.id.text);

		openLinkText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable s) {
				showOpenLinkError(false, 0);
			}
		});

		openLinkText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboardView(managerActivity, v, 0);
					openLink(openLinkText.getText().toString());
					return true;
				}
				return false;
			}
		});

		Util.showKeyboardDelayed(openLinkText);

		openLinkError = v.findViewById(R.id.error);
		openLinkErrorText = v.findViewById(R.id.error_text);

		if (drawerItem == DrawerItem.CLOUD_DRIVE) {
			builder.setTitle(R.string.action_open_link);
			openLinkText.setHint(R.string.hint_paste_link);
		}
		else if (drawerItem == DrawerItem.CHAT) {
			builder.setTitle(R.string.action_open_chat_link);
			openLinkText.setHint(R.string.hint_enter_chat_link);
		}

		openLinkDialog = builder.create();
		openLinkDialog.setCanceledOnTouchOutside(false);

		try {
			openLinkDialog.show();
			openLinkText.requestFocus();
			openLinkDialogIsShown = true;

			// Set onClickListeners for buttons after showing the dialog would prevent
			// the dialog from dismissing automatically on clicking the buttons
			openLinkOpenButton = openLinkDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			openLinkOpenButton.setOnClickListener((view) -> {
				hideKeyboard(managerActivity, 0);
				openLink(openLinkText.getText().toString());
			});
			openLinkDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((view) ->
					dismissOpenLinkDialog());
			openLinkDialog.setOnKeyListener((dialog, keyCode, event) -> {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
					dismissOpenLinkDialog();
				}
				return true;
			});
		} catch (Exception e) {
			logError("Exception showing Open Link dialog", e);
		}
	}

	public void showChatLink(String link) {
		logDebug("Link: " + link);
		Intent openChatLinkIntent = new Intent(this, ChatActivityLollipop.class);

		if (joiningToChatLink) {
			openChatLinkIntent.setAction(ACTION_JOIN_OPEN_CHAT_LINK);
			resetJoiningChatLink();
		} else {
			openChatLinkIntent.setAction(ACTION_OPEN_CHAT_LINK);
		}

		openChatLinkIntent.setData(Uri.parse(link));
		startActivity(openChatLinkIntent);
		drawerItem = DrawerItem.CHAT;
		selectDrawerItemLollipop(drawerItem);
	}

	/**
	 * Initializes the variables to join chat by default.
	 */
	private void resetJoiningChatLink() {
		joiningToChatLink = false;
		linkJoinToChatLink = null;
	}

	public void checkPermissions(){
		typesCameraPermission = TAKE_PROFILE_PICTURE;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						REQUEST_WRITE_STORAGE);
			}

			boolean hasCameraPermission = checkPermission(Manifest.permission.CAMERA);
			if (!hasCameraPermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA},
						REQUEST_CAMERA);
			}

			if (hasStoragePermission && hasCameraPermission){
				this.takeProfilePicture();
			}
		}
		else{
			this.takeProfilePicture();
		}
	}

	public void takeProfilePicture(){
		checkTakePicture(this, TAKE_PICTURE_PROFILE_CODE);
	}

	public void showCancelMessage(){
		logDebug("showCancelMessage");
		AlertDialog cancelDialog;
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
//		builder.setTitle(getString(R.string.title_cancel_subscriptions));

		LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_cancel_subscriptions, null);
		TextView message = (TextView) dialogLayout.findViewById(R.id.dialog_cancel_text);
		final EditText text = (EditText) dialogLayout.findViewById(R.id.dialog_cancel_feedback);

		float density = getResources().getDisplayMetrics().density;

		float scaleW = getScaleW(outMetrics, density);

		message.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleW));
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleW));

		builder.setView(dialogLayout);

		builder.setPositiveButton(getString(R.string.send_cancel_subscriptions), new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				feedback = text.getText().toString();
				if(feedback.matches("")||feedback.isEmpty()){
					showSnackbar(SNACKBAR_TYPE, getString(R.string.reason_cancel_subscriptions), -1);
				}
				else{
					showCancelConfirmation(feedback);
				}
			}
		});

		builder.setNegativeButton(getString(R.string.general_dismiss), new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		cancelDialog = builder.create();
		cancelDialog.show();
//		brandAlertDialog(cancelDialog);
	}

	public void showPresenceStatusDialog(){
		logDebug("showPresenceStatusDialog");

		MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
		final CharSequence[] items = {getString(R.string.online_status), getString(R.string.away_status), getString(R.string.busy_status), getString(R.string.offline_status)};
		int statusToShow = megaChatApi.getOnlineStatus();
		switch(statusToShow){
			case MegaChatApi.STATUS_ONLINE:{
				statusToShow = 0;
				break;
			}
			case MegaChatApi.STATUS_AWAY:{
				statusToShow = 1;
				break;
			}
			case MegaChatApi.STATUS_BUSY:{
				statusToShow = 2;
				break;
			}
			case MegaChatApi.STATUS_OFFLINE:{
				statusToShow = 3;
				break;
			}
		}
		dialogBuilder.setSingleChoiceItems(items, statusToShow, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				presenceStatusDialog.dismiss();
				switch(item) {
					case 0:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_ONLINE, managerActivity);
						break;
					}
					case 1:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_AWAY, managerActivity);
						break;
					}
					case 2:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_BUSY, managerActivity);
						break;
					}
					case 3:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_OFFLINE, managerActivity);
						break;
					}
				}
			}
		});
		dialogBuilder.setTitle(getString(R.string.status_label));
		presenceStatusDialog = dialogBuilder.create();
		presenceStatusDialog.show();
	}

	public void showCancelConfirmation(final String feedback){
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        {
						logDebug("Feedback: " + feedback);
			        	megaApi.creditCardCancelSubscriptions(feedback, managerActivity);
			        	break;
			        }
			        case DialogInterface.BUTTON_NEGATIVE:
			        {
			            //No button clicked
						logDebug("Feedback: " + feedback);
			            break;
			        }
		        }
		    }
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setMessage(R.string.confirmation_cancel_subscriptions).setPositiveButton(R.string.general_yes, dialogClickListener)
		    .setNegativeButton(R.string.general_no, dialogClickListener).show();

	}

	@Override
	public void uploadFromDevice() {
		chooseFromDevice(this);
	}

	@Override
	public void uploadFromSystem() {
		pickFileFromFileSystem(this);
	}

	@Override
	public void takePictureAndUpload() {
		if (!hasPermissions(this, Manifest.permission.CAMERA)) {
			setTypesCameraPermission(TAKE_PICTURE_OPTION);
			requestPermission(this, REQUEST_CAMERA, Manifest.permission.CAMERA);
			return;
		}
		if (!hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			requestPermission(this, REQUEST_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			return;
		}
		checkTakePicture(this, TAKE_PHOTO_CODE);
	}

    @Override
    public void scanDocument() {
        String[] saveDestinations = {
				StringResourcesUtils.getString(R.string.section_cloud_drive),
				StringResourcesUtils.getString(R.string.section_chat)
        };
        Intent intent = DocumentScannerActivity.getIntent(this, saveDestinations);
        startActivityForResult(intent, REQUEST_CODE_SCAN_DOCUMENT);
    }

	@Override
	public void showNewFolderDialog() {
		MegaNodeDialogUtil.showNewFolderDialog(this, this);
	}

	@Override
	public void showNewTextFileDialog(String typedName) {
		newTextFileDialog = MegaNodeDialogUtil.showNewTxtFileDialog(this,
				getCurrentParentNode(getCurrentParentHandle(), INVALID_VALUE), typedName,
				drawerItem == DrawerItem.HOMEPAGE);
	}

	public long getParentHandleBrowser() {
		if (parentHandleBrowser == -1) {
			MegaNode rootNode = megaApi.getRootNode();
			parentHandleBrowser = rootNode != null ? rootNode.getParentHandle() : parentHandleBrowser;
		}

		return parentHandleBrowser;
	}

	private long getCurrentParentHandle() {
		long parentHandle = -1;

		switch (drawerItem) {
            case HOMEPAGE:
                // For home page, its parent is always the root of cloud drive.
                parentHandle = megaApi.getRootNode().getHandle();
                break;
			case CLOUD_DRIVE:
				parentHandle = getParentHandleBrowser();
				break;

			case SHARED_ITEMS:
				if (viewPagerShares == null) break;

				if (getTabItemShares() == INCOMING_TAB) {
					parentHandle = parentHandleIncoming;
				} else if (getTabItemShares() == OUTGOING_TAB) {
					parentHandle = parentHandleOutgoing;
				} else if (getTabItemShares() == LINKS_TAB) {
				    parentHandle = parentHandleLinks;
                }
				break;

			case SEARCH:
				if (parentHandleSearch != -1) {
					parentHandle = parentHandleSearch;
					break;
				}
				switch (searchDrawerItem) {
					case CLOUD_DRIVE:
						parentHandle = getParentHandleBrowser();
						break;
					case SHARED_ITEMS:
						if (searchSharedTab == INCOMING_TAB) {
							parentHandle = parentHandleIncoming;
						} else if (searchSharedTab == OUTGOING_TAB) {
							parentHandle = parentHandleOutgoing;
						} else if (searchSharedTab == LINKS_TAB) {
						    parentHandle = parentHandleLinks;
                        }
						break;
                    case INBOX:
                        parentHandle = getParentHandleInbox();
                        break;
				}
				break;

			default:
				return parentHandle;
		}

		return parentHandle;
	}

	private MegaNode getCurrentParentNode(long parentHandle, int error) {
		String errorString = null;

		if (error != -1) {
			errorString = getString(error);
		}

		if (parentHandle == -1 && errorString != null) {
			showSnackbar(SNACKBAR_TYPE, errorString, -1);
			logDebug(errorString + ": parentHandle == -1");
			return null;
		}

		MegaNode parentNode = megaApi.getNodeByHandle(parentHandle);

		if (parentNode == null && errorString != null){
			showSnackbar(SNACKBAR_TYPE, errorString, -1);
			logDebug(errorString + ": parentNode == null");
			return null;
		}

		return parentNode;
	}

	@Override
	public void createFolder(@NotNull String title) {
		logDebug("createFolder");
		if (!isOnline(this)){
			showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
			return;
		}

		if(isFinishing()){
			return;
		}

		MegaNode parentNode = getCurrentParentNode(getCurrentParentHandle(), R.string.context_folder_no_created);
		if (parentNode == null) return;

		ArrayList<MegaNode> nL = megaApi.getChildren(parentNode);
		for (int i = 0; i < nL.size(); i++) {
			if (title.compareTo(nL.get(i).getName()) == 0) {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_folder_already_exists), -1);
				logDebug("Folder not created: folder already exists");
				return;
			}
		}

		statusDialog = null;
		try {
			statusDialog = new ProgressDialog(this);
			statusDialog.setMessage(getString(R.string.context_creating_folder));
			statusDialog.show();
		}
		catch(Exception e){
			logDebug("Exception showing 'Creating folder' dialog");
			e.printStackTrace();
			return;
		}

		megaApi.createFolder(title, parentNode, this);
	}

	public void showClearRubbishBinDialog(){
		logDebug("showClearRubbishBinDialog");

		rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
		if (rubbishBinFLol != null) {
			if (rubbishBinFLol.isVisible()) {
				rubbishBinFLol.notifyDataSetChanged();
			}
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(getString(R.string.context_clear_rubbish));
		builder.setMessage(getString(R.string.clear_rubbish_confirmation));
		builder.setPositiveButton(getString(R.string.general_clear),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						nC.cleanRubbishBin();
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		clearRubbishBinDialog = builder.create();
		clearRubbishBinDialog.show();
	}

	public void chooseAddContactDialog(boolean isMegaContact) {
		logDebug("chooseAddContactDialog");
		if (isMegaContact) {
			if (megaApi != null && megaApi.getRootNode() != null) {
				Intent intent = new Intent(this, AddContactActivityLollipop.class);
				intent.putExtra("contactType", CONTACT_TYPE_MEGA);
				startActivityForResult(intent, REQUEST_CREATE_CHAT);
			}
			else{
				logWarning("Online but not megaApi");
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
			}
		}
		else{
			addContactFromPhone();
		}
	}

	public void addContactFromPhone() {
		Intent in = new Intent(this, InviteContactActivity.class);
		in.putExtra("contactType", CONTACT_TYPE_DEVICE);
		startActivityForResult(in, REQUEST_INVITE_CONTACT_FROM_DEVICE);
	}

	public void showConfirmationRemoveContact(final MegaUser c){
		logDebug("showConfirmationRemoveContact");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeContact(c);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		String title = getResources().getQuantityString(R.plurals.title_confirmation_remove_contact, 1);
		builder.setTitle(title);
		String message= getResources().getQuantityString(R.plurals.confirmation_remove_contact, 1);
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveContacts(final ArrayList<MegaUser> c){
		logDebug("showConfirmationRemoveContacts");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeMultipleContacts(c);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		String title = getResources().getQuantityString(R.plurals.title_confirmation_remove_contact, c.size());
		builder.setTitle(title);
		String message= getResources().getQuantityString(R.plurals.confirmation_remove_contact, c.size());
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveContactRequest(final MegaContactRequest r){
		logDebug("showConfirmationRemoveContactRequest");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeInvitationContact(r);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		String message= getResources().getString(R.string.confirmation_delete_contact_request,r.getTargetEmail());
		builder.setMessage(message).setPositiveButton(R.string.context_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveContactRequests(final List<MegaContactRequest> r){
		logDebug("showConfirmationRemoveContactRequests");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.deleteMultipleSentRequestContacts(r);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		String message="";
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		if(r.size()==1){
			message= getResources().getString(R.string.confirmation_delete_contact_request,r.get(0).getTargetEmail());
		}else{
			message= getResources().getString(R.string.confirmation_remove_multiple_contact_request,r.size());
		}

		builder.setMessage(message).setPositiveButton(R.string.context_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveAllSharingContacts(final List<MegaNode> shares) {
		if (shares.size() == 1) {
			showConfirmationRemoveAllSharingContacts(megaApi.getOutShares(shares.get(0)), shares.get(0));
			return;
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setMessage(getString(R.string.alert_remove_several_shares, shares.size()))
				.setPositiveButton(R.string.general_remove, (dialog, which) -> nC.removeSeveralFolderShares(shares))
				.setNegativeButton(R.string.general_cancel, (dialog, which) -> {})
				.show();
	}

	public void showConfirmationRemoveAllSharingContacts (final ArrayList<MegaShare> shareList, final MegaNode n){
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		int size = shareList.size();
		String message = getResources().getQuantityString(R.plurals.confirmation_remove_outgoing_shares, size, size);

		builder.setMessage(message)
				.setPositiveButton(R.string.general_remove, (dialog, which) -> nC.removeShares(shareList, n))
				.setNegativeButton(R.string.general_cancel, (dialog, which) -> {})
				.show();
	}

	/**
	 * Save nodes to device.
	 *
	 * @param nodes nodes to save
	 * @param highPriority whether this download is high priority or not
	 * @param isFolderLink whether this download is a folder link
	 * @param fromMediaViewer whether this download is from media viewer
	 * @param fromChat whether this download is from chat
	 */
	public void saveNodesToDevice(List<MegaNode> nodes, boolean highPriority, boolean isFolderLink,
								  boolean fromMediaViewer, boolean fromChat) {
		nodeSaver.saveNodes(nodes, highPriority, isFolderLink, fromMediaViewer, fromChat);
	}

	/**
	 * Save nodes to gallery.
	 *
	 * @param nodes nodes to save
	 */
	public void saveNodesToGallery(List<MegaNode> nodes) {
		nodeSaver.saveNodes(nodes, false, false, false, true, true);
	}

	/**
	 * Save nodes to device.
	 *
	 * @param handles handles of nodes to save
	 * @param highPriority whether this download is high priority or not
	 * @param isFolderLink whether this download is a folder link
	 * @param fromMediaViewer whether this download is from media viewer
	 * @param fromChat whether this download is from chat
	 */
	public void saveHandlesToDevice(List<Long> handles, boolean highPriority, boolean isFolderLink,
								  boolean fromMediaViewer, boolean fromChat) {
		nodeSaver.saveHandles(handles, highPriority, isFolderLink, fromMediaViewer, fromChat);
	}

	/**
	 * Save offline nodes to device.
	 *
	 * @param nodes nodes to save
	 */
	public void saveOfflineNodesToDevice(List<MegaOffline> nodes) {
		nodeSaver.saveOfflineNodes(nodes, false);
	}

	/**
	 * Attach node to chats, only used by NodeOptionsBottomSheetDialogFragment.
	 *
	 * @param node node to attach
	 */
	public void attachNodeToChats(MegaNode node) {
		nodeAttacher.attachNode(node);
	}

	/**
	 * Attach nodes to chats, used by ActionMode of manager fragments.
	 *
	 * @param nodes nodes to attach
	 */
	public void attachNodesToChats(List<MegaNode> nodes) {
		nodeAttacher.attachNodes(nodes);
	}

	public void showConfirmationRemovePublicLink (final MegaNode n){
		logDebug("showConfirmationRemovePublicLink");

		if (showTakenDownNodeActionNotAvailableDialog(n, this)) {
			return;
		}

		ArrayList<MegaNode> nodes = new ArrayList<>();
		nodes.add(n);
		showConfirmationRemoveSeveralPublicLinks(nodes);
	}

	public void showConfirmationRemoveSeveralPublicLinks(ArrayList<MegaNode> nodes) {
		if (nodes == null) {
			logWarning("nodes == NULL");
		}

		String message;
		MegaNode node = null;

		if (nodes.size() == 1) {
			node = nodes.get(0);
			message = getResources().getQuantityString(R.plurals.remove_links_warning_text, 1);
		} else {
			message = getResources().getQuantityString(R.plurals.remove_links_warning_text, nodes.size());
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		MegaNode finalNode = node;
		builder.setMessage(message)
				.setPositiveButton(R.string.general_remove, (dialog, which) -> {
					if (finalNode != null) {
						if (!isOnline(managerActivity)){
							showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
							return;
						}
						nC.removeLink(finalNode, new ExportListener(managerActivity, 1));
					} else {
						nC.removeLinks(nodes);
					}
				})
				.setNegativeButton(R.string.general_cancel, (dialog, which) -> {})
				.show();

		refreshAfterMovingToRubbish();
	}

	@Override
	public void confirmLeaveChat(long chatId) {
		megaChatApi.leaveChat(chatId, new RemoveFromChatRoomListener(this));
	}

	@Override
	public void confirmLeaveChats(@NotNull List<? extends MegaChatListItem> chats) {
		if (getChatsFragment() != null) {
			rChatFL.clearSelections();
			rChatFL.hideMultipleSelect();
		}

		for (MegaChatListItem chat : chats) {
			if (chat != null) {
				megaChatApi.leaveChat(chat.getChatId(), new RemoveFromChatRoomListener(this));
			}
		}
	}

	@Override
	public void leaveChatSuccess() {
		// No update needed.
	}

	public void showConfirmationResetPasswordFromMyAccount (){
		logDebug("showConfirmationResetPasswordFromMyAccount");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
						if(maFLol!=null){
							maFLol.resetPass();
						}
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		String message= getResources().getString(R.string.email_verification_text_change_pass);
		builder.setMessage(message).setPositiveButton(R.string.general_ok, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationResetPassword (final String link){
		logDebug("Link: " + link);

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						Intent intent = new Intent(managerActivity, ChangePasswordActivityLollipop.class);
						intent.setAction(ACTION_RESET_PASS_FROM_LINK);
						intent.setData(Uri.parse(link));
						String key = megaApi.exportMasterKey();
						intent.putExtra("MK", key);
						startActivity(intent);
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle(getResources().getString(R.string.title_dialog_insert_MK));
		String message= getResources().getString(R.string.text_reset_pass_logged_in);
		builder.setMessage(message).setPositiveButton(R.string.pin_lock_enter, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void cameraUploadsClicked(){
		logDebug("cameraUplaodsClicked");
		drawerItem = DrawerItem.CAMERA_UPLOADS;
		setBottomNavigationMenuItemChecked(CAMERA_UPLOADS_BNV);
		selectDrawerItemLollipop(drawerItem);
	}

	public void skipInitialCUSetup() {
		setFirstLogin(false);
		drawerItem = DrawerItem.HOMEPAGE;
		setBottomNavigationMenuItemChecked(HOMEPAGE_BNV);
		selectDrawerItemLollipop(drawerItem);
	}

	public void refreshCameraUpload(){
		drawerItem = DrawerItem.CAMERA_UPLOADS;
		setBottomNavigationMenuItemChecked(CAMERA_UPLOADS_BNV);
		setToolbarTitle();
		refreshFragment(FragmentTag.CAMERA_UPLOADS.getTag());
	}

	/**
	 * Checks if should update some cu view visibility.
	 *
	 * @param visibility New requested visibility update.
	 * @return True if should apply the visibility update, false otherwise.
	 */
	private boolean rightCUVisibilityChange(int visibility) {
		return drawerItem == DrawerItem.CAMERA_UPLOADS || visibility == View.GONE;
	}

	/**
	 * Updates cuViewTypes view visibility.
	 *
	 * @param visibility New visibility value to set.
	 */
	public void updateCUViewTypes(int visibility) {
		if (rightCUVisibilityChange(visibility)) {
			cuViewTypes.setVisibility(visibility);
		}
	}

	/**
	 * Updates cuLayout view visibility.
	 *
	 * @param visibility New visibility value to set.
	 */
	public void updateCULayout(int visibility) {
		if (rightCUVisibilityChange(visibility)) {
			cuLayout.setVisibility(visibility);
		}
	}

	/**
	 * Updates enableCUButton view visibility and cuLayout if needed.
	 *
	 * @param visibility New visibility value to set.
	 */
	public void updateEnableCUButton(int visibility) {
		if (enableCUButton.getVisibility() == visibility) {
			return;
		}

		if ((visibility == View.GONE && cuProgressBar.getVisibility() == View.GONE)
				|| (visibility == View.VISIBLE && cuLayout.getVisibility() == View.GONE)) {
			updateCULayout(visibility);
		}

		if (rightCUVisibilityChange(visibility)) {
			enableCUButton.setVisibility(visibility);
		}
	}

	/**
	 * Hides the CU progress bar.
	 */
	public void hideCUProgress() {
		cuProgressBar.setVisibility(View.GONE);
	}

	/**
	 * Updates the CU progress view.
	 *
	 * @param progress The current progress.
	 * @param pending  The number of pending uploads.
	 */
	public void updateCUProgress(int progress, int pending) {
		if (drawerItem != DrawerItem.CAMERA_UPLOADS || getCameraUploadFragment() == null
				|| !cuFragment.shouldShowFullInfoAndOptions()) {
			return;
		}

		boolean visible = pending > 0;
		int visibility = visible ? View.VISIBLE : View.GONE;

		if ((!visible && enableCUButton.getVisibility() == View.GONE)
				|| (visible && cuLayout.getVisibility() == View.GONE)) {
			updateCULayout(visibility);
		}

		if (getCameraUploadFragment() != null) {
			cuFragment.updateProgress(visibility, pending);
		}

		cuProgressBar.setVisibility(visibility);
		cuProgressBar.setProgress(progress);
	}

	/**
	 * Shows or hides the cuLayout and animates the transition.
	 *
	 * @param hide True if should hide it, false if should show it.
	 */
	public void animateCULayout(boolean hide) {
		boolean visible = cuLayout.getVisibility() == View.VISIBLE;
		if ((hide && !visible) || !hide && visible) {
			return;
		}

		if (hide) {
			cuLayout.animate().translationY(-100).setDuration(ANIMATION_DURATION)
					.withEndAction(() -> cuLayout.setVisibility(View.GONE)).start();
		} else if (drawerItem == DrawerItem.CAMERA_UPLOADS) {
			cuLayout.setVisibility(View.VISIBLE);
			cuLayout.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
		}
	}

	/**
	 * Shows the bottom sheet to manage a completed transfer.
	 *
	 * @param transfer	the completed transfer to manage.
	 */
	public void showManageTransferOptionsPanel(AndroidCompletedTransfer transfer) {
		if (transfer == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedTransfer = transfer;
		bottomSheetDialogFragment =  new ManageTransferBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showNodeOptionsPanel(MegaNode node){
		showNodeOptionsPanel(node, NodeOptionsBottomSheetDialogFragment.MODE0);
	}

	public void showNodeOptionsPanel(MegaNode node, int mode) {
		logDebug("showNodeOptionsPanel");

		if (node == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedNode = node;
		bottomSheetDialogFragment = new NodeOptionsBottomSheetDialogFragment(mode);
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showNodeLabelsPanel(@NonNull MegaNode node) {
        logDebug("showNodeLabelsPanel");

        if (isBottomSheetDialogShown(bottomSheetDialogFragment)) {
            bottomSheetDialogFragment.dismiss();
        }

        selectedNode = node;
        bottomSheetDialogFragment = NodeLabelBottomSheetDialogFragment.newInstance(node.getHandle());
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

	public void showOptionsPanel(MegaOffline sNode){
		logDebug("showNodeOptionsPanel-Offline");

		if (sNode == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedOfflineNode = sNode;
		bottomSheetDialogFragment = new OfflineOptionsBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showNewSortByPanel(int orderType) {
		showNewSortByPanel(orderType, false);
	}

	public void showNewSortByPanel(int orderType, boolean isIncomingRootOrder) {
		if (isBottomSheetDialogShown(bottomSheetDialogFragment)) {
			return;
		}

		bottomSheetDialogFragment = SortByBottomSheetDialogFragment.newInstance(orderType, isIncomingRootOrder);

		bottomSheetDialogFragment.show(getSupportFragmentManager(),
				bottomSheetDialogFragment.getTag());
	}

	public void showOfflineFileInfo(MegaOffline node) {
		Intent intent = new Intent(this, OfflineFileInfoActivity.class);
		intent.putExtra(HANDLE, node.getHandle());
		startActivity(intent);
	}

	public void showContactOptionsPanel(MegaContactAdapter user){
		logDebug("showContactOptionsPanel");

		if(!isOnline(this)){
			showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
			return;
		}

		if (user == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedUser = user;
		bottomSheetDialogFragment = new ContactsBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showSentRequestOptionsPanel(MegaContactRequest request){
		logDebug("showSentRequestOptionsPanel");
		if (request == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedRequest = request;
		bottomSheetDialogFragment = new SentRequestBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showReceivedRequestOptionsPanel(MegaContactRequest request){
		logDebug("showReceivedRequestOptionsPanel");

		if (request == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedRequest = request;
		bottomSheetDialogFragment = new ReceivedRequestBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showMyAccountOptionsPanel() {
		logDebug("showMyAccountOptionsPanel");

		if (isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		bottomSheetDialogFragment = new MyAccountBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	/**
	 * Shows the GENERAL_UPLOAD upload bottom sheet fragment.
	 */
	public void showUploadPanel() {
		showUploadPanel(drawerItem == DrawerItem.HOMEPAGE ? HOMEPAGE_UPLOAD : GENERAL_UPLOAD);
	}

	/**
	 * Shows the upload bottom sheet fragment taking into account the upload type received as param.
	 *
	 * @param uploadType Indicates the type of upload:
	 *                   - GENERAL_UPLOAD if nothing special has to be taken into account.
	 *                   - DOCUMENTS_UPLOAD if an upload from Documents section.
	 */
	public void showUploadPanel(int uploadType) {
		if (!hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			requestPermission(this, REQUEST_READ_WRITE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
			return;
		}

		if (isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		bottomSheetDialogFragment = UploadBottomSheetDialogFragment.newInstance(uploadType);
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void updateAccountDetailsVisibleInfo(){
		logDebug("updateAccountDetailsVisibleInfo");
		if(isFinishing()){
			return;
		}

		if (app == null || app.getMyAccountInfo() == null) {
			return;
		}

		MyAccountInfo info = app.getMyAccountInfo();
		View settingsSeparator = null;

		if (nV != null) {
			settingsSeparator = nV.findViewById(R.id.settings_separator);
		}

		if (usedSpaceLayout != null) {
			if (megaApi.isBusinessAccount()) {
				usedSpaceLayout.setVisibility(View.GONE);
				upgradeAccount.setVisibility(View.GONE);
				if (settingsSeparator != null) {
					settingsSeparator.setVisibility(View.GONE);
				}
				if (megaApi.isBusinessAccount()) {
					businessLabel.setVisibility(View.VISIBLE);
				}

				if (getSettingsFragment() != null) {
					sttFLol.updateCancelAccountSetting();
				}
			} else {
				businessLabel.setVisibility(View.GONE);
				upgradeAccount.setVisibility(View.VISIBLE);
				if (settingsSeparator != null) {
					settingsSeparator.setVisibility(View.GONE);
				}

				String textToShow = String.format(getResources().getString(R.string.used_space), info.getUsedFormatted(), info.getTotalFormatted());
                String colorString = ColorUtils.getThemeColorHexString(this, R.attr.colorSecondary);
				switch (storageState) {
                    case MegaApiJava.STORAGE_STATE_GREEN:
                        break;
                    case MegaApiJava.STORAGE_STATE_ORANGE:
                        colorString = ColorUtils.getColorHexString(this, R.color.amber_600_amber_300);
                        break;
                    case MegaApiJava.STORAGE_STATE_RED:
                    case MegaApiJava.STORAGE_STATE_PAYWALL:
                        ((MegaApplication) getApplication()).getMyAccountInfo().setUsedPerc(100);
                        colorString = ColorUtils.getColorHexString(this, R.color.red_600_red_300);
                        break;
                }

				try {
					textToShow = textToShow.replace("[A]", "<font color=\'"
							+ colorString
							+ "\'>");
					textToShow = textToShow.replace("[/A]", "</font>");
					textToShow = textToShow.replace("[B]", "<font color=\'"
							+ ColorUtils.getThemeColorHexString(this, android.R.attr.textColorPrimary)
							+ "\'>");
					textToShow = textToShow.replace("[/B]", "</font>");
				} catch (Exception e) {
					logWarning("Exception formatting string", e);
				}
				spaceTV.setText(HtmlCompat.fromHtml(textToShow, HtmlCompat.FROM_HTML_MODE_LEGACY));
				int progress = info.getUsedPerc();
				long usedSpace = info.getUsedStorage();
				logDebug("Progress: " + progress + ", Used space: " + usedSpace);
				usedSpacePB.setProgress(progress);
				if (progress >= 0 && usedSpace >= 0) {
					usedSpaceLayout.setVisibility(View.VISIBLE);
				} else {
					usedSpaceLayout.setVisibility(View.GONE);
				}
			}
		}
		else{
			logWarning("usedSpaceLayout is NULL");
		}

		updateSubscriptionLevel(app.getMyAccountInfo());

        int resId = R.drawable.custom_progress_bar_horizontal_ok;
        switch (storageState) {
            case MegaApiJava.STORAGE_STATE_GREEN:
                break;
            case MegaApiJava.STORAGE_STATE_ORANGE:
                resId = R.drawable.custom_progress_bar_horizontal_warning;
                break;
            case MegaApiJava.STORAGE_STATE_RED:
            case MegaApiJava.STORAGE_STATE_PAYWALL:
                ((MegaApplication) getApplication()).getMyAccountInfo().setUsedPerc(100);
                resId = R.drawable.custom_progress_bar_horizontal_exceed;
                break;
        }
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), resId, null);
        usedSpacePB.setProgressDrawable(drawable);
	}

	public void refreshContactsOrder() {
		if (getContactsFragment() != null) {
			cFLol.sortBy();
		}
	}

	public void refreshCloudDrive() {
        if (rootNode == null) {
            rootNode = megaApi.getRootNode();
        }

        if (rootNode == null) {
            logWarning("Root node is NULL. Maybe user is not logged in");
            return;
        }

		MegaNode parentNode = rootNode;

		if (isCloudAdded()) {
			ArrayList<MegaNode> nodes;
			if (parentHandleBrowser == -1) {
				nodes = megaApi.getChildren(parentNode, sortOrderManagement.getOrderCloud());
			} else {
				parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
				if (parentNode == null) return;

				nodes = megaApi.getChildren(parentNode, sortOrderManagement.getOrderCloud());
			}
			logDebug("Nodes: " + nodes.size());
			fbFLol.hideMultipleSelect();
			fbFLol.setNodes(nodes);
			fbFLol.getRecyclerView().invalidate();
		}
	}

	private void refreshSharesPageAdapter() {
		if (sharesPageAdapter != null) {
			sharesPageAdapter.notifyDataSetChanged();
			setSharesTabIcons(getTabItemShares());
		}
	}

	public void refreshCloudOrder(int order) {
		LiveEventBus.get(EVENT_ORDER_CHANGE, Integer.class).post(order);

		//Refresh Cloud Fragment
		refreshCloudDrive();

		//Refresh Rubbish Fragment
		refreshRubbishBin();

		onNodesSharedUpdate();

		if (getInboxFragment() != null) {
			MegaNode inboxNode = megaApi.getInboxNode();
			if (inboxNode != null) {
				ArrayList<MegaNode> nodes = megaApi.getChildren(inboxNode, order);
				iFLol.setNodes(nodes);
				iFLol.getRecyclerView().invalidate();
			}
		}

		refreshSearch();
	}

	public void refreshOthersOrder(){
		refreshSharesPageAdapter();
		refreshSearch();
	}

	public void refreshCUNodes() {
		if (getCameraUploadFragment() != null) {
			cuFragment.reloadNodes();
		}
	}

	public void showStatusDialog(String text){
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(managerActivity);
			temp.setMessage(text);
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
	}

	public void dismissStatusDialog(){
		if (statusDialog != null){
			try{
				statusDialog.dismiss();
			}
			catch(Exception ex){}
		}
	}

	public void setFirstNavigationLevel(boolean firstNavigationLevel){
		logDebug("Set value to: " + firstNavigationLevel);
		this.firstNavigationLevel = firstNavigationLevel;
	}

	public boolean isFirstNavigationLevel() {
		return firstNavigationLevel;
	}

	public void setParentHandleBrowser(long parentHandleBrowser){
		logDebug("Set value to:" + parentHandleBrowser);

		this.parentHandleBrowser = parentHandleBrowser;
	}

	public void setParentHandleRubbish(long parentHandleRubbish){
		logDebug("setParentHandleRubbish");
		this.parentHandleRubbish = parentHandleRubbish;
	}

	public void setParentHandleSearch(long parentHandleSearch){
		logDebug("setParentHandleSearch");
		this.parentHandleSearch = parentHandleSearch;
	}

	public void setParentHandleIncoming(long parentHandleIncoming){
		logDebug("setParentHandleIncoming: " + parentHandleIncoming);
		this.parentHandleIncoming = parentHandleIncoming;
	}

	public void setParentHandleInbox(long parentHandleInbox){
		logDebug("setParentHandleInbox: " + parentHandleInbox);
		this.parentHandleInbox = parentHandleInbox;
	}

	public void setParentHandleOutgoing(long parentHandleOutgoing){
		logDebug("Outgoing parent handle: " + parentHandleOutgoing);
		this.parentHandleOutgoing = parentHandleOutgoing;
	}

	@Override
	protected void onNewIntent(Intent intent){
		logDebug("onNewIntent");

    	if(intent != null) {
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				searchQuery = intent.getStringExtra(SearchManager.QUERY);
				parentHandleSearch = -1;
				setToolbarTitle();
				isSearching = true;

				if (searchMenuItem != null) {
					MenuItemCompat.collapseActionView(searchMenuItem);
				}
				return;
			}
//			When the user clicks on settings option in QR section, set drawerItem to SETTINGS and scroll to auto-accept setting
			else if (intent.getBooleanExtra("fromQR", false)){
				Bundle bundle = intent.getExtras();
				if (bundle.getSerializable("drawerItemQR") != null){
					if (DrawerItem.SETTINGS.equals(bundle.getSerializable("drawerItemQR"))){
						logDebug("From QR Settings");
						moveToSettingsSectionQR();
					}
				}
				return;
			} else if(ACTION_SHOW_UPGRADE_ACCOUNT.equals(intent.getAction())) {
				navigateToUpgradeAccount();
				return;
			} else if (ACTION_SHOW_TRANSFERS.equals(intent.getAction())){
				if (intent.getBooleanExtra(OPENED_FROM_CHAT, false)) {
					sendBroadcast(new Intent(ACTION_CLOSE_CHAT_AFTER_OPEN_TRANSFERS));
				}

				drawerItem = DrawerItem.TRANSFERS;
				indexTransfers = intent.getIntExtra(TRANSFERS_TAB, ERROR_TAB);
				selectDrawerItemLollipop(drawerItem);
				return;
			}

		}
     	super.onNewIntent(intent);
    	setIntent(intent);
	}

	public void navigateToUpgradeAccount(){
		logDebug("navigateToUpgradeAccount");
		setBottomNavigationMenuItemChecked(HIDDEN_BNV);

		getProLayout.setVisibility(View.GONE);
		drawerItemPreUpgradeAccount = drawerItem;
		drawerItem = DrawerItem.ACCOUNT;
		accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
		displayedAccountType = -1;
		selectDrawerItemLollipop(drawerItem);
	}

	public void navigateToAchievements(){
		logDebug("navigateToAchievements");
		drawerItem = DrawerItem.ACCOUNT;
		setBottomNavigationMenuItemChecked(HIDDEN_BNV);
		getProLayout.setVisibility(View.GONE);
		accountFragment = MY_ACCOUNT_FRAGMENT;
		displayedAccountType = -1;
		selectDrawerItemLollipop(drawerItem);

		Intent intent = new Intent(this, AchievementsActivity.class);
		startActivity(intent);
	}

	public void navigateToContacts(int index){
		drawerItem = DrawerItem.CONTACTS;
		indexContacts = index;
		selectDrawerItemLollipop(drawerItem);
	}

	public void navigateToMyAccount(){
		logDebug("navigateToMyAccount");
		drawerItem = DrawerItem.ACCOUNT;
		setBottomNavigationMenuItemChecked(HIDDEN_BNV);
		getProLayout.setVisibility(View.GONE);
		accountFragment = MY_ACCOUNT_FRAGMENT;
		displayedAccountType = -1;
		comesFromNotifications = true;
		selectDrawerItemLollipop(drawerItem);
	}

	@Override
	public void onClick(View v) {
		logDebug("onClick");

		DrawerItem oldDrawerItem = drawerItem;
		boolean sectionClicked = false;

		switch(v.getId()){
			case R.id.navigation_drawer_add_phone_number_button:{
                Intent intent = new Intent(this,SMSVerificationActivity.class);
                startActivity(intent);
				break;
			}
			case R.id.btnLeft_cancel:{
				getProLayout.setVisibility(View.GONE);
				break;
			}
			case R.id.btnRight_upgrade:{
				//Add navigation to Upgrade Account
				logDebug("Click on Upgrade in pro panel!");
				navigateToUpgradeAccount();
				break;
			}
			case R.id.enable_2fa_button: {
				if (enable2FADialog != null) {
					enable2FADialog.dismiss();
				}
				isEnable2FADialogShown = false;
				Intent intent = new Intent(this, TwoFactorAuthenticationActivity.class);
				intent.putExtra(EXTRA_NEW_ACCOUNT, true);
				startActivity(intent);
				break;
			}
			case R.id.skip_enable_2fa_button: {
				isEnable2FADialogShown = false;
				if (enable2FADialog != null) {
					enable2FADialog.dismiss();
				}
				break;
			}
			case R.id.navigation_drawer_account_section:
			case R.id.my_account_section: {
				if (isOnline(this) && megaApi.getRootNode()!=null) {
					sectionClicked = true;
					drawerItem = DrawerItem.ACCOUNT;
					accountFragment = MY_ACCOUNT_FRAGMENT;
				}
				break;
			}
			case R.id.inbox_section: {
				sectionClicked = true;
				drawerItem = DrawerItem.INBOX;
				break;
			}
			case R.id.contacts_section: {
				sectionClicked = true;
				drawerItem = DrawerItem.CONTACTS;
				break;
			}
			case R.id.notifications_section: {
				sectionClicked = true;
				drawerItem = DrawerItem.NOTIFICATIONS;
				break;
			}
			case R.id.offline_section: {
				sectionClicked = true;
				drawerItemBeforeOpenFullscreenOffline = drawerItem;
				openFullscreenOfflineFragment(getPathNavigationOffline());
				break;
			}
			case R.id.transfers_section:
				sectionClicked = true;
				drawerItem = DrawerItem.TRANSFERS;
				break;

			case R.id.rubbish_bin_section:
				sectionClicked = true;
				drawerItem = DrawerItem.RUBBISH_BIN;
				break;

			case R.id.settings_section: {
				sectionClicked = true;
				drawerItem = DrawerItem.SETTINGS;
				break;
			}
			case R.id.upgrade_navigation_view: {
				sectionClicked = true;
				drawerLayout.closeDrawer(Gravity.LEFT);
				drawerItemPreUpgradeAccount = drawerItem;
				drawerItem = DrawerItem.ACCOUNT;
				accountFragment = UPGRADE_ACCOUNT_FRAGMENT;
				displayedAccountType = -1;
				break;
			}
			case R.id.lost_authentication_device: {
				try {
					Intent openTermsIntent = new Intent(this, WebViewActivity.class);
					openTermsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					openTermsIntent.setData(Uri.parse(RECOVERY_URL));
					startActivity(openTermsIntent);
				}
				catch (Exception e){
					Intent viewIntent = new Intent(Intent.ACTION_VIEW);
					viewIntent.setData(Uri.parse(RECOVERY_URL));
					startActivity(viewIntent);
				}
				break;
			}

			case R.id.call_in_progress_layout:{
				returnCallWithPermissions();
				break;
			}
		}

		if (sectionClicked) {
			isFirstTimeCam();
			checkIfShouldCloseSearchView(oldDrawerItem);
			selectDrawerItemLollipop(drawerItem);
		}
	}

	void exportRecoveryKey (){
		AccountController aC = new AccountController(this);
		aC.saveRkToFileSystem();
	}

	public void showConfirmationCloseAllSessions(){
		logDebug("showConfirmationCloseAllSessions");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						AccountController aC = new AccountController(managerActivity);
						aC.killAllSessions(managerActivity);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

		builder.setTitle(R.string.confirmation_close_sessions_title);

		builder.setMessage(R.string.confirmation_close_sessions_text).setPositiveButton(R.string.contact_accept, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveFromOffline(MegaOffline node, Runnable onConfirmed) {
		logDebug("showConfirmationRemoveFromOffline");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						NodeController nC = new NodeController(managerActivity);
						nC.deleteOffline(node);
						onConfirmed.run();
						refreshOfflineNodes();

                        if(isCloudAdded()){
                            String handle = node.getHandle();
                            if(handle != null && !handle.equals("")){
                                fbFLol.refresh(Long.parseLong(handle));
                            }
                        }

						onNodesSharedUpdate();
						LiveEventBus.get(EVENT_NODES_CHANGE).post(false);
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE: {
						//No button clicked
						break;
					}
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

		builder.setMessage(R.string.confirmation_delete_from_save_for_offline).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationRemoveSomeFromOffline(List<MegaOffline> documents,
			Runnable onConfirmed) {
		logDebug("showConfirmationRemoveSomeFromOffline");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE: {
						NodeController nC = new NodeController(managerActivity);
						for (int i=0;i<documents.size();i++) {
							nC.deleteOffline(documents.get(i));
						}
						refreshOfflineNodes();
						onConfirmed.run();
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE: {
						break;
					}
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

		builder.setMessage(R.string.confirmation_delete_from_save_for_offline).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	@Override
	public void showConfirmationEnableLogsSDK(){
		if(getSettingsFragment() != null){
			sttFLol.numberOfClicksSDK = 0;
		}
		super.showConfirmationEnableLogsSDK();
	}

	@Override
	public void showConfirmationEnableLogsKarere(){
		if(getSettingsFragment() != null){
			sttFLol.numberOfClicksKarere = 0;
		}
		super.showConfirmationEnableLogsKarere();
	}

	public void showConfirmationDeleteAvatar(){
		logDebug("showConfirmationDeleteAvatar");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						AccountController aC = new AccountController(managerActivity);
						aC.removeAvatar();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

		builder.setMessage(R.string.confirmation_delete_avatar).setPositiveButton(R.string.context_delete, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void update2FASetting(){
		logDebug("update2FAVisibility");
		if (getSettingsFragment() != null) {
			try {
				sttFLol.update2FAVisibility();
			}catch (Exception e){}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		logDebug("Request code: " + requestCode + ", Result code:" + resultCode);

		if (nodeSaver.handleActivityResult(requestCode, resultCode, intent)) {
			return;
		}

		if (nodeAttacher.handleActivityResult(requestCode, resultCode, intent, this)) {
			return;
		}

		if (resultCode == RESULT_FIRST_USER){
			showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_destination_folder), -1);
			return;
		}

        if (requestCode == REQUEST_CODE_GET && resultCode == RESULT_OK) {
			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			logDebug("Intent action: " + intent.getAction());
			logDebug("Intent type: " + intent.getType());

			intent.setAction(Intent.ACTION_GET_CONTENT);
			FilePrepareTask filePrepareTask = new FilePrepareTask(this);
			filePrepareTask.execute(intent);
			showProcessFileDialog(this,intent);
		}
		else if (requestCode == CHOOSE_PICTURE_PROFILE_CODE && resultCode == RESULT_OK) {

			if (resultCode == RESULT_OK) {
				if (intent == null) {
					logWarning("Intent NULL");
					return;
				}

				boolean isImageAvailable = checkProfileImageExistence(intent.getData());
				if(!isImageAvailable){
					logError("Error when changing avatar: image not exist");
					showSnackbar(SNACKBAR_TYPE, getString(R.string.error_changing_user_avatar_image_not_available), -1);
					return;
				}

				intent.setAction(Intent.ACTION_GET_CONTENT);
				FilePrepareTask filePrepareTask = new FilePrepareTask(this);
				filePrepareTask.execute(intent);
				ProgressDialog temp = null;
				try{
					temp = new ProgressDialog(this);
					temp.setMessage(getQuantityString(R.plurals.upload_prepare, 1));
					temp.show();
				}
				catch(Exception e){
					return;
				}
				statusDialog = temp;

			}
			else {
				logWarning("resultCode for CHOOSE_PICTURE_PROFILE_CODE: " + resultCode);
			}
		}
		else if (requestCode == WRITE_SD_CARD_REQUEST_CODE && resultCode == RESULT_OK) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					ActivityCompat.requestPermissions(this,
			                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
							REQUEST_WRITE_STORAGE);
				}
			}

			if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
				showOverDiskQuotaPaywallWarning();
				return;
			}

			Uri treeUri = intent.getData();
			logDebug("Create the document : " + treeUri);
			long handleToDownload = intent.getLongExtra("handleToDownload", -1);
			logDebug("The recovered handle is: " + handleToDownload);
			//Now, call to the DownloadService

			if(handleToDownload!=0 && handleToDownload!=-1){
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_HASH, handleToDownload);
				service.putExtra(DownloadService.EXTRA_CONTENT_URI, treeUri.toString());
				File tempFolder = getCacheFolder(this, TEMPORAL_FOLDER);
				if (!isFileAvailable(tempFolder)) {
				    showSnackbar(SNACKBAR_TYPE, getString(R.string.general_error), -1);
				    return;
                }
				service.putExtra(DownloadService.EXTRA_PATH, tempFolder.getAbsolutePath());
				startService(service);
			}
        }
		else if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
			logDebug("requestCode == REQUEST_CODE_SELECT_FILE");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
			if(cFLol!=null && cFLol.isMultipleselect()){
				cFLol.hideMultipleSelect();
				cFLol.clearSelectionsNoAnimations();
			}

			nodeAttacher.handleSelectFileResult(intent, this);
		}
		else if (requestCode == REQUEST_CODE_SELECT_FOLDER && resultCode == RESULT_OK) {
			logDebug("REQUEST_CODE_SELECT_FOLDER");

			if (intent == null) {
				logDebug("Intent NULL");
				return;
			}

			final ArrayList<String> selectedContacts = intent.getStringArrayListExtra(SELECTED_CONTACTS);
			final long folderHandle = intent.getLongExtra("SELECT", 0);

			MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
			dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
			final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
			dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					permissionsDialog.dismiss();
					nC.shareFolder(megaApi.getNodeByHandle(folderHandle), selectedContacts, item);
				}
			});
			dialogBuilder.setTitle(getString(R.string.dialog_select_permissions));
			permissionsDialog = dialogBuilder.create();
			permissionsDialog.show();

		}
		else if (requestCode == REQUEST_CODE_SELECT_CONTACT && resultCode == RESULT_OK){
			logDebug("onActivityResult REQUEST_CODE_SELECT_CONTACT OK");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
			megaContacts = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_MEGA_CONTACTS, true);

			final int multiselectIntent = intent.getIntExtra("MULTISELECT", -1);

			//if (megaContacts){

			if(multiselectIntent==0){
				//One file to share
				final long nodeHandle = intent.getLongExtra(AddContactActivityLollipop.EXTRA_NODE_HANDLE, -1);

				MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
				dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
				final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
                dialogBuilder.setSingleChoiceItems(items, -1, (dialog, item) -> {
                    permissionsDialog.dismiss();
                    nC.shareFolder(megaApi.getNodeByHandle(nodeHandle), contactsData, item);
                });
				dialogBuilder.setTitle(getString(R.string.dialog_select_permissions));
				permissionsDialog = dialogBuilder.create();
				permissionsDialog.show();
			}
			else if(multiselectIntent==1){
				//Several folders to share
				final long[] nodeHandles = intent.getLongArrayExtra(AddContactActivityLollipop.EXTRA_NODE_HANDLE);

				MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
				dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
				final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
				dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						permissionsDialog.dismiss();
						nC.shareFolders(nodeHandles, contactsData, item);
					}
				});
				dialogBuilder.setTitle(getString(R.string.dialog_select_permissions));
				permissionsDialog = dialogBuilder.create();
				permissionsDialog.show();
			}
		}
		else if (requestCode == REQUEST_CODE_GET_LOCAL && resultCode == RESULT_OK) {

			if (intent == null) {
				logDebug("Intent NULL");
				return;
			}

			String folderPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			ArrayList<String> paths = intent.getStringArrayListExtra(FileStorageActivityLollipop.EXTRA_FILES);

			UploadServiceTask uploadServiceTask = new UploadServiceTask(folderPath, paths, getCurrentParentHandle());
			uploadServiceTask.start();
		}
		else if (requestCode == REQUEST_CODE_SELECT_FOLDER_TO_MOVE && resultCode == RESULT_OK) {

			if (intent == null) {
				logDebug("Intent NULL");
				return;
			}

			moveToRubbish = false;

			final long[] moveHandles = intent.getLongArrayExtra("MOVE_HANDLES");
			final long toHandle = intent.getLongExtra("MOVE_TO", 0);

			nC.moveNodes(moveHandles, toHandle);

		}
		else if (requestCode == REQUEST_CODE_SELECT_FOLDER_TO_COPY && resultCode == RESULT_OK){
			logDebug("REQUEST_CODE_SELECT_COPY_FOLDER");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}
			final long[] copyHandles = intent.getLongArrayExtra("COPY_HANDLES");
			final long toHandle = intent.getLongExtra("COPY_TO", 0);

			nC.copyNodes(copyHandles, toHandle);
		}
		else if (requestCode == REQUEST_CODE_REFRESH && resultCode == RESULT_OK) {
			logDebug("Resfresh DONE");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			((MegaApplication) getApplication()).askForFullAccountInfo();
			((MegaApplication) getApplication()).askForExtendedAccountDetails();

			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				parentHandleBrowser = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);

				ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode != null
								? parentNode
								: megaApi.getRootNode(),
						sortOrderManagement.getOrderCloud());

				fbFLol.setNodes(nodes);
				fbFLol.getRecyclerView().invalidate();
			}
			else if (drawerItem == DrawerItem.SHARED_ITEMS){
				refreshIncomingShares();
			}
		}
		else if (requestCode == REQUEST_CODE_REFRESH_STAGING && resultCode == RESULT_OK) {
			logDebug("Resfresh DONE");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			((MegaApplication) getApplication()).askForFullAccountInfo();
			((MegaApplication) getApplication()).askForExtendedAccountDetails();

			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				parentHandleBrowser = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);

				ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode != null
								? parentNode
								: megaApi.getRootNode(),
						sortOrderManagement.getOrderCloud());

				fbFLol.setNodes(nodes);
				fbFLol.getRecyclerView().invalidate();
			}
			else if (drawerItem == DrawerItem.SHARED_ITEMS){
				refreshIncomingShares();
			}

			if (getSettingsFragment() != null) {
				try {
					sttFLol.update2FAVisibility();
				}catch (Exception e){}
			}
		}
		else if (requestCode == TAKE_PHOTO_CODE) {
			logDebug("TAKE_PHOTO_CODE");
            if (resultCode == Activity.RESULT_OK) {
                uploadTakePicture(this, getCurrentParentHandle(), megaApi);
            } else {
                logWarning("TAKE_PHOTO_CODE--->ERROR!");
            }
		}
		else if (requestCode == TAKE_PICTURE_PROFILE_CODE){
			logDebug("TAKE_PICTURE_PROFILE_CODE");

			if(resultCode == Activity.RESULT_OK){
				String myEmail =  megaApi.getMyUser().getEmail();
				File imgFile = getCacheFile(this, TEMPORAL_FOLDER, "picture.jpg");
				if (!isFileAvailable(imgFile)) {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.general_error), -1);
					return;
				}

				File qrFile = buildQrFile(this, myEmail + QR_IMAGE_FILE_NAME);
                File newFile = buildAvatarFile(this,myEmail + "Temp.jpg");
				if (isFileAvailable(qrFile)) {
					qrFile.delete();
				}

                if (newFile != null) {
                    MegaUtilsAndroid.createAvatar(imgFile,newFile);
                    megaApi.setAvatar(newFile.getAbsolutePath(),this);
                } else {
					logError("ERROR! Destination PATH is NULL");
                }
			}else{
				logError("TAKE_PICTURE_PROFILE_CODE--->ERROR!");
			}

		}
		else if (requestCode == REQUEST_CODE_SORT_BY && resultCode == RESULT_OK){

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			int orderGetChildren = intent.getIntExtra("ORDER_GET_CHILDREN", 1);
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
				if (parentNode != null){
					if (isCloudAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
				else{
					if (isCloudAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
			}
			else if (drawerItem == DrawerItem.SHARED_ITEMS){
				onNodesSharedUpdate();
			}
		}
		else if (requestCode == REQUEST_CREATE_CHAT && resultCode == RESULT_OK) {
			logDebug("REQUEST_CREATE_CHAT OK");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);

			final boolean isGroup = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_GROUP_CHAT, false);

			if (contactsData != null){
				if(!isGroup){
					logDebug("Create one to one chat");
					MegaUser user = megaApi.getContact(contactsData.get(0));
					if(user!=null){
						logDebug("Chat with contact: " + contactsData.size());
						startOneToOneChat(user);
					}
				}
				else{
					logDebug("Create GROUP chat");
					MegaChatPeerList peers = MegaChatPeerList.createInstance();
					for (int i=0; i<contactsData.size(); i++){
						MegaUser user = megaApi.getContact(contactsData.get(i));
						if(user!=null){
							peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
						}
					}
					final String chatTitle = intent.getStringExtra(AddContactActivityLollipop.EXTRA_CHAT_TITLE);
					boolean isEKR = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_EKR, false);
					boolean chatLink = false;
					if (!isEKR) {
						chatLink = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_CHAT_LINK, false);
					}

					createGroupChat(peers, chatTitle, chatLink, isEKR);
				}
			}
		}
		else if (requestCode == REQUEST_INVITE_CONTACT_FROM_DEVICE && resultCode == RESULT_OK) {
			logDebug("REQUEST_INVITE_CONTACT_FROM_DEVICE OK");

			if (intent == null) {
				logWarning("Intent NULL");
				return;
			}

			final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
			megaContacts = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_MEGA_CONTACTS, true);

			if (contactsData != null){
				cC.inviteMultipleContacts(contactsData);
			}
		}
		else if (requestCode == REQUEST_DOWNLOAD_FOLDER && resultCode == RESULT_OK){
			String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);

			if (parentPath != null){
				String path = parentPath + File.separator + getRecoveryKeyFileName();
				String sdCardUriString = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_SD_URI);

				logDebug("REQUEST_DOWNLOAD_FOLDER:path to download: "+path);
				AccountController ac = new AccountController(this);
				ac.exportMK(path, sdCardUriString);
			}
		}

		else if(requestCode == REQUEST_CODE_FILE_INFO && resultCode == RESULT_OK){
		    if(isCloudAdded()){
                long handle = intent.getLongExtra(NODE_HANDLE, -1);
                fbFLol.refresh(handle);
            }

			onNodesSharedUpdate();
        } else if (requestCode == REQUEST_CODE_SCAN_DOCUMENT) {
            if (resultCode == RESULT_OK) {
                String savedDestination = intent.getStringExtra(DocumentScannerActivity.EXTRA_PICKED_SAVE_DESTINATION);
                Intent fileIntent = new Intent(this, FileExplorerActivityLollipop.class);
				if (StringResourcesUtils.getString(R.string.section_chat).equals(savedDestination)) {
                    fileIntent.setAction(FileExplorerActivityLollipop.ACTION_UPLOAD_TO_CHAT);
                } else {
                    fileIntent.setAction(FileExplorerActivityLollipop.ACTION_SAVE_TO_CLOUD);
                    fileIntent.putExtra(FileExplorerActivityLollipop.EXTRA_PARENT_HANDLE, getCurrentParentHandle());
                }
                fileIntent.putExtra(Intent.EXTRA_STREAM, intent.getData());
                fileIntent.setType(intent.getType());
                startActivity(fileIntent);
            }
        }
		// for HMS purchase only
        else if (requestCode == REQ_CODE_BUY) {
            if (resultCode == Activity.RESULT_OK) {
                int purchaseResult = mBillingManager.getPurchaseResult(intent);
                if (BillingManager.ORDER_STATE_SUCCESS == purchaseResult) {
                    mBillingManager.updatePurchase();
                } else {
                    logWarning("Purchase failed, error code: " + purchaseResult);
                }
            } else {
                logWarning("cancel subscribe");
            }
        }
		else{
			logWarning("No requestcode");
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	public void createGroupChat(MegaChatPeerList peers, String chatTitle, boolean chatLink, boolean isEKR){

		logDebug("Create group chat with participants: " + peers.size());

		if (isEKR) {
			megaChatApi.createChat(true, peers, chatTitle, this);
		}
		else {
			if(chatLink){
				if(chatTitle!=null && !chatTitle.isEmpty()){
					CreateGroupChatWithPublicLink listener = new CreateGroupChatWithPublicLink(this, chatTitle);
					megaChatApi.createPublicChat(peers, chatTitle, listener);
				}
				else{
					showAlert(this, getString(R.string.message_error_set_title_get_link), null);
				}
			}
			else{
				megaChatApi.createPublicChat(peers, chatTitle, this);
			}
		}
	}

	private long[] getChatHandles(ArrayList<MegaChatRoom> chats, long[] _chatHandles) {
		if (_chatHandles != null && chats == null) {
			return _chatHandles;
		}

		long[] chatHandles = new long[chats.size()];

		for (int i = 0; i < chats.size(); i++) {
			chatHandles[i] = chats.get(i).getChatId();
		}

		return chatHandles;
	}

	public void startOneToOneChat(MegaUser user){
		logDebug("User Handle: " + user.getHandle());
		MegaChatRoom chat = megaChatApi.getChatRoomByUser(user.getHandle());
		MegaChatPeerList peers = MegaChatPeerList.createInstance();
		if(chat==null){
			logDebug("No chat, create it!");
			peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
			megaChatApi.createChat(false, peers, this);
		}
		else{
			logDebug("There is already a chat, open it!");
			Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
			intentOpenChat.setAction(ACTION_CHAT_SHOW_MESSAGES);
			intentOpenChat.putExtra(CHAT_ID, chat.getChatId());
			this.startActivity(intentOpenChat);
		}
	}


	public void startGroupConversation(ArrayList<Long> userHandles){
		logDebug("startGroupConversation");
		MegaChatPeerList peers = MegaChatPeerList.createInstance();

		for(int i=0;i<userHandles.size();i++){
			long handle = userHandles.get(i);
			peers.addPeer(handle, MegaChatPeerList.PRIV_STANDARD);
		}

		megaChatApi.createChat(false, peers, this);
	}


	/*
	 * Background task to get files on a folder for uploading
	 */
	private class UploadServiceTask extends Thread {

		String folderPath;
		ArrayList<String> paths;
		long parentHandle;

		UploadServiceTask(String folderPath, ArrayList<String> paths, long parentHandle){
			this.folderPath = folderPath;
			this.paths = paths;
			this.parentHandle = parentHandle;
		}

		@Override
		public void run(){

			logDebug("Run Upload Service Task");

			MegaNode parentNode = megaApi.getNodeByHandle(parentHandle);
			if (parentNode == null){
				parentNode = megaApi.getRootNode();
			}

			if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
				showOverDiskQuotaPaywallWarning();
				return;
			}

			showSnackbar(SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.upload_began, paths.size(), paths.size()), -1);
			for (String path : paths) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Intent uploadServiceIntent;
				if(managerActivity != null)
				{
					uploadServiceIntent = new Intent (managerActivity, UploadService.class);
				}
				else
				{
					uploadServiceIntent = new Intent (ManagerActivityLollipop.this, UploadService.class);
				}

				File file = new File (path);
				if (file.isDirectory()){
					uploadServiceIntent.putExtra(UploadService.EXTRA_FILEPATH, file.getAbsolutePath());
					uploadServiceIntent.putExtra(UploadService.EXTRA_NAME, file.getName());
				}
				else{
					ShareInfo info = ShareInfo.infoFromFile(file);
					if (info == null){
						continue;
					}
					uploadServiceIntent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
					uploadServiceIntent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
					uploadServiceIntent.putExtra(UploadService.EXTRA_SIZE, info.getSize());
				}

				uploadServiceIntent.putExtra(UploadService.EXTRA_FOLDERPATH, folderPath);
				uploadServiceIntent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
				uploadServiceIntent.putExtra(UploadService.EXTRA_UPLOAD_COUNT, paths.size());
				startService(uploadServiceIntent);
			}
		}
	}

	void disableNavigationViewMenu(Menu menu){
		logDebug("disableNavigationViewMenu");

		MenuItem mi = menu.findItem(R.id.bottom_navigation_item_cloud_drive);
		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_camera_uploads);
		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_chat);
		if (mi != null){
			mi.setChecked(false);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_shared_items);
		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_homepage);
		if (mi != null){
			mi.setChecked(false);
		}

		disableNavigationViewLayout();
	}

	void disableNavigationViewLayout() {
		if (myAccountSection != null) {
			myAccountSection.setEnabled(false);
			((TextView) myAccountSection.findViewById(R.id.my_account_section_text)).setTextColor(ContextCompat.getColor(this, R.color.grey_038_white_038));
		}

		if (inboxSection != null){
			if(inboxNode==null){
				inboxSection.setVisibility(View.GONE);
			}
			else{
				boolean hasChildren = megaApi.hasChildren(inboxNode);
				if(hasChildren){
					inboxSection.setEnabled(false);
					inboxSection.setVisibility(View.VISIBLE);
					((TextView) inboxSection.findViewById(R.id.inbox_section_text)).setTextColor(ContextCompat.getColor(this, R.color.grey_038_white_038));
				}
				else{
					inboxSection.setVisibility(View.GONE);
				}
			}
		}

		if (contactsSection != null) {
			contactsSection.setEnabled(false);

			if (contactsSectionText == null) {
				contactsSectionText = contactsSection.findViewById(R.id.contacts_section_text);
			}

			contactsSectionText.setAlpha(0.38F);
			setContactTitleSection();
		}

		if (notificationsSection != null) {
			notificationsSection.setEnabled(false);

			if (notificationsSectionText == null) {
				notificationsSectionText = notificationsSection.findViewById(R.id.contacts_section_text);
			}

			notificationsSectionText.setAlpha(0.38F);
			setNotificationsTitleSection();
		}

		if (upgradeAccount != null) {
			upgradeAccount.setEnabled(false);
		}
	}

	void resetNavigationViewMenu(Menu menu){
		logDebug("resetNavigationViewMenu()");

		if(!isOnline(this) || megaApi==null || megaApi.getRootNode()==null){
			disableNavigationViewMenu(menu);
			return;
		}

		MenuItem mi = menu.findItem(R.id.bottom_navigation_item_cloud_drive);

		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_camera_uploads);
		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_chat);
		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.bottom_navigation_item_shared_items);
		if (mi != null){
			mi.setChecked(false);
			mi.setEnabled(true);
		}

		resetNavigationViewLayout();
	}

	public void resetNavigationViewLayout() {
		if (myAccountSection != null) {
			myAccountSection.setEnabled(true);
			((TextView) myAccountSection.findViewById(R.id.my_account_section_text)).setTextColor(
					ColorUtils.getThemeColor(this, android.R.attr.textColorPrimary));
		}

		if (inboxSection != null){
			if(inboxNode==null){
				inboxSection.setVisibility(View.GONE);
				logDebug("Inbox Node is NULL");
			}
			else{
				boolean hasChildren = megaApi.hasChildren(inboxNode);
				if(hasChildren){
					inboxSection.setEnabled(true);
					inboxSection.setVisibility(View.VISIBLE);
					((TextView) inboxSection.findViewById(R.id.inbox_section_text)).setTextColor(
							ColorUtils.getThemeColor(this, android.R.attr.textColorPrimary));
				}
				else{
					logDebug("Inbox Node NO children");
					inboxSection.setVisibility(View.GONE);
				}
			}
		}

		if (contactsSection != null) {
			contactsSection.setEnabled(true);

			if (contactsSectionText == null) {
				contactsSectionText = contactsSection.findViewById(R.id.contacts_section_text);
			}

			contactsSectionText.setAlpha(1F);
			setContactTitleSection();
		}

		if (notificationsSection != null) {
			notificationsSection.setEnabled(true);

			if (notificationsSectionText == null) {
				notificationsSectionText = notificationsSection.findViewById(R.id.notification_section_text);
			}

			notificationsSectionText.setAlpha(1F);
			setNotificationsTitleSection();
		}

		if (upgradeAccount != null) {
			upgradeAccount.setEnabled(true);
		}
	}

	public void setInboxNavigationDrawer() {
		logDebug("setInboxNavigationDrawer");
		if (nV != null && inboxSection != null){
			if(inboxNode==null){
				inboxSection.setVisibility(View.GONE);
				logDebug("Inbox Node is NULL");
			}
			else{
				boolean hasChildren = megaApi.hasChildren(inboxNode);
				if(hasChildren){
					inboxSection.setEnabled(true);
					inboxSection.setVisibility(View.VISIBLE);
				}
				else{
					logDebug("Inbox Node NO children");
					inboxSection.setVisibility(View.GONE);
				}
			}
		}
	}

	public void showProPanel(){
		logDebug("showProPanel");
		//Left and Right margin
		LinearLayout.LayoutParams proTextParams = (LinearLayout.LayoutParams)getProText.getLayoutParams();
		proTextParams.setMargins(scaleWidthPx(24, outMetrics), scaleHeightPx(23, outMetrics), scaleWidthPx(24, outMetrics), scaleHeightPx(23, outMetrics));
		getProText.setLayoutParams(proTextParams);

		rightUpgradeButton.setOnClickListener(this);
		android.view.ViewGroup.LayoutParams paramsb2 = rightUpgradeButton.getLayoutParams();
		//Left and Right margin
		LinearLayout.LayoutParams optionTextParams = (LinearLayout.LayoutParams)rightUpgradeButton.getLayoutParams();
		optionTextParams.setMargins(scaleWidthPx(6, outMetrics), 0, scaleWidthPx(8, outMetrics), 0);
		rightUpgradeButton.setLayoutParams(optionTextParams);

		leftCancelButton.setOnClickListener(this);
		android.view.ViewGroup.LayoutParams paramsb1 = leftCancelButton.getLayoutParams();
		leftCancelButton.setLayoutParams(paramsb1);
		//Left and Right margin
		LinearLayout.LayoutParams cancelTextParams = (LinearLayout.LayoutParams)leftCancelButton.getLayoutParams();
		cancelTextParams.setMargins(scaleWidthPx(6, outMetrics), 0, scaleWidthPx(6, outMetrics), 0);
		leftCancelButton.setLayoutParams(cancelTextParams);

		getProLayout.setVisibility(View.VISIBLE);
		getProLayout.bringToFront();
	}

	/**
	 * Check the current storage state.
	 * @param onCreate Flag to indicate if the method was called from "onCreate" or not.
	 */
	private void checkCurrentStorageStatus(boolean onCreate) {
		// If the current storage state is not initialized is because the app received the
		// event informing about the storage state  during login, the ManagerActivityLollipop
		// wasn't active and for this reason the value is stored in the MegaApplication object.
		int storageStateToCheck = (storageState != MegaApiJava.STORAGE_STATE_UNKNOWN) ?
				storageState : app.getStorageState();

		checkStorageStatus(storageStateToCheck, onCreate);
	}

	/**
	 * Check the storage state provided as first parameter.
	 * @param newStorageState Storage state to check.
	 * @param onCreate Flag to indicate if the method was called from "onCreate" or not.
	 */
	private void checkStorageStatus(int newStorageState, boolean onCreate) {
        Intent intent = new Intent(this,UploadService.class);
        switch (newStorageState) {
            case MegaApiJava.STORAGE_STATE_GREEN:
				logDebug("STORAGE STATE GREEN");

                intent.setAction(ACTION_STORAGE_STATE_CHANGED);

                // TODO: WORKAROUND, NEED TO IMPROVE AND REMOVE THE TRY-CATCH
                try {
					startService(intent);
				}
				catch (Exception e) {
					logError("Exception starting UploadService", e);
					e.printStackTrace();
				}

				int accountType = app.getMyAccountInfo().getAccountType();
				if(accountType == MegaAccountDetails.ACCOUNT_TYPE_FREE){
					logDebug("ACCOUNT TYPE FREE");
					if(showMessageRandom()){
						logDebug("Show message random: TRUE");
						showProPanel();
					}
				}
				storageState = newStorageState;
                startCameraUploadService(ManagerActivityLollipop.this);
				break;

			case MegaApiJava.STORAGE_STATE_ORANGE:
				logWarning("STORAGE STATE ORANGE");

                intent.setAction(ACTION_STORAGE_STATE_CHANGED);

				// TODO: WORKAROUND, NEED TO IMPROVE AND REMOVE THE TRY-CATCH
                try {
					startService(intent);
				}
				catch (Exception e) {
					logError("Exception starting UploadService", e);
					e.printStackTrace();
				}

				if (onCreate && isStorageStatusDialogShown) {
					isStorageStatusDialogShown = false;
					showStorageAlmostFullDialog();
				} else if (newStorageState > storageState) {
					showStorageAlmostFullDialog();
				}
				storageState = newStorageState;
                logDebug("Try to start CU, false.");
                startCameraUploadService(ManagerActivityLollipop.this);
				break;

			case MegaApiJava.STORAGE_STATE_RED:
				logWarning("STORAGE STATE RED");
				if (onCreate && isStorageStatusDialogShown) {
					isStorageStatusDialogShown = false;
					showStorageFullDialog();
				} else if (newStorageState > storageState) {
					showStorageFullDialog();
				}
				break;

			case MegaApiJava.STORAGE_STATE_PAYWALL:
				logWarning("STORAGE STATE PAYWALL");
				break;

			default:
				return;
		}

		storageState = newStorageState;
		app.setStorageState(storageState);
	}

	/**
	 * Show a dialog to indicate that the storage space is almost full.
	 */
	public void showStorageAlmostFullDialog(){
		logDebug("showStorageAlmostFullDialog");
		showStorageStatusDialog(MegaApiJava.STORAGE_STATE_ORANGE, false, false);
	}

	/**
	 * Show a dialog to indicate that the storage space is full.
	 */
	public void showStorageFullDialog(){
		logDebug("showStorageFullDialog");
		showStorageStatusDialog(MegaApiJava.STORAGE_STATE_RED, false, false);
	}

	/**
	 * Show an overquota alert dialog.
	 * @param preWarning Flag to indicate if is a pre-overquota alert or not.
	 */
	public void showOverquotaAlert(boolean preWarning){
		logDebug("preWarning: " + preWarning);
		showStorageStatusDialog(
				preWarning ? MegaApiJava.STORAGE_STATE_ORANGE : MegaApiJava.STORAGE_STATE_RED,
				true, preWarning);
	}

	public void showSMSVerificationDialog() {
	    isSMSDialogShowing = true;
        smsDialogTimeChecker.update();
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.sms_verification_dialog_layout,null);
        dialogBuilder.setView(dialogView);

        TextView msg = dialogView.findViewById(R.id.sv_dialog_msg);
        boolean isAchievementUser = megaApi.isAchievementsEnabled();
        logDebug("is achievement user: " + isAchievementUser);
        if (isAchievementUser) {
            String message = String.format(getString(R.string.sms_add_phone_number_dialog_msg_achievement_user), bonusStorageSMS);
            msg.setText(message);
        } else {
            msg.setText(R.string.sms_add_phone_number_dialog_msg_non_achievement_user);
        }

        dialogBuilder.setPositiveButton(R.string.general_add, (dialog, which) -> {
			startActivity(new Intent(getApplicationContext(),SMSVerificationActivity.class));
			alertDialogSMSVerification.dismiss();
		}).setNegativeButton(R.string.verify_account_not_now_button, (dialog, which) -> {
			alertDialogSMSVerification.dismiss();
		});

        if(alertDialogSMSVerification == null) {
            alertDialogSMSVerification = dialogBuilder.create();
            alertDialogSMSVerification.setCancelable(false);
            alertDialogSMSVerification.setOnDismissListener(dialog -> isSMSDialogShowing = false);
            alertDialogSMSVerification.setCanceledOnTouchOutside(false);
        }
        alertDialogSMSVerification.show();
    }

	/**
	 * Method to show a dialog to indicate the storage status.
	 * @param storageState Storage status.
	 * @param overquotaAlert Flag to indicate that is an overquota alert or not.
	 * @param preWarning Flag to indicate if is a pre-overquota alert or not.
	 */
	private void showStorageStatusDialog(int storageState, boolean overquotaAlert, boolean preWarning){
		logDebug("showStorageStatusDialog");

		if(((MegaApplication) getApplication()).getMyAccountInfo()==null || ((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()==-1){
			logWarning("Do not show dialog, not info of the account received yet");
			return;
		}

		if(isStorageStatusDialogShown){
			logDebug("Storage status dialog already shown");
			return;
		}

		MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);

		LayoutInflater inflater = this.getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.storage_status_dialog_layout, null);
		dialogBuilder.setView(dialogView);

		TextView title = (TextView) dialogView.findViewById(R.id.storage_status_title);
		title.setText(getString(R.string.action_upgrade_account));

		ImageView image = (ImageView) dialogView.findViewById(R.id.image_storage_status);
		TextView text = (TextView) dialogView.findViewById(R.id.text_storage_status);

		Product pro3 = getPRO3OneMonth();
		String storageString = "";
		String transferString = "";
        if(pro3 != null) {
            storageString = getSizeStringGBBased(pro3.getStorage());
            transferString = getSizeStringGBBased(pro3.getTransfer());
        }

		switch (storageState) {
			case MegaApiJava.STORAGE_STATE_GREEN:
				logDebug("STORAGE STATE GREEN");
				return;

			case MegaApiJava.STORAGE_STATE_ORANGE:
				image.setImageResource(R.drawable.ic_storage_almost_full);
				text.setText(String.format(getString(R.string.text_almost_full_warning), storageString,transferString));
				break;

			case MegaApiJava.STORAGE_STATE_RED:
				image.setImageResource(R.drawable.ic_storage_full);
				text.setText(String.format(getString(R.string.text_storage_full_warning), storageString,transferString));
				break;

			default:
				logWarning("STORAGE STATE INVALID VALUE: " + storageState);
				return;
		}

		if (overquotaAlert) {
			if (!preWarning)
				title.setText(getString(R.string.overquota_alert_title));

			text.setText(getString(preWarning ? R.string.pre_overquota_alert_text :
					R.string.overquota_alert_text));
		}

		LinearLayout horizontalButtonsLayout = (LinearLayout) dialogView.findViewById(R.id.horizontal_buttons_storage_status_layout);
		LinearLayout verticalButtonsLayout = (LinearLayout) dialogView.findViewById(R.id.vertical_buttons_storage_status_layout);

		final OnClickListener dismissClickListener = new OnClickListener() {
			public void onClick(View v) {
				alertDialogStorageStatus.dismiss();
				isStorageStatusDialogShown = false;
			}
		};

		final OnClickListener upgradeClickListener = new OnClickListener(){
			public void onClick(View v) {
				alertDialogStorageStatus.dismiss();
				isStorageStatusDialogShown = false;
				navigateToUpgradeAccount();
			}
		};

		final OnClickListener achievementsClickListener = new OnClickListener(){
			public void onClick(View v) {
				alertDialogStorageStatus.dismiss();
				isStorageStatusDialogShown = false;
				logDebug("Go to achievements section");
				navigateToAchievements();
			}
		};

		final OnClickListener customPlanClickListener = new OnClickListener(){
			public void onClick(View v) {
				alertDialogStorageStatus.dismiss();
				isStorageStatusDialogShown = false;
				askForCustomizedPlan();
			}
		};

		Button verticalDismissButton = (Button) dialogView.findViewById(R.id.vertical_storage_status_button_dissmiss);
		verticalDismissButton.setOnClickListener(dismissClickListener);
		Button horizontalDismissButton = (Button) dialogView.findViewById(R.id.horizontal_storage_status_button_dissmiss);
		horizontalDismissButton.setOnClickListener(dismissClickListener);

		Button verticalActionButton = (Button) dialogView.findViewById(R.id.vertical_storage_status_button_action);
		Button horizontalActionButton = (Button) dialogView.findViewById(R.id.horizontal_storage_status_button_payment);

		Button achievementsButton = (Button) dialogView.findViewById(R.id.vertical_storage_status_button_achievements);
		achievementsButton.setOnClickListener(achievementsClickListener);

		switch (((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()) {
			case MegaAccountDetails.ACCOUNT_TYPE_PROIII:
				logDebug("Show storage status dialog for USER PRO III");
				if (!overquotaAlert) {
					if (storageState == MegaApiJava.STORAGE_STATE_ORANGE) {
						text.setText(getString(R.string.text_almost_full_warning_pro3_account));
					} else if (storageState == MegaApiJava.STORAGE_STATE_RED){
						text.setText(getString(R.string.text_storage_full_warning_pro3_account));
					}
				}
				horizontalActionButton.setText(getString(R.string.button_custom_almost_full_warning));
				horizontalActionButton.setOnClickListener(customPlanClickListener);
				verticalActionButton.setText(getString(R.string.button_custom_almost_full_warning));
				verticalActionButton.setOnClickListener(customPlanClickListener);
				break;

			case MegaAccountDetails.ACCOUNT_TYPE_LITE:
			case MegaAccountDetails.ACCOUNT_TYPE_PROI:
			case MegaAccountDetails.ACCOUNT_TYPE_PROII:
				logDebug("Show storage status dialog for USER PRO");
				if (!overquotaAlert) {
					if (storageState == MegaApiJava.STORAGE_STATE_ORANGE) {
						text.setText(String.format(getString(R.string.text_almost_full_warning_pro_account),storageString,transferString));
					} else if (storageState == MegaApiJava.STORAGE_STATE_RED){
						text.setText(String.format(getString(R.string.text_storage_full_warning_pro_account),storageString,transferString));
					}
				}
				horizontalActionButton.setText(getString(R.string.my_account_upgrade_pro));
				horizontalActionButton.setOnClickListener(upgradeClickListener);
				verticalActionButton.setText(getString(R.string.my_account_upgrade_pro));
				verticalActionButton.setOnClickListener(upgradeClickListener);
				break;

			case MegaAccountDetails.ACCOUNT_TYPE_FREE:
			default:
				logDebug("Show storage status dialog for FREE USER");
				horizontalActionButton.setText(getString(R.string.button_plans_almost_full_warning));
				horizontalActionButton.setOnClickListener(upgradeClickListener);
				verticalActionButton.setText(getString(R.string.button_plans_almost_full_warning));
				verticalActionButton.setOnClickListener(upgradeClickListener);
				break;
		}

		if(megaApi.isAchievementsEnabled()){
			horizontalButtonsLayout.setVisibility(View.GONE);
			verticalButtonsLayout.setVisibility(View.VISIBLE);
		}
		else{
			horizontalButtonsLayout.setVisibility(View.VISIBLE);
			verticalButtonsLayout.setVisibility(View.GONE);
		}

		alertDialogStorageStatus = dialogBuilder.create();
		alertDialogStorageStatus.setCancelable(false);
		alertDialogStorageStatus.setCanceledOnTouchOutside(false);

		isStorageStatusDialogShown = true;

		alertDialogStorageStatus.show();
	}

	private Product getPRO3OneMonth() {
		List<Product> products = MegaApplication.getInstance().getMyAccountInfo().productAccounts;
		if (products != null) {
			for (Product product : products) {
				if (product != null && product.getLevel() == PRO_III && product.getMonths() == 1) {
					return product;
				}
			}
		} else {
			// Edge case: when this method is called, TYPE_GET_PRICING hasn't finished yet.
			logWarning("Products haven't been initialized!");
		}
		return null;
	}

	public void askForCustomizedPlan(){
		logDebug("askForCustomizedPlan");

		StringBuilder body = new StringBuilder();
		body.append(getString(R.string.subject_mail_upgrade_plan));
		body.append("\n\n\n\n\n\n\n");
		body.append(getString(R.string.settings_about_app_version)+" v"+getString(R.string.app_version)+"\n");
		body.append(getString(R.string.user_account_feedback)+"  "+megaApi.getMyEmail());

		if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
			if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()<0||((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()>4){
				body.append(" ("+getString(R.string.my_account_free)+")");
			}
			else{
				switch(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()){
					case 0:{
						body.append(" ("+getString(R.string.my_account_free)+")");
						break;
					}
					case 1:{
						body.append(" ("+getString(R.string.my_account_pro1)+")");
						break;
					}
					case 2:{
						body.append(" ("+getString(R.string.my_account_pro2)+")");
						break;
					}
					case 3:{
						body.append(" ("+getString(R.string.my_account_pro3)+")");
						break;
					}
					case 4:{
						body.append(" ("+getString(R.string.my_account_prolite_feedback_email)+")");
						break;
					}
				}
			}
		}

		String emailAndroid = MAIL_SUPPORT;
		String subject = getString(R.string.title_mail_upgrade_plan);

		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAndroid));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
		startActivity(Intent.createChooser(emailIntent, " "));

	}

	public void updateCancelSubscriptions(){
		logDebug("updateCancelSubscriptions");
		if (cancelSubscription != null){
			cancelSubscription.setVisible(false);
		}
		if (((MegaApplication) getApplication()).getMyAccountInfo().getNumberOfSubscriptions() > 0){
			if (cancelSubscription != null){
				if (drawerItem == DrawerItem.ACCOUNT){
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					if (maFLol != null){
						cancelSubscription.setVisible(true);
					}
				}
			}
		}
	}

	private void refreshOfflineNodes() {
		logDebug("updateOfflineView");
		if (fullscreenOfflineFragment != null) {
			fullscreenOfflineFragment.refreshNodes();
		} else if (pagerOfflineFragment != null) {
			pagerOfflineFragment.refreshNodes();
		}
	}

	public void updateContactsView(boolean contacts, boolean sentRequests, boolean receivedRequests){
		logDebug("updateContactsView");

		if(contacts){
			logDebug("Update Contacts Fragment");
			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
			if (cFLol != null){
				cFLol.hideMultipleSelect();
				cFLol.updateView();
			}
		}

		if(sentRequests){
			logDebug("Update SentRequests Fragment");
			sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SENT_REQUESTS.getTag());
			if (sRFLol != null){
				sRFLol.hideMultipleSelect();
				sRFLol.updateView();
			}
		}

		if(receivedRequests){
			logDebug("Update ReceivedRequest Fragment");
			rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECEIVED_REQUESTS.getTag());
			if (rRFLol != null){
				rRFLol.hideMultipleSelect();
				rRFLol.updateView();
			}
		}
	}

	/*
	 * Handle processed upload intent
	 */
	public void onIntentProcessed(List<ShareInfo> infos) {
		logDebug("onIntentProcessedLollipop");
		if (statusDialog != null) {
			try {
				statusDialog.dismiss();
			}
			catch(Exception ex){}
		}
		dissmisDialog();

		MegaNode parentNode = getCurrentParentNode(getCurrentParentHandle(), -1);

		if(drawerItem == DrawerItem.ACCOUNT){
			if(infos!=null){
				for (ShareInfo info : infos) {
					String avatarPath = info.getFileAbsolutePath();
					if(avatarPath!=null){
						logDebug("Chosen picture to change the avatar");
						File imgFile = new File(avatarPath);
						File qrFile = buildQrFile(this, megaApi.getMyUser().getEmail() + QR_IMAGE_FILE_NAME);
						File newFile = buildAvatarFile(this, megaApi.getMyUser().getEmail() + "Temp.jpg");


						if (isFileAvailable(qrFile)) {
							qrFile.delete();
						}
                        if (newFile != null) {
                            MegaUtilsAndroid.createAvatar(imgFile,newFile);
                            maFLol = (MyAccountFragmentLollipop)getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
                            if (maFLol != null) {
                                megaApi.setAvatar(newFile.getAbsolutePath(),this);
                            }
                        } else {
							logError("ERROR! Destination PATH is NULL");
                        }
					}
					else{
						logError("The chosen avatar path is NULL");
					}
				}
			}
			else{
				logWarning("infos is NULL");
			}
			return;
		}

		if(parentNode == null){
			showSnackbar(SNACKBAR_TYPE, getString(R.string.error_temporary_unavaible), -1);
			return;
		}

		if (infos == null) {
			showSnackbar(SNACKBAR_TYPE, getString(R.string.upload_can_not_open), -1);
		}
		else {
			if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
				showOverDiskQuotaPaywallWarning();
				return;
			}

			showSnackbar(SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.upload_began, infos.size(), infos.size()), -1);

			for (ShareInfo info : infos) {
				if(info.isContact){
					requestContactsPermissions(info, parentNode);
				}
				else{
					Intent intent = new Intent(this, UploadService.class);
					intent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
					intent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
					intent.putExtra(UploadService.EXTRA_LAST_MODIFIED, info.getLastModified());
					intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
					intent.putExtra(UploadService.EXTRA_UPLOAD_COUNT, infos.size());
					startService(intent);
				}
			}
		}
	}

	public void requestContactsPermissions(ShareInfo info, MegaNode parentNode){
		logDebug("requestContactsPermissions");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!checkPermission(Manifest.permission.READ_CONTACTS)) {
				logWarning("No read contacts permission");
				infoManager = info;
				parentNodeManager = parentNode;
				ActivityCompat.requestPermissions(this,	new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_UPLOAD_CONTACT);
			} else {
				uploadContactInfo(info, parentNode);
			}
		}
		else{
			uploadContactInfo(info, parentNode);
		}
	}

	public void uploadContactInfo(ShareInfo info, MegaNode parentNode){
		logDebug("Upload contact info");

		Cursor cursorID = getContentResolver().query(info.contactUri, null, null, null, null);

		if (cursorID != null) {
			if (cursorID.moveToFirst()) {
				logDebug("It is a contact");

				String id = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				int hasPhone = cursorID.getInt(cursorID.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

				// get the user's email address
				String email = null;
				Cursor ce = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
				if (ce != null && ce.moveToFirst()) {
					email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					ce.close();
				}

				// get the user's phone number
				String phone = null;
				if (hasPhone > 0) {
					Cursor cp = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
					if (cp != null && cp.moveToFirst()) {
						phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						cp.close();
					}
				}

				StringBuilder data = new StringBuilder();
				data.append(name);
				if(phone!=null){
					data.append(", "+phone);
				}

				if(email!=null){
					data.append(", "+email);
				}

				createFile(name, data.toString(), parentNode);
			}
			cursorID.close();
		}
		else{
			showSnackbar(SNACKBAR_TYPE, getString(R.string.error_temporary_unavaible), -1);
		}
	}

	private void createFile(String name, String data, MegaNode parentNode){

		if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
			showOverDiskQuotaPaywallWarning();
			return;
		}

		File file = createTemporalTextFile(this, name, data);
		if(file!=null){
			showSnackbar(SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.upload_began, 1, 1), -1);

			Intent intent = new Intent(this, UploadService.class);
			intent.putExtra(UploadService.EXTRA_FILEPATH, file.getAbsolutePath());
			intent.putExtra(UploadService.EXTRA_NAME, file.getName());
			intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
			intent.putExtra(UploadService.EXTRA_SIZE, file.getTotalSpace());
			startService(intent);
		}
		else{
			showSnackbar(SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
		}
	}

	@Override
	public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {
		logDebug("onRequestStart(CHAT): "+ request.getRequestString());
//		if (request.getType() == MegaChatRequest.TYPE_INITIALIZE){
//			MegaApiAndroid.setLoggerObject(new AndroidLogger());
////			MegaChatApiAndroid.setLoggerObject(new AndroidChatLogger());
//		}
	}

	@Override
	public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
		logDebug("onRequestFinish(CHAT): " + request.getRequestString()+"_"+e.getErrorCode());

		if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
			logDebug("Create chat request finish");
			onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle());
		} else if (request.getType() == MegaChatRequest.TYPE_CONNECT){
			logDebug("Connecting chat finished");

			if (MegaApplication.isFirstConnect()){
				logDebug("Set first connect to false");
				MegaApplication.setFirstConnect(false);
			}

			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				logDebug("CONNECT CHAT finished ");
				connected = true;

				if (joiningToChatLink && !isTextEmpty(linkJoinToChatLink)) {
					megaChatApi.checkChatLink(linkJoinToChatLink, this);
				}

				if(drawerItem == DrawerItem.CHAT){
					rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
					if(rChatFL!=null){
						rChatFL.onlineStatusUpdate(megaChatApi.getOnlineStatus());
					}
				}
			}
			else{
				logError("ERROR WHEN CONNECTING " + e.getErrorString());
//				showSnackbar(getString(R.string.chat_connection_error));
			}
		}
		else if (request.getType() == MegaChatRequest.TYPE_DISCONNECT){
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				logDebug("DISConnected from chat!");
			}
			else{
				logError("ERROR WHEN DISCONNECTING " + e.getErrorString());
			}
		}
		else if (request.getType() == MegaChatRequest.TYPE_LOGOUT){
			logDebug("onRequestFinish(CHAT): " + MegaChatRequest.TYPE_LOGOUT);

			if (e.getErrorCode() != MegaError.API_OK){
				logError("MegaChatRequest.TYPE_LOGOUT:ERROR");
			}

			if(getSettingsFragment() != null){
				sttFLol.hidePreferencesChat();
			}

			if (app != null){
				app.disableMegaChatApi();
			}
			resetLoggerSDK();
		}
		else if(request.getType() == MegaChatRequest.TYPE_SET_ONLINE_STATUS){
			if(e.getErrorCode()==MegaChatError.ERROR_OK) {
				logDebug("Status changed to: " + request.getNumber());
			} else if (e.getErrorCode() == MegaChatError.ERROR_ARGS) {
				logWarning("Status not changed, the chosen one is the same");
			} else {
				logError("ERROR WHEN TYPE_SET_ONLINE_STATUS " + e.getErrorString());
				showSnackbar(SNACKBAR_TYPE, getString(R.string.changing_status_error), -1);
			}
		}
		else if(request.getType() == MegaChatRequest.TYPE_ARCHIVE_CHATROOM){
			long chatHandle = request.getChatHandle();
			MegaChatRoom chat = megaChatApi.getChatRoom(chatHandle);
			String chatTitle = getTitleChat(chat);

			if(chatTitle==null){
				chatTitle = "";
			}
			else if(!chatTitle.isEmpty() && chatTitle.length()>60){
				chatTitle = chatTitle.substring(0,59)+"...";
			}

			if(!chatTitle.isEmpty() && chat.isGroup() && !chat.hasCustomTitle()){
				chatTitle = "\""+chatTitle+"\"";
			}

			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				if(request.getFlag()){
					logDebug("Chat archived");
					showSnackbar(SNACKBAR_TYPE, getString(R.string.success_archive_chat, chatTitle), -1);
				}
				else{
					logDebug("Chat unarchived");
					showSnackbar(SNACKBAR_TYPE, getString(R.string.success_unarchive_chat, chatTitle), -1);
				}
			}
			else{
				if(request.getFlag()){
					logError("ERROR WHEN ARCHIVING CHAT " + e.getErrorString());
					showSnackbar(SNACKBAR_TYPE, getString(R.string.error_archive_chat, chatTitle), -1);
				}
				else{
					logError("ERROR WHEN UNARCHIVING CHAT " + e.getErrorString());
					showSnackbar(SNACKBAR_TYPE, getString(R.string.error_unarchive_chat, chatTitle), -1);
				}
			}
		}
		else if(request.getType() == MegaChatRequest.TYPE_LOAD_PREVIEW){
			if(e.getErrorCode()==MegaChatError.ERROR_OK || e.getErrorCode() == MegaChatError.ERROR_EXIST){
				if (joiningToChatLink && isTextEmpty(request.getLink()) && request.getChatHandle() == MEGACHAT_INVALID_HANDLE) {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.error_chat_link_init_error), MEGACHAT_INVALID_HANDLE);
					resetJoiningChatLink();
					return;
				}

				showChatLink(request.getLink());
				dismissOpenLinkDialog();
			}
			else {
				if(e.getErrorCode()==MegaChatError.ERROR_NOENT){
					dismissOpenLinkDialog();
					showAlert(this, getString(R.string.invalid_chat_link), getString(R.string.title_alert_chat_link_error));
				}
				else {
					showOpenLinkError(true, 0);
				}
			}
		}
		else if(request.getType() == MegaChatRequest.TYPE_SET_LAST_GREEN_VISIBLE){
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				logDebug("MegaChatRequest.TYPE_SET_LAST_GREEN_VISIBLE: " + request.getFlag());
            }
            else{
				logError("MegaChatRequest.TYPE_SET_LAST_GREEN_VISIBLE:error: " + e.getErrorType());
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

	}

	public void onRequestFinishCreateChat(int errorCode, long chatHandle){
		if(errorCode==MegaChatError.ERROR_OK){
			logDebug("Chat CREATED.");

			//Update chat view
			rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
			if(rChatFL!=null){

				if(selectMenuItem!=null){
					selectMenuItem.setVisible(true);
				}
			}

			logDebug("Open new chat: " + chatHandle);
			Intent intent = new Intent(this, ChatActivityLollipop.class);
			intent.setAction(ACTION_CHAT_SHOW_MESSAGES);
			intent.putExtra(CHAT_ID, chatHandle);
			this.startActivity(intent);
		}
		else{
			logError("ERROR WHEN CREATING CHAT " + errorCode);
			showSnackbar(SNACKBAR_TYPE, getString(R.string.create_chat_error), -1);
		}
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		logDebug("onRequestStart: " + request.getRequestString());
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		logDebug("onRequestUpdate: " + request.getRequestString());
	}

	@SuppressLint("NewApi") @Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		logDebug("onRequestFinish: " + request.getRequestString()+"_"+e.getErrorCode());
		if (request.getType() == MegaRequest.TYPE_CREDIT_CARD_CANCEL_SUBSCRIPTIONS){
			if (e.getErrorCode() == MegaError.API_OK){
				showSnackbar(SNACKBAR_TYPE, getString(R.string.cancel_subscription_ok), -1);
			}
			else{
				showSnackbar(SNACKBAR_TYPE, getString(R.string.cancel_subscription_error), -1);
			}
			((MegaApplication) getApplication()).askForCCSubscriptions();
		}
		else if (request.getType() == MegaRequest.TYPE_LOGOUT){
			logDebug("onRequestFinish: " + MegaRequest.TYPE_LOGOUT);

			if (e.getErrorCode() == MegaError.API_OK) {
				logDebug("onRequestFinish:OK:" + MegaRequest.TYPE_LOGOUT);
				logDebug("END logout sdk request - wait chat logout");
			} else if (e.getErrorCode() != MegaError.API_ESID) {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
			}
		} else if(request.getType() == MegaRequest.TYPE_GET_ACHIEVEMENTS) {
            if (e.getErrorCode() == MegaError.API_OK) {
                bonusStorageSMS = getSizeString(request.getMegaAchievementsDetails().getClassStorage(MegaAchievementsDetails.MEGA_ACHIEVEMENT_ADD_PHONE));
            }
            showAddPhoneNumberInMenu();
            checkBeforeShowSMSVerificationDialog();
        }
		else if(request.getType() == MegaRequest.TYPE_SET_ATTR_USER) {
			if(request.getParamType()==MegaApiJava.USER_ATTR_FIRSTNAME){
				logDebug("request.getText(): "+request.getText());
				countUserAttributes--;
				if(((MegaApplication) getApplication()).getMyAccountInfo() == null){
					logError("ERROR: MyAccountInfo is NULL");
				}
				((MegaApplication) getApplication()).getMyAccountInfo().setFirstNameText(request.getText());
				if (e.getErrorCode() == MegaError.API_OK){
					logDebug("The first name has changed");
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					if(maFLol!=null){
						maFLol.updateNameView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
					}
					updateUserNameNavigationView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
				}
				else{
					logError("Error with first name");
					errorUserAttibutes++;
				}

				if(countUserAttributes==0){
					if(errorUserAttibutes==0){
						logDebug("All user attributes changed!");
						showSnackbar(SNACKBAR_TYPE, getString(R.string.success_changing_user_attributes), -1);
					}
					else{
						logWarning("Some error ocurred when changing an attribute: " + errorUserAttibutes);
						showSnackbar(SNACKBAR_TYPE, getString(R.string.error_changing_user_attributes), -1);
					}
					AccountController aC = new AccountController(this);
					errorUserAttibutes=0;
					aC.setCount(0);
				}
			}
			else if(request.getParamType()==MegaApiJava.USER_ATTR_LASTNAME){
				logDebug("request.getText(): " + request.getText());
				countUserAttributes--;
				if(((MegaApplication) getApplication()).getMyAccountInfo() == null){
					logError("ERROR: MyAccountInfo is NULL");
				}
				((MegaApplication) getApplication()).getMyAccountInfo().setLastNameText(request.getText());
				if (e.getErrorCode() == MegaError.API_OK){
					logDebug("The last name has changed");
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					if(maFLol!=null){
						maFLol.updateNameView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
					}
					updateUserNameNavigationView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
				}
				else{
					logError("Error with last name");
					errorUserAttibutes++;
				}

				if(countUserAttributes==0){
					if(errorUserAttibutes==0){
						logDebug("All user attributes changed!");
						showSnackbar(SNACKBAR_TYPE, getString(R.string.success_changing_user_attributes), -1);
					}
					else{
						logWarning("Some error ocurred when changing an attribute: " + errorUserAttibutes);
						showSnackbar(SNACKBAR_TYPE, getString(R.string.error_changing_user_attributes), -1);
					}
					AccountController aC = new AccountController(this);
					errorUserAttibutes=0;
					aC.setCount(0);
				}
			}
			else if(request.getParamType() == MegaApiJava.USER_ATTR_PWD_REMINDER){
				logDebug("MK exported - USER_ATTR_PWD_REMINDER finished");
				if (e.getErrorCode() == MegaError.API_OK || e.getErrorCode() == MegaError.API_ENOENT) {
					logDebug("New value of attribute USER_ATTR_PWD_REMINDER: " + request.getText());
				}
			}
			else if (request.getParamType() == MegaApiJava.USER_ATTR_AVATAR) {
				if (e.getErrorCode() == MegaError.API_OK){
					logDebug("Avatar changed!!");
                    if (request.getFile() != null) {
                        File oldFile = new File(request.getFile());
                        if (isFileAvailable(oldFile)) {
                            File newFile = buildAvatarFile(this,megaApi.getMyEmail() + ".jpg");
                            boolean result = oldFile.renameTo(newFile);
                            if (result) {
								logDebug("The avatar file was correctly renamed");
                            }
                        }
						logDebug("User avatar changed!");
						showSnackbar(SNACKBAR_TYPE, getString(R.string.success_changing_user_avatar), -1);
					}
					else{
						logDebug("User avatar deleted!");
						showSnackbar(SNACKBAR_TYPE, getString(R.string.success_deleting_user_avatar), -1);
					}
					setProfileAvatar();

					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					if(maFLol!=null){
						maFLol.updateAvatar(false);
					}

					LiveEventBus.get(EVENT_AVATAR_CHANGE, Boolean.class).post(true);
				}
				else{
					if(request.getFile()!=null) {
						logError("Some error ocurred when changing avatar: " + e.getErrorString() + " " + e.getErrorCode());
						showSnackbar(SNACKBAR_TYPE, getString(R.string.error_changing_user_avatar), -1);
					} else {
						logError("Some error ocurred when deleting avatar: " + e.getErrorString() + " " + e.getErrorCode());
						showSnackbar(SNACKBAR_TYPE, getString(R.string.error_deleting_user_avatar), -1);
					}

				}
			}
			else if (request.getParamType() == MegaApiJava.USER_ATTR_CONTACT_LINK_VERIFICATION) {
				logDebug("change QR autoaccept - USER_ATTR_CONTACT_LINK_VERIFICATION finished");
				if (e.getErrorCode() == MegaError.API_OK) {
					logDebug("OK setContactLinkOption: " + request.getText());
					if (getSettingsFragment() != null) {
						sttFLol.setSetAutoaccept(false);
						if (sttFLol.getAutoacceptSetting()) {
							sttFLol.setAutoacceptSetting(false);
						} else {
							sttFLol.setAutoacceptSetting(true);
						}
						sttFLol.setValueOfAutoaccept(sttFLol.getAutoacceptSetting());
						logDebug("Autoacept: " + sttFLol.getAutoacceptSetting());
					}
				} else {
					logError("Error setContactLinkOption");
				}
			}
			else if(request.getParamType() == MegaApiJava.USER_ATTR_DISABLE_VERSIONS){
				MegaApplication.setDisableFileVersions(Boolean.valueOf(request.getText()));

				if (e.getErrorCode() != MegaError.API_OK) {
					logError("ERROR:USER_ATTR_DISABLE_VERSIONS");
					mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_STORAGE.getTag());
					if(mStorageFLol!=null){
						mStorageFLol.refreshVersionsInfo();
					}
				}
				else{
					logDebug("File versioning attribute changed correctly");
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER){
			if(request.getParamType() == MegaApiJava.USER_ATTR_PWD_REMINDER){
				//Listener from logout menu
				logDebug("TYPE_GET_ATTR_USER. PasswordReminderFromMyAccount: "+getPasswordReminderFromMyAccount());
				if (e.getErrorCode() == MegaError.API_OK || e.getErrorCode() == MegaError.API_ENOENT){
					logDebug("New value of attribute USER_ATTR_PWD_REMINDER: " +request.getText());
					if (request.getFlag()){
						Intent intent = new Intent(this, TestPasswordActivity.class);
						intent.putExtra("logout", getPasswordReminderFromMyAccount());
						startActivity(intent);
					}
					else if (getPasswordReminderFromMyAccount()){
						if (aC == null){
							aC = new AccountController(this);
						}
						aC.logout(this, megaApi);
					}
				}
				setPasswordReminderFromMyAccount(false);
			}
			else if(request.getParamType()==MegaApiJava.USER_ATTR_AVATAR){
				logDebug("Request avatar");
				if (e.getErrorCode() == MegaError.API_OK){
					setProfileAvatar();
					//refresh MyAccountFragment if visible
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					if(maFLol!=null){
						logDebug("Update the account fragment");
						maFLol.updateAvatar(false);
					}
				}
				else{
					if(e.getErrorCode()==MegaError.API_ENOENT) {
						setDefaultAvatar();
					}

					if(e.getErrorCode()==MegaError.API_EARGS){
						logError("Error changing avatar: ");
					}

					//refresh MyAccountFragment if visible
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
					if(maFLol!=null){
						logDebug("Update the account fragment");
						maFLol.updateAvatar(false);
					}
				}
				LiveEventBus.get(EVENT_AVATAR_CHANGE, Boolean.class).post(false);
			} else if (request.getParamType() == MegaApiJava.USER_ATTR_FIRSTNAME) {
				updateMyData(true, request.getText(), e);
			} else if (request.getParamType() == MegaApiJava.USER_ATTR_LASTNAME) {
				updateMyData(false, request.getText(), e);
			} else if (request.getParamType() == MegaApiJava.USER_ATTR_GEOLOCATION) {

				if(e.getErrorCode() == MegaError.API_OK){
					logDebug("Attribute USER_ATTR_GEOLOCATION enabled");
					MegaApplication.setEnabledGeoLocation(true);
				}
				else{
					logDebug("Attribute USER_ATTR_GEOLOCATION disabled");
					MegaApplication.setEnabledGeoLocation(false);
				}
			}
            else if (request.getParamType() == MegaApiJava.USER_ATTR_CONTACT_LINK_VERIFICATION) {
				logDebug("Type: GET_ATTR_USER ParamType: USER_ATTR_CONTACT_LINK_VERIFICATION --> getContactLinkOption");
				if (e.getErrorCode() == MegaError.API_OK) {
					if (getSettingsFragment() != null) {
						sttFLol.setAutoacceptSetting(request.getFlag());
						logDebug("OK getContactLinkOption: " + request.getFlag());
//						If user request to set QR autoaccept
						if (sttFLol.getSetAutoaccept()) {
							if (sttFLol.getAutoacceptSetting()) {
								logDebug("setAutoaccept false");
//								If autoaccept is enabled -> request to disable
								megaApi.setContactLinksOption(true, this);
							} else {
								logDebug("setAutoaccept true");
//								If autoaccept is disabled -> request to enable
								megaApi.setContactLinksOption(false, this);
							}
						} else {
							sttFLol.setValueOfAutoaccept(sttFLol.getAutoacceptSetting());
						}
						logDebug("Autoacept: " + sttFLol.getAutoacceptSetting());
					}
				} else if (e.getErrorCode() == MegaError.API_ENOENT) {
					logError("Error MegaError.API_ENOENT getContactLinkOption: " + request.getFlag());
					if (getSettingsFragment() != null) {
						sttFLol.setAutoacceptSetting(request.getFlag());
					}
					megaApi.setContactLinksOption(false, this);
				} else {
					logError("Error getContactLinkOption: " + e.getErrorString());
				}
			}
            else if(request.getParamType() == MegaApiJava.USER_ATTR_DISABLE_VERSIONS){
				MegaApplication.setDisableFileVersions(request.getFlag());
				mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_STORAGE.getTag());
				if(mStorageFLol!=null){
					mStorageFLol.refreshVersionsInfo();
				}
			}
        } else if (request.getType() == MegaRequest.TYPE_GET_CHANGE_EMAIL_LINK) {
            logDebug("TYPE_GET_CHANGE_EMAIL_LINK: " + request.getEmail());
            hideKeyboard(managerActivity, 0);

            if (e.getErrorCode() == MegaError.API_OK) {
                logDebug("The change link has been sent");
                showAlert(this, getString(R.string.email_verification_text_change_mail), getString(R.string.email_verification_title));
            } else if (e.getErrorCode() == MegaError.API_EACCESS) {
                logWarning("The new mail already exists");
                showAlert(this, getString(R.string.mail_already_used), getString(R.string.email_verification_title));
            } else if (e.getErrorCode() == MegaError.API_EEXIST) {
                logWarning("Email change already requested (confirmation link already sent).");
                showAlert(this, getString(R.string.mail_changed_confirm_requested), getString(R.string.email_verification_title));
            } else {
                logError("Error when asking for change mail link: " + e.getErrorString() + "___" + e.getErrorCode());
                showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
            }
        }
		else if(request.getType() == MegaRequest.TYPE_CONFIRM_CHANGE_EMAIL_LINK){
			logDebug("CONFIRM_CHANGE_EMAIL_LINK: " + request.getEmail());
			if(e.getErrorCode() == MegaError.API_OK){
				logDebug("Email changed");
				updateMyEmail(request.getEmail());
				showSnackbar(SNACKBAR_TYPE, getString(R.string.email_changed, request.getEmail()), INVALID_HANDLE);
			}
			else if(e.getErrorCode() == MegaError.API_EEXIST){
				logWarning("The new mail already exists");
				showAlert(this, getString(R.string.mail_already_used), getString(R.string.general_error_word));
			}
			else if(e.getErrorCode() == MegaError.API_ENOENT){
				logError("Email not changed -- API_ENOENT");
				showAlert(this, "Email not changed!" + getString(R.string.old_password_provided_incorrect), getString(R.string.general_error_word));
			}
			else{
				logError("Error when asking for change mail link: " + e.getErrorString() + "___" + e.getErrorCode());
				showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_QUERY_RECOVERY_LINK) {
			logDebug("TYPE_GET_RECOVERY_LINK");
			if (e.getErrorCode() == MegaError.API_OK){
				String url = request.getLink();
				logDebug("Cancel account url");
				String myEmail = request.getEmail();
				if(myEmail!=null){
					if(myEmail.equals(megaApi.getMyEmail())){
						logDebug("The email matchs!!!");
						showDialogInsertPassword(url, true);
					}
					else{
						logWarning("Not logged with the correct account: " + e.getErrorString() + "___" + e.getErrorCode());
						showAlert(this, getString(R.string.error_not_logged_with_correct_account), getString(R.string.general_error_word));
					}
				}
				else{
					logError("My email is NULL in the request");
				}
			}
			else if(e.getErrorCode() == MegaError.API_EEXPIRED){
				logError("Error expired link: " + e.getErrorString() + "___" + e.getErrorCode());
				showAlert(this, getString(R.string.cancel_link_expired), getString(R.string.general_error_word));
			}
			else{
				logError("Error when asking for recovery pass link: " + e.getErrorString() + "___" + e.getErrorCode());
				showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
			}
        } else if (request.getType() == MegaRequest.TYPE_GET_CANCEL_LINK) {
            logDebug("TYPE_GET_CANCEL_LINK");
            hideKeyboard(managerActivity, 0);

            if (e.getErrorCode() == MegaError.API_OK) {
                logDebug("Cancelation link received!");
                showAlert(this, getString(R.string.email_verification_text), getString(R.string.email_verification_title));
            } else {
                logError("Error when asking for the cancelation link: " + e.getErrorString() + "___" + e.getErrorCode());
                showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
            }
        }
		else if(request.getType() == MegaRequest.TYPE_CONFIRM_CANCEL_LINK){
			if (e.getErrorCode() == MegaError.API_OK){
				logDebug("ACCOUNT CANCELED");
			}
			else if (e.getErrorCode() == MegaError.API_ENOENT){
				logError("Error cancelling account - API_ENOENT: " + e.getErrorString() + "___" + e.getErrorCode());
				showAlert(this, getString(R.string.old_password_provided_incorrect), getString(R.string.general_error_word));
			}
			else{
				logError("Error cancelling account: " + e.getErrorString() + "___" + e.getErrorCode());
				showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE_CONTACT){

			if (e.getErrorCode() == MegaError.API_OK) {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_contact_removed), -1);
			} else if (e.getErrorCode() == MegaError.API_EMASTERONLY) {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_remove_business_contact, request.getEmail()), -1);
			} else{
				logError("Error deleting contact");
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_contact_not_removed), -1);
			}
			updateContactsView(true, false, false);
		}
		else if (request.getType() == MegaRequest.TYPE_INVITE_CONTACT){
			logDebug("MegaRequest.TYPE_INVITE_CONTACT finished: " + request.getNumber());

			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}


			if(request.getNumber()==MegaContactRequest.INVITE_ACTION_REMIND){
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_contact_invitation_resent), -1);
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					logDebug("OK INVITE CONTACT: " + request.getEmail());
					if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD)
					{
						showSnackbar(SNACKBAR_TYPE, getString(R.string.context_contact_request_sent, request.getEmail()), -1);
					}
					else if(request.getNumber()==MegaContactRequest.INVITE_ACTION_DELETE)
					{
						showSnackbar(SNACKBAR_TYPE, getString(R.string.context_contact_invitation_deleted), -1);
					}
				}
				else{
					logError("ERROR invite contact: " + e.getErrorCode() + "___" + e.getErrorString());
					if(e.getErrorCode()==MegaError.API_EEXIST)
					{
						boolean found = false;
						ArrayList<MegaContactRequest> outgoingContactRequests = megaApi.getOutgoingContactRequests();
						if (outgoingContactRequests != null){
							for (int i=0; i< outgoingContactRequests.size(); i++) {
								if (outgoingContactRequests.get(i).getTargetEmail().equals(request.getEmail())) {
									found = true;
									break;
								}
							}
						}
						if (found) {
							showSnackbar(SNACKBAR_TYPE, getString(R.string.invite_not_sent_already_sent, request.getEmail()), -1);
						}
						else {
							showSnackbar(SNACKBAR_TYPE, getString(R.string.context_contact_already_exists, request.getEmail()), -1);
						}
					}
					else if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD && e.getErrorCode()==MegaError.API_EARGS)
					{
						showSnackbar(SNACKBAR_TYPE, getString(R.string.error_own_email_as_contact), -1);
					}
					else{
						showSnackbar(SNACKBAR_TYPE, getString(R.string.general_error), -1);
					}
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REPLY_CONTACT_REQUEST){
			logDebug("MegaRequest.TYPE_REPLY_CONTACT_REQUEST finished: " + request.getType());

			if (e.getErrorCode() == MegaError.API_OK){

				if(request.getNumber()==MegaContactRequest.REPLY_ACTION_ACCEPT){
					logDebug("I've accepted the invitation");
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_invitacion_reply_accepted), -1);
					MegaContactRequest contactRequest = megaApi.getContactRequestByHandle(request.getNodeHandle());
					logDebug("Handle of the request: " + request.getNodeHandle());
					if(contactRequest!=null){
						//Get the data of the user (avatar and name)
						MegaContactDB contactDB = dbH.findContactByEmail(contactRequest.getSourceEmail());
						if(contactDB==null){
							logWarning("Contact " + contactRequest.getHandle() + " not found! Will be added to DB!");
							cC.addContactDB(contactRequest.getSourceEmail());
						}
						//Update view to get avatar
						cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
						if (cFLol != null){
							cFLol.updateView();
						}
					}
					else{
						logError("ContactRequest is NULL");
					}
				}
				else if(request.getNumber()==MegaContactRequest.REPLY_ACTION_DENY){
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_invitacion_reply_declined), -1);
				}
				else if(request.getNumber()==MegaContactRequest.REPLY_ACTION_IGNORE){
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_invitacion_reply_ignored), -1);
				}
			}
			else{
				showSnackbar(SNACKBAR_TYPE, getString(R.string.general_error), -1);
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){
//				Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_LONG).show();

					if (moveToRubbish){
						//Update both tabs
        				//Rubbish bin
						logDebug("Move to Rubbish");
						refreshAfterMovingToRubbish();
						showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_moved_to_rubbish), -1);
						if (drawerItem == DrawerItem.INBOX) {
							setInboxNavigationDrawer();
						}
						moveToRubbish = false;
						resetAccountDetailsTimeStamp();
					}
					else if(restoreFromRubbish){
						logDebug("Restore from rubbish");
						MegaNode destination = megaApi.getNodeByHandle(request.getParentHandle());
						showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_node_restored, destination.getName()), -1);
						restoreFromRubbish = false;
						resetAccountDetailsTimeStamp();
					}
					else{
						logDebug("Not moved to rubbish");
						refreshAfterMoving();
						showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_moved), -1);
					}
			}
			else {
				if(restoreFromRubbish){
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_restored), -1);
					restoreFromRubbish = false;
				}
				else{
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_moved), -1);
					moveToRubbish = false;
				}
			}

			logDebug("SINGLE move nodes request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFERS){
			logDebug("MegaRequest.TYPE_PAUSE_TRANSFERS");
			//force update the pause notification to prevent missed onTransferUpdate
			sendBroadcast(new Intent(BROADCAST_ACTION_INTENT_UPDATE_PAUSE_NOTIFICATION));

			if (e.getErrorCode() == MegaError.API_OK) {
			    transfersWidget.updateState();

			    if (drawerItem == DrawerItem.TRANSFERS && isTransfersInProgressAdded()) {
					boolean paused = megaApi.areTransfersPaused(MegaTransfer.TYPE_DOWNLOAD) || megaApi.areTransfersPaused(MegaTransfer.TYPE_UPLOAD);
					refreshFragment(FragmentTag.TRANSFERS.getTag());
					mTabsAdapterTransfers.notifyDataSetChanged();

					pauseTransfersMenuIcon.setVisible(!paused);
					playTransfersMenuIcon.setVisible(paused);
				}

                // Update CU backup state.
                int newBackupState = megaApi.areTransfersPaused(MegaTransfer.TYPE_UPLOAD)
                        ? CuSyncManager.State.CU_SYNC_STATE_PAUSE_UP
                        : CuSyncManager.State.CU_SYNC_STATE_ACTIVE;

                CuSyncManager.INSTANCE.updatePrimaryBackupState(newBackupState);
                CuSyncManager.INSTANCE.updateSecondaryBackupState(newBackupState);
            }
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFER) {
			logDebug("One MegaRequest.TYPE_PAUSE_TRANSFER");

			if (e.getErrorCode() == MegaError.API_OK){
				TransfersManagement transfersManagement = MegaApplication.getTransfersManagement();
				int transferTag = request.getTransferTag();

				if (request.getFlag()) {
					transfersManagement.addPausedTransfers(transferTag);
				} else {
					transfersManagement.removePausedTransfers(transferTag);
				}

				if (isTransfersInProgressAdded()){
					tFLol.changeStatusButton(request.getTransferTag());
				}
			}
			else{
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_general_nodes), -1);
			}
		} else if (request.getType() == MegaRequest.TYPE_CANCEL_TRANSFER) {
			if (e.getErrorCode() == MegaError.API_OK){
				MegaApplication.getTransfersManagement().removePausedTransfers(request.getTransferTag());
				transfersWidget.update();
				supportInvalidateOptionsMenu();
			} else {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_general_nodes), MEGACHAT_INVALID_HANDLE);
			}
		} else if(request.getType() == MegaRequest.TYPE_CANCEL_TRANSFERS){
			logDebug("MegaRequest.TYPE_CANCEL_TRANSFERS");
			//After cancelling all the transfers
			if (e.getErrorCode() == MegaError.API_OK){
				transfersWidget.hide();

				if (drawerItem == DrawerItem.TRANSFERS && isTransfersInProgressAdded()) {
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
				}

				MegaApplication.getTransfersManagement().resetPausedTransfers();
			}
			else{
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_general_nodes), -1);
			}

		} else if (request.getType() == MegaRequest.TYPE_KILL_SESSION){
			logDebug("requestFinish TYPE_KILL_SESSION"+MegaRequest.TYPE_KILL_SESSION);
			if (e.getErrorCode() == MegaError.API_OK){
				logDebug("Success kill sessions");
				showSnackbar(SNACKBAR_TYPE, getString(R.string.success_kill_all_sessions), -1);
			}
			else
			{
				logError("Error when killing sessions: " + e.getErrorString());
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_kill_all_sessions), -1);
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE){
			logDebug("requestFinish " + MegaRequest.TYPE_REMOVE);
			if (e.getErrorCode() == MegaError.API_OK){
				if (statusDialog != null){
					if (statusDialog.isShowing()){
						try {
							statusDialog.dismiss();
						}
						catch (Exception ex) {}
					}
				}
				refreshAfterRemoving();
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_removed), -1);
				resetAccountDetailsTimeStamp();
			} else if (e.getErrorCode() == MegaError.API_EMASTERONLY) {
				showSnackbar(SNACKBAR_TYPE, e.getErrorString(), -1);
			} else{
			    showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_removed), -1);
			}
			logDebug("Remove request finished");
		} else if (request.getType() == MegaRequest.TYPE_COPY){
			logDebug("TYPE_COPY");

			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){
				logDebug("Show snackbar!!!!!!!!!!!!!!!!!!!");
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_copied), -1);

				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					if (isCloudAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser),
								sortOrderManagement.getOrderCloud());
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
				else if (drawerItem == DrawerItem.RUBBISH_BIN){
					refreshRubbishBin();
				}
				else if (drawerItem == DrawerItem.INBOX){
					refreshInboxList();
				}

				resetAccountDetailsTimeStamp();
			}
			else{
				if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
					logWarning("OVERQUOTA ERROR: " + e.getErrorCode());
					showOverquotaAlert(false);
				}
				else if(e.getErrorCode()==MegaError.API_EGOINGOVERQUOTA){
					logDebug("OVERQUOTA ERROR: " + e.getErrorCode());
					showOverquotaAlert(true);
				}
				else
				{
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_copied), -1);
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CREATE_FOLDER){
			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}
            if (e.getErrorCode() == MegaError.API_OK){
                showSnackbar(SNACKBAR_TYPE, getString(R.string.context_folder_created), -1);
				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					if (isCloudAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser),
								sortOrderManagement.getOrderCloud());
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				} else if (drawerItem == DrawerItem.SHARED_ITEMS){
					onNodesSharedUpdate();
				} else if (drawerItem == DrawerItem.SEARCH) {
					refreshFragment(FragmentTag.SEARCH.getTag());
				}
			}
			else{
				logError("TYPE_CREATE_FOLDER ERROR: " + e.getErrorCode() + " " + e.getErrorString());
				showSnackbar(SNACKBAR_TYPE, getString(R.string.context_folder_no_created), -1);
			}
		} else if (request.getType() == MegaRequest.TYPE_SUBMIT_PURCHASE_RECEIPT){
			if (e.getErrorCode() == MegaError.API_OK){
				logDebug("PURCHASE CORRECT!");
				drawerItem = DrawerItem.CLOUD_DRIVE;
				selectDrawerItemLollipop(drawerItem);
			}
			else{
				logError("PURCHASE WRONG: " + e.getErrorString() + " (" + e.getErrorCode() + ")");
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REGISTER_PUSH_NOTIFICATION){
			if (e.getErrorCode() == MegaError.API_OK){
				logDebug("FCM OK TOKEN MegaRequest.TYPE_REGISTER_PUSH_NOTIFICATION");
			}
			else{
				logError("FCM ERROR TOKEN TYPE_REGISTER_PUSH_NOTIFICATION: " + e.getErrorCode() + "__" + e.getErrorString());
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MULTI_FACTOR_AUTH_CHECK) {
			// Re-enable 2fa switch first.
			if (getSettingsFragment() != null) {
				sttFLol.reEnable2faSwitch();
			}

			if (e.getErrorCode() == MegaError.API_OK) {
				is2FAEnabled = request.getFlag();

				if (getSettingsFragment() != null) {
					sttFLol.update2FAPreference(is2FAEnabled);
				}
			}
		}
		else if(request.getType() == MegaRequest.TYPE_FOLDER_INFO) {
			if (e.getErrorCode() == MegaError.API_OK) {
				MegaFolderInfo info = request.getMegaFolderInfo();
				int numVersions = info.getNumVersions();
				logDebug("Num versions: " + numVersions);
				long previousVersions = info.getVersionsSize();
				logDebug("Previous versions: " + previousVersions);

				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
					((MegaApplication) getApplication()).getMyAccountInfo().setNumVersions(numVersions);
					((MegaApplication) getApplication()).getMyAccountInfo().setPreviousVersionsSize(previousVersions);
				}

			} else {
				logError("ERROR requesting version info of the account");
			}

			//Refresh My Storage if it is shown
			mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_STORAGE.getTag());
			if(mStorageFLol!=null){
				mStorageFLol.refreshVersionsInfo();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CONTACT_LINK_CREATE) {
			maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
			if (maFLol != null) {
				maFLol.initCreateQR(request, e);
			}
		}
	}

	/**
	 * Updates own firstName/lastName and fullName data in UI and DB.
	 *
	 * @param firstName True if the update makes reference to the firstName, false it to the lastName.
	 * @param newName   New firstName/lastName text.
	 * @param e         MegaError of the request.
	 */
	private void updateMyData(boolean firstName, String newName, MegaError e) {
		MyAccountInfo accountInfo = app.getMyAccountInfo();
		AccountController.updateMyData(firstName, newName, e);

		if (accountInfo != null) {
			accountInfo.setFullName();
			updateUserNameNavigationView(accountInfo.getFullName());

			if (getMyAccountFragment() != null) {
				logDebug("Update the account fragment");
				maFLol.updateNameView(accountInfo.getFullName());
			}
		}
	}

	public void updateAccountStorageInfo(){
		logDebug("updateAccountStorageInfo");
		megaApi.getFolderInfo(megaApi.getRootNode(), this);
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		logWarning("onRequestTemporaryError: " + request.getRequestString() + "__" + e.getErrorCode() + "__" + e.getErrorString());
	}

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		logDebug("onUsersUpdateLollipop");

		if (users != null){
			logDebug("users.size(): "+users.size());
			for(int i=0; i<users.size();i++){
				MegaUser user=users.get(i);

				if(user!=null){
					// 0 if the change is external.
					// >0 if the change is the result of an explicit request
					// -1 if the change is the result of an implicit request made by the SDK internally

					if(user.isOwnChange()>0){
						logDebug("isOwnChange!!!: " + user.getEmail());
						if (user.hasChanged(MegaUser.CHANGE_TYPE_RICH_PREVIEWS)){
							logDebug("Change on CHANGE_TYPE_RICH_PREVIEWS");
							megaApi.shouldShowRichLinkWarning(this);
							megaApi.isRichPreviewsEnabled(this);
						}
					}
					else{
						logDebug("NOT OWN change");

						logDebug("Changes: " + user.getChanges());

						if(megaApi.getMyUser()!=null) {
							if (user.getHandle() == megaApi.getMyUser().getHandle()) {
								logDebug("Change on my account from another client");


								if (user.hasChanged(MegaUser.CHANGE_TYPE_CONTACT_LINK_VERIFICATION)) {
									logDebug("Change on CHANGE_TYPE_CONTACT_LINK_VERIFICATION");
									megaApi.getContactLinksOption(this);
								}
							}
						}

						if (user.hasChanged(MegaUser.CHANGE_TYPE_FIRSTNAME)) {
							if (user.getEmail().equals(megaApi.getMyUser().getEmail())) {
								logDebug("I change my first name");
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_FIRSTNAME, this);
							} else {
								logDebug("The user: " + user.getHandle() + "changed his first name");
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_FIRSTNAME, new GetAttrUserListener(this));
							}
						}

						if (user.hasChanged(MegaUser.CHANGE_TYPE_LASTNAME)) {
							if (user.getEmail().equals(megaApi.getMyUser().getEmail())) {
								logDebug("I change my last name");
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_LASTNAME, this);
							} else {
								logDebug("The user: " + user.getHandle() + "changed his last name");
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_LASTNAME, new GetAttrUserListener(this));
							}
						}

						if (user.hasChanged(MegaUser.CHANGE_TYPE_ALIAS)) {
							logDebug("I changed the user: " + user.getHandle() + " nickname");
							megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_ALIAS, new GetAttrUserListener(this));
						}

						if (user.hasChanged(MegaUser.CHANGE_TYPE_AVATAR)){
							logDebug("The user: " + user.getHandle() + "changed his AVATAR");

							File avatar = buildAvatarFile(this, user.getEmail() + ".jpg");
							Bitmap bitmap = null;
							if (isFileAvailable(avatar)){
								avatar.delete();
							}

							if(user.getEmail().equals(megaApi.getMyUser().getEmail())){
								logDebug("I change my avatar");
                                String destinationPath = buildAvatarFile(this,megaApi.getMyEmail() + ".jpg").getAbsolutePath();
								megaApi.getUserAvatar(megaApi.getMyUser(),destinationPath,this);
							}
							else {
								logDebug("Update de ContactsFragment");
								cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
								if (cFLol != null) {
									if (drawerItem == DrawerItem.CONTACTS) {
										cFLol.updateView();
									}
								}
							}
						}

						if (user.hasChanged(MegaUser.CHANGE_TYPE_EMAIL)){
							logDebug("CHANGE_TYPE_EMAIL");
							if(user.getEmail().equals(megaApi.getMyUser().getEmail())){
								logDebug("I change my mail");
								updateMyEmail(user.getEmail());
							}
							else{
								logDebug("The contact: " + user.getHandle() + " changes the mail.");
								if(dbH.findContactByHandle(String.valueOf(user.getHandle()))==null){
									logWarning("The contact NOT exists -> DB inconsistency! -> Clear!");
									if (dbH.getContactsSize() != megaApi.getContacts().size()){
										dbH.clearContacts();
										FillDBContactsTask fillDBContactsTask = new FillDBContactsTask(this);
										fillDBContactsTask.execute();
									}
								}
								else{
									logDebug("The contact already exists -> update");
									dbH.setContactMail(user.getHandle(),user.getEmail());
								}
							}
						}

						cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
						if(cFLol!=null){
							updateContactsView(true, false, false);
						}
						//When last contact changes avatar, update view.
						maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
						if(maFLol != null) {
							maFLol.updateContactsCount();
							maFLol.updateView();
                        }
					}
				}
				else{
					logWarning("user == null --> Continue...");
					continue;
				}
			}
		}
	}

	public void openLocation(long nodeHandle){
		logDebug("Node handle: " + nodeHandle);

		MegaNode node = megaApi.getNodeByHandle(nodeHandle);
		if(node == null){
			return;
		}
		comesFromNotifications = true;
		comesFromNotificationHandle = nodeHandle;
		MegaNode parent = nC.getParent(node);
		if (parent.getHandle() == megaApi.getRootNode().getHandle()){
			//Cloud Drive
			drawerItem = DrawerItem.CLOUD_DRIVE;
			openFolderRefresh = true;
			comesFromNotificationHandleSaved = parentHandleBrowser;
			setParentHandleBrowser(nodeHandle);
			selectDrawerItemLollipop(drawerItem);
		}
		else if (parent.getHandle() == megaApi.getRubbishNode().getHandle()){
			//Rubbish
			drawerItem = DrawerItem.RUBBISH_BIN;
			openFolderRefresh = true;
			comesFromNotificationHandleSaved = parentHandleRubbish;
			setParentHandleRubbish(nodeHandle);
			selectDrawerItemLollipop(drawerItem);
		}
		else if (parent.getHandle() == megaApi.getInboxNode().getHandle()){
			//Inbox
			drawerItem = DrawerItem.INBOX;
			openFolderRefresh = true;
			comesFromNotificationHandleSaved = parentHandleInbox;
			setParentHandleInbox(nodeHandle);
			selectDrawerItemLollipop(drawerItem);
		}
		else{
			//Incoming Shares
			drawerItem = DrawerItem.SHARED_ITEMS;
			indexShares = 0;
			comesFromNotificationDeepBrowserTreeIncoming = deepBrowserTreeIncoming;
			comesFromNotificationHandleSaved = parentHandleIncoming;
			if (parent != null){
				comesFromNotificationsLevel = deepBrowserTreeIncoming = calculateDeepBrowserTreeIncoming(node, this);
			}
			openFolderRefresh = true;
			setParentHandleIncoming(nodeHandle);
			selectDrawerItemLollipop(drawerItem);
		}
	}

	@Override
	public void onUserAlertsUpdate(MegaApiJava api, ArrayList<MegaUserAlert> userAlerts) {
		logDebug("onUserAlertsUpdate");

		setNotificationsTitleSection();
		notificFragment = (NotificationsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.NOTIFICATIONS.getTag());
		if (notificFragment!=null && userAlerts != null) {
            notificFragment.updateNotifications(userAlerts);
		}

		updateNavigationToolbarIcon();
	}

	@Override
	public void onEvent(MegaApiJava api, MegaEvent event) {

	}

	public void updateMyEmail(String email){
		logDebug("New email: " + email);
		nVEmail.setText(email);
		String oldEmail = dbH.getMyEmail();
		if(oldEmail!=null){
			logDebug("Old email: " + oldEmail);
            try {
                File avatarFile = buildAvatarFile(this,oldEmail + ".jpg");
                if (isFileAvailable(avatarFile)) {
                    File newFile = buildAvatarFile(this, email + ".jpg");
                    if(newFile != null) {
                        boolean result = avatarFile.renameTo(newFile);
                        if (result) {
							logDebug("The avatar file was correctly renamed");
                        }
                    }
                }
            }
			catch(Exception e){
				logError("EXCEPTION renaming the avatar on changing email", e);
			}
		}
		else{
			logError("ERROR: Old email is NULL");
		}

		dbH.saveMyEmail(email);

		maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
		if(maFLol!=null){
			maFLol.updateMailView(email);
		}
	}

	public void onNodesCloudDriveUpdate() {
		logDebug("onNodesCloudDriveUpdate");

		rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
		if (rubbishBinFLol != null) {
			rubbishBinFLol.hideMultipleSelect();

			if (isClearRubbishBin) {
				isClearRubbishBin = false;
				parentHandleRubbish = megaApi.getRubbishNode().getHandle();
				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(),
						sortOrderManagement.getOrderCloud());
				rubbishBinFLol.setNodes(nodes);
				rubbishBinFLol.getRecyclerView().invalidate();
			} else {
				refreshRubbishBin();
			}
		}
		if (pagerOfflineFragment != null) {
			pagerOfflineFragment.refreshNodes();
		}

		refreshCloudDrive();
	}

	public void onNodesInboxUpdate() {
		iFLol = (InboxFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.INBOX.getTag());
		if (iFLol != null){
		    iFLol.hideMultipleSelect();
			iFLol.refresh();
		}
	}

	public void onNodesSearchUpdate() {
		if (getSearchFragment() != null){
			//stop from query for empty string.
			textSubmitted = true;
			sFLol.refresh();
		}
	}

	public void refreshIncomingShares () {
		if (!isIncomingAdded()) return;

		inSFLol.hideMultipleSelect();
		inSFLol.refresh();
	}

	private void refreshOutgoingShares () {
		if (!isOutgoingAdded()) return;

		outSFLol.hideMultipleSelect();
		outSFLol.refresh();
    }

    private void refreshLinks () {
        if (!isLinksAdded()) return;

        lF.refresh();
    }

	public void refreshInboxList () {
		iFLol = (InboxFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.INBOX.getTag());
		if (iFLol != null){
			iFLol.getRecyclerView().invalidate();
		}
	}

	public void onNodesSharedUpdate() {
		logDebug("onNodesSharedUpdate");

		refreshOutgoingShares();
		refreshIncomingShares();
		refreshLinks();

		refreshSharesPageAdapter();
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> updatedNodes) {
		logDebug("onNodesUpdateLollipop");
		try {
			statusDialog.dismiss();
		}
		catch (Exception ex) {}

		boolean updateContacts = false;

		if(updatedNodes!=null){
			//Verify is it is a new item to the inbox
			for(int i=0;i<updatedNodes.size(); i++){
				MegaNode updatedNode = updatedNodes.get(i);

				if(!updateContacts){
					if(updatedNode.isInShare()){
						updateContacts = true;
					}
				}

				if(updatedNode.getParentHandle()==inboxNode.getHandle()){
					logDebug("New element to Inbox!!");
					setInboxNavigationDrawer();
				}
			}
		}

		if(updateContacts){
			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
			if (cFLol != null){
				logDebug("Incoming update - update contacts section");
				cFLol.updateShares();
			}
		}


		onNodesCloudDriveUpdate();

		onNodesSearchUpdate();

		onNodesSharedUpdate();

		onNodesInboxUpdate();

		checkCameraUploadFolder(false,updatedNodes);

		refreshCUNodes();

		LiveEventBus.get(EVENT_NODES_CHANGE).post(true);

		// Invalidate the menu will collapse/expand the search view and set the query text to ""
		// (call onQueryTextChanged) (BTW, SearchFragment uses textSubmitted to avoid the query
		// text changed to "" for once)
		if (drawerItem == DrawerItem.HOMEPAGE) return;

		setToolbarTitle();
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		logDebug("onReloadNeeded");
	}

	@Override
	public void onAccountUpdate(MegaApiJava api) {
		logDebug("onAccountUpdate");
	}

	@Override
	public void onContactRequestsUpdate(MegaApiJava api,ArrayList<MegaContactRequest> requests) {
		logDebug("onContactRequestsUpdate");

		if(requests!=null){
			for(int i=0; i<requests.size();i++){
				MegaContactRequest req = requests.get(i);
				if(req.isOutgoing()){
					logDebug("SENT REQUEST");
					logDebug("STATUS: " + req.getStatus() + ", Contact Handle: " + req.getHandle());
					if(req.getStatus()==MegaContactRequest.STATUS_ACCEPTED){
						cC.addContactDB(req.getTargetEmail());
					}
					updateContactsView(true, true, false);
				}
				else{
					logDebug("RECEIVED REQUEST");
					setContactTitleSection();
					logDebug("STATUS: " + req.getStatus() + " Contact Handle: " + req.getHandle());
					if(req.getStatus()==MegaContactRequest.STATUS_ACCEPTED){
						cC.addContactDB(req.getSourceEmail());
					}
					updateContactsView(true, false, true);
				}
			}
		}

		updateNavigationToolbarIcon();
	}

	/**
	 * Pauses a transfer.
	 *
	 * @param mT	the transfer to pause
	 */
    public void pauseIndividualTransfer(MegaTransfer mT) {
        if (mT == null) {
            logWarning("Transfer object is null.");
            return;
        }

        logDebug("Resume transfer - Node handle: " + mT.getNodeHandle());
        megaApi.pauseTransfer(mT, mT.getState() != MegaTransfer.STATE_PAUSED, managerActivity);
    }

	/**
	 * Shows a warning to ensure if it is sure of remove all completed transfers.
	 */
	public void showConfirmationClearCompletedTransfers() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setMessage(R.string.confirmation_to_clear_completed_transfers)
				.setPositiveButton(R.string.general_clear, (dialog, which) -> {
					dbH.emptyCompletedTransfers();

					if (isTransfersCompletedAdded()) {
						completedTFLol.clearCompletedTransfers();
					}
					supportInvalidateOptionsMenu();
				})
				.setNegativeButton(R.string.general_dismiss, null);

		confirmationTransfersDialog = builder.create();
		setConfirmationTransfersDialogNotCancellableAndShow();
	}

	/**
	 * Shows a warning to ensure if it is sure of cancel selected transfers.
	 */
	public void showConfirmationCancelSelectedTransfers(List<MegaTransfer> selectedTransfers) {
		if (selectedTransfers == null || selectedTransfers.isEmpty()) {
			return;
		}

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setMessage(getResources().getQuantityString(R.plurals.cancel_selected_transfers, selectedTransfers.size()))
				.setPositiveButton(R.string.button_continue, (dialog, which) -> {
					CancelTransferListener cancelTransferListener = new CancelTransferListener(managerActivity);
					cancelTransferListener.cancelTransfers(selectedTransfers);

					if(isTransfersInProgressAdded()) {
						tFLol.destroyActionMode();
					}
				})
				.setNegativeButton(R.string.general_dismiss, null);

		confirmationTransfersDialog = builder.create();
		setConfirmationTransfersDialogNotCancellableAndShow();
	}

	/**
	 * Shows a warning to ensure if it is sure of cancel all transfers.
	 */
	public void showConfirmationCancelAllTransfers() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setMessage(getResources().getString(R.string.cancel_all_transfer_confirmation))
				.setPositiveButton(R.string.cancel_all_action, (dialog, which) -> {
					megaApi.cancelTransfers(MegaTransfer.TYPE_DOWNLOAD, managerActivity);
					megaApi.cancelTransfers(MegaTransfer.TYPE_UPLOAD, managerActivity);
					cancelAllUploads(ManagerActivityLollipop.this);
					refreshFragment(FragmentTag.TRANSFERS.getTag());
					refreshFragment(FragmentTag.COMPLETED_TRANSFERS.getTag());
				})
				.setNegativeButton(R.string.general_dismiss, null);

		confirmationTransfersDialog = builder.create();
        setConfirmationTransfersDialogNotCancellableAndShow();
	}

    private void setConfirmationTransfersDialogNotCancellableAndShow() {
        if (confirmationTransfersDialog != null) {
            confirmationTransfersDialog.setCancelable(false);
            confirmationTransfersDialog.setCanceledOnTouchOutside(false);
            confirmationTransfersDialog.show();
        }
    }

	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		logDebug("onTransferStart: " + transfer.getNotificationNumber()+ "-" + transfer.getNodeHandle() + " - " + transfer.getTag());

		if (!existOngoingTransfers(megaApi)) {
			updateLogoutWarnings();
		}

		if(transfer.isStreamingTransfer()){
			return;
		}

		if(transferCallback<transfer.getNotificationNumber()) {
			transferCallback = transfer.getNotificationNumber();
			long now = Calendar.getInstance().getTimeInMillis();
			lastTimeOnTransferUpdate = now;

			if(!transfer.isFolderTransfer()){
				transfersInProgress.add(transfer.getTag());

				if (isTransfersInProgressAdded()){
					tFLol.transferStart(transfer);
				}
			}
		}
	}

	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		logDebug("onTransferFinish: " + transfer.getNodeHandle() + " - " + transfer.getTag() + "- " +transfer.getNotificationNumber());
		if(transfer.isStreamingTransfer()){
			return;
		}

		if(transferCallback<transfer.getNotificationNumber()) {

			transferCallback = transfer.getNotificationNumber();
			long now = Calendar.getInstance().getTimeInMillis();
			lastTimeOnTransferUpdate = now;

			if(!transfer.isFolderTransfer()){
				ListIterator li = transfersInProgress.listIterator();
				int index = 0;
				while(li.hasNext()) {
					Integer next = (Integer) li.next();
					if(next == transfer.getTag()){
						index=li.previousIndex();
						break;
					}
				}

				if(!transfersInProgress.isEmpty()){
					transfersInProgress.remove(index);
					logDebug("The transfer with index " + index + " has been removed, left: " + transfersInProgress.size());
				}
				else{
					logDebug("The transferInProgress is EMPTY");
				}

				int pendingTransfers = 	megaApi.getNumPendingDownloads() + megaApi.getNumPendingUploads();

				if(pendingTransfers<=0){
					if (pauseTransfersMenuIcon != null) {
						pauseTransfersMenuIcon.setVisible(false);
						playTransfersMenuIcon.setVisible(false);
						cancelAllTransfersMenuItem.setVisible(false);
					}
				}

                onNodesCloudDriveUpdate();
				onNodesInboxUpdate();
				onNodesSearchUpdate();
				onNodesSharedUpdate();
				LiveEventBus.get(EVENT_NODES_CHANGE).post(false);

				if (isTransfersInProgressAdded()){
					tFLol.transferFinish(transfer.getTag());
				}
			}
		}

		if (!existOngoingTransfers(megaApi)) {
			updateLogoutWarnings();
		}
	}

	private void updateLogoutWarnings() {
		if (getMyAccountFragment() != null) {
			maFLol.checkLogoutWarnings();
		}
	}

	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {

		if(transfer.isStreamingTransfer()){
			return;
		}

		long now = Calendar.getInstance().getTimeInMillis();
		if((now - lastTimeOnTransferUpdate)>ONTRANSFERUPDATE_REFRESH_MILLIS){
			logDebug("Update onTransferUpdate: " + transfer.getNodeHandle() + " - " + transfer.getTag()+ " - "+ transfer.getNotificationNumber());
			lastTimeOnTransferUpdate = now;

			if (!transfer.isFolderTransfer() && transferCallback < transfer.getNotificationNumber()) {
				transferCallback = transfer.getNotificationNumber();

				if (isTransfersInProgressAdded()) {
					tFLol.transferUpdate(transfer);
				}
			}
		}
	}

	@Override
	public void onTransferTemporaryError(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		logWarning("onTransferTemporaryError: " + transfer.getNodeHandle() + " - " + transfer.getTag());

		if(e.getErrorCode() == MegaError.API_EOVERQUOTA){
			if (e.getValue() != 0) {
				logDebug("TRANSFER OVERQUOTA ERROR: " + e.getErrorCode());
                transfersWidget.update();
			}
			else {
				logWarning("STORAGE OVERQUOTA ERROR: " + e.getErrorCode());
                //work around - SDK does not return over quota error for folder upload,
                //so need to be notified from global listener
                if (transfer.getType() == MegaTransfer.TYPE_UPLOAD) {
					logDebug("Over quota");
                    Intent intent = new Intent(this,UploadService.class);
                    intent.setAction(ACTION_OVERQUOTA_STORAGE);
                    startService(intent);
                }
            }
        }
	}

	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer, byte[] buffer) {
		logDebug("onTransferData");
		return true;
	}

	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public boolean isListCameraUploads() {
		return false;
	}

	public boolean isSmallGridCameraUploads() {
		return isSmallGridCameraUploads;
	}
	public void setSmallGridCameraUploads(boolean isSmallGridCameraUploads) {
		this.isSmallGridCameraUploads = isSmallGridCameraUploads;
	}

	public boolean getFirstLogin() {
		return firstLogin;
	}
	public void setFirstLogin(boolean flag){
		firstLogin = flag;
	}

	public boolean getAskPermissions() {
		return askPermissions;
	}

	public String getPathNavigationOffline() {
		return pathNavigationOffline;
	}

	public void setPathNavigationOffline(String pathNavigationOffline) {
		logDebug("setPathNavigationOffline: " + pathNavigationOffline);
		this.pathNavigationOffline = pathNavigationOffline;
	}

	public int getDeepBrowserTreeIncoming() {
		return deepBrowserTreeIncoming;
	}

	public void setDeepBrowserTreeIncoming(int deep) {
		deepBrowserTreeIncoming=deep;
	}

	public void increaseDeepBrowserTreeIncoming() {
		deepBrowserTreeIncoming++;
	}

	public void decreaseDeepBrowserTreeIncoming() {
		deepBrowserTreeIncoming--;
	}

	public int getDeepBrowserTreeOutgoing() {
		return deepBrowserTreeOutgoing;
	}

	public void setDeepBrowserTreeOutgoing(int deep) {
		this.deepBrowserTreeOutgoing = deep;
	}

	public void increaseDeepBrowserTreeOutgoing() {
		deepBrowserTreeOutgoing++;
	}

	public void decreaseDeepBrowserTreeOutgoing() {
		deepBrowserTreeOutgoing--;
	}

	public void setDeepBrowserTreeLinks(int deepBrowserTreeLinks) {
		this.deepBrowserTreeLinks = deepBrowserTreeLinks;
	}

	public int getDeepBrowserTreeLinks() {
		return deepBrowserTreeLinks;
	}

	public void increaseDeepBrowserTreeLinks() {
		deepBrowserTreeLinks++;
	}

	public void decreaseDeepBrowserTreeLinks() {
		deepBrowserTreeLinks--;
	}

	public DrawerItem getDrawerItem() {
		return drawerItem;
	}

	public void setDrawerItem(DrawerItem drawerItem) {
		this.drawerItem = drawerItem;
	}

	public int getTabItemShares(){
		if (viewPagerShares == null) return ERROR_TAB;

		return viewPagerShares.getCurrentItem();
	}

	public int getTabItemContacts(){
		if (viewPagerContacts == null) return ERROR_TAB;

		return viewPagerContacts.getCurrentItem();
	}

	private int getTabItemMyAccount () {
		if (viewPagerMyAccount == null) return ERROR_TAB;

		return viewPagerMyAccount.getCurrentItem();
	}

	private int getTabItemTransfers() {
		return viewPagerTransfers == null ? ERROR_TAB : viewPagerTransfers.getCurrentItem();
	}

	public void setTabItemShares(int index){
		viewPagerShares.setCurrentItem(index);
	}

	public void setTabItemContacts(int index){
		viewPagerContacts.setCurrentItem(index);
	}

	public void showChatPanel(MegaChatListItem chat){
		logDebug("showChatPanel");

		if (chat == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedChatItemId = chat.getChatId();
		bottomSheetDialogFragment = new ChatBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void updateUserNameNavigationView(String fullName){
		logDebug("updateUserNameNavigationView");

		nVDisplayName.setText(fullName);
		setProfileAvatar();
	}

	public void updateMailNavigationView(String email){
		logDebug("updateMailNavigationView");
		nVEmail.setText(megaApi.getMyEmail());
	}

	public void hideFabButton(){
		fabButton.hide();
	}

	/**
	 * Updates the fabButton icon and shows it.
	 */
	private void updateFabAndShow() {
		fabButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_white));
		fabButton.show();
	}

	/**
	 * Shows or hides the fabButton depending on the current section.
	 */
	public void showFabButton() {
		if (drawerItem == null) {
			return;
		}

		switch (drawerItem) {
			case CLOUD_DRIVE:
				updateFabAndShow();
				break;

			case SHARED_ITEMS:
				switch (getTabItemShares()) {
					case INCOMING_TAB:
						if (!isIncomingAdded()) break;

						MegaNode parentNodeInSF = megaApi.getNodeByHandle(parentHandleIncoming);
						if (deepBrowserTreeIncoming <= 0 || parentNodeInSF == null) {
							hideFabButton();
							break;
						}

						switch (megaApi.getAccess(parentNodeInSF)) {
							case MegaShare.ACCESS_OWNER:
							case MegaShare.ACCESS_READWRITE:
							case MegaShare.ACCESS_FULL:
								updateFabAndShow();
								break;

							case MegaShare.ACCESS_READ:
								hideFabButton();
								break;
						}
						break;

					case OUTGOING_TAB:
						if (!isOutgoingAdded()) break;

						if (deepBrowserTreeOutgoing <= 0) {
							hideFabButton();
						} else {
							updateFabAndShow();
						}
						break;

					case LINKS_TAB:
						if (!isLinksAdded()) break;

						if (deepBrowserTreeLinks <= 0) {
							hideFabButton();
						} else {
							updateFabAndShow();
						}
						break;

					default:
						hideFabButton();
				}
				break;

			case CONTACTS:
				switch (getTabItemContacts()) {
					case CONTACTS_TAB:
					case SENT_REQUESTS_TAB:
						updateFabAndShow();
						break;

					default:
						hideFabButton();
				}
				break;

			case CHAT:
				if (megaChatApi == null) {
					hideFabButton();
					break;
				}

				updateFabAndShow();
				break;

			default:
				hideFabButton();
		}
	}

	public AndroidCompletedTransfer getSelectedTransfer() {
		return selectedTransfer;
	}

	public MegaNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(MegaNode selectedNode) {
		this.selectedNode = selectedNode;
	}


	public ContactsFragmentLollipop getContactsFragment() {
		return cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
	}

	public MyAccountFragmentLollipop getMyAccountFragment() {
		return maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_ACCOUNT.getTag());
	}

	public MyStorageFragmentLollipop getMyStorageFragment() {
		return mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.MY_STORAGE.getTag());
	}

	public UpgradeAccountFragmentLollipop getUpgradeAccountFragment() {
		return upAFL;
	}

	public void setContactsFragment(ContactsFragmentLollipop cFLol) {
		this.cFLol = cFLol;
	}

	public SettingsFragmentLollipop getSettingsFragment() {
		return sttFLol = (SettingsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SETTINGS.getTag());
	}

	public void setSettingsFragment(SettingsFragmentLollipop sttFLol) {
		this.sttFLol = sttFLol;
	}

	public MegaContactAdapter getSelectedUser() {
		return selectedUser;
	}


	public MegaContactRequest getSelectedRequest() {
		return selectedRequest;
	}

	public MegaOffline getSelectedOfflineNode() {
		return selectedOfflineNode;
	}

	public void setSelectedAccountType(int selectedAccountType) {
		this.selectedAccountType = selectedAccountType;
	}


	public int getDisplayedAccountType() {
		return displayedAccountType;
	}

	public void setDisplayedAccountType(int displayedAccountType) {
		this.displayedAccountType = displayedAccountType;
	}

	@Override
	public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {
		if (item != null){
			logDebug("Chat ID:" + item.getChatId());
			if (item.isPreview()) {
				return;
			}
		}
		else{
			logWarning("Item NULL");
			return;
		}

		rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
		if(rChatFL!=null){
			rChatFL.listItemUpdate(item);
		}

		if (item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT)) {
			logDebug("Change unread count: " + item.getUnreadCount());
			setChatBadge();
			updateNavigationToolbarIcon();
		}
	}

	@Override
	public void onChatInitStateUpdate(MegaChatApiJava api, int newState) {
		logDebug("New state: " + newState);
		if (newState == MegaChatApi.INIT_ERROR) {
			// chat cannot initialize, disable chat completely
		}
	}

	@Override
	public void onChatOnlineStatusUpdate(MegaChatApiJava api, long userHandle, int status, boolean inProgress) {
		logDebug("Status: " + status + ", In Progress: " + inProgress);
		if(inProgress){
			status = -1;
		}

		if (megaChatApi != null) {
			rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
			if (userHandle == megaChatApi.getMyUserHandle()) {
				logDebug("My own status update");
				setContactStatus();
				if (drawerItem == DrawerItem.CHAT) {
					if (rChatFL != null) {
						rChatFL.onlineStatusUpdate(status);
					}
				}
			} else {
				logDebug("Status update for the user: " + userHandle);
				rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
				if (rChatFL != null) {
					logDebug("Update Recent chats view");
					rChatFL.contactStatusUpdate(userHandle, status);
				}

				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
				if (cFLol != null) {
					logDebug("Update Contacts view");
					cFLol.contactPresenceUpdate(userHandle, status);
				}
			}
		}
	}

	@Override
	public void onChatPresenceConfigUpdate(MegaChatApiJava api, MegaChatPresenceConfig config) { }

	@Override
	public void onChatConnectionStateUpdate(MegaChatApiJava api, long chatid, int newState) {
		logDebug("Chat ID: " + chatid + ", New state: " + newState);
		if(newState==MegaChatApi.CHAT_CONNECTION_ONLINE && chatid==-1){
			logDebug("Online Connection: " + chatid);
			rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
			if (rChatFL != null){
				rChatFL.setChats();
				if(drawerItem == DrawerItem.CHAT){
					rChatFL.setStatus();
				}
			}
		}

		MegaChatRoom chatRoom = api.getChatRoom(chatid);
		if (MegaApplication.isWaitingForCall() && newState == MegaChatApi.CHAT_CONNECTION_ONLINE
				&& chatRoom != null && chatRoom.getPeerHandle(0) == MegaApplication.getUserWaitingForCall()) {
			startCallWithChatOnline(this, api.getChatRoom(chatid));
		}
	}

    @Override
    public void onChatPresenceLastGreen(MegaChatApiJava api, long userhandle, int lastGreen) {
		logDebug("User Handle: " + userhandle + ", Last green: " + lastGreen);

		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.CONTACTS.getTag());
		if(cFLol!=null){
			logDebug("Update Contacts view");
			cFLol.contactLastGreenUpdate(userhandle, lastGreen);
		}
    }

	public void copyError(){
		try {
			statusDialog.dismiss();
			showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_copied), -1);
		}
		catch (Exception ex) {}
	}

	public void setDrawerLockMode (boolean locked) {
        if (locked){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        else{
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

	/**
	 * This method is used to change the elevation of the AppBarLayout when
	 * scrolling the RecyclerView
	 * @param withElevation true if need elevation, false otherwise
	 */
	public void changeAppBarElevation(boolean withElevation) {
		changeAppBarElevation(withElevation, ELEVATION_SCROLL);
	}

	/**
	 * This method is used to change the elevation of the AppBarLayout for some reason
	 *
	 * @param withElevation true if need elevation, false otherwise
	 * @param cause for what cause adding/removing elevation. Only if mElevationCause(cause bitmap)
	 *              is zero will the elevation being eliminated
	 */
	public void changeAppBarElevation(boolean withElevation, int cause) {
		if (withElevation) {
			mElevationCause |= cause;
		} else if ((mElevationCause & cause) > 0) {
			mElevationCause ^= cause;
		}

		// In landscape mode, if no call in progress layout ("Tap to return call"), then don't show elevation
		if (mElevationCause == ELEVATION_CALL_IN_PROGRESS && callInProgressLayout.getVisibility() != View.VISIBLE) return;

		// If any Tablayout is visible, set the background of the toolbar to transparent (or its elevation
		// overlay won't be correctly set via AppBarLayout) and then set the elevation of AppBarLayout,
		// in this way, both Toolbar and TabLayout would have expected elevation overlay.
		// If TabLayout is invisible, directly set toolbar's color for the elevation effect. Set AppBarLayout
		// elevation in this case, a crack would appear between toolbar and ChatRecentFragment's Appbarlayout, for example.
		float elevation = getResources().getDimension(R.dimen.toolbar_elevation);
		int toolbarElevationColor = ColorUtils.getColorForElevation(this, elevation);
		int transparentColor = ContextCompat.getColor(this, android.R.color.transparent);
		boolean onlySetToolbar = Util.isDarkMode(this) && !mShowAnyTabLayout;
		boolean enableCUVisible = cuLayout.getVisibility() == View.VISIBLE;

		if (mElevationCause > 0) {
			if (onlySetToolbar) {
				toolbar.setBackgroundColor(toolbarElevationColor);
				if (enableCUVisible) cuLayout.setBackgroundColor(toolbarElevationColor);
			} else {
				toolbar.setBackgroundColor(transparentColor);
				if (enableCUVisible) cuLayout.setBackground(null);
				abL.setElevation(elevation);
			}
		} else {
			toolbar.setBackgroundColor(transparentColor);
			if (enableCUVisible) cuLayout.setBackground(null);
			abL.setElevation(0);
		}

		ColorUtils.changeStatusBarColorForElevation(this, mElevationCause > 0);
	}

	public long getParentHandleInbox() {
		return parentHandleInbox;
	}

	public void setContactTitleSection(){
		ArrayList<MegaContactRequest> requests = megaApi.getIncomingContactRequests();

		if (contactsSectionText != null) {
			if(requests!=null){
				int pendingRequest = requests.size();
				if(pendingRequest==0){
					contactsSectionText.setText(getString(R.string.section_contacts));
				}
				else{
					setFormattedContactTitleSection(pendingRequest, true);
				}
			}
		}
	}

	void setFormattedContactTitleSection (int pendingRequest, boolean enable) {
		String textToShow = String.format(getString(R.string.section_contacts_with_notification), pendingRequest);
		try {
			if (enable) {
				textToShow = textToShow.replace("[A]", "<font color=\'" + ColorUtils.getColorHexString(this, R.color.red_600_red_300) + "\'>");
			}
			else {
				textToShow = textToShow.replace("[A]", "<font color=\'#ffcccc\'>");
			}
			textToShow = textToShow.replace("[/A]", "</font>");
		}
		catch(Exception e){
			logError("Formatted string: " + textToShow, e);
		}

		Spanned result = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
		} else {
			result = Html.fromHtml(textToShow);
		}
		contactsSectionText.setText(result);
	}

	public void setNotificationsTitleSection(){
		int unread = megaApi.getNumUnreadUserAlerts();

		if(unread == 0){
			notificationsSectionText.setText(getString(R.string.title_properties_chat_contact_notifications));
		}
		else{
			setFormattedNotificationsTitleSection(unread, true);
		}
	}

	void setFormattedNotificationsTitleSection (int unread, boolean enable) {
		String textToShow = String.format(getString(R.string.section_notification_with_unread), unread);
		try {
			if (enable) {
				textToShow = textToShow.replace("[A]", "<font color=\'"
						+ ColorUtils.getColorHexString(this, R.color.red_600_red_300)
						+ "\'>");
			}
			else {
				textToShow = textToShow.replace("[A]", "<font color=\'#ffcccc\'>");
			}
			textToShow = textToShow.replace("[/A]", "</font>");
		}
		catch(Exception e){
			logError("Formatted string: " + textToShow, e);
		}

		Spanned result = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
		} else {
			result = Html.fromHtml(textToShow);
		}
		notificationsSectionText.setText(result);
	}

	public void setChatBadge() {
		if(megaChatApi != null) {
			int numberUnread = megaChatApi.getUnreadChats();
			if (numberUnread == 0) {
				chatBadge.setVisibility(View.GONE);
			}
			else {
				chatBadge.setVisibility(View.VISIBLE);
				if (numberUnread > 9) {
					((TextView) chatBadge.findViewById(R.id.chat_badge_text)).setText("9+");
				}
				else {
					((TextView) chatBadge.findViewById(R.id.chat_badge_text)).setText("" + numberUnread);
				}
			}
		}
		else {
			chatBadge.setVisibility(View.GONE);
		}
	}

	private void setCallBadge(){
		if (!isOnline(this) || megaChatApi == null || megaChatApi.getNumCalls() <= 0 || (megaChatApi.getNumCalls() == 1 && participatingInACall())) {
			callBadge.setVisibility(View.GONE);
			return;
		}

		callBadge.setVisibility(View.VISIBLE);
	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public boolean getPasswordReminderFromMyAccount() {
		return passwordReminderFromMyAccount;
	}

	public void setPasswordReminderFromMyAccount(boolean passwordReminderFromMyAccount) {
		this.passwordReminderFromMyAccount = passwordReminderFromMyAccount;
	}

	public void refreshMenu(){
		logDebug("refreshMenu");
		supportInvalidateOptionsMenu();
	}

	public boolean is2FAEnabled (){
		return is2FAEnabled;
	}

	//need to check image existence before use due to android content provider issue.
	//Can not check query count - still get count = 1 even file does not exist
	private boolean checkProfileImageExistence(Uri uri){
		boolean isFileExist = false;
		InputStream inputStream;
		try {
			inputStream = this.getContentResolver().openInputStream(uri);
			if(inputStream != null){
				isFileExist = true;
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return isFileExist;
	}

	/**
	 * Sets or removes the layout behaviour to hide the bottom view when scrolling.
	 *
	 * @param enable True if should set the behaviour, false if should remove it.
	 */
	public void enableHideBottomViewOnScroll(boolean enable) {
		LinearLayout layout = findViewById(R.id.container_bottom);
		if (layout == null) {
			return;
		}

		final CoordinatorLayout.LayoutParams fParams
				= new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fParams.setMargins(0, 0, 0, enable ? 0 : getResources().getDimensionPixelSize(R.dimen.bottom_navigation_view_height));
		fragmentLayout.setLayoutParams(fParams);

		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();
		params.setBehavior(enable ? new CustomHideBottomViewOnScrollBehaviour<LinearLayout>() : null);
		layout.setLayoutParams(params);
	}

	/**
	 * Shows all the content of bottom view.
	 */
	public void showBottomView() {
		LinearLayout bottomView = findViewById(R.id.container_bottom);
		if (bottomView == null || fragmentLayout == null) {
			return;
		}

		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fragmentLayout.getLayoutParams();
		params.setMargins(0, 0, 0,
				getResources().getDimensionPixelSize(R.dimen.bottom_navigation_view_height));
		bottomView.animate().translationY(0).setDuration(175)
				.withStartAction(() -> bottomView.setVisibility(View.VISIBLE))
				.withEndAction(() -> fragmentLayout.setLayoutParams(params)).start();
	}

	/**
	 * Shows or hides the bottom view and animates the transition.
	 *
	 * @param hide True if should hide it, false if should show it.
	 */
	public void animateBottomView(boolean hide) {
		LinearLayout bottomView = findViewById(R.id.container_bottom);
		if (bottomView == null || fragmentLayout == null) {
			return;
		}

		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fragmentLayout.getLayoutParams();

		if (hide && bottomView.getVisibility() == View.VISIBLE) {
			bottomView.animate().translationY(bottomView.getHeight()).setDuration(ANIMATION_DURATION)
					.withStartAction(() -> params.bottomMargin = 0)
					.withEndAction(() -> bottomView.setVisibility(View.GONE)).start();
		} else if (!hide && bottomView.getVisibility() == View.GONE) {
			int bottomMargin = getResources().getDimensionPixelSize(R.dimen.bottom_navigation_view_height);

			bottomView.animate().translationY(0).setDuration(ANIMATION_DURATION)
					.withStartAction(() -> bottomView.setVisibility(View.VISIBLE))
					.withEndAction(() -> params.bottomMargin = bottomMargin)
					.start();
		}
	}

	public void showHideBottomNavigationView(boolean hide) {
		if (bNV == null) return;

		final CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		int height = getResources().getDimensionPixelSize(R.dimen.bottom_navigation_view_height);

		if (hide && bNV.getVisibility() == View.VISIBLE) {
			updateMiniAudioPlayerVisibility(false);
			params.setMargins(0, 0, 0, 0);
			fragmentLayout.setLayoutParams(params);
			bNV.animate().translationY(height).setDuration(ANIMATION_DURATION).withEndAction(() ->
				bNV.setVisibility(View.GONE)
			).start();
		} else if (!hide && bNV.getVisibility() == View.GONE) {
			bNV.animate().translationY(0).setDuration(ANIMATION_DURATION).withStartAction(() ->
				bNV.setVisibility(View.VISIBLE)
			).withEndAction(() -> {
				updateMiniAudioPlayerVisibility(true);
				params.setMargins(0, 0, 0, height);
				fragmentLayout.setLayoutParams(params);
			}).start();
		}

		updateTransfersWidgetPosition(hide);
	}

	public void markNotificationsSeen(boolean fromAndroidNotification){
		logDebug("fromAndroidNotification: " + fromAndroidNotification);

		if(fromAndroidNotification){
			megaApi.acknowledgeUserAlerts();
		}
		else{
			if(drawerItem == ManagerActivityLollipop.DrawerItem.NOTIFICATIONS && app.isActivityVisible()){
				megaApi.acknowledgeUserAlerts();
			}
		}
	}

	public void showKeyboardForSearch() {
		showKeyboardDelayed(searchView.findViewById(R.id.search_src_text));
		if (searchView != null) {
			searchView.requestFocus();
		}
	}

	public void hideKeyboardSearch() {
		hideKeyboard(this);
		if (searchView != null) {
			searchView.clearFocus();
		}
	}

	public void openSearchView () {
		String querySaved = searchQuery;
		if (searchMenuItem != null) {
			searchMenuItem.expandActionView();
			if (searchView != null) {
				searchView.setQuery(querySaved, false);
			}
		}
	}

	public void clearSearchViewFocus() {
		if (searchView != null) {
			searchView.clearFocus();
		}
	}

	public void requestSearchViewFocus() {
		if (searchView == null || textSubmitted) {
			return;
		}

		searchView.setIconified(false);
	}

	public boolean checkPermission(String permission) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}

		try {
			return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
		} catch (IllegalArgumentException ex) {
			logWarning("IllegalArgument Exception is thrown");
			return false;
		}
	}

	public boolean isValidSearchQuery() {
		return searchQuery != null && !searchQuery.isEmpty();
	}

    public void openSearchFolder(MegaNode node) {
        switch (drawerItem) {
            case HOMEPAGE:
                // Redirect to Cloud drive.
                selectDrawerItemLollipop(DrawerItem.CLOUD_DRIVE);
            case CLOUD_DRIVE:
                setParentHandleBrowser(node.getHandle());
                refreshFragment(FragmentTag.CLOUD_DRIVE.getTag());
                break;
            case SHARED_ITEMS:
                if (viewPagerShares == null || sharesPageAdapter == null) break;

                if (getTabItemShares() == INCOMING_TAB) {
                    setParentHandleIncoming(node.getHandle());
                    increaseDeepBrowserTreeIncoming();
                } else if (getTabItemShares() == OUTGOING_TAB) {
                    setParentHandleOutgoing(node.getHandle());
                    increaseDeepBrowserTreeOutgoing();
                } else if (getTabItemShares() == LINKS_TAB) {
                    setParentHandleLinks(node.getHandle());
                    increaseDeepBrowserTreeLinks();
                }
                refreshSharesPageAdapter();

                break;
            case INBOX:
                setParentHandleInbox(node.getHandle());
                refreshFragment(FragmentTag.INBOX.getTag());
                break;
        }
    }

	public void closeSearchView () {
	    if (searchMenuItem != null && searchMenuItem.isActionViewExpanded()) {
	        searchMenuItem.collapseActionView();
        }
    }

	public void setTextSubmitted () {
	    if (searchView != null) {
	    	if (!isValidSearchQuery()) return;
	        searchView.setQuery(searchQuery, true);
        }
    }

	public boolean isSearchOpen() {
		return searchQuery != null && searchExpand;
	}

    public void setAccountFragmentPreUpgradeAccount (int accountFragment) {
		this.accountFragmentPreUpgradeAccount = accountFragment;
	}

    private void refreshAddPhoneNumberButton(){
        navigationDrawerAddPhoneContainer.setVisibility(View.GONE);
        if(maFLol != null){
            maFLol.updateAddPhoneNumberLabel();
        }
    }

    public void showAddPhoneNumberInMenu(){
	    if(megaApi == null){
	        return;
        }
        if(canVoluntaryVerifyPhoneNumber()) {
            if(megaApi.isAchievementsEnabled()) {
                String message = String.format(getString(R.string.sms_add_phone_number_dialog_msg_achievement_user), bonusStorageSMS);
                addPhoneNumberLabel.setText(message);
            } else {
                addPhoneNumberLabel.setText(R.string.sms_add_phone_number_dialog_msg_non_achievement_user);
            }
            navigationDrawerAddPhoneContainer.setVisibility(View.VISIBLE);
        } else {
            navigationDrawerAddPhoneContainer.setVisibility(View.GONE);
        }
    }

	public void deleteInviteContactHandle(){
		handleInviteContact = -1;
	}

    @Override
    public void onTrimMemory(int level){
        // Determine which lifecycle or system event was raised.
        //we will stop creating thumbnails while the phone is running low on memory to prevent OOM
		logDebug("Level: " + level);
        if(level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL){
			logWarning("Low memory");
			ThumbnailUtilsLollipop.isDeviceMemoryLow = true;
        }else{
			logDebug("Memory OK");
			ThumbnailUtilsLollipop.isDeviceMemoryLow = false;
        }
    }

	private void setSearchDrawerItem() {
		if (drawerItem == DrawerItem.SEARCH) return;

		searchDrawerItem = drawerItem;
		searchSharedTab = getTabItemShares();

		drawerItem = DrawerItem.SEARCH;
	}

    public DrawerItem getSearchDrawerItem(){
		return searchDrawerItem;
	}

	/**
	 * This method sets "Tap to return to call" banner when there is a call in progress
	 * and it is in Cloud Drive section, Recents section, Incoming section, Outgoing section or in the chats list.
	 */
	private void setCallWidget() {
		setCallBadge();

		if (drawerItem == DrawerItem.SETTINGS || drawerItem == DrawerItem.ACCOUNT ||
				drawerItem == DrawerItem.SEARCH || drawerItem == DrawerItem.TRANSFERS ||
				drawerItem == DrawerItem.NOTIFICATIONS || drawerItem == DrawerItem.HOMEPAGE || !isScreenInPortrait(this)) {
			hideCallWidget(this, callInProgressChrono, callInProgressLayout);
			return;
		}

		showCallLayout(this, callInProgressLayout, callInProgressChrono, callInProgressText);
		MegaChatCall callInProgress = getCallInProgress();
		if (callInProgress != null) {
			showHideMicroAndVideoIcons(callInProgress, microOffLayout, videoOnLayout);
		}
	}

    public void homepageToSearch() {
        hideItemsWhenSearchSelected();
        searchMenuItem.expandActionView();
    }

	public String getSearchQuery() {
		return searchQuery;
	}

	public int getSearchSharedTab() {
		return searchSharedTab;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
		this.searchView.setQuery(searchQuery, false);
	}

	public long getParentHandleIncoming() {
		return parentHandleIncoming;
	}

	public long getParentHandleOutgoing() {
		return parentHandleOutgoing;
	}

	public long getParentHandleRubbish() {
		return parentHandleRubbish;
	}

	public long getParentHandleSearch() {
		return parentHandleSearch;
	}

	public long getParentHandleLinks() {
		return parentHandleLinks;
	}

	public void setParentHandleLinks(long parentHandleLinks) {
		this.parentHandleLinks = parentHandleLinks;
	}

	private SearchFragmentLollipop getSearchFragment() {
		return sFLol = (SearchFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SEARCH.getTag());
	}

	/**
	 * Removes a completed transfer from Completed tab in Transfers section.
	 *
	 * @param transfer	the completed transfer to remove
	 */
	public void removeCompletedTransfer(AndroidCompletedTransfer transfer) {
		dbH.deleteTransfer(transfer.getId());

		if (isTransfersCompletedAdded()) {
			completedTFLol.transferRemoved(transfer);
		}
	}

	/**
	 * Retries a transfer that finished wrongly.
	 *
	 * @param transfer	the transfer to retry
	 */
	public void retryTransfer(AndroidCompletedTransfer transfer) {
		if (transfer.getType() == MegaTransfer.TYPE_DOWNLOAD) {
			MegaNode node = megaApi.getNodeByHandle(Long.parseLong(transfer.getNodeHandle()));
			if (node == null) {
				logWarning("Node is null, not able to retry");
				return;
			}

			if (transfer.getIsOfflineFile()) {
				File offlineFile = new File(transfer.getOriginalPath());
				saveOffline(offlineFile.getParentFile(), node, this, ManagerActivityLollipop.this);
			} else {
				nodeSaver.saveNode(node, transfer.getPath());
			}
		} else if (transfer.getType() == MegaTransfer.TYPE_UPLOAD) {
			String originalPath = transfer.getOriginalPath();
			int lastSeparator = originalPath.lastIndexOf(SEPARATOR);
			String parentFolder = "";
			if (lastSeparator != -1) {
				parentFolder = originalPath.substring(0, lastSeparator + 1);
			}

			ArrayList<String> paths = new ArrayList<>();
			paths.add(originalPath);

			UploadServiceTask uploadServiceTask = new UploadServiceTask(parentFolder, paths, transfer.getParentHandle());
			uploadServiceTask.start();
		}

		removeCompletedTransfer(transfer);
	}

	/**
	 * Opens a location of a transfer.
	 *
	 * @param transfer	the transfer to open its location
	 */
	public void openTransferLocation(AndroidCompletedTransfer transfer) {
		if (transfer.getType() == MegaTransfer.TYPE_DOWNLOAD) {
			if (transfer.getIsOfflineFile()) {
				selectDrawerItemLollipop(drawerItem = DrawerItem.HOMEPAGE);
				openFullscreenOfflineFragment(
						removeInitialOfflinePath(transfer.getPath()) + SEPARATOR);
			} else {
				Intent intent = new Intent(this, FileStorageActivityLollipop.class);
				intent.setAction(FileStorageActivityLollipop.Mode.BROWSE_FILES.getAction());
				intent.putExtra(FileStorageActivityLollipop.EXTRA_PATH, transfer.getPath());
				intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, false);
				startActivity(intent);
			}
		} else if (transfer.getType() == MegaTransfer.TYPE_UPLOAD) {
			MegaNode node = megaApi.getNodeByHandle(Long.parseLong(transfer.getNodeHandle()));
			if (node == null) {
				showSnackbar(SNACKBAR_TYPE, getString(!isOnline(this) ? R.string.error_server_connection_problem
						: R.string.warning_folder_not_exists), MEGACHAT_INVALID_HANDLE);
				return;
			}

			viewNodeInFolder(node);
		}
	}

	/**
	 * Opens the location of a node.
	 *
	 * @param node	the node to open its location
	 */
	public void viewNodeInFolder(MegaNode node) {
		MegaNode parentNode = getRootParentNode(node);
		if (parentNode.getHandle() == megaApi.getRootNode().getHandle()) {
			parentHandleBrowser = node.getParentHandle();
			refreshFragment(FragmentTag.CLOUD_DRIVE.getTag());
			selectDrawerItemLollipop(DrawerItem.CLOUD_DRIVE);
		} else if (parentNode.getHandle() == megaApi.getRubbishNode().getHandle()) {
			parentHandleRubbish = node.getParentHandle();
			refreshFragment(FragmentTag.RUBBISH_BIN.getTag());
			selectDrawerItemLollipop(DrawerItem.RUBBISH_BIN);
		} else if (parentNode.isInShare()) {
			parentHandleIncoming = node.getParentHandle();
			deepBrowserTreeIncoming = calculateDeepBrowserTreeIncoming(megaApi.getParentNode(node),
					this);
			refreshFragment(FragmentTag.INCOMING_SHARES.getTag());
			indexShares = INCOMING_TAB;
			if (viewPagerShares != null) {
				viewPagerShares.setCurrentItem(indexShares);
				if (sharesPageAdapter != null) {
					sharesPageAdapter.notifyDataSetChanged();
				}
			}
			selectDrawerItemLollipop(DrawerItem.SHARED_ITEMS);
		}
	}

	public int getStorageState() {
		return storageState;
	}

    /**
     * Shows a "transfer over quota" warning.
     */
	public void showTransfersTransferOverQuotaWarning() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		int messageResource = R.string.warning_transfer_over_quota;

		transferOverQuotaWarning = builder.setTitle(R.string.label_transfer_over_quota)
				.setMessage(getString(messageResource, getHumanizedTime(megaApi.getBandwidthOverquotaDelay())))
				.setPositiveButton(R.string.my_account_upgrade_pro, (dialog, which) -> { navigateToUpgradeAccount();
				})
				.setNegativeButton(R.string.general_dismiss, null)
				.setCancelable(false)
				.setOnDismissListener(dialog -> isTransferOverQuotaWarningShown = false)
				.create();

		transferOverQuotaWarning.setCanceledOnTouchOutside(false);
		TimeUtils.createAndShowCountDownTimer(messageResource, transferOverQuotaWarning);
		transferOverQuotaWarning.show();
		isTransferOverQuotaWarningShown = true;
	}

    /**
     * Updates the position of the transfers widget.
     *
     * @param bNVHidden  true if the bottom navigation view is hidden, false otherwise
     */
    public void updateTransfersWidgetPosition(boolean bNVHidden) {
        RelativeLayout transfersWidgetLayout = findViewById(R.id.transfers_widget_layout);
        if (transfersWidgetLayout == null) return;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) transfersWidgetLayout.getLayoutParams();
        params.gravity = Gravity.END;

        if (!bNVHidden && drawerItem == DrawerItem.HOMEPAGE && mHomepageScreen == HomepageScreen.HOMEPAGE) {
            params.bottomMargin = Util.dp2px(TRANSFER_WIDGET_MARGIN_BOTTOM, outMetrics);
        } else {
            params.bottomMargin = 0;
        }

        transfersWidgetLayout.setLayoutParams(params);
    }

    /**
     * Updates values of TransfersManagement object after the activity comes from background.
     */
	private void checkTransferOverQuotaOnResume() {
		TransfersManagement transfersManagement = MegaApplication.getTransfersManagement();
		transfersManagement.setIsOnTransfersSection(drawerItem == DrawerItem.TRANSFERS);
		if (transfersManagement.isTransferOverQuotaNotificationShown()) {
			transfersManagement.setTransferOverQuotaBannerShown(true);
			transfersManagement.setTransferOverQuotaNotificationShown(false);
		}
	}

    /**
     * Gets the failed and cancelled transfers.
     *
     * @return  A list with the failed and cancelled transfers.
     */
	public ArrayList<AndroidCompletedTransfer> getFailedAndCancelledTransfers() {
		return dbH.getFailedOrCancelledTransfers();
	}

    /**
     * Retries all the failed and cancelled transfers.
     */
	private void retryAllTransfers() {
		ArrayList<AndroidCompletedTransfer> failedOrCancelledTransfers = getFailedAndCancelledTransfers();
		for (AndroidCompletedTransfer transfer : failedOrCancelledTransfers) {
			retryTransfer(transfer);
		}
	}

    /**
     * Checks if there are failed or cancelled transfers.
     *
     * @return True if there are failed or cancelled transfers, false otherwise.
     */
	private boolean thereAreFailedOrCancelledTransfers() {
		ArrayList<AndroidCompletedTransfer> failedOrCancelledTransfers = getFailedAndCancelledTransfers();
		return failedOrCancelledTransfers.size() > 0;
	}

	private RubbishBinFragmentLollipop getRubbishBinFragment() {
		return rubbishBinFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RUBBISH_BIN.getTag());
	}

	private CameraUploadsFragment getCameraUploadFragment() {
		return cuFragment = (CameraUploadsFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.CAMERA_UPLOADS.getTag());
	}

	private InboxFragmentLollipop getInboxFragment() {
		return iFLol = (InboxFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.INBOX.getTag());
	}

	private SentRequestsFragmentLollipop getSentRequestFragment() {
		return sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.SENT_REQUESTS.getTag());
	}

	private ReceivedRequestsFragmentLollipop getReceivedRequestFragment() {
		return rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECEIVED_REQUESTS.getTag());
	}

	private RecentChatsFragmentLollipop getChatsFragment() {
		return rChatFL = (RecentChatsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(FragmentTag.RECENT_CHAT.getTag());
	}

	@Override
	public void finishRenameActionWithSuccess(@NonNull String newName) {
		switch (drawerItem) {
			case CLOUD_DRIVE:
				refreshCloudDrive();
				break;
			case RUBBISH_BIN:
				refreshRubbishBin();
				break;
			case INBOX:
				refreshInboxList();
				break;
			case SHARED_ITEMS:
				onNodesSharedUpdate();
				break;
			case HOMEPAGE:
				refreshOfflineNodes();
		}
	}

	@Override
	public void actionConfirmed() {
		//No update needed
	}
}
