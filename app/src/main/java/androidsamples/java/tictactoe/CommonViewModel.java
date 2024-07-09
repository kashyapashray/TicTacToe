package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class CommonViewModel extends ViewModel {
    String email="";
    String password="";
    User user;

    void logout(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
    }

    void writeUserData(User userData){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", userData.name);
        data.put("email", userData.email);
        data.put("wins", userData.wins);
        data.put("losses", userData.losses);

        //Write user data
        DocumentReference docRef = db.collection("users").document(userData.uid);
        docRef.set(data);
    }

    void updateUserStats(String uid, int win){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();

        //fetch user Data
        DocumentReference docRef = db.collection("users").document(uid);
        Task<DocumentSnapshot> docSnapTask = docRef.get();

        docSnapTask.addOnSuccessListener(docSnap -> {
            Log.d("user UPDATE","SUCCESS");
            data.put("name", docSnap.get("name"));
            data.put("email", docSnap.get("email"));
            if(win == 1){
                data.put("wins", (Long) docSnap.get("wins") + 1L);
                data.put("losses", docSnap.get("losses"));
            }else if(win == -1){
                data.put("wins", docSnap.get("wins"));
                data.put("losses", (Long) docSnap.get("losses") + 1L);
            }
            docRef.update(data);
        });
        docSnapTask.addOnFailureListener(e -> Log.e("user GET ERROR", Objects.requireNonNull(e.getMessage())));
    }

    void addSecondPlayer(String uid, String gameId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> data = new HashMap<>();
        data.put("user2",uid);
        DocumentReference docRef = db.collection("games").document(gameId);
        docRef.update(data);
    }

}
