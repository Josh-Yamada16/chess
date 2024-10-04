package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private int round = 0;
    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    public ChessGame() {
        board.resetBoard();
    }
    // Copy constructor for shallow copy
    public ChessGame(ChessGame other) {
        round = other.round;
        teamTurn = other.teamTurn; // Copy primitive or immutable fields
        board = other.board;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }


    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // check if startposition is a null
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        // get moveset of the piece in question
        var moveSet = piece.pieceMoves(board, startPosition);
        // iterate through moves to see if they are valid
        for (ChessMove move : moveSet) {
            // create copy instance of the game so that nothing saves to the real game
            ChessGame testGame = new ChessGame(this);
            testGame.board.addPiece(move.getEndPosition(), piece);
            testGame.board.removePiece(move.getStartPosition());
            // if that move puts their king in check in the game copy then remove the move from the moveset
            if (testGame.isInCheck(piece.getTeamColor())) {
                moveSet.remove(move);
            }
        }
        return moveSet;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // check if move is in the collection from validmoves or it is that team's turn otherwise throws exception
//        throw new InvalidMoveException();
        this.round++;
    }

    public boolean kingGettingAttacked(TeamColor team, ChessBoard testBoard){
        ChessPosition kingPos = board.getKingPos(team);
        if (testBoard != null) {
            kingPos = testBoard.getKingPos(team);
        }
        // loop through every piece on the board and find the opposite teams pieces
        for (int i = 0; i < 7; i++){
            for (int j = 0; j < 7; j++){
                ChessPiece queryPiece = board.getPiece(new ChessPosition(8-i, j+1));
                if (queryPiece.getTeamColor() != team){
                    // loop through their moveset and see if the king's position is in their getEndposition
                    var moveSet = queryPiece.pieceMoves(board, new ChessPosition(8-i, j+1));
                    for (ChessMove item : moveSet) {
                        // if getEndPosition() is == to king position then return true
                        if (item.getEndPosition() == kingPos){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // if kingGettingAttacked() then return true
        return kingGettingAttacked(teamColor, null);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // if isInCheck() and validMoves() is empty for all pieces then return true
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // if isInCheck() is false and validMoves() is empty for all pieces then return true
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "ChessGame{}";
    }
}
