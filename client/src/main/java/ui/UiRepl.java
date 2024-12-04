package ui;

import websocket.NotificationHandler;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class UiRepl implements NotificationHandler {
    private final UiClient uiClient;

    public UiRepl(String serverUrl) {
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
            else{
                func = this::printLoggedinPrompt;
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


}
