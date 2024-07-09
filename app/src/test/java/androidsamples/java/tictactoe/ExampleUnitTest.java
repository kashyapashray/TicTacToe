package androidsamples.java.tictactoe;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private Game game;
    int win =4;
    GameViewModel vm;
    GameFragment g;

    @Before
    public void setUp() {
        // Initialize the Game before each test
        List<Long> initialState = Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
        game = new Game(initialState, "user1", "user2", false, 1L, false);
        vm = new GameViewModel();
        vm.game = game;
        g = new GameFragment();
        vm.user = "user1";
        vm.opponent = "user2";
    }

    @Test
    public void testInitialState() {
        // Ensure that the initial state is set correctly
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L), game.State);
        assertEquals("user1", game.user1);
        assertEquals("user2", game.user2);
        assertFalse(game.winState);
        assertEquals(1L, game.next.longValue());
        assertFalse(game.reload);
    }

    @Test
    public void testLoss(){
        vm.game.State = new ArrayList<>(Arrays.asList(
                1L,1L,1L,
                2L,2L,0L,
                2L,0L,0L));
        g.checkVictor(vm);
        win = g.win;
        assertEquals(-1,win);
    }

    @Test
    public void testWin(){
        vm.game.State = new ArrayList<>(Arrays.asList(
                1L, 2L, 1L,
                0L, 2L, 0L,
                0L, 2L, 0L));
        g.checkVictor(vm);
        win = g.win;
        assertEquals(1,win);
    }

    @Test
    public void testTie(){
        vm.game.State = new ArrayList<>(Arrays.asList(  1L, 1L, 2L,
                2L, 2L, 1L,
                1L, 2L, 2L));
        g.checkVictor(vm);
        win = g.win;
        assertEquals(0,win);
    }

    @Test
    public void testOngoing(){
        vm.game.State = new ArrayList<>(Arrays.asList(  1L, 1L, 2L,
                0L, 2L, 0L,
                0L, 0L, 2L));
        g.checkVictor(vm);
        win = g.win;
        assertEquals(4,win);
    }

}