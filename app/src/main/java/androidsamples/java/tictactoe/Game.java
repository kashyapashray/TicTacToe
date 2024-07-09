package androidsamples.java.tictactoe;

import java.util.List;

public class Game {
    List<Long> State;
    String user1;
    String user2;
    boolean winState;
    Long next;
    boolean reload;

    public Game(List<Long> State, String user1, String user2, boolean winState, Long next, boolean reload){
        this.State = State;
        this.user1 = user1;
        this.user2 = user2;
        this.winState = winState;
        this.next = next;
        this.reload = reload;
    }

}
