import server.Server;

public class Main {

    /**
     * Starts the server on the given port. If port is 0 then a random port is used.
     */
    public static void main(String[] args) {
//        boolean changeDatAcc = false;
        try {
            var port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }
//            if (args.length >= 2 && args[1].equals("sql")) {
//                changeDatAcc = true;
//            }

            var server = new Server();
            server.run(port);
//            if (changeDatAcc) {
//            }

            System.out.printf("Server started on port %d%n", server.port());

            return;
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
        System.out.println("""
                Pet Server:
                java ServerMain <port> [sql]
                """);
    }
}