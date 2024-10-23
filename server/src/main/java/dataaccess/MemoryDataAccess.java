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
    public boolean matchUsername(String username) {
        if (users.get(username) != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean matchPassword(UserData user) {
        if (users.get(user.username()).password().equals(user.password())) {
            return true;
        }
        return false;
    }

    public AuthData createAuth(UserData user) {
        AuthData auth = new AuthData(generateToken(), users.get(user.username()).username());
        return auth;
    }

    public void addUser(UserData user) {
        users.put(user.username(), user);
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