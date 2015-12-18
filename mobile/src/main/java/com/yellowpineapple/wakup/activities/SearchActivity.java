package com.yellowpineapple.wakup.activities;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.communications.Request;
import com.yellowpineapple.wakup.communications.requests.search.SearchRequest;
import com.yellowpineapple.wakup.controllers.SearchResultAdapter;
import com.yellowpineapple.wakup.models.SearchResult;
import com.yellowpineapple.wakup.models.SearchResultItem;
import com.yellowpineapple.wakup.utils.Ln;
import com.yellowpineapple.wakup.utils.Strings;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@OptionsMenu(R.menu.search_menu)
@EActivity(R.layout.activity_search)
public class SearchActivity extends ParentActivity {

    SearchView searchView;
    Request searchRequest = null;
    @Extra Location location = null;

    // Views
    @ViewById ListView listView;
    SearchResultAdapter listAdapter;

    String searchQuery = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setQuery("", false);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @AfterViews
    void afterViews() {
        listAdapter = new SearchResultAdapter(this, location);
        listAdapter.setListener(new SearchResultAdapter.Listener() {
            @Override
            public void onItemClick(SearchResultItem item, View view) {
                SearchResultActivity_.intent(SearchActivity.this).searchItem(item).start();
                slideInTransition();
                getPersistence().addRecentSearch(item);
                listAdapter.setRecentSearches(getPersistence().getRecentSearches());
                listAdapter.notifyDataSetChanged();
            }
        });
        listAdapter.setRecentSearches(getPersistence().getRecentSearches());
        listAdapter.notifyDataSetChanged();
        listView.setAdapter(listAdapter);
    }

    void search(final String query) {
        this.searchQuery = query;
        if (searchRequest != null) {
            searchRequest.cancel();
        }
        if (Strings.notEmpty(query)) {
            searchRequest = getRequestClient().search(query.trim(), new SearchRequest.Listener() {
                @Override
                public void onSuccess(SearchResult searchResult) {
                    searchRequest = null;
                    // Check if query is still valid
                    if (Strings.equals(query, searchQuery)) {
                        listAdapter.setCompanies(searchResult.getCompanies());
                        refreshList();
                    }
                }

                @Override
                public void onError(Exception exception) {
                    searchRequest = null;
                    Ln.e(exception);
                    Toast.makeText(SearchActivity.this, R.string.search_error, Toast.LENGTH_LONG).show();
                }
            });
            geoSearch(query.trim(), "Spain", "ES");
        } else {
            listAdapter.setCompanies(null);
            listAdapter.setAddresses(null);
            refreshList();
        }
    }

    @UiThread
    void refreshList() {
        listAdapter.notifyDataSetChanged();
    }

    @Background
    void geoSearch(final String query, String country, String countryCode) {
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addresses = geocoder.getFromLocationName(String.format("%s, %s", query, country), 5);
                List<Address> validAddresses = new ArrayList<>();
                for (Address address : addresses) {
                    // Only include addresses from selected country
                    if (Strings.equals(countryCode, address.getCountryCode())) {
                        validAddresses.add(address);
                    }
                }
                // Check if query is still valid
                if (Strings.equals(query, searchQuery)) {
                    listAdapter.setAddresses(validAddresses);
                    refreshList();
                }
            } catch (Exception exception) {
                Ln.e(exception);
                Toast.makeText(SearchActivity.this, R.string.search_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
