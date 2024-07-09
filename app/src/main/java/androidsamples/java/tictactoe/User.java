package androidsamples.java.tictactoe;

public class User {
    String uid;
    String name;
    String email;
    long losses;
    long wins;
    public User(String uid, String name, String email, Long wins, Long losses){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.wins = wins;
        this.losses = losses;
    }

    private User(){
        uid = "NULL";
        uid = "NULL";
        uid = "NULL";
        losses = -99;
        wins = -99;
    }

    public static User forError(){
        return new User();
    }

}
