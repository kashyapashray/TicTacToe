package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DashBoardFragmentViewModel extends ViewModel {

    User user;
    private static final String TAG = "DASH_VM";

    LiveData<User> getUserData(String uid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicReference<User> _userData = new AtomicReference<>(User.forError());
        MutableLiveData<User> userData = new MutableLiveData<>();

        //fetch user Data
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "User GET failed.", error);
                return;
            }
            if (value != null) {
                Log.d(TAG,"user GET SUCCESS");
                _userData.set(new User(uid,
                        (String) value.get("name"),
                        (String) value.get("email"),
                        (Long) value.get("wins"),
                        (Long) value.get("losses")
                ));
                userData.setValue(_userData.get());
            } else {
                Log.d(TAG, "User data: null");
            }
        });

        return userData;
    }

    LiveData<ArrayList<GameList>> getAvailableGameList(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        MutableLiveData<ArrayList<GameList>> games = new MutableLiveData<>();

        CollectionReference colRef = db.collection("list");
        colRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "List GET failed.", error);
                return;
            }
            if (value != null) {
                ArrayList<GameList> temp = new ArrayList<>();
                value.getDocuments().forEach(documentSnapshot -> {
                    long wins = (long)documentSnapshot.get("OpponentWin");
                    long losses =  (long)documentSnapshot.get("OpponentLoss");

                    temp.add(new GameList((String)documentSnapshot.get("Against"), wins,
                            losses, (String)documentSnapshot.get("gameId"), documentSnapshot.getId()));
                });

                games.setValue(temp);

            } else {
                Log.d(TAG, "List data: null");
            }
        });

        return games;
    }

    void deleteGameList(String listId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("list").document(listId);
        docRef.delete();
    }

    void writeGameList(User userData, String gameId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> data = new HashMap<>();
        data.put("Against", userData.name);
        data.put("OpponentWin", userData.wins);
        data.put("OpponentLoss", userData.losses);
        data.put("gameId", gameId);

        CollectionReference collectionReference = db.collection("list");
        collectionReference.add(data);
    }

    void initGameEntry(String uid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> data = new HashMap<>();
        List<Long> State = new ArrayList<>(9);
        for(int i=0;i < 9;i++){
            State.add(i, 0L);
        }

        data.put("State", State);
        data.put("user1", uid);
        data.put("user2", null);
        data.put("winState", false);
        data.put("next",1);
        data.put("reload", false);
//        data.put("legalEnd", false);

        DocumentReference docRef = db.collection("games").document(uid);
        docRef.set(data);
    }

    void deleteGame(String gameId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.delete();
    }

}
