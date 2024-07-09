package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Objects;

public class GameFragment extends Fragment {
  private static final String TAG = "GAME";
  private static final int GRID_X = 3;
  private static final int GRID_Y = 3;

  /**
   * 1 is 'O'
   * 2 is 'X'
   */
  private final ImageButton[] mButtons = new ImageButton[GRID_Y*GRID_X];
  private final HashMap<Long, Integer> conv = new HashMap<>();
  private final HashMap<Boolean, Integer> symbol = new HashMap<>();

  NavController navController;
  NavBackStackEntry navBackStackEntry;
  CommonViewModel model;
  GameViewModel vm;

  private FirebaseAuth.AuthStateListener authListener;
  private FirebaseAuth auth;
  public String user;

  boolean pressed = false;

  long gameType = 0;
  TextView status;
  int win = 4;
  int[][] winPositions = {{0,1,2}, {3,4,5}, {6,7,8},
          {0,3,6}, {1,4,7}, {2,5,8},
          {0,4,8}, {2,4,6}};

  int offline_moves = 0;

  OnBackPressedCallback callback = new OnBackPressedCallback(true) {
    @Override
    public void handleOnBackPressed() {
      Log.d(TAG, "Back pressed");
      if(win == 4 && ((vm.game.user1 != null)|| gameType == 1)) {
        String info = getText(R.string.forfeit_game_dialog_message).toString();
        if(gameType == 2 && vm.game.user2 == null){
          info = getText(R.string.wait_game_dialog_message).toString();
        }
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.confirm)
                .setMessage(info)
                .setPositiveButton(R.string.yes, (d, which) -> ongoingGameExit())
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();
        dialog.show();
      }else{
        finishedGameExit();
      }
    }
  };

  void finishedGameExit(){
    model.updateUserStats(vm.user, win);
    Log.d(TAG, "User 1");

    if(gameType == 2){
      if(vm.game.user2 != null) {
        model.updateUserStats(vm.opponent, -win);
        Log.d(TAG, "User 2");
      }
    }
      Objects.requireNonNull(navController.getPreviousBackStackEntry())
              .getSavedStateHandle().set(getString(R.string.true_delete), vm.gameId);
    navController.popBackStack();
  }

  void ongoingGameExit(){

    if((vm.game.user2 != null && gameType == 2) || gameType == 1) {
      model.updateUserStats(vm.user, -1);
    }
    if (gameType == 2 && vm.game.user2 != null) {
      model.updateUserStats(vm.opponent, 1);
      vm.updateEnd();
        Objects.requireNonNull(navController.getPreviousBackStackEntry())
                .getSavedStateHandle().set(getString(R.string.true_delete), vm.gameId);
    }
    navController.popBackStack();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    vm = new ViewModelProvider(this).get(GameViewModel.class);
    auth = FirebaseAuth.getInstance();
    authListener = firebaseAuth -> {
      FirebaseUser user = firebaseAuth.getCurrentUser();
      if (user != null) {
        Log.d(TAG,"Recovered Login State in "+TAG);
      } else {
        Log.d(TAG,"Logging off in "+TAG);
        if(vm.game != null) {
          if (win == 4 && ((vm.game.user1 != null)|| gameType == 1)) {
            ongoingGameExit();
          }else{
            finishedGameExit();
          }
        }else{
          Log.d(TAG,"game state null");
        }
        if(navController.getCurrentBackStackEntry() == null){
          NavDirections action = GameFragmentDirections.actionNeedAuthGame();
          navController.navigate(action);
        }
        navController.popBackStack(R.id.loginFragment, false, false);
      }
    };

    conv.put(1L,R.drawable.zero);
    conv.put(2L,R.drawable.x);
    conv.put(0L, R.drawable.blank);
    symbol.put(true, 2);
    symbol.put(false, 1);

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    vm.setGameId(args.getGameId());
    gameType = args.getGametypeonline();
    vm.user = args.getCurrentUserId();
    Log.d(TAG, "New game type = " + args.getGametypeonline());
    if(gameType == 1 && vm.game == null){
      vm.initBoard();
    }

    // Handle the back press by adding a confirmation dialog
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_game, container, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar_game);

    status = view.findViewById(R.id.status);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    updateWinState();
    MenuProvider menuProvider = new MenuProvider() {
      @Override
      public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_logout, menu);
      }

      @Override
      public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.menu_logout){
          model.logout();
          Log.d(TAG,"SIGN OUT");
        }
        return false;
      }
    };
    toolbar.addMenuProvider(menuProvider);
    vm.firstTurn = Objects.equals(vm.gameId, vm.user);

    //game State Listener
    if (gameType == 2) {
      vm.getGameState().observe(getViewLifecycleOwner(), game1 -> {
        Log.e(TAG,"listened= "+game1+ " stored="+vm.game);
        if (game1 != null && game1.next != null && game1.State != null) {
          if (vm.game != null && vm.game.user2 == null && game1.user2 != null) {
            Toast.makeText(getContext(), "2nd Player has joined", Toast.LENGTH_SHORT).show();
          }
          if (game1.winState) {
            vm.game = game1;
            win = 2;
            pressed = true;
          }else{
            vm.game = game1;
            checkVictor(vm);
          }
          if(game1.user1 != null && game1.user2!= null){
            if (vm.firstTurn) {
              vm.opponent = game1.user2;
            } else {
              vm.opponent = game1.user1;
            }
          }
          updateButton();
          updateMessage();
        }
//          else {
//            //create force update by changing reload
//          }
      });
    }
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navController = Navigation.findNavController(view);
    navBackStackEntry = navController.getBackStackEntry(R.id.nav_graph);
    model = new ViewModelProvider(navBackStackEntry).get(CommonViewModel.class);

    for (int i = 0; i < 9; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        if(win == 4) {
          if(gameType == 2 && vm.game.user2 == null){
            Toast.makeText(getContext(), "Wait for other player", Toast.LENGTH_SHORT).show();
          }
          else if (!pressed && vm.game != null && gameType == 2 &&
                  ((vm.game.next == 1 && vm.game.user1.equals(vm.user))||(vm.game.next == 2 && vm.game.user2.equals(vm.user)))) {
              Log.d(TAG, "Button " + finalI + " clicked");
              pressed = true;
              if(vm.game.State.get(finalI) == 0){
                vm.game.State.set(finalI, Long.valueOf(symbol.get(vm.firstTurn)));
                vm.updateTurn();
              }else{
                Toast.makeText(getContext(), "Invalid position", Toast.LENGTH_SHORT).show();
                pressed = false;
                return;
              }
              vm.setGameState();
              updateButton();
//              checkVictor();
              Log.d(TAG,"win="+win);
              updateMessage();
              pressed = false;
          } else if (!pressed && vm.game!=null && gameType != 2) {
            pressed = true;
            if(vm.game.State.get(finalI) == 0){
              vm.game.State.set(finalI, Long.valueOf(symbol.get(vm.firstTurn)));
            }else{
              Toast.makeText(getContext(), "Invalid position", Toast.LENGTH_SHORT).show();
              pressed = false;
              return;
            }
            Log.d("vs CPU", String.valueOf(finalI));
            updateButton();
            checkVictor(vm);
            updateMessageWin();
            if(win !=4){
              pressed = false;
              return;
            }
            if (offline_moves == 8) {
              checkVictor(vm);
              updateMessageWin();
              pressed= false;
              return;
            }
            updateTurnMessage(1);
            vm.cpuMove();
            updateButton();
            updateTurnMessage(2);
            offline_moves += 2;
            checkVictor(vm);
            updateMessageWin();
            pressed = false;
          }
        }
      });
    }
  }
  void updateButton(){
    int i;
    for(i = 0;i<9;i++){
        if(vm.game.State.get(i)!=0)
          mButtons[i].setImageResource(conv.get(vm.game.State.get(i)));
    }
  }
  void updateMessage(){
    if(win != 4){
      updateMessageWin();
    }else{
      vm.win = win;
      if((vm.game.next == 1 && vm.game.user1.equals(vm.user))||(vm.game.next == 2 && vm.game.user2.equals(vm.user))){
        vm.message = "Your turn";
      }else{
        vm.message = "Wait for opponent";
      }
    }
    status.setText(vm.message);
  }

  void updateTurnMessage(int s){
    if(s==1){
      vm.message = "Wait for CPU";
    }else{
      vm.message = "Your Turn";
    }
    status.setText(vm.message);
  }

  void updateMessageWin(){
    vm.win = win;
    String display = "";
    if(win == 2){
      vm.message = "You won\nOpponent Left Room";
      display = "Congratulations!!\n Opponent Left";
    } else if(win  == 1){
      vm.message = "You won";
      display = "Congratulations !!";
    }else if(win == -1){
      vm.message = "You lost";
      display = "Sorry :(";
    }else if (win == 0){
      vm.message = "Tie";
      display = "Tie ?\nExpected Better :|";
    }
    status.setText(vm.message);

    if(win != 4 && (vm.game.user2 != null || gameType == 1) && !vm.term){
      vm.term = true;
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
              .setTitle("Game Ended")
              .setMessage(display)
              .setPositiveButton("OK", (d, which) -> finishedGameExit())
              .create();
      dialog.show();
    }

  }
  void updateWinState(){
    win = vm.win;
  }

  void checkVictor(GameViewModel vm){

    // if 1 has won then if vm.game.user2 == user then win = 1 otherwise -1
    // if 2 has won then if vm.game.user1 == user then win = -1 otherwise 1
    for (int i = 0; i < 8; i++) {
      if(vm.game.State.get(winPositions[i][0]) != 0 &&
              Objects.equals(vm.game.State.get(winPositions[i][0]), vm.game.State.get(winPositions[i][1])) &&
              Objects.equals(vm.game.State.get(winPositions[i][1]), vm.game.State.get(winPositions[i][2]))) {
//              vm.updateDelete();
        if(vm.game.State.get(winPositions[i][0]) == 1){
          if(Objects.equals(vm.game.user2, vm.user)){
            win = 1;
          }else{
            win = -1;
          }
          return;
        }else if(vm.game.State.get(winPositions[i][0]) == 2){
          if(Objects.equals(vm.game.user1, vm.user)){
            win = 1;
          }else{
            win = -1;
          }
          return;
        }
      }
    }
    // if none of the above 4 then win = 0
    for(int i=0;i<9;i++)
    {
      if (vm.game.State.get(i) == 0) {
        win = 4;
        return;
      }
    }
    win = 0;
  }

  @Override
  public void onStart() {
    super.onStart();
    auth.addAuthStateListener(authListener);
  }

  @Override
  public void onResume(){
    win = vm.win;
    vm.term = false;
    super.onResume();
    if(gameType == 1){
      updateWinState();
      updateButton();
      updateTurnMessage(2);
//      checkVictor(vm);
      updateMessageWin();
    }
    if(gameType == 2 && vm.game != null){
      updateButton();
//      checkVictor(vm);
      updateMessageWin();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    vm.win = win;
    if (authListener != null) {
      auth.removeAuthStateListener(authListener);
    }
  }

}