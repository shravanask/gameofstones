package com.shravan.gameofstones.resource;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import com.shravan.gameofstones.core.RestResponse;
import com.shravan.gameofstones.model.Play;
import com.shravan.gameofstones.model.Player;

/**
 * Test class to check the {@link PlayResource} resource methods
 * 
 * @author shravanshetty
 */
public class PlayResourceTest extends TestFramework {

    /**
     * Test is the play can be successfully started. Asserts if the {@link Play}
     * entity is persisted with corresponding details
     */
    @Test
    public void playStartTest() {

        PlayResource playResource = new PlayResource();
        //create a two player game
        Map<Integer, Player> twoPlayerGame = new HashMap<Integer, Player>();
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        twoPlayerGame.put(1, player1);
        twoPlayerGame.put(2, player2);
        RestResponse twoPlayerPlayResponse = playResource.startTwoPlayerPlay(twoPlayerGame);

        //assert that a game is created and persisted in the db
        Play play = Play.getPlay(new ObjectId(twoPlayerPlayResponse.getResult().toString()));
        Assert.assertThat(play, Matchers.notNullValue());
    }
}
