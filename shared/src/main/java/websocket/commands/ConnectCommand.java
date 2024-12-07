package websocket.commands;

import requests.JoinGameRequest;

public class ConnectCommand extends UserGameCommand{
    private String username;
    private JoinGameRequest.PlayerColor team;
    private Integer gameID;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String username, JoinGameRequest.PlayerColor team) {
        super(commandType, authToken, gameID);
        this.username = username;
        this.team = team;
        this.gameID = gameID;
    }

    public String getUsername() {
        return username;
    }

    public Integer getGameID(){
        return gameID;
    }

    public String getMessage(){
        return username + "joined as team" + team.name();
    }

    public String getColor(){
        return team.name();
    }
}
