package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int PORT = 9999;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ExecutorService executor = Executors.newFixedThreadPool(64);
    public void start() {

        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                executor.execute(() -> {
                    try (
                            final Socket socket = serverSocket.accept();
                            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        final String requestLine = in.readLine();
                        final String[] parts = requestLine.split(" ");

                        if (parts.length != 3) {
                            return;
                        }

                        final String path = parts[1];
                        if (!validPaths.contains(path)) {
                            responseNotFound(out);
                            out.flush();
                            return;
                        }

                        final Path filePath = Paths.get(".", "public", path);
                        final String mimeType = Files.probeContentType(filePath);

                        if (path.equals("/classic.html")) {
                            final String template = Files.readString(filePath);
                            final byte[] content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            responseOK(out, mimeType, content.length);
                            out.write(content);
                            out.flush();
                            return;
                        }

                        final var length = Files.size(filePath);
                        responseOK(out, mimeType, length);
                        Files.copy(filePath, out);
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void responseNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

    public void responseOK(BufferedOutputStream out, String mimeType, long length) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }
}
