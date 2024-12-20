package dataaccess.implementations;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.interfaces.GameDAO;
import exception.DataAccessException;
import model.GameData;
import requests.JoinGameRequest;

import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlGameDAO implements GameDAO {

    // added the throw block to catch in case there are any bugs that come from the initialization
    public MySqlGameDAO() {
        try{
            DatabaseManager.configureDatabase(createStatements);
        } catch (Exception ignored){
            System.out.println(ignored.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO games (whiteusername, blackusername, gamename, chessgame) VALUES (?, ?, ?, ?)";
        var chessgame = new Gson().toJson(new ChessGame());
        return executeUpdate(statement, null, null, gameName, chessgame);
    }

    // Selecting all rows from the db and returning a collection of gamesdatas
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> gamelst = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            String statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        var gameid = rs.getInt("gameid");
                        var whiteusername = rs.getString("whiteusername");
                        var blackusername = rs.getString("blackusername");
                        var gamename = rs.getString("gamename");
                        var chessgame = rs.getString("chessgame");
                        gamelst.add(new GameData(gameid, whiteusername, blackusername, gamename, new Gson().fromJson(chessgame, ChessGame.class)));
                    }
                    return gamelst;
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    // Gets just the gameids from the games table for a list of just ids
    @Override
    public ArrayList<Integer> onlyGames() throws DataAccessException {
        ArrayList<Integer> gameIds = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameid FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gameIds.add(rs.getInt("gameid"));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return gameIds;
    }

    // gets all rows in the games table then makes them into game datas and sticks them in a list
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData gamedat = null;
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameid=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        var gameid = rs.getInt("gameid");
                        var whiteusername = rs.getString("whiteusername");
                        var blackusername = rs.getString("blackusername");
                        var gamename = rs.getString("gamename");
                        var chessgame = rs.getString("chessgame");
                        gamedat = new GameData(gameid, whiteusername, blackusername, gamename, new Gson().fromJson(chessgame, ChessGame.class));
                    }
                    return gamedat;
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    // updates an existing game by adding the username to the correct team and then executing the statment for sql to put in the table
    @Override
    public boolean addPlayer(int gameID, JoinGameRequest.PlayerColor teamColor, String userName) throws DataAccessException {
        GameData game = this.getGame(gameID);
        var statement = "UPDATE games SET whiteusername=?, blackusername=?, gamename=?, chessgame=? WHERE gameid=?";
        var gamejson = new Gson().toJson(game);
        if (teamColor == JoinGameRequest.PlayerColor.WHITE){
            if (game.whiteUsername() == null){
                executeUpdate(statement, userName, game.blackUsername(), game.gameName(), gamejson, gameID);
                return true;
            }
            if (game.whiteUsername().equals(userName)){
                return true;
            }
            else{
                throw new DataAccessException(403, "Error: already taken");
            }
        }
        else{
            if (game.blackUsername() == null){
                executeUpdate(statement, game.whiteUsername(), userName, game.gameName(), gamejson, gameID);
                return true;
            }
            if (game.blackUsername().equals(userName)){
                return true;
            }
            else{
                throw new DataAccessException(403, "Error: already taken");
            }
        }
    }

    @Override
    public boolean removePlayer(int gameID, String userName) throws DataAccessException {
        GameData game = this.getGame(gameID);
        if (game == null) {
            throw new DataAccessException(403, "Error: Game not found");
        }

        String statement = "UPDATE games SET whiteusername=?, blackusername=? WHERE gameid=?";
        String newWhiteUsername = game.whiteUsername();
        String newBlackUsername = game.blackUsername();

        // Check which player to remove
        if (userName.equals(game.whiteUsername())) {
            newWhiteUsername = null;
        } else if (userName.equals(game.blackUsername())) {
            newBlackUsername = null;
        } else {
            throw new DataAccessException(404, "Error: Player not found in the game");
        }

        // Execute the update
        executeUpdate(statement, newWhiteUsername, newBlackUsername, gameID);
        return true;
    }

    public ChessGame updateGame(int gameID, ChessGame newGame) throws DataAccessException {
        try{
            var statement = "UPDATE games SET chessgame=? WHERE gameid=?";
            var gamejson = new Gson().toJson(newGame);
            executeUpdate(statement, gamejson, gameID);
            return getGame(gameID).game();
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public JoinGameRequest.PlayerColor getTeamColor(int gameID, String username) throws DataAccessException {
        GameData game = this.getGame(gameID);
        if (game == null){
            throw new DataAccessException(403, "Error: Game not found");
        }
        if (Objects.equals(getGame(gameID).blackUsername(), username)){
            return JoinGameRequest.PlayerColor.BLACK;
        }
        else if (Objects.equals(getGame(gameID).whiteUsername(), username)){
            return JoinGameRequest.PlayerColor.WHITE;
        }
        return null;
    }

    // The common method that actually implements the statement for the sql
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                setParams(params, ps);
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private void setParams(Object[] params, PreparedStatement ps) throws SQLException {
        for (var i = 0; i < params.length; i++) {
            var param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case null -> ps.setNull(i + 1, NULL);
                default -> {
                }
            }
        }
    }

    // statment is for creating the table if it doesn't already exist
    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS games (
            `gameid` INT NOT NULL AUTO_INCREMENT,
            `whiteusername` VARCHAR(256) DEFAULT NULL,
            `blackusername` VARCHAR(256) DEFAULT NULL,
            `gamename` VARCHAR(256) NOT NULL,
            `chessgame` TEXT DEFAULT NULL,
            PRIMARY KEY (`gameid`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };
}
