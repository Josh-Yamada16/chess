package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand{
    private ChessMove move;
    private String username;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move, String username) {
        super(commandType, authToken, gameID);
        this.move = move;
        this.username = username;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getUsername() {
        return username;
    }
}
