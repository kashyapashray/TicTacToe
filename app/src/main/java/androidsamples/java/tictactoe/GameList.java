package androidsamples.java.tictactoe;

public class GameList {
    String against;
    long wins;
    long losses;
    String gameId;
    String listId;

    public GameList(String against,long wins,long losses, String gameId, String listId){
        this.against = against;
        this.losses = losses;
        this.wins = wins;
        this.gameId = gameId;
        this.listId = listId;
    }
}
