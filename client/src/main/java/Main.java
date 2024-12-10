import chess.*;
import ui.UiRepl;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws DeploymentException, URISyntaxException, IOException {
        UiRepl repl = new UiRepl(String.format("http://localhost:%s", "8080"));
        repl.run();
    }
}