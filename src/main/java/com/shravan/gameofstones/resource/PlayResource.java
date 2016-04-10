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

    @GET
    @Path("{playId}")
    public RestResponse getPlay(@PathParam("playId") String playId) throws Exception {

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

    @POST
    @Path("start/twoPlayer")
    public RestResponse startTwoPlayerPlay(Map<String, Player> twoPlayerGamePlayload) throws Exception {

        if (twoPlayerGamePlayload != null && twoPlayerGamePlayload.size() == 2) {
            Play twoPlayerPlay = Play.startTwoPlayerGame(twoPlayerGamePlayload.get("1"),
                twoPlayerGamePlayload.get("2"));
            return RestResponse.ok(twoPlayerPlay.getId());
        }
        else {
            return RestResponse.error(Status.PRECONDITION_FAILED.getStatusCode(),
                "Two player requirement not met. Please given details for both players");
        }
    }

    @DELETE
    @Path("reset/{playId}")
    public RestResponse resetPlay(@PathParam("playId") String playId) {

        if (playId != null) {
            Play play = Play.getPlay(playId);
            if (play != null) {
                play.setPlayState(PlayState.ABORTED);
                play.createOrUpdate();
                return RestResponse.ok(play.getId());
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
