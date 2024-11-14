import com.sun.nio.sctp.NotificationHandler;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;
    private final InGameClient inGameClient;

    public Repl(String serverUrl) {
        preLoginClient = new PreLoginClient(serverUrl, this);
        postLoginClient = new PostLoginClient(serverUrl);
        inGameClient = new InGameClient(serverUrl);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to the pet store. Sign in to start.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void notify(Notification notification) {
        System.out.println(SET_TEXT_COLOR_RED + notification.message());
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

}
