package com.mad.poleato.View.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.mad.poleato.FirebaseData.MyFirebaseData;
import com.mad.poleato.History.HistoryItem;

import java.util.HashMap;


public class MyViewModel extends ViewModel {
    private MutableLiveData<HashMap<String, HistoryItem>> _MapDataHistory;
    private MutableLiveData<Boolean> _showProgressBar;
    private MyFirebaseData myFirebaseData;

    public MyViewModel(){
        this.myFirebaseData = new MyFirebaseData();
        this._MapDataHistory = myFirebaseData.getMapDataHistory();
        if(myFirebaseData != null && _MapDataHistory != null){
            myFirebaseData.fillFieldsHistory();
        }
        this._showProgressBar = myFirebaseData.getShowProgressBar();
    }


    public LiveData<HashMap<String, HistoryItem>> getListH() {
//        if (_MapDataHistory == null)
//        _MapDataHistory = new MutableLiveData<>(); // header titles
        return _MapDataHistory;
    }

/*    public void insertChild(String orderID, HistoryItem history) {
        this._MapDataHistory.getValue().put(orderID, history);
        this._MapDataHistory.postValue(_MapDataHistory.getValue());
    }

    public void removeChild(final String orderID) {
        this._MapDataHistory.getValue().remove(orderID);
    }

    public void initChild(){
        _MapDataHistory.setValue(new HashMap<String, HistoryItem>());
    }
*/
}
