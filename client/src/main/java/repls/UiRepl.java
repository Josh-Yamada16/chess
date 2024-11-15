package repls;

import clients.State;
import exception.DataAccessException;

import java.util.Scanner;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class UiRepl {
    private final UiClient uiClient;

    public UiRepl(String serverUrl) {
        uiClient = new UiClient(serverUrl);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to Chess. Sign in to start.");
        System.out.print(preLoginClient.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = preLoginClient.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + "[LOGGED OUT]" + ">>> ");
    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.PRESIGNIN) {
            throw new DataAccessException(400, "You must sign in");
        }
    }
}
