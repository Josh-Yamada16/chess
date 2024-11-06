package chess.piececalculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class BaseMovement {
    private ChessPosition position;
    private ChessBoard board;
    private boolean blocked = false;

    public BaseMovement(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }
    public void moveCalculator(Collection<ChessMove> moveSet, int rowInc, int colInc, boolean slide) {
        int row = position.getRow();
        int col = position.getColumn();
        int multiplier = 1;
        do {
            ChessPosition possiblePos = new ChessPosition(8-row+(rowInc * multiplier),
                    1+col+(colInc * multiplier));
            if (isValid(possiblePos)){
                moveSet.add(new ChessMove(this.position, possiblePos, null));
                if (board.getPiece(possiblePos) != null) {
                    break;
                }
            }
            multiplier += 1;
        } while (!blocked && slide && 8-row+(rowInc * multiplier) < 9 && 8-row+(rowInc * multiplier) > 0 &&
                1+col+(colInc * multiplier) < 9 && 1+col+(colInc * multiplier) > 0);
        blocked = false;
    }

    public boolean isValid(ChessPosition possiblePos){
        if (possiblePos.getRow() > 7 || possiblePos.getColumn() > 7 || possiblePos.getRow() < 0 ||
                possiblePos.getColumn() < 0){
            return false;
        }
        if (board.getPiece(possiblePos) == null){
            return true;
        }
        if (board.getPiece(possiblePos).getTeamColor() == board.getPiece(this.position).getTeamColor()) {
            blocked = true;
            return false;
        }
        return true;
    }
}
