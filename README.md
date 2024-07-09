# Tic Tac Toe

This is code for the Tic Tac Toe multiplayer game app.

# Contributors

    Saahir Vaidya   2020B3A71142G   f20201142@goa.bits-pilani.ac.in
    Ashray Kashyap  2020B3A70494G   f20200494@goa.bits-pilani.ac.in

It uses Android Navigation Component, with a single activity and three fragments:

- The DashboardFragment is the home screen. If a user is not logged in, it navigates to the
  LoginFragment.

- The floating button in the dashboard creates a dialog that asks which type of game to create and
  passes that information to the GameFragment (using SafeArgs).

- The GameFragment UI has a 3x3 grid of buttons. They are initialized in the starter code.
  Appropriate listeners and game play logic needs to be provided.

- Pressing the back button in the GameFragment opens a dialog that confirms if the user wants to
  forfeit the game. (See the FIXME comment in code.)

- A "log out" action bar menu is shown on both the dashboard and the game fragments. Clicking it
  should log the user out and show the LoginFragment. This click is handled in the MainActivity.

# Description

1. There are 3 collections
   a.list:
   It contains all games available to be joined with randomised documentID since searching is never needed.
   It will store opponent Information like name, wins and losses and winState.
   b.users:
   It contains the list of all users with documentID as a function of userId (in this case a one-one
   function with no change) since security is not a issue so that search can be faster for retrieving
   user stats which are stored in this
   c.games:
   It contains the list of all games being played. For the purpose of simplicity and quick access, the gameId
   is creating with user's Id
2. modulus of 3 counter localised for each player decides game turn
3. Game prevents any tap before initial loading by a null check for state
4. uid comparison is used to determine initial value of modulus 3 counter and therefore the first move
5. Game creator gets the first move
6. Delete List and Game at end while updating win/loss
7. Log out in game also updates user stats like a game exit

<img height="600" src="https://github.com/csf314-2023/a5-tic-tac-toe-a5-saahir-ashray_/blob/main/app/src/main/res/drawable/s1.jpg" width="300"/> | <img height="600" src="https://github.com/csf314-2023/a5-tic-tac-toe-a5-saahir-ashray_/blob/main/app/src/main/res/drawable/s2.jpg" width="300"/>
<img height="600" src="https://github.com/csf314-2023/a5-tic-tac-toe-a5-saahir-ashray_/blob/main/app/src/main/res/drawable/s3.jpg" width="300"/>
|<img height="600" src="https://github.com/csf314-2023/a5-tic-tac-toe-a5-saahir-ashray_/blob/main/app/src/main/res/drawable/s4.jpg" width="300"/>
<img height="600" src="https://github.com/csf314-2023/a5-tic-tac-toe-a5-saahir-ashray_/blob/main/app/src/main/res/drawable/s5.jpg" width="300"/>
|<img height="600" src="https://github.com/csf314-2023/a5-tic-tac-toe-a5-saahir-ashray_/blob/main/app/src/main/res/drawable/s6.jpg" width="300"/>

# Instructions on How to run the app

1. Enter the correct Email and password to login or type in new credentials to register as new user.
2. The dashboard appears where the wins and losses of the user are displayed.
3. The + button has 2 options to play as a 2-Player or 1-player game.
4. One player game can be played against the Comuputer and 2 player game can be played against another logged in player.

# Accessibility Scanner

Contrast issues in screen fixed by changing themes for both day and night mode
Title size changed

# Testing

Unit tests to check whether game state is win/loss or tie since victory directly detrmines what is shown on screen and
what happens to firebase firestore database
Used %unit tests for various states of the board

# References

https://medium.com/@stevdza-san/new-logcat-in-android-studio-dolphin-is-amazing-cce5ffecb07b
https://firebase.google.com/docs/auth/android/start
https://stackoverflow.com/questions/37859582/how-to-catch-a-firebase-auth-specific-exceptions
https://stackoverflow.com/questions/56113778/how-to-handle-firebase-auth-exceptions-on-flutter
https://stackoverflow.com/questions/45546833/use-multiple-firebase-accounts-in-single-android-app-for-google-analytics
