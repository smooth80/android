package mega.privacy.android.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import mega.privacy.android.app.R
import mega.privacy.android.app.activities.GiphyPickerActivity.Companion.GIF_DATA
import mega.privacy.android.app.databinding.ActivityGiphyViewerBinding
import mega.privacy.android.app.objects.GifData
import mega.privacy.android.app.utils.Constants.ACTION_PREVIEW_GIPHY
import mega.privacy.android.app.utils.FrescoUtils.loadGif
import mega.privacy.android.app.utils.GiphyUtil.Companion.getOriginalGiphySrc
import mega.privacy.android.app.utils.Util.isScreenInPortrait

class GiphyViewerActivity : PasscodeActivity() {

    private lateinit var binding: ActivityGiphyViewerBinding

    private var gifData: GifData? = null

    private var picking = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.statusBarColor = ContextCompat.getColor(this,R.color.black)
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or 0x00000010

        binding = ActivityGiphyViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.action.equals(ACTION_PREVIEW_GIPHY)) {
            picking = false
            binding.sendFab.visibility = View.GONE
        } else {
            binding.sendFab.setOnClickListener { sendGifToChat() }
        }

        gifData = intent.getParcelableExtra(GIF_DATA)
        updateGifDimensionsView()

        loadGif(binding.gifImage, binding.gifProgressBar, false, null, getOriginalGiphySrc(gifData?.webpUrl))
    }

    /**
     * Updates the dimensions of the view where the GIF will be shown to show the right ones
     * depending on the available space on screen and orientation.
     */
    private fun updateGifDimensionsView() {
        val params = binding.gifImage.layoutParams ?: return
        val gifWidth = gifData?.width
        val gifHeight = gifData?.height
        val gifScreenWidth: Int
        val gifScreenHeight: Int

        if (isScreenInPortrait(this@GiphyViewerActivity)) {
            gifScreenWidth = outMetrics.widthPixels

            gifScreenHeight = if (gifWidth == gifHeight) {
                gifScreenWidth
            } else {
                val factor = gifScreenWidth.toFloat() / (gifWidth ?: 0).toFloat()
                ((gifHeight ?: 0) * factor).toInt()
            }

            if (gifScreenHeight > 0) {
                params.height = gifScreenHeight
            }
        } else {
            gifScreenHeight = outMetrics.heightPixels

            gifScreenWidth = if (gifWidth == gifHeight) {
                gifScreenHeight
            } else {
                val factor = gifScreenHeight.toFloat() / (gifHeight ?: 0).toFloat()
                ((gifWidth ?: 0) * factor).toInt()
            }

            if (gifScreenWidth > 0) {
                params.width = gifScreenWidth
            }
        }

        binding.gifImage.layoutParams = params
    }

    /**
     * Confirms the selected GIF has to be sent to the chat.
     */
    private fun sendGifToChat() {
        if (picking) {
            setResult(RESULT_OK, Intent().putExtra(GIF_DATA, gifData))
        }
        finish()
    }

    override fun onBackPressed() {
        if (psaWebBrowser.consumeBack()) return
        if (picking) {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed()
    }
}