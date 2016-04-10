package com.shravan.gameofstones.resource;

import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import com.shravan.gameofstones.core.RestResponse;
import com.shravan.gameofstones.model.Board;
import com.shravan.gameofstones.model.Play;
import com.shravan.gameofstones.model.Play.PlayState;
import com.shravan.gameofstones.model.Player;

/**
 * Test class to check the {@link PlayResource} resource methods
 * 
 * @author shravanshetty
 */
public class PlayResourceTest extends TestFramework {

    private String player1Id;
    private String player2Id;
    private String playId;

    /**
     * Test is the play can be successfully started. Asserts if the {@link Play}
     * entity is persisted with corresponding details
     * @throws Exception 
     */
    @Test
    public void playStartTest() throws Exception {

        PlayResource playResource = new PlayResource();
        //create a two player game
        Map<String, Player> twoPlayerGame = new HashMap<String, Player>();
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        twoPlayerGame.put("1", player1);
        twoPlayerGame.put("2", player2);
        RestResponse twoPlayerPlayResponse = playResource.startTwoPlayerPlay(twoPlayerGame);

        //assert that a game is created and persisted in the db
        Play play = Play.getPlay(twoPlayerPlayResponse.getResult().toString());
        Assert.assertThat(play, Matchers.notNullValue());
        player1Id = play.getPlayer1();
        player2Id = play.getPlayer2();
        playId = play.getId();
        assertThat(Player.getPlayer(player1Id), Matchers.notNullValue());
        assertThat(Player.getPlayer(player2Id), Matchers.notNullValue());
        
        //assert that the game board is setup with equal stones
        Board board = Board.getBoard(play.getBoardId());
        assertThat(board.getPlayer1Pits(), Matchers.contains(6, 6, 6, 6, 6, 6, 0));
        assertThat(board.getPlayer2Pits(), Matchers.contains(6, 6, 6, 6, 6, 6, 0));
        
        //assert that the game is in progress
        assertThat(play.getPlayState(), Matchers.is(PlayState.IN_PROGRESS));
    }

    /**
     * Simple test to make sure a game is still persists in the db with reset
     * @throws Exception 
     */
    @Test
    public void resetPlayTest() throws Exception {

        //start a play
        playStartTest();
        //reset a play
        new PlayResource().resetPlay(playId);
        //assert that the play exists with ABORTED status
        Play play = Play.getPlay(playId);
        assertThat(play, Matchers.notNullValue());
        assertThat(play.getPlayState(), Matchers.is(PlayState.ABORTED));
    }
}
