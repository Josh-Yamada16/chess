package ui;

import java.util.Scanner;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class UiRepl {
    private final UiClient uiClient;

    public UiRepl(String serverUrl) {
        uiClient = new UiClient(serverUrl);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to Chess. Sign in to start.");
        System.out.print(uiClient.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        FuncInter func = null;
        while (!result.equals("quit")) {
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
