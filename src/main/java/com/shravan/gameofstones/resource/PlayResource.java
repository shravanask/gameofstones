package com.shravan.gameofstones.resource;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import org.bson.types.ObjectId;
import com.shravan.gameofstones.core.RestResponse;
import com.shravan.gameofstones.model.Play;
import com.shravan.gameofstones.model.Play.PlayState;
import com.shravan.gameofstones.model.Player;

@Path("play")
public class PlayResource {

    @GET
    @Path("ping")
    public String ping() {

        return "pong";
    }

    @POST
    @Path("start/twoPlayer")
    public RestResponse startTwoPlayerPlay(Map<Integer, Player> twoPlayerGame) {

        if (twoPlayerGame != null && twoPlayerGame.size() == 2) {
            Play twoPlayerPlay = Play.startTwoPlayerGame(twoPlayerGame.get(1), twoPlayerGame.get(2));
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
            Play play = Play.getPlay(new ObjectId(playId));
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
