import server.Handler;
import server.Request;
import server.Server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/spring.svg", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    Path path = Paths.get(".", "public", request.getPath());
                    responseStream.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " +  Files.probeContentType(path) + "\r\n" +
                            "Content-Length: " + Files.size(path) + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n").getBytes());
                    Files.copy(path, responseStream);
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.addHandler("GET", "/forms.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    Path path = Paths.get(".", "public", request.getPath());
                    responseStream.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " +  Files.probeContentType(path) + "\r\n" +
                            "Content-Length: " + Files.size(path) + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
                    ).getBytes());
                    responseStream.write(Files.readString(path).getBytes());
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.start();
    }
}

