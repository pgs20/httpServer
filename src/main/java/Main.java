import server.Handler;
import server.Request;
import server.Server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/spring.svg", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    responseStream.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " +  Files.probeContentType(Paths.get(".", "public", request.getPath())) + "\r\n" +
                            "Content-Length: " + Files.size(Paths.get(".", "public", request.getPath())) + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n").getBytes());
                    Files.copy(Paths.get(".", "public", request.getPath()), responseStream);
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.start();
    }
}

