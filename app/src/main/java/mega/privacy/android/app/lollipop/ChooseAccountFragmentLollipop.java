package mega.privacy.android.app.lollipop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import java.util.ArrayList;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.Product;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.ListenScrollChangesHelper;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.lollipop.managerSections.UpgradeAccountFragmentLollipop;

import static mega.privacy.android.app.constants.IntentConstants.*;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.Util.*;

public class ChooseAccountFragmentLollipop extends UpgradeAccountFragmentLollipop implements View.OnClickListener {

    private Toolbar tB;

    public ArrayList<Product> accounts;

    private TextView storageSectionFree;
    private TextView bandwidthSectionFree;
    private TextView achievementsSectionFree;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myAccountInfo = app.getMyAccountInfo();

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        final DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        DatabaseHandler dbH = DatabaseHandler.getDbHandler(context.getApplicationContext());
        if (dbH.getCredentials() == null){
            //Show Login Fragment
            ((LoginActivityLollipop)context).showFragment(LOGIN_FRAGMENT);
        }

        accounts = new ArrayList<>();

        View v = inflater.inflate(R.layout.fragment_choose_account, container, false);

        tB =  v.findViewById(R.id.toolbar_choose_account);
        ((LoginActivityLollipop) context).showAB(tB);

        scrollView = v.findViewById(R.id.scroll_view_choose_account);
        new ListenScrollChangesHelper().addViewToListen(scrollView, (v1, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollView.canScrollVertically(-1)) {
                tB.setElevation(dp2px(4, outMetrics));
            } else {
                tB.setElevation(0);
            }
        });

        //FREE ACCOUNT
        RelativeLayout freeLayout = v.findViewById(R.id.choose_account_free_layout);
        freeLayout.setOnClickListener(this);
        TextView titleFree = v.findViewById(R.id.choose_account_free_title_text);
        titleFree.setText(getString(R.string.free_account).toUpperCase());
        storageSectionFree = v.findViewById(R.id.storage_free);
        bandwidthSectionFree = v.findViewById(R.id.bandwidth_free);
        achievementsSectionFree = v.findViewById(R.id.achievements_free);
        //END -- PRO LITE ACCOUNT

        //PRO LITE ACCOUNT
        proLiteLayout = v.findViewById(R.id.choose_account_prolite_layout);
        proLiteLayout.setOnClickListener(this);
        TextView titleProLite = v.findViewById(R.id.choose_account_prolite_title_text);
        titleProLite.setText(getString(R.string.prolite_account));
        monthSectionLite = v.findViewById(R.id.month_lite);
        storageSectionLite = v.findViewById(R.id.storage_lite);
        bandwidthSectionLite = v.findViewById(R.id.bandwidth_lite);
        //END -- PRO LITE ACCOUNT

        //PRO I ACCOUNT
        pro1Layout = v.findViewById(R.id.choose_account_pro_i_layout);
        pro1Layout.setOnClickListener(this);
        TextView titlePro1 = v.findViewById(R.id.choose_account_pro_i_title_text);
        titlePro1.setText(getString(R.string.pro1_account));
        monthSectionPro1 = v.findViewById(R.id.month_pro_i);
        storageSectionPro1 = v.findViewById(R.id.storage_pro_i);
        bandwidthSectionPro1 = v.findViewById(R.id.bandwidth_pro_i);
        //END -- PRO I ACCOUNT

        //PRO II ACCOUNT
        pro2Layout = v.findViewById(R.id.choose_account_pro_ii_layout);
        pro2Layout.setOnClickListener(this);
        TextView titlePro2 = v.findViewById(R.id.choose_account_pro_ii_title_text);
        titlePro2.setText(getString(R.string.pro2_account));
        monthSectionPro2 = v.findViewById(R.id.month_pro_ii);
        storageSectionPro2 = v.findViewById(R.id.storage_pro_ii);
        bandwidthSectionPro2 = v.findViewById(R.id.bandwidth_pro_ii);
        //END -- PRO II ACCOUNT

        //PRO III ACCOUNT
        pro3Layout = v.findViewById(R.id.choose_account_pro_iii_layout);
        pro3Layout.setOnClickListener(this);
        TextView titlePro3 = v.findViewById(R.id.choose_account_pro_iii_title_text);
        titlePro3.setText(getString(R.string.pro3_account));
        monthSectionPro3 = v.findViewById(R.id.month_pro_iii);
        storageSectionPro3 = v.findViewById(R.id.storage_pro_iii);
        bandwidthSectionPro3 = v.findViewById(R.id.bandwidth_pro_iii);
        //END -- PRO III ACCOUNT

        setPricingInfo();
        return v;
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(context,ManagerActivityLollipop.class);
        intent.putExtra(EXTRA_FIRST_LOGIN, true);
        intent.putExtra(EXTRA_NEW_ACCOUNT, true);
        intent.putExtra(ManagerActivityLollipop.NEW_CREATION_ACCOUNT, true);

        switch (v.getId()){
            case R.id.choose_account_free_layout:
                intent.putExtra(EXTRA_UPGRADE_ACCOUNT, false);
                intent.putExtra(EXTRA_ACCOUNT_TYPE, FREE);
                break;
            case R.id.choose_account_prolite_layout:
                intent.putExtra(EXTRA_UPGRADE_ACCOUNT, true);
                intent.putExtra(EXTRA_ACCOUNT_TYPE, PRO_LITE);
                break;
            case R.id.choose_account_pro_i_layout:
                intent.putExtra(EXTRA_UPGRADE_ACCOUNT, true);
                intent.putExtra(EXTRA_ACCOUNT_TYPE, PRO_I);
                break;
            case R.id.choose_account_pro_ii_layout:
                intent.putExtra(EXTRA_UPGRADE_ACCOUNT, true);
                intent.putExtra(EXTRA_ACCOUNT_TYPE, PRO_II);
                break;
            case R.id.choose_account_pro_iii_layout:
                intent.putExtra(EXTRA_UPGRADE_ACCOUNT, true);
                intent.putExtra(EXTRA_ACCOUNT_TYPE, PRO_III);
                break;
        }

        startActivity(intent);
        ((LoginActivityLollipop)context).finish();
    }

    void onFreeClick() {
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.putExtra(EXTRA_FIRST_LOGIN, true);
        intent.putExtra(EXTRA_UPGRADE_ACCOUNT, false);
        intent.putExtra(EXTRA_ACCOUNT_TYPE, FREE);
        intent.putExtra(EXTRA_NEW_ACCOUNT, true);
        intent.putExtra(ManagerActivityLollipop.NEW_CREATION_ACCOUNT, true);
        startActivity(intent);
        ((LoginActivityLollipop) context).finish();
    }

    @Override
    public void setPricingInfo() {
        //Currently the API side doesn't return this value, so we have to hardcode.
        String textToShowFreeStorage = "[A] 20 GB+ [/A]" + getString(R.string.label_storage_upgrade_account) + " ";
        try {
            textToShowFreeStorage = textToShowFreeStorage.replace("[A]", "<font color='"
                    + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                    + "'>");
            textToShowFreeStorage = textToShowFreeStorage.replace("[/A]", "</font>");
        } catch (Exception e) {
            logWarning("Exception formatting string", e);
        }
        storageSectionFree.setText(HtmlCompat.fromHtml(textToShowFreeStorage + "<sup><small><font color='#ff333a'>1</font></small></sup>", HtmlCompat.FROM_HTML_MODE_LEGACY));

        String textToShowFreeBandwidth = "[A] " + getString(R.string.limited_bandwith) + "[/A] " + getString(R.string.label_transfer_quota_upgrade_account);
        try {
            textToShowFreeBandwidth = textToShowFreeBandwidth.replace("[A]", "<font color='"
                    + ColorUtils.getColorHexString(context, R.color.grey_900_grey_100)
                    + "'>");
            textToShowFreeBandwidth = textToShowFreeBandwidth.replace("[/A]", "</font>");
        } catch (Exception e) {
            logWarning("Exception formatting string", e);
        }
        bandwidthSectionFree.setText(HtmlCompat.fromHtml(textToShowFreeBandwidth, HtmlCompat.FROM_HTML_MODE_LEGACY));
        achievementsSectionFree.setText(HtmlCompat.fromHtml("<sup><small><font color='#ff333a'>1</font></small></sup> " + getString(R.string.footnote_achievements), HtmlCompat.FROM_HTML_MODE_LEGACY));

        super.setPricingInfo();
    }
}
