package mega.privacy.android.app.activities.settingsActivities

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.inputmethod.EditorInfo.*
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.BaseActivity
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.app.R
import mega.privacy.android.app.components.ListenScrollChangesHelper
import mega.privacy.android.app.databinding.ActivityPasscodeBinding
import mega.privacy.android.app.lollipop.controllers.AccountController
import mega.privacy.android.app.modalbottomsheet.ModalBottomSheetUtil.isBottomSheetDialogShown
import mega.privacy.android.app.modalbottomsheet.PasscodeOptionsBottomSheetDialogFragment
import mega.privacy.android.app.utils.Constants.*
import mega.privacy.android.app.utils.PasscodeUtil
import mega.privacy.android.app.utils.StringResourcesUtils
import mega.privacy.android.app.utils.TextUtil.isTextEmpty
import mega.privacy.android.app.utils.Util.dp2px
import mega.privacy.android.app.utils.Util.hideKeyboardView
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PasscodeLockActivity : BaseActivity() {

    companion object {
        const val ACTION_SET_PASSCODE_LOCK = "ACTION_SET"
        const val ACTION_RESET_PASSCODE_LOCK = "ACTION_RESET"
        private const val MAX_ATTEMPTS = 10
        private const val MIN_ATTEMPTS_TO_SHOW_WARNING = 5
        private const val UNLOCK_MODE = 0
        private const val SET_MODE = 1
        private const val RESET_MODE = 2
        private const val SECOND_ROUND = "SECOND_ROUND"
        private const val SB_FIRST = "SB_FIRST"
        private const val ATTEMPTS = "ATTEMPTS"
        private const val PASSCODE_TYPE = "PASSCODE_TYPE"
    }

    private var attempts = 0
    private var mode = UNLOCK_MODE
    private var setOrUnlockMode = true

    @Inject
    lateinit var passcodeUtil: PasscodeUtil
    private lateinit var binding: ActivityPasscodeBinding
    private var passcodeType = PIN_4

    private var secondRound = false
    private val sbFirst = StringBuilder()
    private val sbSecond = StringBuilder()

    private var passcodeOptionsBottomSheetDialogFragment: PasscodeOptionsBottomSheetDialogFragment? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mode = when (intent.action) {
            ACTION_SET_PASSCODE_LOCK -> SET_MODE
            ACTION_RESET_PASSCODE_LOCK -> RESET_MODE
            else -> UNLOCK_MODE
        }

        setOrUnlockMode = mode == SET_MODE || mode == UNLOCK_MODE

        if (savedInstanceState != null) {
            secondRound = savedInstanceState.getBoolean(SECOND_ROUND, false)

            if (secondRound) {
                sbFirst.append(savedInstanceState.get(SB_FIRST))
            }

            attempts = savedInstanceState.getInt(ATTEMPTS, 0)
            passcodeType = savedInstanceState.getString(PASSCODE_TYPE, PIN_4)
        } else {
            val prefs = dbH.preferences

            passcodeType =
                if (prefs != null && !isTextEmpty(prefs.passcodeLockType)) prefs.passcodeLockType else PIN_4
        }

        binding = ActivityPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (mode == UNLOCK_MODE) {
            binding.toolbarPasscodeLockTitle.isVisible = true
        } else {
            setSupportActionBar(binding.toolbarPasscode)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title =
                StringResourcesUtils.getString(
                    if (mode == SET_MODE) R.string.settings_passcode_lock
                    else R.string.title_change_passcode
                ).toUpperCase(Locale.ROOT)
        }

        initPasscodeScreen()
        setListeners()
    }

    /**
     * Increments the number of failed attempts.
     */
    private fun incrementAttempts() {
        attempts++
        dbH.setAttrAttemps(attempts)
    }

    /**
     * Resets the number of failed attempts to 0.
     */
    private fun resetAttempts() {
        attempts = 0
        dbH.setAttrAttemps(attempts)
    }

    /**
     * Resets the number of failed attempts and then logs out.
     */
    private fun logout() {
        resetAttempts()
        AccountController.logout(this, megaApi)
    }

    /**
     * Sets the whole initial passcode screen.
     */
    private fun initPasscodeScreen() {
        setTitleText()
        binding.passcodeOptionsButton.isVisible = !secondRound

        if (mode == UNLOCK_MODE) {
            attempts = dbH.attributes.attemps
            binding.passcodeOptionsButton.isVisible = false
        }

        binding.doNotMatchWarning.isVisible = false

        if (attempts > 0) {
            showAttemptsError()
        } else {
            binding.failedAttemptsText.isVisible = false
            binding.failedAttemptsErrorText.isVisible = false
            binding.logoutButton.isVisible = false
        }

        if (passcodeType == PIN_ALPHANUMERIC) {
            binding.passFirstInput.isVisible = false
            binding.passSecondInput.isVisible = false
            binding.passThirdInput.isVisible = false
            binding.passFourthInput.isVisible = false
            binding.passFifthInput.isVisible = false
            binding.passSixthInput.isVisible = false

            binding.passwordInput.apply {
                isVisible = true
                requestFocus()

                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == IME_ACTION_DONE) {
                        checkPasscode()
                        true
                    } else false

                }
            }
        } else {
            binding.passFirstInput.apply {
                isVisible = true
                requestFocus()

                doAfterTextChanged {
                    if (this.text.toString().isNotEmpty()) {
                        binding.passSecondInput.requestFocus()
                    }
                }
            }

            binding.passSecondInput.apply {
                isVisible = true

                doAfterTextChanged {
                    if (this.text.toString().isNotEmpty()) {
                        binding.passThirdInput.requestFocus()
                    }
                }

                setEt(binding.passFirstInput)
            }


            binding.passThirdInput.apply {
                isVisible = true

                doAfterTextChanged {
                    if (this.text.toString().isNotEmpty()) {
                        binding.passFourthInput.requestFocus()
                    }
                }

                setEt(binding.passSecondInput)
            }

            binding.passFourthInput.apply {
                isVisible = true

                doAfterTextChanged {
                    if (this.text.toString().isNotEmpty()) {
                        if (passcodeType == PIN_4) {
                            binding.passFirstInput.apply {
                                isCursorVisible = false
                                requestFocus()
                            }

                            checkPasscode()
                        } else {
                            binding.passFifthInput.requestFocus()
                        }
                    }
                }

                setEt(binding.passThirdInput)
            }

            val params = binding.passFourthInput.layoutParams as ConstraintLayout.LayoutParams

            if (passcodeType == PIN_4) {
                binding.passFourthInput.imeOptions = IME_ACTION_DONE or IME_FLAG_NO_FULLSCREEN

                params.marginEnd = 0

                binding.passFifthInput.isVisible = false
                binding.passSixthInput.isVisible = false
            } else {
                binding.passFourthInput.imeOptions = IME_ACTION_NEXT or IME_FLAG_NO_FULLSCREEN

                params.marginEnd = dp2px(16F, resources.displayMetrics)

                binding.passFifthInput.apply {
                    isVisible = true

                    doAfterTextChanged {
                        if (this.text.toString().isNotEmpty()) {
                            binding.passSixthInput.requestFocus()
                        }
                    }

                    setEt(binding.passFourthInput)
                }

                binding.passSixthInput.apply {
                    isVisible = true

                    doAfterTextChanged {
                        if (this.text.toString().isNotEmpty()) {
                            binding.passFirstInput.apply {
                                isCursorVisible = false
                                requestFocus()
                            }

                            checkPasscode()
                        }
                    }

                    setEt(binding.passFifthInput)
                }
            }

            binding.passFourthInput.layoutParams = params

            binding.passwordInput.isVisible = false
        }
    }

    /**
     * Sets the text of the title depending on the current situation.
     */
    private fun setTitleText() {
        binding.titleText.text = StringResourcesUtils.getString(
            if (secondRound && setOrUnlockMode) R.string.unlock_pin_title_2
            else if (secondRound) R.string.reset_pin_title_2
            else if (setOrUnlockMode) R.string.unlock_pin_title
            else R.string.reset_pin_title
        )
    }

    /**
     * Sets the necessary listeners to all available views in passcode screen.
     */
    private fun setListeners() {
        ListenScrollChangesHelper().addViewToListen(
            binding.passcodeScrollView
        ) { _, _, _, _, _ ->
            binding.toolbarPasscode.elevation =
                if (binding.passcodeScrollView.canScrollVertically(-1)) {
                    dp2px(4F, resources.displayMetrics).toFloat()
                } else 0F
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.passcodeOptionsButton.setOnClickListener {
            showPasscodeOptions()
        }
    }

    /**
     * Saves the typed passcode after check if it is completed and confirms it:
     *  - After the first time the passcode was typed if it's unlocking.
     *  - After the second time the passcode was typed if it's setting or resetting.
     */
    private fun checkPasscode() {
        if (!isPassCodeComplete()) {
            return
        }

        val sb = if (secondRound) sbSecond else sbFirst

        when (passcodeType) {
            PIN_4 -> savePin4(sb)
            PIN_6 -> savePin6(sb)
            PIN_ALPHANUMERIC -> sb.append(binding.passwordInput.text)
        }

        when {
            secondRound -> confirmPasscode()
            mode == UNLOCK_MODE -> confirmUnlockPasscode()
            else -> {
                secondRound = true
                clearTypedPasscode()
                binding.passcodeOptionsButton.isVisible = false
            }
        }
    }

    /**
     * Saves the typed pin if passcode type is PIN_4.
     *
     * @param sb StringBuilder in which the pin has to be saved.
     */
    private fun savePin4(sb: StringBuilder) {
        sb.apply {
            append(binding.passFirstInput.text)
            append(binding.passSecondInput.text)
            append(binding.passThirdInput.text)
            append(binding.passFourthInput.text)
        }
    }

    /**
     * Saves the typed pin if passcode type is PIN_6.
     *
     * @param sb StringBuilder in which the pin has to be saved.
     */
    private fun savePin6(sb: StringBuilder) {
        savePin4(sb)
        sb.apply {
            append(binding.passFifthInput.text)
            append(binding.passSixthInput.text)
        }
    }

    /**
     * Confirms the passcode after type it the second time if setting or resetting.
     * Updates the passcode in DB and finishes if successful.
     * Shows an error if not successful.
     */
    private fun confirmPasscode() {
        if (sbFirst.toString() == sbSecond.toString()) {
            passcodeUtil.enablePasscode(passcodeType, sbFirst.toString())
            setResult(RESULT_OK)
            finish()
        } else {
            clearTypedPasscode()
            sbSecond.clear()
            binding.doNotMatchWarning.isVisible = true
        }
    }

    /**
     * Confirms the passcode after type it the first time if unlocking.
     * Updates the passcode behaviour, failed attempts and finishes if successful.
     * Shows an error and increments failed attempts if not successful.
     */
    private fun confirmUnlockPasscode() {
        if (sbFirst.toString() == dbH.preferences.passcodeLockCode) {
            passcodeUtil.update()
            resetAttempts()
            finish()
        } else {
            sbFirst.clear()
            incrementAttempts()
            clearTypedPasscode()
            showAttemptsError()
        }
    }

    /**
     * Shows the right attempts error depending on the number of failed attempts.
     */
    private fun showAttemptsError() {
        binding.failedAttemptsText.apply {
            isVisible = true
            text = StringResourcesUtils.getQuantityString(
                R.plurals.passcode_lock_alert_attempts,
                attempts,
                attempts
            )
        }

        when {
            attempts == MAX_ATTEMPTS -> {
                binding.passcodeParentView.isEnabled = false
                hideKeyboardView(this, currentFocus, 0)
                logout()
            }
            attempts >= MIN_ATTEMPTS_TO_SHOW_WARNING -> {
                binding.failedAttemptsErrorText.isVisible = true
                binding.logoutButton.isVisible = true
            }
            attempts > 0 -> {
                binding.failedAttemptsErrorText.isVisible = false
                binding.logoutButton.isVisible = true
            }
            else -> {
                binding.failedAttemptsErrorText.isVisible = false
                binding.logoutButton.isVisible = false
            }
        }
    }

    /**
     * Checks if all the fields are filled in before confirm the passcode.
     */
    private fun isPassCodeComplete(): Boolean {
        when (passcodeType) {
            PIN_4 -> {
                return binding.passFirstInput.length() == 1
                        && binding.passSecondInput.length() == 1
                        && binding.passThirdInput.length() == 1
                        && binding.passFourthInput.length() == 1
            }
            PIN_6 -> {
                return binding.passFirstInput.length() == 1
                        && binding.passSecondInput.length() == 1
                        && binding.passThirdInput.length() == 1
                        && binding.passFourthInput.length() == 1
                        && binding.passFifthInput.length() == 1
                        && binding.passSixthInput.length() == 1
            }
            PIN_ALPHANUMERIC -> {
                return binding.passwordInput.text.isNotEmpty()
            }
        }

        return false
    }

    /**
     * Clears the passcode fields.
     */
    private fun clearTypedPasscode() {
        binding.passFirstInput.text.clear()
        binding.passSecondInput.text.clear()
        binding.passThirdInput.text.clear()
        binding.passFourthInput.text.clear()
        binding.passFifthInput.text.clear()
        binding.passSixthInput.text.clear()

        binding.passwordInput.text.clear()

        setTitleText()

        if (passcodeType == PIN_ALPHANUMERIC) {
            binding.passwordInput.requestFocus()
        } else {
            binding.passFirstInput.requestFocus()
        }
    }

    /**
     * Opens the bottom sheet dialog to change the passcode type.
     */
    private fun showPasscodeOptions() {
        if (isBottomSheetDialogShown(passcodeOptionsBottomSheetDialogFragment)) return

        passcodeOptionsBottomSheetDialogFragment =
            PasscodeOptionsBottomSheetDialogFragment.newInstance(passcodeType)
        passcodeOptionsBottomSheetDialogFragment?.show(
            supportFragmentManager,
            passcodeOptionsBottomSheetDialogFragment?.tag
        )
    }

    /**
     * Updates the passcode screen after change the type.
     *
     * @param passcodeType New passcode type.
     */
    fun setPasscodeType(passcodeType: String) {
        this.passcodeType = passcodeType
        initPasscodeScreen()
        clearTypedPasscode()

        Handler().postDelayed({
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                currentFocus,
                InputMethodManager.SHOW_IMPLICIT
            )
        }, 1000)
    }

    override fun onBackPressed() {
        if (psaWebBrowser.consumeBack()) return
        if (attempts < MAX_ATTEMPTS) {
            when (mode) {
                UNLOCK_MODE -> return
                RESET_MODE -> MegaApplication.getPasscodeManagement().showPasscodeScreen = false
                else -> finish()
            }
        }

        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressed()

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        MegaApplication.getPasscodeManagement().showPasscodeScreen = isFinishing
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        if (mode != UNLOCK_MODE) {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SECOND_ROUND, secondRound)

        if (secondRound) {
            outState.putString(SB_FIRST, sbFirst.toString())
        }

        outState.putInt(ATTEMPTS, attempts)
        outState.putString(PASSCODE_TYPE, passcodeType)

        super.onSaveInstanceState(outState)
    }
}