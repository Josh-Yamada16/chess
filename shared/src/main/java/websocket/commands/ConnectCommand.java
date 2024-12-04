package websocket.commands;

public class ConnectCommand extends UserGameCommand{
    private String username;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String username) {
        super(commandType, authToken, gameID);
        this.username = username;
    }
}
