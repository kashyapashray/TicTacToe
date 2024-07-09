package androidsamples.java.tictactoe;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

  NavHostFragment navHostFragment;
  NavController navController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    assert navHostFragment != null;
    navController = navHostFragment.getNavController();
    NavBackStackEntry navBackStackEntry = navController.getBackStackEntry(R.id.nav_graph);
    CommonViewModel vm = new ViewModelProvider(navBackStackEntry).get(CommonViewModel.class);

    FirebaseApp.initializeApp(getApplicationContext());
  }
}