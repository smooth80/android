package mega.privacy.android.app.lollipop;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.activities.WebViewActivity;
import mega.privacy.android.app.activities.PasscodeActivity;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.utils.ColorUtils;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.ConstantsUrl.RECOVERY_URL;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.Util.*;


@SuppressLint("NewApi")
public class ChangePasswordActivityLollipop extends PasscodeActivity implements OnClickListener, MegaRequestListenerInterface {

	public static final String KEY_IS_LOGOUT = "logout";

	private ProgressDialog progress;
	
	float scaleH, scaleW;
	float density;
	DisplayMetrics outMetrics;
	Display display;
	
	private MegaApiAndroid megaApi;

	boolean changePassword = true;
	
	private TextInputLayout newPassword1Layout;
	private AppCompatEditText newPassword1;
	private ImageView newPassword1Error;
	private TextInputLayout newPassword2Layout;
	private AppCompatEditText newPassword2;
	private ImageView newPassword2Error;
	private Button changePasswordButton;
    private RelativeLayout fragmentContainer;
	private TextView title;
	private String linkToReset;
	private String mk;

	// TOP for 'terms of password'
    private CheckBox chkTOP;

	private ActionBar aB;
	Toolbar tB;

	private LinearLayout containerPasswdElements;
	private ImageView firstShape;
	private ImageView secondShape;
	private ImageView tirdShape;
	private ImageView fourthShape;
	private ImageView fifthShape;
	private TextView passwdType;
	private TextView passwdAdvice;
	private boolean passwdValid;

