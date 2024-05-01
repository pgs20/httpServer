package server;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int PORT = 9999;
    private final int LIMIT = 4096;
    private ExecutorService executor = Executors.newFixedThreadPool(64);

    private ConcurrentHashMap<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    public void start() {

        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                executor.execute(() -> {
                    try (
                            final Socket socket = serverSocket.accept();
                            final BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                            final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        Request request = parserRequest(in);

                        if (request == null) {
                            responseNotFound(out);
                            out.flush();
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
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public Request parserRequest(BufferedInputStream in) {
        try {
            in.mark(LIMIT);
            final byte[] buffer = new byte[LIMIT];
            final int read = in.read(buffer);

            final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
            final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

            if (requestLineEnd == -1) {
                return null;
            }

            final String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

            if (requestLine.length != 3) {
                return null;
            }

            final var method = requestLine[0];
            System.out.println(method);

            final var path = requestLine[1];
            System.out.println(path);

            final List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(path), Charset.forName("UTF-8"));
            System.out.println(queryParams);

            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

            in.reset();
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println(headers);

            if (method.equals("POST")) {
                in.skip(headersDelimiter.length);
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);
                    final var body = new String(bodyBytes);
                    System.out.println(body);

                    final List<NameValuePair> postParams = URLEncodedUtils.parse(body, Charset.forName("UTF-8"));
                    System.out.println(postParams);
                    return new Request(method, path, queryParams, postParams);
                }
            }
            return new Request(method, path, queryParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
    }

    public Handler findHandler(String method, String path) {
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers == null) return null;

        return methodHandlers.get(path);
    }

    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
