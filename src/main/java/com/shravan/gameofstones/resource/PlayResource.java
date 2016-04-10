package com.shravan.gameofstones.resource;

import java.util.Map;
import javax.ws.rs.Consumes;
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
                return RestResponse.ok(play);
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

    @POST
    @Path("reset")
    public RestResponse resetPlay(String playId) {

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
}
