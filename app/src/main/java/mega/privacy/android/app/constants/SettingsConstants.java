package mega.privacy.android.app.constants;

public class SettingsConstants {
    /* General settings */
    public static final String KEY_APPEARNCE_COLOR_THEME = "settings_appearance_color_theme";
    public static final String KEY_FEATURES = "settings_features";
    public static final String KEY_FEATURES_CAMERA_UPLOAD = "settings_features_camera_upload";
    public static final String KEY_FEATURES_CHAT = "settings_features_chat";
    public static final String CATEGORY_STORAGE = "settings_storage";
    public static final String KEY_STORAGE_DOWNLOAD = "settings_nested_download_location";
    public static final String KEY_STORAGE_FILE_MANAGEMENT = "settings_storage_file_management";
    public static final String CATEGORY_SECURITY = "settings_security";
    public static final String KEY_RECOVERY_KEY = "settings_recovery_key";
    public static final String KEY_PASSCODE_LOCK = "settings_passcode_lock";
    public static final String KEY_CHANGE_PASSWORD = "settings_change_password";
    public static final String KEY_2FA = "settings_2fa_activated";
    public static final String KEY_QR_CODE_AUTO_ACCEPT = "settings_qrcode_autoaccept";
    public static final String KEY_SECURITY_ADVANCED = "settings_security_advanced";
    public static final String KEY_HELP_CENTRE = "settings_help_centre";
    public static final String KEY_HELP_SEND_FEEDBACK = "settings_help_send_feedback";
    public static final String CATEGORY_ABOUT = "settings_about";
    public static final String KEY_ABOUT_PRIVACY_POLICY = "settings_about_privacy_policy";
    public static final String KEY_ABOUT_COOKIE_POLICY = "settings_about_cookie_policy";
    public static final String KEY_COOKIE_SETTINGS = "settings_cookie";
    public static final String KEY_ABOUT_TOS = "settings_about_terms_of_service";
    public static final String KEY_ABOUT_CODE_LINK = "settings_about_code_link";
    public static final String KEY_ABOUT_SDK_VERSION = "settings_about_sdk_version";
    public static final String KEY_ABOUT_KARERE_VERSION = "settings_about_karere_version";
    public static final String KEY_ABOUT_APP_VERSION = "settings_about_app_version";
    public static final String KEY_CANCEL_ACCOUNT = "settings_about_cancel_account";
    public static final String KEY_AUDIO_BACKGROUND_PLAY_ENABLED = "settings_audio_background_play_enabled";
    public static final String KEY_AUDIO_SHUFFLE_ENABLED = "settings_audio_shuffle_enabled";
    public static final String KEY_AUDIO_REPEAT_MODE = "settings_audio_repeat_mode";

    /* CU settings */
    public static final String KEY_CAMERA_UPLOAD_ON_OFF = "settings_camera_upload_on_off";
    public static final String KEY_CAMERA_UPLOAD_HOW_TO = "settings_camera_upload_how_to_upload";
    public static final String KEY_CAMERA_UPLOAD_WHAT_TO = "settings_camera_upload_what_to_upload";
    public static final String KEY_CAMERA_UPLOAD_INCLUDE_GPS = "settings_camera_upload_include_gps";
    public static final String KEY_CAMERA_UPLOAD_VIDEO_QUALITY = "settings_video_upload_quality";
    public static final String KEY_CAMERA_UPLOAD_CHARGING = "settings_camera_upload_charging";
    public static final String KEY_CAMERA_UPLOAD_VIDEO_QUEUE_SIZE = "video_compression_queue_size";
    public static final String KEY_KEEP_FILE_NAMES = "settings_keep_file_names";
    public static final String KEY_CAMERA_UPLOAD_CAMERA_FOLDER = "settings_local_camera_upload_folder";
    public static final String KEY_CAMERA_UPLOAD_MEGA_FOLDER = "settings_mega_camera_folder";
    public static final String KEY_SECONDARY_MEDIA_FOLDER_ON = "settings_secondary_media_folder_on";
    public static final String KEY_LOCAL_SECONDARY_MEDIA_FOLDER = "settings_local_secondary_media_folder";
    public static final String KEY_MEGA_SECONDARY_MEDIA_FOLDER = "settings_mega_secondary_media_folder";
    public static final int VIDEO_QUALITY_ORIGINAL = 0;
    public static final int VIDEO_QUALITY_MEDIUM = 1;
    public static final int DEFAULT_CONVENTION_QUEUE_SIZE = 200;
    public static final int COMPRESSION_QUEUE_SIZE_MIN = 100;
    public static final int COMPRESSION_QUEUE_SIZE_MAX = 1000;
    public static final int REQUEST_CAMERA_FOLDER = 2000;
    public static final int REQUEST_MEGA_CAMERA_FOLDER = 3000;
    public static final int REQUEST_LOCAL_SECONDARY_MEDIA_FOLDER = 4000;
    public static final int REQUEST_MEGA_SECONDARY_MEDIA_FOLDER = 5000;
    public static final String KEY_SET_QUEUE_DIALOG = "KEY_SET_QUEUE_DIALOG";
    public static final String KEY_SET_QUEUE_SIZE = "KEY_SET_QUEUE_SIZE";
    public final static String SELECTED_MEGA_FOLDER = "SELECT_MEGA_FOLDER";
    public static final int CAMERA_UPLOAD_WIFI_OR_DATA_PLAN = 1001;
    public static final int CAMERA_UPLOAD_WIFI = 1002;
    public static final int CAMERA_UPLOAD_FILE_UPLOAD_PHOTOS = 1001;
    public static final int CAMERA_UPLOAD_FILE_UPLOAD_VIDEOS = 1002;
    public static final int CAMERA_UPLOAD_FILE_UPLOAD_PHOTOS_AND_VIDEOS = 1003;
    public static final String INVALID_PATH = "";

