package com.shravan.gameofstones.resource;

import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
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

    private static Logger log = Logger.getLogger(PlayResourceTest.class.getSimpleName());
    private String player1Id;
    private String player2Id;
    private String playId;

    /**
     * Test is the play can be successfully started. Assertsx if the {@link Play}
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
        Board board = play.getBoard();
        assertThat(board.getPlayer1Moves(), Matchers.is(1));
        assertThat(board.getPlayer2Moves(), Matchers.is(0));
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
        Board board = play.getBoard();
        assertThat(board.getPlayer1Moves(), Matchers.is(2));
        assertThat(board.getPlayer2Moves(), Matchers.is(0));
        //check the scores of the play
        assertThat(board.getPlayer1Score(), Matchers.is(2));
        assertThat(board.getPlayer2Score(), Matchers.is(0));
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
        Board board = play.getBoard();
        assertThat(board.getPlayer1Moves(), Matchers.is(2));
        assertThat(board.getPlayer2Moves(), Matchers.is(1));
        //check the scores of the play
        assertThat(board.getPlayer1Score(), Matchers.is(2));
        assertThat(board.getPlayer2Score(), Matchers.is(1));
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
        //fetch the board
        Board board = JSONFormatter.convert(makeMoveResponseNode.get("board"), Board.class);
        //check the scores of the play
        assertThat(board.getPlayer1Score(), Matchers.is(2));
        assertThat(board.getPlayer2Score(), Matchers.is(1));
    }

    /**
     * Simple test to see if a sequence of automated moves for 10secs will end
     * the game when all the moves are performed on the pit having the highest
     * stones
     * 
     * @throws Exception
     */
    @Test
    public void automatedHighestStoneMoveTest() throws Exception {

        automatedPlayerMovesTest(true, 10000);
    }

    /**
     * Simple test to see if a sequence of automated moves for 10secs will end
     * the game when all the moves are performed on the pit having the lowest
     * stones
     * 
     * @throws Exception
     */
    @Test
    public void automatedLowestStoneMoveTest() throws Exception {

        automatedPlayerMovesTest(false, 10000);
    }

    /**
     * Simple test to see if a sequence of automated moves for 15secs will end
     * the game when all the moves are performed on a randomly selected pit
     * 
     * @throws Exception
     */
    @Test
    public void automatedRandomIndexMoveTest() throws Exception {

        automatedPlayerMovesTest(null, 15000);
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

        //based on who is playing next, move all but one stone to big pit
        List<Integer> playerPits = null;
        if (play.isPlayer1sMove()) {
            playerPits = board.getPlayer1Pits();
        }
        else {
            playerPits = board.getPlayer2Pits();
        }
        //move all but one stone to the big pit
        Integer pitStoneSum = 0;
        for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
            pitStoneSum += playerPits.get(pitIndex);
            playerPits.set(pitIndex, 0);
        }
        //keep one stone in the small pit 
        playerPits.set(6, pitStoneSum + playerPits.get(6) - 1);
        playerPits.set(5, 1);

        //update the board
        board.createOrUpdate();

        //make a move in the last small pit
        new PlayResource().makeMove(playId, play.isPlayer1sMove() ? player1Id : player2Id, 5);

        //validate that the play is updated with completed status and leaderId as player1
        play = Play.getPlay(playId);
        assertThat(play.getPlayState(), Matchers.is(PlayState.COMPLETED));
    }

    /**
     * A play that is completed must be not switched to aborted
     * 
     * @throws Exception
     */
    @Test
    public void abortCompletedPlayTest() throws Exception {

        //start a play and complete it
        completedPlayTest();
        RestResponse resetPlay = new PlayResource().resetPlay(playId);
        assertThat(resetPlay.getCode(), Matchers.is(Status.NOT_ACCEPTABLE.getStatusCode()));
        assertThat(resetPlay.getResult(), Matchers.nullValue());
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

    //private methods

    /**
     * Run random moves for the given timefram, to see if there is a winner.
     * When there is, see if the data is okay
     * 
     * @param pickHighestPitIndex
     *            If true, the move will be performed on the the pit having the
     *            highest stones. If false, move will be the pit having the
     *            least stones. If null, a random index 0...5 will be chosen
     * @param timeframeForTest
     *            The timerange in milliseconds until which the moves must be
     *            performed. If its greater than 20secs, it will be defaulted to
     *            it.
     * @throws Exception
     */
    private void automatedPlayerMovesTest(Boolean pickHighestPitIndex, long timeframeForTest) throws Exception {

        //reset timeframe for test to a max of 20secs
        timeframeForTest = timeframeForTest > 20000 ? 20000 : timeframeForTest;

        //pick up from where it is left off. 
        player1MakesSingleStoneMoveTest();
        //load the game to see whose play its next
        Play play = Play.getPlay(playId);
        long startTimestamp = System.currentTimeMillis();
        Board board = play.getBoard();
        Integer player1Moves = board.getPlayer1Moves();
        Integer player2Moves = board.getPlayer2Moves();
        while (System.currentTimeMillis() - startTimestamp > 0) {
            //make approprepriate move based on who has to play
            board = play.getBoard();
            if (play.isPlayer1sMove()) {
                play = makeStonePitMove(pickHighestPitIndex, playId, player1Id, board.getPlayer1Pits());
                player1Moves++;
            }
            else {
                play = makeStonePitMove(pickHighestPitIndex, playId, player2Id, board.getPlayer2Pits());
                player2Moves++;
            }
            //check if the play state, break if its completed
            if (PlayState.COMPLETED.equals(play.getPlayState())) {
                break;
            }
        }
        //assert game statistics
        board = play.getBoard();
        if (PlayState.COMPLETED.equals(play.getPlayState())) {
            //check that all stones are moved to big pit
            for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
                assertThat(board.getPlayer1Pits().get(pitIndex), Matchers.is(0));
                assertThat(board.getPlayer2Pits().get(pitIndex), Matchers.is(0));
            }
            //check that sum of all the stones in the big pits are equal to sum of all the stones in the board
            assertThat(board.getPlayer1Pits().get(6) + board.getPlayer2Pits().get(6), Matchers.is(6 * 6 * 2));
        }
        else {
            //check that all stones are not moved to big pit
            Integer player1SmallPitSum = 0;
            Integer player2SmallPitSum = 0;
            for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
                player1SmallPitSum += board.getPlayer1Pits().get(pitIndex);
                player2SmallPitSum += board.getPlayer2Pits().get(pitIndex);
            }
            assertThat(player1SmallPitSum, Matchers.greaterThan(0));
            assertThat(player2SmallPitSum, Matchers.greaterThan(0));
            //check that sum of all the stones in all the pits are equal to sum of all the stones in the board
            assertThat((player1SmallPitSum + board.getPlayer1Pits().get(6)) +
                (player2SmallPitSum + board.getPlayer2Pits().get(6)), Matchers.is(6 * 6 * 2));
        }
        //check that stones are in the big pit either cases, if the game is compeleted or not
        assertThat(board.getPlayer1Pits().get(6), Matchers.not(0));
        assertThat(board.getPlayer2Pits().get(6), Matchers.not(0));

        //check if leader is updated either case, if the game is completed or not
        if (board.getPlayer1Pits().get(6) > board.getPlayer2Pits().get(6)) {
            assertThat(play.getLeaderId(), Matchers.is(player1Id));
            //check player1 score is greater than player2
            assertThat(board.getPlayer1Score(), Matchers.greaterThan(board.getPlayer2Score()));
        }
        else if (board.getPlayer1Pits().get(6) < board.getPlayer2Pits().get(6)) {
            assertThat(play.getLeaderId(), Matchers.is(player2Id));
            //check player2 score is greater than player1
            assertThat(board.getPlayer2Score(), Matchers.greaterThan(board.getPlayer1Score()));
        }
        else {
            assertThat(play.getLeaderId(), Matchers.nullValue());
            //check player1 score is same as player2
            assertThat(board.getPlayer1Score(), Matchers.is(board.getPlayer2Score()));
        }
        assertThat(board.getPlayer1Moves(), Matchers.is(player1Moves));
        log.info(String.format("%s moves performed by Player1. Score: %s. Pit: %s", board.getPlayer1Moves(),
            board.getPlayer1Score(), JSONFormatter.serialize(board.getPlayer1Pits())));
        assertThat(board.getPlayer2Moves(), Matchers.is(player2Moves));
        log.info(String.format("%s moves performed by Player2. Score: %s. Pit: %s", board.getPlayer2Moves(),
            board.getPlayer2Score(), JSONFormatter.serialize(board.getPlayer2Pits())));
    }

    /**
     * Make a move based on the given parameters
     * 
     * @param pickHighest
     *            If true, make the move from the pit having the highest number
     *            of stones
     * @param playId
     *            playId in play
     * @param playerId
     *            playerId of the player performing this move
     * @param playerPits
     *            The current pit of the given player making the move
     * @return
     */
    private static Play makeStonePitMove(Boolean pickHighest, String playId, String playerId,
        List<Integer> playerPits) {

        Integer selectedPitIndex = 0;
        //pick a random number between 0...5
        if (pickHighest == null) {
            selectedPitIndex = new Random().nextInt(6);
        }
        else {
            //pick the pit with the highest stones
            if (pickHighest) {
                Integer previousPitStoneCount = Integer.MIN_VALUE;
                for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
                    if (playerPits.get(pitIndex) > previousPitStoneCount) {
                        previousPitStoneCount = playerPits.get(pitIndex);
                        selectedPitIndex = pitIndex;
                    }
                }
            }
            //pick the pit with the least pit
            else {
                Integer previousPitStoneCount = Integer.MAX_VALUE;
                for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
                    if (playerPits.get(pitIndex) < previousPitStoneCount) {
                        previousPitStoneCount = playerPits.get(pitIndex);
                        selectedPitIndex = pitIndex;
                    }
                }
            }
        }
        //if the selected pit has zero stones, randomize it anyways
        while (playerPits.get(selectedPitIndex) == 0) {
            selectedPitIndex = new Random().nextInt(6);
        }
        new PlayResource().makeMove(playId, playerId, selectedPitIndex);
        return Play.getPlay(playId);
    }
}
