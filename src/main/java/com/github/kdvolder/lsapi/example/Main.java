package com.github.kdvolder.lsapi.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.kdvolder.lsapi.util.LoggingFormat;

import io.typefox.lsapi.services.json.LoggingJsonAdapter;

public class Main {
    private static final Logger LOG = Logger.getLogger("main");

    public static void main(String[] args) throws IOException {
    	LOG.info("Starting LS");
        try {
            LoggingFormat.startLogging();

            Connection connection = connectToNode();

            run(connection);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);

            System.exit(1);
        }
    }

    private static Connection connectToNode() throws IOException {
        String port = System.getProperty("server.port");

        if (port != null) {
            Socket socket = new Socket("localhost", Integer.parseInt(port));

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            OutputStream intercept = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }
            };

            LOG.info("Connected to parent using socket on port " + port);

            return new Connection(in, intercept);
        }
        else {
            InputStream in = System.in;
            PrintStream out = System.out;

            LOG.info("Connected to parent using stdio");

            return new Connection(in, out);
        }
    }

    private static class Connection {
        final InputStream in;
        final OutputStream out;

        private Connection(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }
    }

    /**
     * Listen for requests from the parent node process.
     * Send replies asynchronously.
     * When the request stream is closed, wait for 5s for all outstanding responses to compute, then return.
     */
    public static void run(Connection connection) {
    	MyLanguageServer server = new MyLanguageServer();
    	LoggingJsonAdapter jsonServer = new LoggingJsonAdapter(server);
    	jsonServer.setMessageLog(new PrintWriter(System.out));

        jsonServer.connect(connection.in, connection.out);
        jsonServer.getProtocol().addErrorListener((message, err) -> {
            LOG.log(Level.SEVERE, message, err);

            server.onError(message, err);
        });
        
        try {
            jsonServer.join();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
