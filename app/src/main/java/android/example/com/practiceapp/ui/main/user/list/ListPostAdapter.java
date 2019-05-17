package android.example.com.practiceapp.ui.main.user.list;

import android.content.Context;
import android.example.com.practiceapp.R;
import android.example.com.practiceapp.data.models.Post;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import android.example.com.practiceapp.databinding.ItemListPostBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;

public class ListPostAdapter extends FirestorePagingAdapter<Post, ListPostViewHolder> {
    private ProgressBar mProgressBar;
    private Context mContext;

    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions}.
     *
     * @param options
     */
    ListPostAdapter(@NonNull FirestorePagingOptions<Post> options, Context context, ProgressBar progressBar) {
        super(options);
        mContext = context;
        mProgressBar = progressBar;
    }

    @Override protected void onBindViewHolder(@NonNull ListPostViewHolder holder, int position, @NonNull Post item) {
        holder.bind(item);
    }

    @NonNull @Override public ListPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListPostBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_list_post, parent, false);
        return new ListPostViewHolder(binding);
    }

    @Override protected void onLoadingStateChanged(@NonNull LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
            case LOADING_MORE:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case LOADED:
                mProgressBar.setVisibility(View.GONE);
                break;
            case FINISHED:
                mProgressBar.setVisibility(View.GONE);
                showToast(R.string.reached_end_data);
                break;
            case ERROR:
                showToast(R.string.error_ocurred);
                retry();
                break;
        }
    }

    private void showToast(@StringRes int message) { Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show(); }

}