    /* Chat settings */
    public static final String KEY_CHAT_NOTIFICATIONS_CHAT = "settings_chat_notification_chat";
    public static final String KEY_CHAT_STATUS = "settings_chat_list_status";
    public static final String KEY_CHAT_AUTOAWAY_SWITCH = "settings_chat_autoaway_switch";
    public static final String KEY_CHAT_AUTOAWAY_PREFERENCE = "settings_chat_autoaway_preference";
    public static final String KEY_CHAT_PERSISTENCE = "settings_chat_persistence";
    public static final String KEY_CHAT_LAST_GREEN = "settings_chat_last_green";
    public static final String KEY_CHAT_SEND_ORIGINALS = "settings_chat_send_originals";
    public static final String KEY_CHAT_RICH_LINK = "settings_chat_rich_links_enable";

    /* Chat notifications settings */
    public static final String KEY_CHAT_NOTIFICATIONS = "settings_chat_notifications";
    public static final String KEY_CHAT_SOUND = "settings_chat_sound";
    public static final String KEY_CHAT_VIBRATE = "settings_chat_vibrate";
    public static final String KEY_CHAT_DND = "settings_chat_dnd";

    /* Download settings */
    public static final String KEY_STORAGE_DOWNLOAD_LOCATION = "settings_storage_download_location";
    public static final String KEY_STORAGE_ASK_ME_ALWAYS = "settings_storage_ask_me_always";

    /* File management settings */
    public static final String KEY_OFFLINE = "settings_file_management_offline";
    public static final String KEY_CACHE = "settings_advanced_features_cache";
    public static final String KEY_RUBBISH = "settings_file_management_rubbish";
    public static final String KEY_ENABLE_RB_SCHEDULER = "settings_rb_scheduler_switch";
    public static final String KEY_DAYS_RB_SCHEDULER = "settings_days_rb_scheduler";
    public static final String KEY_ENABLE_VERSIONS = "settings_file_versioning_switch";
    public static final String KEY_FILE_VERSIONS = "settings_file_management_file_version";
    public static final String KEY_CLEAR_VERSIONS = "settings_file_management_clear_version";
    public static final String KEY_AUTO_PLAY_SWITCH = "auto_play_switch";

    /* PassCode Lock settings */
    public static final String KEY_PASSCODE_ENABLE = "settings_passcode_enable";
    public static final String KEY_RESET_PASSCODE = "settings_change_passcode";
    public static final String KEY_REQUIRE_PASSCODE = "settings_require_passcode";

    /* Advance settings  */
    public static final String KEY_HTTPS_ONLY = "settings_use_https_only";

    /* Cookie settings  */
    public static final String KEY_COOKIE_ACCEPT = "settings_cookie_accept";
    public static final String KEY_COOKIE_ESSENTIAL = "settings_cookie_essential";
    public static final String KEY_COOKIE_PREFERENCE = "settings_cookie_preference";
    public static final String KEY_COOKIE_ANALYTICS = "settings_cookie_performance_analytics";
    public static final String KEY_COOKIE_ADVERTISING = "settings_cookie_advertising";
    public static final String KEY_COOKIE_THIRD_PARTY = "settings_cookie_third_party";
    public static final String KEY_COOKIE_POLICIES = "setting_cookie_policies";
}
