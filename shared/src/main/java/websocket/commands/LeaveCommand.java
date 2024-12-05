package websocket.commands;

public class LeaveCommand extends UserGameCommand{
    private String username;

    public LeaveCommand(CommandType commandType, String authToken, Integer gameID, String username) {
        super(commandType, authToken, gameID);
        username = username;
    }

    public String getUsername() {
        return username;
    }
}
