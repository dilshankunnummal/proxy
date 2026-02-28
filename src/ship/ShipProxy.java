package ship;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ShipProxy {

    private static Socket offshoreSocket;
    private static DataOutputStream offshoreOut;
    private static DataInputStream offshoreIn;

    private static BlockingQueue<RequestWrapper> queue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws Exception {

        String offshoreHost = System.getenv().getOrDefault("OFFSHORE_HOST", "localhost");
        int offshorePort = 9000;

        offshoreSocket = new Socket(offshoreHost, offshorePort);
        offshoreOut = new DataOutputStream(offshoreSocket.getOutputStream());
        offshoreIn = new DataInputStream(offshoreSocket.getInputStream());

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", ShipProxy::handleRequest);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Ship Proxy running on port 8080");

        new Thread(ShipProxy::processQueue).start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {

        byte[] body = exchange.getRequestBody().readAllBytes();

        StringBuilder raw = new StringBuilder();
        raw.append(exchange.getRequestMethod())
                .append(" ")
                .append(exchange.getRequestURI())
                .append(" HTTP/1.1\r\n");

        exchange.getRequestHeaders().forEach((k, v) -> {
            for (String val : v) {
                raw.append(k).append(": ").append(val).append("\r\n");
            }
        });

        raw.append("\r\n");

        byte[] headerBytes = raw.toString().getBytes();
        byte[] fullRequest = new byte[headerBytes.length + body.length];

        System.arraycopy(headerBytes, 0, fullRequest, 0, headerBytes.length);
        System.arraycopy(body, 0, fullRequest, headerBytes.length, body.length);

        queue.add(new RequestWrapper(fullRequest, exchange));
    }

    private static void processQueue() {
        try {
            while (true) {

                RequestWrapper wrapper = queue.take();

                // Send length first (framing)
                offshoreOut.writeInt(wrapper.requestBytes.length);
                offshoreOut.write(wrapper.requestBytes);
                offshoreOut.flush();

                // Read response length
                int responseLength = offshoreIn.readInt();
                byte[] responseBytes = new byte[responseLength];
                offshoreIn.readFully(responseBytes);

                // Send back to client
                wrapper.exchange.sendResponseHeaders(200, responseLength);
                OutputStream clientOut = wrapper.exchange.getResponseBody();
                clientOut.write(responseBytes);
                clientOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RequestWrapper {
        byte[] requestBytes;
        HttpExchange exchange;

        RequestWrapper(byte[] requestBytes, HttpExchange exchange) {
            this.requestBytes = requestBytes;
            this.exchange = exchange;
        }
    }
}