package com.shravan.gameofstones.resource;

import static org.junit.Assert.assertThat;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.shravan.gameofstones.core.RestResponse;
import com.shravan.gameofstones.model.Board;
import com.shravan.gameofstones.model.Play;
import com.shravan.gameofstones.model.Play.PlayState;
import com.shravan.gameofstones.model.Player;
import com.shravan.gameofstones.util.JSONFormatter;

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
     * 
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
        player1Id = play.getPlayer1Id();
        player2Id = play.getPlayer2Id();
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
     * 
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

    /**
     * Simple test to validate a player1 move
     * 
     * @throws Exception
     */
    @Test
    public void makeFirstMoveTest() throws Exception {

        //start a play
        playStartTest();
        //make a move in the 1st pit (oth index of the array)
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player1Id, 0);
        assertThat(makeMoveResponse.getResult(), Matchers.notNullValue());
        //validate that the updated play has changed. player1Id has got the chance again
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().valueToTree(makeMoveResponse.getResult());
        assertThat(makeMoveResponseNode.get("isPlayer1sMove").asBoolean(), Matchers.is(true));
        //assert that player1 has made one moves and player2 has none
        Play play = Play.getPlay(playId);
        Player player1 = play.getPlayer1();
        Player player2 = play.getPlayer2();
        assertThat(player1.getMoves(), Matchers.is(1));
        assertThat(player2.getMoves(), Matchers.is(0));
    }

    /**
     * Simple test to validate a player1s second move following his first move
     * to get back the attempt
     * 
     * @throws Exception
     */
    @Test
    public void makeSecondMoveTest() throws Exception {

        //start and make a move
        makeFirstMoveTest();
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player1Id, 1);
        assertThat(makeMoveResponse.getResult(), Matchers.notNullValue());
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().valueToTree(makeMoveResponse.getResult());
        //validate that the play has changed. player1Id has not got the chance again.
        assertThat(makeMoveResponseNode.get("isPlayer1sMove").asBoolean(), Matchers.is(false));
        //but has got all the stones from the opponent player
        Board currentPlayBoard = JSONFormatter.deserialize(makeMoveResponseNode.get("board").toString(), false,
            Board.class);
        assertThat(currentPlayBoard.getPlayer1Pits(), Matchers.contains(1, 7, 8, 8, 8, 8, 2));
        assertThat(currentPlayBoard.getPlayer2Pits(), Matchers.contains(6, 6, 6, 6, 0, 6, 0));
        //assert that player1 has made two moves and player2 has none
        Play play = Play.getPlay(playId);
        Player player1 = play.getPlayer1();
        Player player2 = play.getPlayer2();
        assertThat(player1.getMoves(), Matchers.is(2));
        assertThat(player2.getMoves(), Matchers.is(0));
    }

    /**
     * Simple test to validate a player 2's first move following the player1's
     * first two moves
     * 
     * @throws Exception
     */
    @Test
    public void player2MakeFirstMoveTest() throws Exception {

        //start and make player1 move's as above tests
        makeSecondMoveTest();
        //make player2 move
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player2Id, 5);
        assertThat(makeMoveResponse.getResult(), Matchers.notNullValue());
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().valueToTree(makeMoveResponse.getResult());
        //validate that the play has changed. player1Id has the chance again.
        assertThat(makeMoveResponseNode.get("isPlayer1sMove").asBoolean(), Matchers.is(true));
        //but has got all the stones from the opponent player
        Board currentPlayBoard = JSONFormatter.deserialize(makeMoveResponseNode.get("board").toString(), false,
            Board.class);
        assertThat(currentPlayBoard.getPlayer1Pits(), Matchers.contains(1, 0, 8, 8, 8, 8, 2));
        assertThat(currentPlayBoard.getPlayer2Pits(), Matchers.contains(7, 7, 7, 7, 8, 0, 1));
        //assert that player1 has made two moves and player2 has one
        Play play = Play.getPlay(playId);
        Player player1 = play.getPlayer1();
        Player player2 = play.getPlayer2();
        assertThat(player1.getMoves(), Matchers.is(2));
        assertThat(player2.getMoves(), Matchers.is(1));
    }

    /**
     * Test to validate if Winner of the game is the player who has the most
     * stones in his big pit.
     * 
     * @throws Exception
     */
    @Test
    public void playResetAfterFewMoves() throws Exception {

        //make initial set of moves based on the tests above
        player2MakeFirstMoveTest();
        //abort the game
        RestResponse resetPlayResponse = new PlayResource().resetPlay(playId);

        //validate that the player1 is declared the winner as he has 2 stones against 1 of player2
        assertThat(resetPlayResponse.getResult(), Matchers.notNullValue());
        JsonNode resetPlayNode = JSONFormatter.getMapper().valueToTree(resetPlayResponse.getResult());
        String winnerId = resetPlayNode.get("winnerId").textValue();
        assertThat(winnerId, Matchers.is(player1Id));
        //validate that the game is indeed aborted
        PlayState playState = PlayState.getValue(resetPlayNode.get("playState").textValue());
        assertThat(playState, Matchers.is(PlayState.ABORTED));
    }
}
