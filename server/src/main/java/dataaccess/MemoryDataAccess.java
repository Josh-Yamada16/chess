package dataaccess;

import exception.ResponseException;
import model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, GameData> games = new HashMap<>();
    final private HashMap<String, Integer> auths = new HashMap<>();
    private int gameID = 0;

    public MemoryDataAccess() {
    }
    public int matchUsername(String username) {
        if (users.get(username) != null){
            return 1;
        }
        return 0;
    }

    @Override
    public int matchPassword(UserData user) throws ResponseException {
        if (users.get(user.getUsername()).getPassword().equals(user.getPassword())) {
            return 1;
        }
        return 0;
    }

    public AuthData createAuth(UserData user) {
        AuthData auth = new AuthData(generateToken(), users.get(user.getUsername()).getUsername());
        return auth;
    }



    public AuthData addUser(UserData user) {
        AuthData auth = new AuthData(generateToken(), user.getUsername());
        users.put(user.getUsername(), user);
        return auth;
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    public void addGame(GameData game) {
        games.put(Integer.toString(gameID), game);
        gameID++;
    }

    public void deleteAuth(String UUID) {
        auths.remove(UUID);
    }

    public void deleteAllData() {
        users.clear();
        games.clear();
        auths.clear();
    }


//    @Override
//    public Collection<GameData> listGames() throws ResponseException {
//        return List.of();
//    }
//
//    @Override
//    public UserData getUser(UserData user) throws ResponseException {
//        return null;
//    }
//
//    @Override
//    public void deleteUser(Integer id) throws ResponseException {
//
//    }
//
//    @Override
//    public void deleteAllData() throws ResponseException {
//
//    }
//
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}