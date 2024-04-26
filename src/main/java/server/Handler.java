package server;

import java.io.BufferedOutputStream;

public interface Handler {
    public void handle(Request request, BufferedOutputStream responseStream);
}
