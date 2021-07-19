package mega.privacy.android.app.fragments.managerFragments.cu

import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mega.privacy.android.app.R
import mega.privacy.android.app.databinding.ItemCuCardBinding
import mega.privacy.android.app.fragments.managerFragments.cu.CameraUploadsFragment.*
import mega.privacy.android.app.utils.LogUtil
import mega.privacy.android.app.utils.StringResourcesUtils.getString
import mega.privacy.android.app.utils.StringUtils.toSpannedHtmlText

/**
 * Holder representing a card view.
 *
 * @param viewType   Type of view: DAYS_VIEW if days, MONTHS_VIEW if months, YEARS_VIEW if years.
 * @param binding    Binding of the card view.
 * @param cardWidth  Size to set as card view width.
 * @param cardMargin Size to set as card view margin.
 */
class CUCardViewHolder(
    private val viewType: Int,
    private val binding: ItemCuCardBinding,
    cardWidth: Int,
    cardMargin: Int
) : RecyclerView.ViewHolder(binding.root) {

    init {
        val params = binding.root.layoutParams as GridLayoutManager.LayoutParams
        params.width = cardWidth
        params.height = cardWidth
        params.setMargins(cardMargin, cardMargin, cardMargin, cardMargin)
        binding.root.layoutParams = params
    }

    @SuppressLint("SetTextI18n")
    fun bind(position: Int, card: CUCard, listener: CUCardViewAdapter.Listener) {
        itemView.setOnClickListener { listener.onCardClicked(position, card) }

        var date = when (viewType) {
            YEARS_VIEW -> "[B]" + card.year + "[/B]"
            MONTHS_VIEW -> if (card.year == null) "[B]" + card.month + "[/B]" else getString(
                R.string.cu_month_year_date,
                card.month,
                card.year
            )
            DAYS_VIEW -> if (card.year == null) "[B]" + card.date + "[/B]" else getString(
                R.string.cu_day_month_year_date,
                card.day,
                card.month,
                card.year
            )
            else -> null
        }

        if (date != null) {
            try {
                date = date.replace("[B]", "<b><font face=\"sans-serif\">")
                    .replace("[/B]", "</font></b>")
            } catch (e: Exception) {
                LogUtil.logWarning("Exception formatting text.", e)
            }

            binding.dateText.text = date?.toSpannedHtmlText()
        }

        val numItems = card.numItems

        binding.numberItemsText.isVisible = viewType == DAYS_VIEW && numItems > 0
        binding.numberItemsText.text = "+${numItems}"

        val preview = card.preview

        if (preview != null) {
            binding.progressBar.isVisible = false
            binding.preview.apply {
                isVisible = true
                setImageURI(Uri.fromFile(preview))
            }
        } else {
            binding.progressBar.isVisible = true
            binding.preview.isVisible = false
        }
    }
}