package websocket.commands;

import chess.ChessMove;

public class ResignCommand extends UserGameCommand{
    private ChessMove move;

    public ResignCommand(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
        this.move = move;
    }
}
