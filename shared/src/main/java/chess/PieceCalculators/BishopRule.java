package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class BishopRule extends BaseMovementRule{
    public BishopRule(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        calculateMoves(board, position, 1, -1, moves, true);
        calculateMoves(board, position, 1, 1, moves, true);
        calculateMoves(board, position, -1, 1, moves, true);
        calculateMoves(board, position, -1, -1, moves, true);

        return moves;
    }
}
