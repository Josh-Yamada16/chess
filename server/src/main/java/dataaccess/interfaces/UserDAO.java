package dataaccess.interfaces;

import model.UserData;

public interface UserDAO {
    void clear();

    void addUser(UserData user);

    UserData getUser(String username);

    boolean matchPassword(UserData user);
}
