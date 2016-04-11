package com.shravan.gameofstones.resource;

import java.util.Arrays;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import com.shravan.gameofstones.core.RestResponse;
import com.shravan.gameofstones.model.Play;
import com.shravan.gameofstones.model.Play.PlayState;
import com.shravan.gameofstones.model.Player;

@Path("play")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayResource {

    @GET
    @Path("ping")
    public String ping() {

        return "pong";
    }

    /**
     * Resource method to fetch an existing {@link Play}
     * 
     * @param playId
     *            A valid playId which has to be fetched
     * @return {@link RestResponse} having the {@link Play#getFullPlayDetails()}
     *         as the result
     */
    @GET
    @Path("{playId}")
    public RestResponse getPlay(@PathParam("playId") String playId) {

        if (playId != null) {
            Play play = Play.getPlay(playId);
            if (play != null) {
                return RestResponse.ok(play.getFullPlayDetails());
            }
            else {
                return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(),
                    String.format("Play for Id: %s not found", playId));
            }
        }
        else {
            return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(), "PlayId cannot be null");
        }
    }

    /**
     * Resource method to add a {@link Player} to a two player game
     * 
     * @param playId
     *            A valid playId to which the given player must be added. If no
     *            Play is fetched for this, creates a new and adds the given
     *            player as the first player.
     * @param player
     *            The player who must be added the play
     * @return {@link RestResponse} having the {@link Play#getFullPlayDetails()}
     *         as the result
     */
    @POST
    @Path("twoPlayer/player")
    public RestResponse playerJoin(@QueryParam("playId") String playId, Player player) {

        Play play = Play.getPlay(playId);
        if (player != null) {
            Play twoPlayerPlay = Play.addPlayerInPlay(play, player);
            return RestResponse.ok(twoPlayerPlay.getFullPlayDetails());
        }
        else {
            return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(),
                "Two player requirement not met. Please given details for both players");
        }
    }

    /**
     * Resource method to start a two player game when both the player details
     * are known.
     * 
     * @param twoPlayerGamePlayload
     *            Map of <"playerIndex", Player>. PlayerIndex can be "1" and "2"
     *            implying 1st and 2nd players
     * @return {@link RestResponse} having the {@link Play#getFullPlayDetails()}
     *         as the result
     */
    @POST
    @Path("twoPlayer/simultaneous")
    public RestResponse startTwoPlayerPlay(Map<String, Player> twoPlayerGamePlayload) {

        if (twoPlayerGamePlayload != null && twoPlayerGamePlayload.size() == 2) {
            Play twoPlayerPlay = Play.startTwoPlayerGame(twoPlayerGamePlayload.get("1"),
                twoPlayerGamePlayload.get("2"));
            return RestResponse.ok(twoPlayerPlay.getFullPlayDetails());
        }
        else {
            return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(),
                "Two player requirement not met. Please given details for both players");
        }
    }

    /**
     * Resets/abords an existing Play, which will be fetched based on its id.
     * 
     * @param playId
     *            Id of the {@link Play} which must be aborted
     * @return {@link RestResponse} having the {@link Play#getFullPlayDetails()}
     *         as the result
     */
    @DELETE
    @Path("reset/{playId}")
    public RestResponse resetPlay(@PathParam("playId") String playId) {

        if (playId != null) {
            Play play = Play.getPlay(playId);
            if (play != null) {
                play.setPlayState(PlayState.ABORTED);
                play.createOrUpdate();
                return RestResponse.ok(play.getFullPlayDetails());
            }
            else {
                return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(),
                    String.format("No Play with id: %s found to reset", playId));
            }
        }
        else {
            return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(), "No PlayId found to reset");
        }
    }

    /**
     * Makes a player move based on the given play, the player and the selected
     * pit index
     * 
     * @param playId
     *            The {@link Play} on which the move is performed
     * @param playerId
     *            The {@link Player} who performs the move
     * @param pitIndex
     *            The index/position of the pit on which the move is performed
     * @return {@link RestResponse} having the {@link Play#getFullPlayDetails()}
     *         as the result
     */
    @POST
    @Path("makeMove/{playId}/{playerId}/{pitIndex}")
    public RestResponse makeMove(@PathParam("playId") String playId, @PathParam("playerId") String playerId,
        @PathParam("pitIndex") Integer pitIndex) {

        if (playId != null) {
            Play play = Play.getPlay(playId);
            if (play != null && PlayState.IN_PROGRESS.equals(play.getPlayState())) {
                if (Arrays.asList(play.getPlayer1Id(), play.getPlayer2Id()).contains(playerId)) {
                    play.makeMove(playerId, pitIndex);
                    return RestResponse.ok(play.getFullPlayDetails());
                }
                else {
                    return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(),
                        String.format("Given Player with id: %s not part of Play with id: %s", playerId, play.getId()));
                }
            }
            else {
                return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(), String.format(
                    "No inprogress Play with id: %s found to make a move. State: %s", playId, play.getPlayState()));
            }
        }
        else {
            return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(), "No PlayId found to make a move");
        }
    }
}
