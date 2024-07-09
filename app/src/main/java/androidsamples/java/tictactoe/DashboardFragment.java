package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

  NavController navController;
  NavBackStackEntry navBackStackEntry;
  NavBackStackEntry navBackStackEntry_DF;
  CommonViewModel model;
  DashBoardFragmentViewModel vm;

  private static final String TAG = "DASHBOARD";

  private FirebaseAuth.AuthStateListener authListener;
  private FirebaseAuth auth;
  private String uid;

  TextView score;

  OpenGamesAdapter adapter;


  final LifecycleEventObserver killGame = (source, event) -> {
    if (event.equals(Lifecycle.Event.ON_RESUME)
            && navBackStackEntry_DF.getSavedStateHandle().contains(getString(R.string.true_delete))) {
      String result = navBackStackEntry_DF.getSavedStateHandle().get(getString(R.string.true_delete));
      Log.d(TAG,"DELETE GAME="+result);
      vm.deleteGame(result);
    }
  };


  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    auth = FirebaseAuth.getInstance();
    authListener = firebaseAuth -> {
      FirebaseUser user = firebaseAuth.getCurrentUser();
      if (user != null) {
        Log.d(TAG,"Recovered Login State in "+TAG);
      } else {
        if(navController.getCurrentBackStackEntry() == null){
          NavDirections action = DashboardFragmentDirections.actionNeedAuth();
          navController.navigate(action);
        }
        navController.popBackStack(R.id.loginFragment, false, false);
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
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
    vm = new ViewModelProvider(this).get(DashBoardFragmentViewModel.class);
    DashboardFragmentArgs args = DashboardFragmentArgs.fromBundle(getArguments());
    uid = args.getUid();

    Toolbar toolbar = view.findViewById(R.id.toolbar_dashboard);
    score = view.findViewById(R.id.txt_score);

    MenuProvider menuProvider = new MenuProvider() {
      @Override
      public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_logout, menu);
      }

      @Override
      public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.menu_logout){
          model.logout();
          Log.d("DASHBOARD","SIGN OUT");
        }
        return false;
      }
    };
    toolbar.addMenuProvider(menuProvider);

    ArrayList<GameList> list = new ArrayList<>();
    adapter = new OpenGamesAdapter(list);
    RecyclerCallBack rc = this::navToExistingGame;
    adapter.setOpenGame(rc);

    RecyclerView recyclerView = view.findViewById(R.id.list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerView.setAdapter(adapter);

    // Receive and update new games
    vm.getAvailableGameList().observe(getViewLifecycleOwner(),gameLists -> adapter.refresh(gameLists));

    // Receive and present user stats
    vm.getUserData(uid).observe(getViewLifecycleOwner(), user -> {
      String t = "Wins:"+user.wins+"\nLosses:"+user.losses;
      Log.d("USER_DATA",uid+" UPDATED");
      score.setText(t);
      vm.user = user;
    });

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navController = Navigation.findNavController(view);
    navBackStackEntry = navController.getBackStackEntry(R.id.nav_graph);
    navBackStackEntry_DF = navController.getBackStackEntry(R.id.dashboardFragment);
    model = new ViewModelProvider(navBackStackEntry).get(CommonViewModel.class);
    navController.clearBackStack(R.id.gameFragment);

    Log.d("onViewCreated_DASH",uid+" CREATE");
    navBackStackEntry_DF.getLifecycle().addObserver(killGame);

    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        vm.deleteGame(uid);
        int gameType = 0;
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = 2;
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = 1;
        }
        Log.d(TAG, "New Game: " + gameType);

        //Generate game DB entry here and assign to both list and game
        if(gameType == 2){
          FirebaseUser user = auth.getCurrentUser();
          assert user != null;
          vm.initGameEntry(uid);
          vm.writeGameList(vm.user, uid);
        }
        // gametype == 1will be dealt with locally

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(uid, uid);
        action.setGametypeonline(gameType);
        navController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  void navToExistingGame(String gameId, String listId){
    if(uid.equals(gameId)){
      DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(gameId,uid);
      action.setGametypeonline(2);
      navController.navigate(action);
    }else{
      model.addSecondPlayer(uid,gameId);
      vm.deleteGameList(listId);
      DashboardFragmentDirections.ActionGame action = DashboardFragmentDirections.actionGame(gameId,uid);
      action.setGametypeonline(2);
      navController.navigate(action);
    }
  }
}