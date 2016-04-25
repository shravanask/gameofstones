package com.shravan.gameofstones.socket;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.fasterxml.jackson.core.type.TypeReference;
import com.shravan.gameofstones.model.Play;
import com.shravan.gameofstones.model.Player;
import com.shravan.gameofstones.util.JSONFormatter;

/**
 * Class to open a websocket with the server
 * 
 * @author shravanshetty
 */
@WebSocket
public class PlayWebSocket {

    private static final Logger log = Logger.getLogger(PlayWebSocket.class.getSimpleName());
    private static Map<String, HashSet<Session>> playSessionMap;

    @OnWebSocketMessage
    public void makeMove(Session session, String playPayload) throws Exception {

        log.info(String.format("play received:%s, session: %s", playPayload, session.getLocalAddress()));
        HashMap<String, String> moveDetails = JSONFormatter.deserialize(playPayload, false,
            new TypeReference<HashMap<String, String>>() {
            });
        if (moveDetails != null) {

            String playId = moveDetails.get("playId");
            String playerId = moveDetails.get("playerId");
            Integer pitIndex = moveDetails.get("pitIndex") != null ? Integer.parseInt(moveDetails.get("pitIndex"))
                : null;
            String playerDetails = moveDetails.get("player");
            //fetch the play by id
            Play play = Play.getPlay(playId);
            //make a move
            if (play != null && playerId != null && pitIndex != null) {
                play.makeMove(playerId, pitIndex, session);
            }
            //check if there is a request to add player to game
            else if (playerDetails != null) {
                Player player = JSONFormatter.deserialize(playerDetails, false, Player.class);
                if (player != null) {
                    Play.addPlayerInPlay(play, player, session);
                }
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {

        //fetch playId from session url
        HashMap<String, String> allQueryParameters = getAllQueryParamsFromSession(session);
        if (allQueryParameters.get("playId") != null) {
            HashSet<Session> sessions = playSessionMap.get(allQueryParameters.get("playId"));
            sessions = sessions != null ? sessions : new HashSet<Session>();
            sessions.add(session);
            //update the session with current play details
            Play play = Play.getPlay(allQueryParameters.get("playId"));
            if (play != null) {
                session.getRemote().sendString(JSONFormatter.serialize(play));
            }
        }
        log.info(session.getRemoteAddress().getHostString() + " connected!");
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) throws Exception {

        HashMap<String, String> allQueryParams = getAllQueryParamsFromSession(session);
        if (allQueryParams.get("playId") != null) {
            HashSet<Session> sessions = playSessionMap.get(allQueryParams.get("playId"));
            if (sessions != null) {
                sessions.remove(session);
            }
        }
        log.info(session.getRemoteAddress().getHostString() + " closed!");
    }

    private static HashMap<String, String> getAllQueryParamsFromSession(Session session) throws Exception {

        //fetch playId from session url
        String requestUrl = session.getUpgradeRequest().getRequestURI().toString();
        return getAllQuerParameters(requestUrl);
    }

    /**
     * Returns all the query parameters in the url given.
     * 
     * @return
     * @throws Exception
     */
    private static HashMap<String, String> getAllQuerParameters(String url) throws Exception {

        HashMap<String, String> result = new HashMap<String, String>();
        if (result != null) {
            url = url.replace(" ", URLEncoder.encode(" ", "UTF-8"));
            URIBuilder uriBuilder = new URIBuilder(new URI(url));
            for (NameValuePair nameValue : uriBuilder.getQueryParams()) {
                result.put(nameValue.getName(), nameValue.getValue());
            }
        }
        return result;
    }
}
