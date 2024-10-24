package server.requests;

public class JoinGameRequest {
    private final playerColor playerColor;
    private final int gameID;

    public JoinGameRequest(playerColor playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public enum playerColor { WHITE, BLACK }

    public int getGameID() {
        return gameID;
    }

    public JoinGameRequest.playerColor getPlayerColor() {
        return playerColor;
    }
}
