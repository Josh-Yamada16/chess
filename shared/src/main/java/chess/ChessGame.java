package chess;

import java.util.Collection;
import java.util.Iterator;

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
        if (piece == null) {
            return null;
        }
        // get moveset of the piece in question
        var moveSet = piece.pieceMoves(board, startPosition);
        // iterate through moves to see if they are valid
        Iterator<ChessMove> iterator = moveSet.iterator();
        while (iterator.hasNext()) {
            ChessMove move = iterator.next();
            // create copy instance of the game so that nothing saves to the real game
            ChessBoard copy = board.getBoardCopy();
            copy.addPiece(move.getEndPosition(), piece);
            copy.removePiece(move.getStartPosition());
            ChessGame copyGame = new ChessGame();
            copyGame.setBoard(copy);
            // if that move puts their king in check in the game copy then remove the move from the moveset
            if (copyGame.isInCheck(piece.getTeamColor())) {
                iterator.remove();
            }
        }
        return moveSet;
    }


    public boolean moveInSet(ChessMove move, Collection<ChessMove> validMoves) {
        // checks if the moe is in the valid moves moveset
        for (ChessMove validMove : validMoves) {
            // if the move is in the moveset -> return true
            if (validMove.equals(move)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // check if move is in the collection from validmoves or it is that team's turn otherwise throws exception
        if (board.getPiece(move.getStartPosition()) != null) {
            // checks if the move in question is in the valid move set and if that is the piece's turn
            if (moveInSet(move, validMoves(move.getStartPosition())) &&
                    board.getPiece(move.getStartPosition()).getTeamColor() == teamTurn) {
                board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
                // checks if the piece is going to be promoted or not for a pawn
                if (move.getPromotionPiece() != null) {
                    board.addPiece(move.getEndPosition(),
                    new ChessPiece(board.getPiece(move.getStartPosition()).getTeamColor(), move.getPromotionPiece()));
                }
                board.removePiece(move.getStartPosition());
            } else {
                throw new InvalidMoveException();
            }
            this.round++;
            // sets whose turn is next depending on the round
            if (this.round % 2 == 0) {
                setTeamTurn(TeamColor.WHITE);
            } else {
                setTeamTurn(TeamColor.BLACK);
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    public boolean kingGettingAttacked(TeamColor team) {
        ChessPosition kingPos = board.getKingPos(team);
        // loop through every piece on the board and find the opposite teams pieces
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // make a fake piece to test it on the fake board
                if (fakeBoardCheck(i, j, team, kingPos) == 1){
                    return true;
                }
            }
        }
        return false;
    }

    private int fakeBoardCheck(int i, int j, TeamColor team, ChessPosition kingPos) {
        ChessPiece queryPiece = board.getPiece(new ChessPosition(8 - i, j + 1));
        if (queryPiece != null) {
            if (queryPiece.getTeamColor() != team) {
                // loop through their moveset and see if the king's position is in their getEndposition
                var moveSet = queryPiece.pieceMoves(board, new ChessPosition(8 - i, j + 1));
                for (ChessMove item : moveSet) {
                    // if getEndPosition() is == to king position then return true
                    if (item.getEndPosition().equals(kingPos)) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    public enum GameState {
        NORMAL,
        CHECK,
        CHECKMATE,
        STALEMATE;
    }

    public GameState gameState(ChessGame hypoGame, TeamColor team) {
        if (hypoGame.isInCheck(team)){
            if (hypoGame.isInCheckmate(team)){
                return GameState.CHECKMATE;
            }
            return GameState.CHECK;
        }
        if (hypoGame.isInStalemate(team)){
            return GameState.STALEMATE;
        }
        return GameState.NORMAL;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // if kingGettingAttacked() then return true
        return kingGettingAttacked(teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // if isInCheck() and validMoves() is empty for all pieces then return true
        // the team has to be in check in order for it to be checkmate
        if (isInCheck(teamColor)) {
            return checkPositions(teamColor);
        }
        return false;
    }

    private boolean checkPositions(TeamColor teamColor) {
        int count = 0;
        Collection<ChessPosition> positions = board.getNumberOfPieces(teamColor);
        for (ChessPosition pos : positions) {
            // counting how many of the pieces have an empty valid moveset
            if (validMoves(pos).isEmpty()) {
                count++;
            }
        }
        // if all the pieces have nothing in the valid moveset -> the team is in checkmate
        if (count == positions.size()) {
            return true;
        } else {
            return false;
        }
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
        // the team cannot be in check in order for a stalemate to happen
        if (!isInCheck(teamColor)) {
            return checkPositions(teamColor);
        }
        return false;
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
