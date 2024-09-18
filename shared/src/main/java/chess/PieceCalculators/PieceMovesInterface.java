package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface PieceMovesInterface {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);

    boolean isValid(ChessBoard board, ChessPosition position);
}
