package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    public boolean lobbyStarted = false;

    public String lobbyName;
    public String getClientName() {
        return clientName;
    }

    public String clientName;
    PrintWriter out;
    BufferedReader in;
    String received;
    String[] prepared;

    public ClientHandler(Socket clientSocket,Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.lobbyName=null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            out.println("Enter your name:");
            clientName = in.readLine();
            out.println("Hello, " + clientName + "! Welcome to the kierki.");

            while ((received = in.readLine()) != null) {
                prepared = received.split(" ",2);
                if(lobbyName==null) handleTerminalInput(prepared);
                else if(lobbyStarted) handleGameInput(prepared);
                else handleLobbyInput(prepared);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void handleTerminalInput(String[] inputArray)
    {
        switch(inputArray[0]){
            case "create":
                server.createLobby(this,inputArray[1]);
                break;
            case "list":
                server.listLobbies(this);
                break;
            case "join":
                server.joinLobby(this,inputArray[1]);
                break;
            case "send":
                String[] message = inputArray[1].split(" ",2);
                if(message.length == 2)
                {
                    server.sendMessage(this,message[0],message[1]);
                }
                else sendMessage("Wrong command!");
                break;
            default:
                sendMessage("Wrong command!");
        }
    }

    public void handleLobbyInput(String[] inputArray)
    {
        switch(inputArray[0]){
            case "leave":
                server.leaveLobby(this,lobbyName);
                break;
            case "invite":
                server.inviteToLobby(this,lobbyName,inputArray[1]);
                break;
            case "start":
                lobbyStarted = true;
                server.starLobby(lobbyName,this);
                break;
            case "players":
                server.listPlayers(this);
                break;
            case "send":
                String[] message = inputArray[1].split(" ",2);
                if(message.length == 2)
                {
                    server.sendMessage(this,message[0],message[1]);
                }
                else sendMessage("Wrong command!");
                break;
            default:
                sendMessage("Wrong command!");
        }
    }

    public void handleGameInput(String[] inputArray)
    {
        switch(inputArray[0]){
            case "play":
                server.playCard(lobbyName,clientName,inputArray[1]);
                break;
            case "send":
                String[] message = inputArray[1].split(" ",2);
                if(message.length == 2)
                {
                    server.sendMessage(this,message[0],message[1]);
                }
                else sendMessage("Wrong command!");
                break;
            default:
                sendMessage("Wrong command!");
        }
    }
    public void sendMessage(String message) {
        out.println(message);
    }
}