	private InputMethodManager imm;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
        fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container_change_pass);
		megaApi = ((MegaApplication)getApplication()).getMegaApi();

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;
		
	    scaleW = getScaleW(outMetrics, density);
	    scaleH = getScaleH(outMetrics, density);

		title = (TextView) findViewById(R.id.title_change_pass);

		passwdValid = false;

		containerPasswdElements = (LinearLayout) findViewById(R.id.container_passwd_elements);
		containerPasswdElements.setVisibility(View.GONE);
		firstShape = (ImageView) findViewById(R.id.shape_passwd_first);
		secondShape = (ImageView) findViewById(R.id.shape_passwd_second);
		tirdShape = (ImageView) findViewById(R.id.shape_passwd_third);
		fourthShape = (ImageView) findViewById(R.id.shape_passwd_fourth);
		fifthShape = (ImageView) findViewById(R.id.shape_passwd_fifth);
		passwdType = (TextView) findViewById(R.id.password_type);
		passwdAdvice = (TextView) findViewById(R.id.password_advice_text);

		newPassword1Layout = findViewById(R.id.change_password_newPassword1_layout);
		newPassword1Layout.setEndIconVisible(false);
		newPassword1 = findViewById(R.id.change_password_newPassword1);
		newPassword1.setOnFocusChangeListener((v1, hasFocus) ->
				newPassword1Layout.setEndIconVisible(hasFocus));
		newPassword1Error = findViewById(R.id.change_password_newPassword1_error_icon);
		newPassword1Error.setVisibility(View.GONE);

		newPassword1.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				logDebug("Text changed: " + s.toString() + "_ " + start + "__" + before + "__" + count);
				if (s != null){
					if (s.length() > 0) {
						String temp = s.toString();
						containerPasswdElements.setVisibility(View.VISIBLE);

						checkPasswordStrength(temp.trim());
					}
					else{
						passwdValid = false;
						containerPasswdElements.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editable.toString().isEmpty()) {
					quitError(newPassword1);
				}
			}
		});


		newPassword2Layout = findViewById(R.id.change_password_newPassword2_layout);
		newPassword2Layout.setEndIconVisible(false);
		newPassword2 = findViewById(R.id.change_password_newPassword2);
		newPassword2.setOnFocusChangeListener((v1, hasFocus) ->
				newPassword2Layout.setEndIconVisible(hasFocus));
		newPassword2Error = findViewById(R.id.change_password_newPassword2_error_icon);
		newPassword2Error.setVisibility(View.GONE);

		newPassword2.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				quitError(newPassword2);
			}
		});

		changePasswordButton = (Button) findViewById(R.id.action_change_password);
		changePasswordButton.setOnClickListener(this);

        TextView top = findViewById(R.id.top);

        String textToShowTOP = getString(R.string.top);
        try {
            textToShowTOP = textToShowTOP.replace("[B]", "<font color=\'"
					+ ColorUtils.getThemeColorHexString(this, R.attr.colorSecondary)
					+ "\'>")
                    .replace("[/B]", "</font>")
                    .replace("[A]", "<u>")
                    .replace("[/A]", "</u>");
        } catch (Exception e) {
            logError("Exception formatting string", e);
        }

        Spanned resultTOP;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            resultTOP = Html.fromHtml(textToShowTOP,Html.FROM_HTML_MODE_LEGACY);
        } else {
            resultTOP = Html.fromHtml(textToShowTOP);
        }

        top.setText(resultTOP);

        top.setOnClickListener(this);

        chkTOP = findViewById(R.id.chk_top);
        chkTOP.setOnClickListener(this);
		
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);

		tB  =(Toolbar) findViewById(R.id.toolbar);
		hideAB();

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		Intent intentReceived = getIntent();
		if (intentReceived != null) {
			logDebug("There is an intent!");
			if(intentReceived.getAction()!=null){
				if (getIntent().getAction().equals(ACTION_RESET_PASS_FROM_LINK)) {
					logDebug("ACTION_RESET_PASS_FROM_LINK");
					changePassword=false;
					linkToReset = getIntent().getDataString();
					if (linkToReset == null) {
						logWarning("link is NULL - close activity");
						finish();
					}
					mk = getIntent().getStringExtra("MK");
					if(mk==null){
						logWarning("MK is NULL - close activity");
						showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
					}

					title.setText(getString(R.string.title_enter_new_password));
				}
				if (getIntent().getAction().equals(ACTION_RESET_PASS_FROM_PARK_ACCOUNT)) {
					changePassword=false;
					logDebug("ACTION_RESET_PASS_FROM_PARK_ACCOUNT");
					linkToReset = getIntent().getDataString();
					if (linkToReset == null) {
						logWarning("link is NULL - close activity");
						showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
					}
					mk = null;

					title.setText(getString(R.string.title_enter_new_password));
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		logDebug("onBackPressed");

		if (psaWebBrowser.consumeBack()) return;
		if (getIntent() != null && getIntent().getBooleanExtra(KEY_IS_LOGOUT, false)) {
			Intent intent = new Intent(this, TestPasswordActivity.class);
			intent.putExtra(KEY_IS_LOGOUT, true);
			startActivity(intent);
		}
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
		    case android.R.id.home:{
					finish();
		    	return true;
		    }
		}	    
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		logDebug("onClick");
		switch(v.getId()){
			case R.id.action_change_password: {
				if (changePassword) {
					logDebug("Ok proceed to change");
					onChangePasswordClick();
				} else {
					logDebug("Reset pass on click");
					if (linkToReset == null) {
						logWarning("link is NULL");
						showAlert(this, getString(R.string.general_text_error), getString(R.string.general_error_word));
					} else {
						if (mk == null) {
							logDebug("Proceed to park account");
							onResetPasswordClick(false);
						} else {
							logDebug("Ok proceed to reset");
							onResetPasswordClick(true);
						}
					}
				}
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
            case R.id.top:
                logDebug("Show top");
                try {
                    Intent openTermsIntent = new Intent(this, WebViewActivity.class);
                    openTermsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    openTermsIntent.setData(Uri.parse(URL_E2EE));
                    startActivity(openTermsIntent);
                }
                catch (Exception e){
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setData(Uri.parse(URL_E2EE));
                    startActivity(viewIntent);
                }

                break;
//			case R.id.cancel_change_password:{
//				changePasswordActivity.finish();
//				break;
//			}
		}
	}

	public void checkPasswordStrength(String s) {
		newPassword1Layout.setErrorEnabled(false);

		if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_VERYWEAK || s.length() < 4){
			firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_very_weak));
			secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));

			passwdType.setText(getString(R.string.pass_very_weak));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.red_600_red_300));

			passwdAdvice.setText(getString(R.string.passwd_weak));

			passwdValid = false;

			newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_VeryWeak);
			newPassword1Layout.setErrorTextAppearance(R.style.TextAppearance_InputHint_VeryWeak);
		}
		else if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_WEAK){
			firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_weak));
			secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_weak));
			tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));

			passwdType.setText(getString(R.string.pass_weak));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.yellow_600_yellow_300));

			passwdAdvice.setText(getString(R.string.passwd_weak));

			passwdValid = true;

			newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_Weak);
			newPassword1Layout.setErrorTextAppearance(R.style.TextAppearance_InputHint_Weak);
		}
		else if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_MEDIUM){
			firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
			secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
			tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
			fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));

			passwdType.setText(getString(R.string.pass_medium));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.green_500_green_400));

			passwdAdvice.setText(getString(R.string.passwd_medium));

			passwdValid = true;

			newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_Medium);
			newPassword1Layout.setErrorTextAppearance(R.style.TextAppearance_InputHint_Medium);
		}
		else if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_GOOD){
			firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
			secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
			tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
			fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
			fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));

			passwdType.setText(getString(R.string.pass_good));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.lime_green_500_200));

			passwdAdvice.setText(getString(R.string.passwd_good));

			passwdValid = true;

			newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_Good);
			newPassword1Layout.setErrorTextAppearance(R.style.TextAppearance_InputHint_Good);
		}
		else {
			firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
			secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
			tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
			fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
			fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));

			passwdType.setText(getString(R.string.pass_strong));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.dark_blue_500_200));

			passwdAdvice.setText(getString(R.string.passwd_strong));

			passwdValid = true;

			newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_Strong);
			newPassword1Layout.setErrorTextAppearance(R.style.TextAppearance_InputHint_Strong);
		}

		newPassword1Error.setVisibility(View.GONE);
		newPassword1Layout.setError(" ");
		newPassword1Layout.setErrorEnabled(true);
	}

	public void onResetPasswordClick(boolean hasMk){
		logDebug("hasMk: " + hasMk);

		if(!isOnline(this))
		{
			showSnackbar(getString(R.string.error_server_connection_problem));
			return;
		}

		if (!validateForm(false)) {
			return;
		}

		imm.hideSoftInputFromWindow(newPassword1.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(newPassword2.getWindowToken(), 0);

		final String newPass1 = newPassword1.getText().toString();

		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.show();

		if(hasMk){
			logDebug("reset with mk");
			megaApi.confirmResetPassword(linkToReset, newPass1, mk, this);
		}
		else{
			megaApi.confirmResetPassword(linkToReset, newPass1, null, this);
		}
	}
	
	public void onChangePasswordClick(){
		logDebug("onChangePasswordClick");
		if(!isOnline(this))
		{
			showSnackbar(getString(R.string.error_server_connection_problem));
			return;
		}

		if (!validateForm(true)) {
			return;
		}

        imm.hideSoftInputFromWindow(newPassword1.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(newPassword2.getWindowToken(), 0);
		
		megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);
	}
	
	/*
	 * Validate old password and new passwords 
	 */
	private boolean validateForm(boolean withOldPass) {
		if(withOldPass){
			String newPassword1Error = getNewPassword1Error();
			String newPassword2Error = getNewPassword2Error();

			setError(newPassword1, newPassword1Error);
			setError(newPassword2, newPassword2Error);

			if(newPassword1Error != null) {
				newPassword1.requestFocus();
				return false;
			}
			else if(newPassword2Error != null) {
				newPassword2.requestFocus();
				return false;
			}
		}
		else{
			String newPassword1Error = getNewPassword1Error();
			String newPassword2Error = getNewPassword2Error();

			setError(newPassword1, newPassword1Error);
			setError(newPassword2, newPassword2Error);

			if(newPassword1Error != null) {
				newPassword1.requestFocus();
				return false;
			}
			else if(newPassword2Error != null) {
				newPassword2.requestFocus();
				return false;
			}
		}
		if(!chkTOP.isChecked()) {
            showSnackbar(getString(R.string.create_account_no_top));
            return false;
        }
		return true;
	}

	/*
	 * Validate new password1
	 */
	private String getNewPassword1Error() {
		String value = newPassword1.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		else if (!passwdValid){
			containerPasswdElements.setVisibility(View.GONE);
			return getString(R.string.error_password);
		}
		return null;
	}
	
	/*
	 * Validate new password2
	 */
	private String getNewPassword2Error() {
		String value = newPassword2.getText().toString();
		String confirm = newPassword1.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		else if (!value.equals(confirm)) {
			return getString(R.string.error_passwords_dont_match);
		}
		return null;
	}

    private void changePassword(String newPassword) {
        logDebug("changePassword");
        megaApi.changePassword(null, newPassword, this);
        progress.setMessage(getString(R.string.my_account_changing_password));
        progress.show();
    }
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		logDebug("onRequestStart: " + request.getName());
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		logDebug("onRequestFinish");

        if (request.getType() == MegaRequest.TYPE_CHANGE_PW) {
            logDebug("TYPE_CHANGE_PW");

            if (e.getErrorCode() != MegaError.API_OK) {
                logWarning("e.getErrorCode = " + e.getErrorCode() + "__ e.getErrorString = " + e.getErrorString());
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                    logWarning("Exception dismissing progress dialog", ex);
                }

                showSnackbar(getString(R.string.general_text_error));
            } else {
                logDebug("Pass changed OK");
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                    logWarning("Exception dismissing progress dialog", ex);
                }

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if (getIntent() != null && getIntent().getBooleanExtra("logout", false)) {
                    AccountController ac = new AccountController(this);
                    ac.logout(this, megaApi);
                } else {
                    //Intent to MyAccount
                    Intent resetPassIntent = new Intent(this, ManagerActivityLollipop.class);
                    resetPassIntent.setAction(ACTION_PASS_CHANGED);
                    resetPassIntent.putExtra(RESULT, e.getErrorCode());
                    startActivity(resetPassIntent);
                    finish();
                }
            }
        } else if (request.getType() == MegaRequest.TYPE_CONFIRM_RECOVERY_LINK) {
			logDebug("TYPE_CONFIRM_RECOVERY_LINK");

			try {
				progress.dismiss();
			} catch (Exception ex) {
				logWarning("Exception dismissing progress dialog", ex);
			}

			if (e.getErrorCode() != MegaError.API_OK) {
				logWarning("e.getErrorCode = " + e.getErrorCode() + "__ e.getErrorString = " + e.getErrorString());
			} else {
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}

			Intent resetPassIntent;

			if (megaApi.getRootNode() == null) {
				logDebug("Not logged in");

				//Intent to Login
				resetPassIntent = new Intent(this, LoginActivityLollipop.class);
				resetPassIntent.putExtra(VISIBLE_FRAGMENT, LOGIN_FRAGMENT);
			} else {
				logDebug("Logged IN");

				resetPassIntent = new Intent(this, ManagerActivityLollipop.class);
			}

			resetPassIntent.setAction(ACTION_PASS_CHANGED);
			resetPassIntent.putExtra(RESULT, e.getErrorCode());
			startActivity(resetPassIntent);
			finish();
        } else if (request.getType() == MegaRequest.TYPE_MULTI_FACTOR_AUTH_CHECK) {
            if (e.getErrorCode() == MegaError.API_OK) {
                if (request.getFlag()) {
                    Intent intent = new Intent(this, VerifyTwoFactorActivity.class);
                    intent.putExtra(VerifyTwoFactorActivity.KEY_VERIFY_TYPE, CHANGE_PASSWORD_2FA);
                    intent.putExtra(VerifyTwoFactorActivity.KEY_NEW_PASSWORD, newPassword1.getText().toString());
                    intent.putExtra(KEY_IS_LOGOUT, getIntent() != null && getIntent().getBooleanExtra(KEY_IS_LOGOUT, false));

                    startActivity(intent);
                } else {
                    changePassword(newPassword1.getText().toString());
                }
            }
        }
	}

	private void setError(final EditText editText, String error){
		logDebug("setError");
		if(error == null || error.equals("")){
			return;
		}
		switch (editText.getId()){

			case R.id.change_password_newPassword1:{
				newPassword1Layout.setError(error);
				newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_Error);
				newPassword1Layout.setErrorTextAppearance(R.style.TextAppearance_InputHint_Error);
				newPassword1Error.setVisibility(View.VISIBLE);
				break;
			}
			case R.id.change_password_newPassword2:{
				newPassword2Layout.setError(error);
				newPassword2Layout.setHintTextAppearance(R.style.TextAppearance_InputHint_Error);
				newPassword2Error.setVisibility(View.VISIBLE);
				break;
			}
		}
	}

	private void quitError(EditText editText){
		switch (editText.getId()){
			case R.id.change_password_newPassword1:{
				newPassword1Layout.setError(null);
				newPassword1Layout.setHintTextAppearance(R.style.TextAppearance_Design_Hint);
				newPassword1Error.setVisibility(View.GONE);
				break;
			}
			case R.id.change_password_newPassword2:{
				newPassword2Layout.setError(null);
				newPassword2Layout.setHintTextAppearance(R.style.TextAppearance_Design_Hint);
				newPassword2Error.setVisibility(View.GONE);
				break;
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		logWarning("onRequestTemporaryError: " + request.getName());
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDestroy(){
		if(megaApi != null)
		{	
			megaApi.removeRequestListener(this);
		}
		
		super.onDestroy();
	}

	public void showSnackbar(String s){
		showSnackbar(fragmentContainer, s);
	}

	void hideAB(){
		if (aB != null){
			aB.hide();
		}
	}
}
