
package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.implementations.MySqlAuthDAO;
import dataaccess.implementations.MySqlGameDAO;
import exception.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import requests.JoinGameRequest;
import utility.Utility;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private MySqlAuthDAO authDAO = new MySqlAuthDAO();
    private MySqlGameDAO gameDAO = new MySqlGameDAO();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand com = new Gson().fromJson(message, UserGameCommand.class);

        switch (com.getCommandType()) {
            case CONNECT -> connect(new Gson().fromJson(message, ConnectCommand.class), session);
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class), session);
            case LEAVE -> leave(new Gson().fromJson(message, LeaveCommand.class), session);
            case RESIGN -> resign(new Gson().fromJson(message, ResignCommand.class), session);
        }
    }

    private void connect(ConnectCommand com, Session session) throws IOException {
        try{
            connections.add(authDAO.getAuth(com.getAuthToken()).username(), session, com.getGameID());
            JoinGameRequest.PlayerColor team = gameDAO.getTeamColor(com.getGameID(), authDAO.getAuth(com.getAuthToken()).username());
            String message;
            if (team != null) {
                message = String.format("%s joined the game as %s!", authDAO.getAuth(com.getAuthToken()).username(), team.name());
            }
            else{
                message = String.format("%s joined as an observer!", authDAO.getAuth(com.getAuthToken()).username());
            }
            broadcast(message, authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
            connections.sendLoadGame(ServerMessage.ServerMessageType.LOAD_GAME, gameDAO.getGame(com.getGameID()), session);
        } catch (DataAccessException | IOException ex){
            connections.sendLoadGame(ServerMessage.ServerMessageType.ERROR, "ERROR", session);
        }
    }

    private void makeMove(MakeMoveCommand com, Session session) throws IOException {
        try{
            var result = Utility.convertMoveToString(com.getMove());
            JoinGameRequest.PlayerColor team = gameDAO.getTeamColor(com.getGameID(), authDAO.getAuth(com.getAuthToken()).username());
            if (team == null){
                connections.sendLoadGame(ServerMessage.ServerMessageType.ERROR, "ERROR", session);
                return;
            }
            ChessGame.TeamColor opTeam = switch (team){
                case WHITE -> ChessGame.TeamColor.BLACK;
                case BLACK -> ChessGame.TeamColor.WHITE;
            };
            verifyChessMove(com.getMove(), com.getGameID(), team);
            String start = (String) result.getFirst();
            String end = (String) result.getSecond();
            var message = String.format("%s moved %s to %s!", authDAO.getAuth(com.getAuthToken()).username(), start, end);
            broadcast(message, authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
            ChessGame game = gameDAO.getGame(com.getGameID()).game();
            game.makeMove(com.getMove());
            gameDAO.updateGame(com.getGameID(), game);
            switch (game.gameState(game, opTeam)){
                case CHECK -> broadcast(String.format(opTeam.name() + "is in Check"), "", com.getGameID());
                case CHECKMATE -> broadcast(String.format(opTeam.name() + "is in Checkmate"), "", com.getGameID());
                case STALEMATE -> broadcast("The game is in Stalemate", "", com.getGameID());
            }
            connections.loadGameToEveryone(com.getGameID(), game, ServerMessage.ServerMessageType.LOAD_GAME);
        } catch (DataAccessException | IOException | InvalidMoveException ex) {
            connections.sendLoadGame(ServerMessage.ServerMessageType.ERROR, "ERROR", session);
        }
    }

    private void leave(LeaveCommand com, Session session) throws IOException, DataAccessException {
        try{
            connections.remove(authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
            gameDAO.removePlayer(com.getGameID(), authDAO.getAuth(com.getAuthToken()).username());
            var message = String.format("%s left the game", authDAO.getAuth(com.getAuthToken()).username());
            broadcast(message, authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        } catch (DataAccessException | IOException ex) {
            broadcast(ex.getMessage(), authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        }
    }

    private void resign(ResignCommand com, Session session) throws IOException, DataAccessException {
        try{
            ChessGame game = gameDAO.getGame(com.getGameID()).game();
            if (game.getGameState().equals(ChessGame.GameState.RESIGNED)){
                connections.sendLoadGame(ServerMessage.ServerMessageType.ERROR, "ERROR", session);
            }
            var message = String.format("%s resigns the match", authDAO.getAuth(com.getAuthToken()).username());
            JoinGameRequest.PlayerColor team = gameDAO.getTeamColor(com.getGameID(), authDAO.getAuth(com.getAuthToken()).username());
            if (team == null){
                connections.sendLoadGame(ServerMessage.ServerMessageType.ERROR, "ERROR", session);
                return;
            }
            game.setGameState(ChessGame.GameState.RESIGNED);
            gameDAO.updateGame(com.getGameID(), game);
            broadcast(message, "", com.getGameID());
        } catch (DataAccessException | IOException ex) {
            broadcast(ex.getMessage(), authDAO.getAuth(com.getAuthToken()).username(), com.getGameID());
        }
    }

    private void broadcast(String message, String player, Integer gameID) throws IOException {
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(player, notification, gameID);
    }

    private void verifyChessMove(ChessMove move, int gameID, JoinGameRequest.PlayerColor team) throws DataAccessException {
        ChessGame game = gameDAO.getGame(gameID).game();
        if (game.getGameState().equals(ChessGame.GameState.RESIGNED)){
            throw new DataAccessException(500, "ERROR: No more move available.");
        }
        if (!game.moveInSet(move, game.validMoves(move.getStartPosition()))){
            throw new DataAccessException(500, "ERROR: Move not in moveset.");
        }
        if (!game.getTeamTurn().name().equals(team.name())) {
            throw new DataAccessException(500, "ERROR: Not team's turn.");
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK) || game.isInCheckmate(ChessGame.TeamColor.WHITE)
                || game.isInStalemate(ChessGame.TeamColor.BLACK) || game.isInStalemate(ChessGame.TeamColor.WHITE)){
            throw new DataAccessException(500, "ERROR: No more move available.");
        }
    }
}
