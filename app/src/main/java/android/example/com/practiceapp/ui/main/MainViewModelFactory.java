package android.example.com.practiceapp.ui.main;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.example.com.practiceapp.data.PracticeAppRepository;
import android.support.annotation.NonNull;

public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final PracticeAppRepository mRepository;

    public MainViewModelFactory(PracticeAppRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MainViewModel(mRepository);
    }
}
