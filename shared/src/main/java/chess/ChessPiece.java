package chess;

import chess.piececalculator.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private PieceType t;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        color = pieceColor;
        t = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return t;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (this.t) {
            case KING:
                KingRule kingMoves = new KingRule(board, myPosition);
                return kingMoves.moves();
            case QUEEN:
                QueenRule queenMoves = new QueenRule(board, myPosition);
                return queenMoves.moves();
            case BISHOP:
                BishopRule bishopMoves = new BishopRule(board, myPosition);
                return bishopMoves.moves();
            case KNIGHT:
                KnightRule knightMoves = new KnightRule(board, myPosition);
                return knightMoves.moves();
            case ROOK:
                RookRule rookMoves = new RookRule(board, myPosition);
                return rookMoves.moves();
            case PAWN:
                PawnRule pawnMoves = new PawnRule(board, myPosition);
                return pawnMoves.moves();
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && t == that.t;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", t=" + t +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, t);
    }
}
