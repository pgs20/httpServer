package server;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int PORT = 9999;
    private ExecutorService executor = Executors.newFixedThreadPool(64);

    private ConcurrentHashMap<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
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
                        final Request request = parserRequest(requestLine);

                        if (request == null) {
                            return;
                        }

                        Handler handler = findHandler(request.getMethod(), request.getPath());
                        if (handler == null) {
                            responseNotFound(out);
                            out.flush();
                            return;
                        }

                        handler.handle(request, out);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Request parserRequest(String requestLine) throws URISyntaxException {
        String[] request = requestLine.split(" ");
        if (request.length != 3) return null;
        URI uri = new URI(request[1]);


        return new Request(request[0], uri.getPath(), URLEncodedUtils.parse(uri, Charset.forName("UTF-8")),request[2]);
    }

    public void responseNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public Handler findHandler(String method, String path) {
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers == null) return null;

        return methodHandlers.get(path);
    }
}
