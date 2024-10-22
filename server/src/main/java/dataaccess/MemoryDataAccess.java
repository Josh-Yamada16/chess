package dataaccess;

import exception.ResponseException;
import model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();

    public UserData addUser(UserData user) {
        user = new UserData(user.getUsername(), user.getEmail(), user.getPassword());

        users.put(generateToken(), user);
        return user;
    }

    public Collection<GameData> listGames() {
        return pets.values();
    }


    public UserData getUser(int id) {
        return pets.get(id);
    }

    public void deletePet(Integer id) {
        pets.remove(id);
    }

    public void deleteAllPets() {
        pets.clear();
    }

    @Override
    public UserData addUser(UserData user) throws ResponseException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws ResponseException {
        return List.of();
    }

    @Override
    public UserData getUser(int id) throws ResponseException {
        return null;
    }

    @Override
    public void deleteUser(Integer id) throws ResponseException {

    }

    @Override
    public void deleteAllData() throws ResponseException {

    }

    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}