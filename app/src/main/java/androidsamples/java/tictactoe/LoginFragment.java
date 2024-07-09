package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginFragment extends Fragment {

    NavController navController;
    NavBackStackEntry navBackStackEntry;
    CommonViewModel model;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private boolean reg = false;

    private static final String TAG_LOG = "LOGIN";
    private static final String TAG_REG = "REGISTER";

    private TextView email;
    private TextView password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG_LOG, "Recovered Login State in " + TAG_LOG);

                User user_me = new User(user.getUid(),
                        Objects.requireNonNull(user.getEmail()).substring(0, user.getEmail().indexOf('@')),
                        user.getEmail(), 0L, 0L);

                if (reg) {
                    Log.d(TAG_REG, "REG DATA NEWUSER");
                    model.writeUserData(user_me);
                }

                model.user = user_me;
                loggedNavToDashBoard(user.getUid());
            } else {
                Log.d("AUTH_STATUS", "Currently SIGNED OUT");
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        email = view.findViewById(R.id.edit_email);
        password = view.findViewById(R.id.edit_password);

        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    if (authListener != null) {
                        auth.removeAuthStateListener(authListener);
                    }
                    model.email = email.getText().toString();
                    model.password = password.getText().toString();
                    if (model.email.equals("")) {
                        Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show();
                    }else if(model.password.equals("")){
                        Toast.makeText(getContext(), "Enter password", Toast.LENGTH_SHORT).show();
                    }else{
                        register(model.email, model.password);
                    }
                });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        navBackStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        model = new ViewModelProvider(navBackStackEntry).get(CommonViewModel.class);
        navController.clearBackStack(R.id.dashboardFragment);
        navController.clearBackStack(R.id.gameFragment);
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reflect ViewModel changes whenever returning back
        updateUI();
        model.password="";
        model.email="";
    }

    private void updateUI(){
        if(model.email == null && model.password == null){
            email.setText("");
            password.setText("");
            return;
        }
        if(!email.getText().toString().equals("")){
            model.email = email.getText().toString();
        }
        if(!password.getText().toString().equals("")){
            model.password = password.getText().toString();
        }
        email.setText(model.email);
        password.setText(model.password);
    }

    private void loggedNavToDashBoard(String uid){
        // Enable Logged In features, maybe pass uid
        model.email=null;
        model.password=null;
        LoginFragmentDirections.ActionLoginSuccessful action = LoginFragmentDirections.actionLoginSuccessful(uid);
        navController.navigate(action);
    }

    private void login(String email, String password){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG_LOG, "success");
                        auth.addAuthStateListener(authListener);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG_LOG, "failure", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            Log.d(TAG_LOG, "BAD_FIRE", task.getException());
                            Toast.makeText(getContext(), "Firebase down",
                                    Toast.LENGTH_SHORT).show();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Log.d(TAG_LOG, "BAD_CRED", task.getException());
                            Toast.makeText(getContext(), "Incorrect Password",
                                    Toast.LENGTH_SHORT).show();
                        } catch (FirebaseNetworkException e) {
                            Log.d(TAG_LOG, "NETWORK_ISSUE", task.getException());
                            Toast.makeText(getContext(), "Please check network",
                                    Toast.LENGTH_SHORT).show();
                        } catch (FirebaseAuthException e) {
                            Log.d(TAG_LOG, e.getErrorCode());
                            Toast.makeText(getContext(), "FIREBASE OFFLINE",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d(TAG_LOG, "FATAL_ERROR", task.getException());
                            Toast.makeText(getContext(), "FATAL ERROR",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void register(String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("REGISTER", "success");
                        reg = true;
                        auth.addAuthStateListener(authListener);
                    } else {
                        reg = false;
                        // If sign in fails, display a message to the user.
                        Log.d(TAG_REG, "failure", task.getException());
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG_REG, "BAD_CRED", task.getException());
                            Log.d(TAG_REG, e.getErrorCode());
                            if(e.getErrorCode().equals("ERROR_INVALID_EMAIL")){
                                Toast.makeText(getContext(), "Invalid Email",
                                        Toast.LENGTH_SHORT).show();
                            }else if(e.getErrorCode().equals("ERROR_WEAK_PASSWORD")){
                                Toast.makeText(getContext(), "Weak Password ",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (FirebaseAuthUserCollisionException e) {
//                            Log.d(TAG_REG, "RE-REGISTER_ERROR", task.getException());
                            login(email, password);
                        } catch (FirebaseNetworkException e) {
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG_LOG, "NETWORK_ISSUE", task.getException());
                            Toast.makeText(getContext(), "Please check network",
                                    Toast.LENGTH_SHORT).show();
                        } catch (FirebaseAuthException e) {
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG_REG, e.getErrorCode());
                            Toast.makeText(getContext(), "FIREBASE OFFLINE",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG_REG, "FATAL_ERROR", task.getException());
                            Toast.makeText(getContext(), "FATAL ERROR",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // No options menu in login fragment.
}