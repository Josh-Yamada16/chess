package chess.piececalculator;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnRule{
    private ChessBoard board;
    private ChessPosition position;

    public PawnRule(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }
    public Collection<ChessMove> moves() {
        Collection<ChessMove> moveSet = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        // white piece
        if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE){
            // in promotion row
            if (position.getRow() == 1 && isValid(new ChessPosition(8-row+1, 1+col), false)){
                promote(moveSet,false, false);
            }
            else if (isValid(new ChessPosition(8-row+1, 1+col), false)){
                moveSet.add(new ChessMove(this.position, new ChessPosition(8-row+1, 1+col), null));
                if (isValid(new ChessPosition(8-row+2, 1+col), false) && row == 6){
                    moveSet.add(new ChessMove(this.position, new ChessPosition(8-row+2, 1+col), null));
                }
            }
            // diag right
            ChessPosition potDiagRight = new ChessPosition(8-row+1, 1+col+1);
            if (isValid(potDiagRight, true)){
                if (position.getRow() == 1){
                    promote(moveSet, true, true);
                }
                else{
                    moveSet.add(new ChessMove(this.position, new ChessPosition(8-row+1, 1+col+1), null));
                }
            }
            // diag left
            ChessPosition potDiagLeft = new ChessPosition(8-row+1, 1+col-1);
            if (isValid(potDiagLeft, true)){
                if (position.getRow() == 1){
                    promote(moveSet, true, false);
                }
                else{
                    moveSet.add(new ChessMove(this.position, new ChessPosition(8-row+1, 1+col-1), null));
                }
            }
        }
        // black piece
        else{
            // in promotion row
            if (position.getRow() == 6 && isValid(new ChessPosition(8-row-1, 1+col), false)){
                promote(moveSet,false, false);
            }
            else if (isValid(new ChessPosition(8-row-1, 1+col), false)){
                moveSet.add(new ChessMove(this.position, new ChessPosition(8-row-1, 1+col), null));
                if (isValid(new ChessPosition(8-row-2, 1+col), false) && row == 1){
                    moveSet.add(new ChessMove(this.position, new ChessPosition(8-row-2, 1+col), null));
                }
            }
            // diag right
            ChessPosition potDiagRight = new ChessPosition(8-row-1, 1+col+1);
            if (isValid(potDiagRight, true)){
                if (position.getRow() == 6){
                    promote(moveSet, true, true);
                }
                else{
                    moveSet.add(new ChessMove(this.position, new ChessPosition(8-row-1, 1+col+1), null));
                }
            }
            // diag left
            ChessPosition potDiagLeft = new ChessPosition(8-row-1, 1+col-1);
            if (isValid(potDiagLeft, true)){
                if (position.getRow() == 6){
                    promote(moveSet, true, false);
                }
                else{
                    moveSet.add(new ChessMove(this.position, new ChessPosition(8-row-1, 1+col-1), null));
                }
            }
        }
        return moveSet;
    }

    public boolean isValid(ChessPosition possiblePos, boolean diagonal){
        if (possiblePos.getRow() > 7 || possiblePos.getColumn() > 7 || possiblePos.getRow() < 0 || possiblePos.getColumn() < 0) {
            return false;
        }
        if (diagonal) {
            if (board.getPiece(possiblePos) == null){
                return false;
            }
            if (board.getPiece(possiblePos).getTeamColor() == board.getPiece(position).getTeamColor()){
                return false;
            }
            return true;
        }
        if (board.getPiece(possiblePos) == null) {
            return true;
        }
        return false;
    }
    public void promote(Collection<ChessMove> moveSet, boolean attacking, boolean right){
        int row = position.getRow();
        int column = position.getColumn();
        ChessPosition end;
        if (attacking){
            if (right){
                if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE){
                    end = new ChessPosition(8-row+1, 1+column+1);
                }
                else{
                    end = new ChessPosition(8-row-1, 1+column+1);
                }
            }
            else{
                if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE){
                    end = new ChessPosition(8-row+1, 1+column-1);
                }
                else{
                    end = new ChessPosition(8-row-1, 1+column-1);
                }
            }
        }
        else{
            if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE){
                end = new ChessPosition(8-row+1, 1+column);
            }
            else{
                end = new ChessPosition(8-row-1, 1+column);
            }
        }
        moveSet.add(new ChessMove(this.position, end, ChessPiece.PieceType.QUEEN));
        moveSet.add(new ChessMove(this.position, end, ChessPiece.PieceType.BISHOP));
        moveSet.add(new ChessMove(this.position, end, ChessPiece.PieceType.ROOK));
        moveSet.add(new ChessMove(this.position, end, ChessPiece.PieceType.KNIGHT));
    }
}
