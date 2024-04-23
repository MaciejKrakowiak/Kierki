package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final String serverAddress;
    private final int serverPort;
    private final BufferedReader userInput;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            new Thread(() -> messageReceiver(serverInput)).start();

            String userInput;
            while ((userInput = this.userInput.readLine()) != null) {
                out.println(userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void messageReceiver(BufferedReader serverInput) {
        try {
            String serverResponse;
            while ((serverResponse = serverInput.readLine()) != null) {
                System.out.println(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12345;

        Client client = new Client(serverAddress, serverPort);
        client.start();
    }
}
