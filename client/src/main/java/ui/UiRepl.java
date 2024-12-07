package ui;

import chess.ChessGame;
import websocket.NotificationHandler;
import websocket.messages.*;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class UiRepl implements NotificationHandler{
    private UiClient uiClient;

    public UiRepl(String serverUrl) throws DeploymentException, URISyntaxException, IOException {
        uiClient = new UiClient(serverUrl, this);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to Chess. Sign in to start.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        FuncInter func;
        while (!result.equals("quit")) {
            System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
            System.out.print(SET_TEXT_COLOR_BLUE + uiClient.help());
            if (uiClient.state == State.PRESIGNIN){
                func = this::printLoggedoutPrompt;
            }
            else if (uiClient.state == State.POSTSIGNIN){
                func = this::printLoggedinPrompt;
            }
            else{
                func = this::printInGamePrompt;
            }
            func.execute();
            String line = scanner.nextLine();

            try {
                result = uiClient.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()){
            case NOTIFICATION, ERROR -> System.out.println(message.getMessage());
            case LOAD_GAME -> loadGame((LoadGameMessage) message).getGame();
        }
    }

    public void loadGame(ChessGame game){

    }

    interface FuncInter{
        void execute();
    }

    private void printLoggedoutPrompt() {
        System.out.print("\n" + "[LOGGED_OUT]" + ">>> ");
    }

    private void printLoggedinPrompt() {
        System.out.print("\n" + "[LOGGED_IN]" + ">>> ");
    }

    private void printInGamePrompt() {
        System.out.print("\n" + "[IN_GAME]" + ">>> ");
    }
}
