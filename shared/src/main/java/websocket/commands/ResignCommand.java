package websocket.commands;

import chess.ChessMove;

public class ResignCommand extends UserGameCommand{
    private String username;

    public ResignCommand(CommandType commandType, String authToken, Integer gameID, String username) {
        super(commandType, authToken, gameID);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
