package dataaccess.implementations;

import dataaccess.interfaces.UserDAO;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<String, UserData> users = new HashMap<>();

    public MemoryUserDAO() {
    }

    @Override
    public void clear() {
        users.clear();
    }

    public UserData getUser(String username) {
        if (users.get(username) != null){
            return users.get(username);
        }
        return null;
    }

    public boolean matchPassword(UserData user) {
        String hashedpass = users.get(user.username()).password();
        return BCrypt.checkpw(user.password(), hashedpass);
    }

    public void addUser(UserData user) {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        user = new UserData(user.username(), hashedPassword, user.email());
        users.put(user.username(), user);
    }

    public HashMap<String,UserData> getUserList() {
        return users;
    }
}
