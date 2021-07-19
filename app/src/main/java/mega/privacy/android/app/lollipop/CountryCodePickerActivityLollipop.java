package mega.privacy.android.app.lollipop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import mega.privacy.android.app.R;
import mega.privacy.android.app.activities.PasscodeActivity;
import mega.privacy.android.app.lollipop.adapters.CountryListAdapter;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.utils.Util;

import static mega.privacy.android.app.utils.ColorUtils.tintIcon;
import static mega.privacy.android.app.utils.LogUtil.*;

public class CountryCodePickerActivityLollipop extends PasscodeActivity implements CountryListAdapter.CountrySelectedCallback {
    private final String SAVED_QUERY_STRING = "SAVED_QUERY_STRING";
    private static List<Country> countries;

    private List<Country> selectedCountries = new ArrayList<>();

    private AppBarLayout abL;

    private RecyclerView countryList;

    private CountryListAdapter adapter;

    private ActionBar actionBar;

    private DisplayMetrics outMetrics;

    private MaterialToolbar toolbar;
    private String searchInput;
    private SearchView.SearchAutoComplete searchAutoComplete;
    private ArrayList<String> receivedCountryCodes;
    public static final String COUNTRY_NAME = "name";
    public static final String DIAL_CODE = "dial_code";
    public static final String COUNTRY_CODE = "code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contry_code_picker);
        receivedCountryCodes = getIntent().getExtras().getStringArrayList("country_code");
        Display display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        if (countries == null) {
            countries = loadCountries();
        }

        abL = findViewById(R.id.app_bar_layout);
        countryList = findViewById(R.id.country_list);
        countryList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Util.changeActionBarElevation(CountryCodePickerActivityLollipop.this, abL, recyclerView.canScrollVertically(-1));
            }
        });
        adapter = new CountryListAdapter(countries);
        adapter.setCallback(this);
        countryList.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        countryList.setLayoutManager(manager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.action_search_country).toUpperCase());
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(tintIcon(this, R.drawable.ic_arrow_back_white));
    
        if(savedInstanceState != null){
            searchInput = savedInstanceState.getString(SAVED_QUERY_STRING);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_country_picker,menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchMenuItem.getActionView();
        if (searchView != null) {
            searchView.setIconifiedByDefault(true);
        }

        View v = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setHint(getString(R.string.hint_action_search));
        if (searchInput != null) {
            searchMenuItem.expandActionView();
            searchView.setQuery(searchInput,true);
            search(searchInput);
        }
        v.setBackgroundColor(ContextCompat.getColor(this,android.R.color.transparent));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                logDebug("onQueryTextSubmit: " + query);
                InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getCurrentFocus();
                if (view == null) {
                    view = new View(CountryCodePickerActivityLollipop.this);
                }
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void search(String query) {
        selectedCountries.clear();
        List<Country> l1 = new ArrayList<>();
        List<Country> l2 = new ArrayList<>();

        for (Country country : countries) {
            if (country.name.toLowerCase().contains(query.toLowerCase())) {
                if(country.name.toLowerCase().startsWith(query.toLowerCase())) {
                    l1.add(country);
                } else {
                    l2.add(country);
                }
            }
        }
        //the countries with name starts with the query string should be put first
        Collections.sort(l1,new Comparator<Country>() {

            @Override
            public int compare(Country o1,Country o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        Collections.sort(l2,new Comparator<Country>() {

            @Override
            public int compare(Country o1,Country o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        selectedCountries.addAll(l1);
        selectedCountries.addAll(l2);
        adapter.refresh(selectedCountries);
    }

    @Override
    public void onBackPressed() {
        if (psaWebBrowser.consumeBack()) return;
        finish();
    }

    public List<Country> loadCountries() {
        //To decode received country codes from SMSVerificationActivity
        // Each string in ArrayList is "DO:1809,1829,1849,"
        ArrayList<Country> countryCodeList = new ArrayList<>();
        if (this.receivedCountryCodes != null) {
            for (String countryString : receivedCountryCodes) {
                int splitIndex = countryString.indexOf(":");
                String countryCode = countryString.substring(0, countryString.indexOf(":"));
                String [] dialCodes = countryString.substring(splitIndex + 1).split(",");
                for (String dialCode : dialCodes) {
                    Locale locale = new Locale("", countryCode);
                    String countryName = locale.getDisplayName();
                    countryCodeList.add(new Country(countryName, ("+" + dialCode), countryCode));
                }
            }
        }
        return countryCodeList;
    }

    @Override
    public void onCountrySelected(Country country) {
        Intent result = new Intent();
        result.putExtra(COUNTRY_NAME,country.getName());
        result.putExtra(DIAL_CODE,country.getCode());
        result.putExtra(COUNTRY_CODE,country.getCountryCode());
        setResult(Activity.RESULT_OK,result);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class Country {

        private String name;
        private String countryCode;
        private String code;

        public Country() {
            name = "";
            countryCode = "";
            code = "";
        }

        public Country(String name,String code,String countryCode) {
            this.code = code;
            this.countryCode = countryCode;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    
        public String getCountryCode() {
            return countryCode;
        }
    
        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        @Override
        public String toString() {
            return "Country{" +
                    "name='" + name + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    ", code='" + code + '\'' +
                    '}';
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String query = searchAutoComplete.getText().toString();
        if(searchAutoComplete.hasFocus() || (query != null && !query.isEmpty())){
            outState.putString(SAVED_QUERY_STRING, query);
        }
    }
}
