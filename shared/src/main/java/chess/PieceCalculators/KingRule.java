package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class KingRule extends BaseMovement{
    public KingRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    public Collection<ChessMove> moves(){
        Collection<ChessMove> moveSet = new ArrayList<ChessMove>();
        moveCalculator(moveSet, 1, -1, false);
        moveCalculator(moveSet, 1, 0, false);
        moveCalculator(moveSet, 1, 1, false);
        moveCalculator(moveSet, 0, 1, false);
        moveCalculator(moveSet, -1, 1, false);
        moveCalculator(moveSet, -1, 0, false);
        moveCalculator(moveSet, -1, -1, false);
        moveCalculator(moveSet, 0, -1, false);
        return moveSet;
    }
}
