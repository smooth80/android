package mega.privacy.android.app.fragments.managerFragments.cu;

import android.util.Pair;

import mega.privacy.android.app.R;
import mega.privacy.android.app.databinding.ItemCameraUploadsTitleBinding;

import static mega.privacy.android.app.utils.LogUtil.logWarning;
import static mega.privacy.android.app.utils.StringResourcesUtils.getString;
import static mega.privacy.android.app.utils.StringUtils.toSpannedHtmlText;
import static mega.privacy.android.app.utils.TextUtil.isTextEmpty;

class CuTitleViewHolder extends CuGridViewHolder {

    private final ItemCameraUploadsTitleBinding mBinding;

    public CuTitleViewHolder(ItemCameraUploadsTitleBinding binding) {
        super(binding.getRoot());

        mBinding = binding;
    }

    @Override
    protected void bind(CuNode node) {
        Pair<String, String> date = node.getHeaderDate();
        String dateText = isTextEmpty(date.second)
                ? "[B]" + date.first + "[/B]"
                : getString(R.string.cu_month_year_date, date.first, date.second);

        try {
            dateText = dateText.replace("[B]", "<font face=\"sans-serif-medium\">")
                    .replace("[/B]", "</font>");
        } catch (Exception e) {
            logWarning("Exception formatting text.", e);
        }

        mBinding.headerText.setText(toSpannedHtmlText(dateText));
    }

    @Override protected boolean handleClick() {
        return false;
    }
}
