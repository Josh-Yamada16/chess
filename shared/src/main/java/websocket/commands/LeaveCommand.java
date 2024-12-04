package websocket.commands;

import chess.ChessMove;

public class LeaveCommand extends UserGameCommand{
    private ChessMove move;

    public LeaveCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
        this.move = move;
    }
}
