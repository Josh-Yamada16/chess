package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class KnightRule extends BaseMovement{
    public KnightRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    public Collection<ChessMove> moves(){
        Collection<ChessMove> moveSet = new ArrayList<ChessMove>();
        moveCalculator(moveSet, 2, -1, false);
        moveCalculator(moveSet, 2, 1, false);
        moveCalculator(moveSet, 1, 2, false);
        moveCalculator(moveSet, -1, 2, false);
        moveCalculator(moveSet, -2, 1, false);
        moveCalculator(moveSet, -2, -1, false);
        moveCalculator(moveSet, -1, -2, false);
        moveCalculator(moveSet, 1, -2, false);
        return moveSet;
    }
}
