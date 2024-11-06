package chess.piececalculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class RookRule extends BaseMovement{
    public RookRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    public Collection<ChessMove> moves(){
        Collection<ChessMove> moveSet = new ArrayList<ChessMove>();
        moveCalculator(moveSet, 1, 0, true);
        moveCalculator(moveSet, 0, 1, true);
        moveCalculator(moveSet, -1, 0, true);
        moveCalculator(moveSet, 0, -1, true);
        return moveSet;
    }
}
