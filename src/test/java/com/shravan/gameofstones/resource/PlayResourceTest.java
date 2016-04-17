package com.shravan.gameofstones.resource;

import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
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
        assertThat(twoPlayerPlayResponse.getResult(), Matchers.notNullValue());
        JsonNode twoPlayerPlayResponseNode = JSONFormatter.getMapper()
                                                          .readTree(twoPlayerPlayResponse.getResult().toString());
        playId = twoPlayerPlayResponseNode.get("id").asText();
        Play play = Play.getPlay(playId);
        Assert.assertThat(play, Matchers.notNullValue());
        player1Id = play.getPlayer1Id();
        player2Id = play.getPlayer2Id();
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
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().readTree(makeMoveResponse.getResult().toString());
        assertThat(makeMoveResponseNode.get("isPlayer1sMove").asBoolean(), Matchers.is(true));
        //assert that player1 has made one moves and player2 has none
        Play play = Play.getPlay(playId);
        Player player1 = play.getPlayer1();
        Player player2 = play.getPlayer2();
        assertThat(player1.getMoves(), Matchers.is(1));
        assertThat(player2.getMoves(), Matchers.is(0));
    }

    /**
     * Simple test to validate if a player2 can make a move when its actually
     * player1s move
     * 
     * @throws Exception
     */
    @Test
    public void makeInvalidPlayerMoveTest() throws Exception {

        //start a play
        playStartTest();
        //make a move in the 1st pit (oth index of the array)
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player2Id, 0);
        assertThat(makeMoveResponse.getResult(), Matchers.nullValue());
        assertThat(makeMoveResponse.getCode(), Matchers.is(Status.NOT_ACCEPTABLE.getStatusCode()));
    }

    /**
     * Simple test to validate if an unauthorized player move is not allowd
     * 
     * @throws Exception
     */
    @Test
    public void makeUnauthorizedPlayerIdMoveTest() throws Exception {

        //start a play
        playStartTest();
        //make a move in the 1st pit (oth index of the array)
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, UUID.randomUUID().toString(), 0);
        assertThat(makeMoveResponse.getResult(), Matchers.nullValue());
        assertThat(makeMoveResponse.getCode(), Matchers.is(Status.FORBIDDEN.getStatusCode()));
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
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().readTree(makeMoveResponse.getResult().toString());
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
        //check the scores of the play
        assertThat(player1.getScore(), Matchers.is(2));
        assertThat(player2.getScore(), Matchers.is(0));
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
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().readTree(makeMoveResponse.getResult().toString());
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
        //check the scores of the play
        assertThat(player1.getScore(), Matchers.is(2));
        assertThat(player2.getScore(), Matchers.is(1));
    }

    /**
     * Continuing in the above flow of play, test if the player1's move of
     * moving a single stone to an empty right pit is a valid one.
     * 
     * @throws Exception
     */
    @Test
    public void player1MakesSingleStoneMoveTest() throws Exception {

        //make 3rounds of the game. Player1 has the chance again
        player2MakeFirstMoveTest();
        //make player1 move
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player1Id, 0);
        assertThat(makeMoveResponse.getResult(), Matchers.notNullValue());
        JsonNode makeMoveResponseNode = JSONFormatter.getMapper().readTree(makeMoveResponse.getResult().toString());
        //validate that the play has changed. player2 has the chance again.
        assertThat(makeMoveResponseNode.get("isPlayer1sMove").asBoolean(), Matchers.is(false));
        //but has got all the stones from the opponent player
        Board currentPlayBoard = JSONFormatter.deserialize(makeMoveResponseNode.get("board").toString(), false,
            Board.class);
        assertThat(currentPlayBoard.getPlayer1Pits(), Matchers.contains(0, 9, 8, 8, 8, 8, 2));
        assertThat(currentPlayBoard.getPlayer2Pits(), Matchers.contains(7, 7, 7, 7, 0, 0, 1));
        //fetch the players
        Player player1 = JSONFormatter.convert(makeMoveResponseNode.get("player1"), Player.class);
        Player player2 = JSONFormatter.convert(makeMoveResponseNode.get("player2"), Player.class);
        //check the scores of the play
        assertThat(player1.getScore(), Matchers.is(2));
        assertThat(player2.getScore(), Matchers.is(1));
    }

    /**
     * Simple test to see if a player trying to make a move from a pit having
     * zero stones is not allowed.
     * 
     * @throws Exception
     */
    @Test
    public void playerMakesZeroStoneMoveTest() throws Exception {

        //make few rounds of the game. Call earlier sequence of plays
        player1MakesSingleStoneMoveTest();
        //make player2 try to attempt make a move at 5th pit (4th index)
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player2Id, 4);
        assertThat(makeMoveResponse.getResult(), Matchers.nullValue());
        assertThat(makeMoveResponse.getCode(), Matchers.is(Status.BAD_REQUEST.getStatusCode()));
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
        JsonNode resetPlayNode = JSONFormatter.getMapper().readTree(resetPlayResponse.getResult().toString());
        String leaderId = resetPlayNode.get("leaderId").textValue();
        assertThat(leaderId, Matchers.is(player1Id));
        //validate that the game is indeed aborted
        PlayState playState = PlayState.getValue(resetPlayNode.get("playState").textValue());
        assertThat(playState, Matchers.is(PlayState.ABORTED));
    }

    /**
     * Mock a completed play to see if its details are updated with completed
     * status
     * 
     * @throws Exception
     */
    @Test
    public void completedPlayTest() throws Exception {

        //make initial set of moves based on the tests above
        player2MakeFirstMoveTest();

        //get the board linked to this play
        Play play = Play.getPlay(playId);
        assertThat(play.getPlayState(), Matchers.is(PlayState.IN_PROGRESS));
        Board board = play.getBoard();

        //update player2 with no stones left in the small pits
        List<Integer> player2Pits = board.getPlayer2Pits();
        for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
            player2Pits.set(pitIndex, 0);
        }
        //update the board
        board.createOrUpdate();

        //refetch the play with full details 
        //validate that the play is updated with completed status and leaderId as player1
        play = Play.getPlay(playId);
        assertThat(play.getPlayState(), Matchers.is(PlayState.COMPLETED));
        assertThat(play.getLeaderId(), Matchers.is(player1Id));
    }

    /**
     * Simple test to see if the play is in {@link PlayState#WAITING} state when
     * only the first player joins in a 2 player game
     * 
     * @throws Exception
     */
    @Test
    public void twoPlayerGameFirstJoinsTest() throws Exception {

        //setup a game with one player
        RestResponse playWithOnePlayerResponse = new PlayResource().playerJoin(null, new Player("Player1"));
        assertThat(playWithOnePlayerResponse.getResult(), Matchers.notNullValue());
        JsonNode playWithOnePlayerResponseNode = JSONFormatter.getMapper().readTree(
            playWithOnePlayerResponse.getResult().toString());
        playId = playWithOnePlayerResponseNode.get("id").asText();
        player1Id = playWithOnePlayerResponseNode.get("player1Id").asText();

        //validate that the playState is WAITING for the second user
        assertThat(PlayState.getValue(playWithOnePlayerResponseNode.get("playState").asText()),
            Matchers.is(PlayState.WAITING));
        //validate that the updated play has changed. player1Id has got the chance to play
        assertThat(playWithOnePlayerResponseNode.get("isPlayer1sMove").asBoolean(), Matchers.is(true));
        //make sure that trying to make a move gives a PRECONDITION failure error code
        RestResponse makeMoveWhileWaiting = new PlayResource().makeMove(playId, player1Id, 0);
        assertThat(makeMoveWhileWaiting.getCode(), Matchers.is(Status.PRECONDITION_FAILED.getStatusCode()));
    }

    /**
     * Simple test to see if the play is in {@link PlayState#IN_PROGRESS} state
     * when both players have joined
     * 
     * @throws IOException
     */
    @Test
    public void twoPlayerGameSecondJoinsTest() throws Exception {

        //setup the play and make first player join
        twoPlayerGameFirstJoinsTest();
        //make player 2 join the game
        RestResponse playWithTwoPlayerResponse = new PlayResource().playerJoin(playId, new Player("Player2"));
        assertThat(playWithTwoPlayerResponse.getResult(), Matchers.notNullValue());
        JsonNode playWithTwoPlayerResponseNode = JSONFormatter.getMapper().readTree(
            playWithTwoPlayerResponse.getResult().toString());
        //make sure the second player joins the same game
        assertThat(playWithTwoPlayerResponseNode.get("id").asText(), Matchers.is(playId));
        player1Id = playWithTwoPlayerResponseNode.get("player1Id").asText();
        player2Id = playWithTwoPlayerResponseNode.get("player2Id").asText();

        //validate that the playState is WAITING for the second user
        assertThat(PlayState.getValue(playWithTwoPlayerResponseNode.get("playState").asText()),
            Matchers.is(PlayState.IN_PROGRESS));

        //make sure that trying to make a move gives a 200 OK code
        RestResponse makeMoveResponse = new PlayResource().makeMove(playId, player1Id, 0);
        assertThat(makeMoveResponse.getCode(), Matchers.is(Status.OK.getStatusCode()));
    }
}
