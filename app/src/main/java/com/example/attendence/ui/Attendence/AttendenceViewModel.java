package com.example.attendence.ui.Attendence;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AttendenceViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AttendenceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("attendence fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}