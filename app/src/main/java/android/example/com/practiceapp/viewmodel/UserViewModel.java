package android.example.com.practiceapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.example.com.practiceapp.models.User;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserViewModel extends ViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();
    private static final String UNFOLLOW = "UNFOLLOW";
    private static final String FOLLOW = "FOLLOW";
    private static final String EDIT_PROFILE = "EDIT_PROFILE";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<User> userSigned = new MutableLiveData<>();
    private MutableLiveData<User> userSelected;
    private MutableLiveData<String> buttonText = new MutableLiveData<>();


    public void setUserSigned(User user) {
        Log.d(TAG, "setUserSigned()");
        userSigned.setValue(user);
    }

    public void select(User user) {
        userSelected.setValue(user);
        loadFollows();
    }

    public MutableLiveData<User> getUserSelected() {
        if (userSelected == null) {
            userSelected = new MutableLiveData<>();
        }
        return userSelected;
    }

    private boolean isMyProfile() { return (userSelected.getValue().getEmail().equals(userSigned.getValue().getEmail())); }

    private void loadFollows() {
        if (isMyProfile()){
            Log.d(TAG, EDIT_PROFILE);
            buttonText.setValue(EDIT_PROFILE);
            return;
        }
        db.collection("following/" + userSigned.getValue().getEmail() + "/userFollowing/")
            .whereEqualTo(FieldPath.documentId(), userSelected.getValue().getEmail())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()){
                            Log.d(TAG, "you are following " + userSelected.getValue().getEmail());
                            buttonText.setValue(UNFOLLOW);
                        } else {
                            Log.d(TAG, "you are not following " + userSelected.getValue().getEmail());
                            buttonText.setValue(FOLLOW);
                        }
                    } else {
                        Log.w(TAG, "Failed with ", task.getException());
                    }
                }
            });
    }

    public MutableLiveData<String> getActionButton() { return buttonText; }

    public void loadFollowers() {
        db.collection("followers/" + userSigned.getValue().getEmail() + "/userFollowers")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.w(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

}
