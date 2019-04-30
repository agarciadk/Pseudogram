package android.example.com.practiceapp;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.example.com.practiceapp.models.User;
import android.example.com.practiceapp.utilities.OnSearchSelectedListener;
import android.example.com.practiceapp.viewmodel.UserViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.algolia.instantsearch.core.events.CancelEvent;
import com.algolia.instantsearch.core.events.ResultEvent;
import com.algolia.instantsearch.core.events.SearchEvent;
import com.algolia.instantsearch.core.helpers.Searcher;
import com.algolia.instantsearch.core.model.AlgoliaErrorListener;
import com.algolia.instantsearch.core.model.AlgoliaResultsListener;
import com.algolia.instantsearch.core.model.SearchResults;
import com.algolia.instantsearch.ui.helpers.InstantSearch;
import com.algolia.instantsearch.ui.utils.ItemClickSupport;
import com.algolia.instantsearch.ui.views.Hits;
import com.algolia.instantsearch.ui.views.SearchBox;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Query;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

public class SearchFragment extends Fragment {

    public static final String TAG = SearchFragment.class.getSimpleName();
    public static final String ALGOLIA_APP_ID = "62MAN5SB6V";
    public static final String ALGOLIA_SEARCH_API_KEY = "a5b6026886482735cb7d88d84ce65848";
    public static final String ALGOLIA_INDEX_NAME = "usuarios";

    private Context mContext;
    private UserViewModel model;
    private Boolean userVisibleHint = true;
    private Searcher mSearcher;
    private Hits mHits;
    private AlgoliaResultsListener mResultListener;
    private AlgoliaErrorListener mErrorListener;
    private OnSearchSelectedListener callback;

    public void setOnSearchSelectedListener(OnSearchSelectedListener callback) {
        this.callback = callback;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        model = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        bindView();
        userVisibleHint = true;
        subscribeToSearcher();
    }

    @Override
    public void onStart() {
        super.onStart();
        mErrorListener = new AlgoliaErrorListener() {
            @Override
            public void onError(@NonNull Query query, @NonNull AlgoliaException error) {
                Log.w(TAG, "Error searching" + query.getQuery() + ":" + error.getLocalizedMessage());
                Toast.makeText(mContext, "Error searching" + query.getQuery() + ":" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        mSearcher.registerErrorListener(mErrorListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Highlight the selected item has been done by NavigationView
        ((MainActivity)getActivity()).setNavItemChecked(2);
    }

    @Override
    public void onPause() {
        userVisibleHint = false;
        hideKeyboard();
        super.onPause();
    }

    @Override
    public void onStop() {
        mSearcher.unregisterResultListener(mResultListener);
        mSearcher.unregisterErrorListener(mErrorListener);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean getUserVisibleHint() { return userVisibleHint; }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        new InstantSearch(getActivity(), menu, R.id.action_search, mSearcher); // link the Searcher to the UI
        mSearcher.search();

        final MenuItem itemSearch = menu.findItem(R.id.action_search);
        SearchBox searchBox = (SearchBox) itemSearch.getActionView();
        searchBox.disableFullScreen();
        itemSearch.expandActionView(); // open SearchBar on startup
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void bindView(){
        mHits = getView().findViewById(R.id.hits);
    }

    private void subscribeToSearcher() {
        mSearcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);

        mResultListener = new AlgoliaResultsListener() {
            @Override
            public void onResults(@NonNull SearchResults results, boolean isLoadingMore) {
                if(TextUtils.isEmpty(mSearcher.getQuery().getQuery())) {
                    mHits.setVisibility(View.INVISIBLE);
                    return;
                }
                mHits.setVisibility(View.VISIBLE);
            }
        };
        mSearcher.registerResultListener(mResultListener);
        mHits.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, int position, View v) {
                hideKeyboard();
                JSONObject json = mHits.get(position);
                Log.d(TAG, "JSONObject: " + json.toString());
                User user = new User(
                        json.optString(User.FIELD_UID, null),
                        json.optString(User.FIELD_USERNAME, null),
                        json.optString(User.FIELD_DISPLAYNAME, null),
                        json.optString(User.FIELD_EMAIL, null),
                        json.optString(User.FIELD_PHOTO_URL, null),
                        json.optString(User.FIELD_TLFNO, null),
                        json.optString(User.FIELD_SEX, null),
                        json.optLong(User.FIELD_LAST_LOGIN));
                model.select(user);
                callback.onUserSelected();
                Toast.makeText(mContext, "TODO", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Subscribe
    public void onCancelEvent(CancelEvent event){}
    @Subscribe
    public void onResultEvent(ResultEvent event){}
    @Subscribe
    public void onSearchEvent(SearchEvent event){}

}
