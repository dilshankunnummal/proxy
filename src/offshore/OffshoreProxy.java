package offshore;

import java.io.*;
import java.net.*;

public class OffshoreProxy {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(9000);
        System.out.println("Offshore Proxy running on port 9000");

        Socket client = serverSocket.accept();
        System.out.println("Ship connected");

        DataInputStream in = new DataInputStream(client.getInputStream());
        DataOutputStream out = new DataOutputStream(client.getOutputStream());

        while (true) {

            // Read request length
            int length = in.readInt();
            byte[] requestBytes = new byte[length];
            in.readFully(requestBytes);

            String requestText = new String(requestBytes);
            String firstLine = requestText.split("\r\n")[0];

            String[] parts = firstLine.split(" ");
            String method = parts[0];
            String urlString = parts[1];

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoInput(true);

            connection.connect();

            int status = connection.getResponseCode();
            InputStream responseStream = connection.getInputStream();
            byte[] responseBytes = responseStream.readAllBytes();

            // Send back response length + body
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();
        }
    }
}