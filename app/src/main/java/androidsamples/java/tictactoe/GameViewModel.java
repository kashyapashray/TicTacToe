package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameViewModel extends ViewModel {

    String gameId;
    Game game = null;
    int win = 4;
    String message = "";
    boolean firstTurn = true;
    String opponent;
    String user;
    boolean term = false;

    private static final String TAG = "GAME_VM";

    public GameViewModel(){}

    void setGameId(String gameId){
        this.gameId = gameId;
    }

    void initBoard(){
        List<Long> State = new ArrayList<>(9);
        for(int i=0;i < 9;i++){
            State.add(i, 0L);
        }
        game = new Game(State, gameId, null, false, 1L, false);
    }

    void updateTurn(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        if(game.next == 1) {
            data.put("next", 2);
        }else{
            data.put("next", 1);
        }
        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.update(data);
    }

    void setGameState(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        data.put("user1",game.user1);
        data.put("user2",game.user2);
        data.put("State",game.State);
        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.update(data);
    }

    void updateEnd(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        data.put("winState",true);
        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.update(data);
    }

//    void updateDelete(){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("legalEnd", true);
//        DocumentReference docRef = db.collection("games").document(gameId);
//        docRef.update(data);
//    }

    LiveData<Game> getGameState(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        MutableLiveData<Game> game= new MutableLiveData<>(null);

        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Game GET failed.", error);
                return;
            }
            if (value != null && value.getData() != null) {
                Log.d(TAG,"Game GET SUCCESS");
//                Log.d(TAG,"value is "+value);
                game.setValue(new Game(
                        (List<Long>) value.get("State"),
                        value.getString("user1"),
                        value.getString("user2"),
                        Boolean.TRUE.equals(value.getBoolean("winState")),
                        value.getLong("next"),
                        value.getBoolean("reload")
                        )
                );
            } else {
                Log.d(TAG, "Game data: null");
                game.setValue(null);
            }
        });
        return game;
    }

    void cpuMove(){
        int action=0;
        for(int i=0;i<9;i++){
            if(game.State.get(i) == 0){
                action = i;
                break;
            }
        }
        game.State.set(action, 1L);
    }

}
