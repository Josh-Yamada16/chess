package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.List;

public class BaseMovementRule{
    private ChessBoard board;
    private ChessPosition position;
    private boolean pathBlocked = false;

    public BaseMovementRule(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int rowInc, int colInc, Collection<ChessMove> moves, boolean allowDistance) {
        int row = pos.getRow();
        int column = pos.getColumn();
        int multiplier = 1;
        do {
            ChessPosition possiblePos = new ChessPosition(8-row+(rowInc * multiplier), 1+column+(colInc * multiplier));
            if (isValid(board, possiblePos, pos)){
                moves.add(new ChessMove(pos, possiblePos, null));
                if (board.getPiece(possiblePos) != null){
                    break;
                }
            }
            multiplier += 1;
        } while (!pathBlocked && allowDistance && 8-row+(rowInc * multiplier) <= 8 && 1+column+(colInc * multiplier) <= 8 && 8-row+(rowInc * multiplier) >= 1 && 1+column+(colInc * multiplier) >= 1);
        pathBlocked = false;
    }

    public boolean isValid(ChessBoard board, ChessPosition position, ChessPosition ogPosition) {
        // check if it's not going to fall off the board
        if (position.getRow() > 7 || position.getColumn() > 7 || position.getRow() < 0 || position.getColumn() < 0){
            return false;
        }
        if ((board.getPiece(position)) == null) {
            return true;
        }
        // check if it's not going to move into the same team
        if ((board.getPiece(position)).getTeamColor() == board.getPiece(ogPosition).getTeamColor()) {
            pathBlocked = true;
            return false;
        }
        return true;
    }
}