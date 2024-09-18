package chess.PieceCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class kingCalc implements PieceMovesInterface {
    private ChessBoard board;
    private ChessPosition position;

    public kingCalc(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        int row = position.getRow();
        int column = position.getColumn();
        for (int i = (Math.abs(8-row) + 1); i < (Math.abs(8-row) - 2); i--) {
            for (int j = ((1+column) - 1); j < ((1+column) + 2); j++) {
                ChessPosition possiblePos = new ChessPosition(i, j);
                if (isValid(board, possiblePos)){
                    possibleMoves.add(new ChessMove(position, possiblePos, null));
                }
            }
        }
        return possibleMoves;
    }

    @Override
    public boolean isValid(ChessBoard board, ChessPosition position) {
        // check if it's not going to fall off the board
        if (position.getRow() > 7 || position.getColumn() > 7){
            return false;
        }
        // check if it's not going to move into the same team
//        else if ((board.getPiece(position)).getTeamColor() == ) {
//            return false;
//        }
        // check if it's not going to move into the attack of the opposite team
        return true;
    }
}