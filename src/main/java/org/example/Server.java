package org.example;

import org.example.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private final List<ClientHandler> clients = new ArrayList<>();
    public static final List<Lobby> lobbies = new ArrayList<>();

    public static void main(String[] args) {
        Server server = new Server();
        server.start(12345);
    }

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            new Thread(Server::handleConsoleInput).start();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createLobby(ClientHandler client, String name) {
        Lobby lobby = new Lobby(client, name);
        lobbies.add(lobby);
        client.lobbyName = name;
        client.sendMessage("You have created the lobby: " + lobby.getName());
    }

    public void joinLobby(ClientHandler client, String name) {
        Lobby joinLobby = null;
        for (Lobby lobby : lobbies) {
            if (lobby.getName().equals(name)) {
                joinLobby = lobby;
                break;
            }
        }
        if (joinLobby != null && !joinLobby.isfull) {
            joinLobby.addClient(client);
            client.lobbyName = name;
            client.sendMessage("You have joined the lobby: " + joinLobby.getName());
            if (joinLobby.clients.size() == joinLobby.maxPlayers) joinLobby.isfull = true;
        } else if (joinLobby != null && joinLobby.isfull) client.sendMessage("Lobby with name '" + name + "' is full.");
        else client.sendMessage("Lobby with name '" + name + "' not found.");

    }

    public void inviteToLobby(ClientHandler inviter, String lobbyName, String invitedPlayerName) {
        Lobby targetLobby = null;

        for (Lobby lobby : lobbies) {
            if (lobby.getName().equals(lobbyName)) {
                targetLobby = lobby;
                break;
            }
        }
        if (targetLobby != null && !targetLobby.isfull) {
            ClientHandler invitedPlayer = null;

            for (ClientHandler client : clients) {
                if (client.getClientName().equals(invitedPlayerName)) {
                    invitedPlayer = client;
                    break;
                }
            }
            if (invitedPlayer != null) {
                invitedPlayer.sendMessage("You are invited to lobby '" + lobbyName + "'. You can join by command 'join " + lobbyName + "'");
                inviter.sendMessage("Invitation sent to player '" + invitedPlayerName + "'.");
            } else {
                inviter.sendMessage("Player named: " + invitedPlayerName + " not found.");
            }
        } else {
            inviter.sendMessage("Lobby with name '" + lobbyName + "' not found or is full.");
        }
    }
    public void listLobbies(ClientHandler requester) {
        for (ClientHandler client : clients) {
            if (client == requester) {
                client.sendMessage("\nLOBBIES\n");
                for (Lobby lobby : lobbies) {
                    String state = null;
                    if (lobby.started) state = "started";
                    else state = "waiting";
                    client.sendMessage(lobby.getName() + " " + lobby.numOfClients() + "/4 (" + state + ") :");
                    for (ClientHandler name : lobby.clients) client.sendMessage('-' + name.clientName);
                    client.sendMessage("\n");
                }
            }
        }
    }

    public void listPlayers(ClientHandler requester) {
        for (Lobby lobby : lobbies) {
            if (lobby.getName().equals(requester.lobbyName))
            {
                requester.sendMessage(lobby.getName());
                for (ClientHandler name : lobby.clients) requester.sendMessage('-' + name.clientName);
            }

        }
    }

    public void leaveLobby(ClientHandler leaver, String name) {
        Lobby lobby = null;
        for (Lobby lobby1 : lobbies) if (lobby1.getName().equals(name)) lobby = lobby1;
        leaver.lobbyName = null;
        if (lobby != null) {
            lobby.clients.remove(leaver);
            lobby.isfull = false;
        }
    }

    public void starLobby(String name, ClientHandler cli) {
        for (Lobby lobby : lobbies)
            if (lobby.getName().equals(name) && lobby.clients.contains(cli) && lobby.isfull) lobby.startGame();
    }

    public void playCard(String lobbyName, String player, String card) {
        for (Lobby lobby : lobbies) if (lobby.getName().equals(lobbyName)) lobby.game.makeMove(player, card);
    }

    public void sendMessage(ClientHandler sender, String receiver, String message) {
        if (receiver.equals("all"))
        {
            for (ClientHandler client : clients) {
                if (!client.clientName.equals(sender.clientName)) client.sendMessage("Message from " + sender.clientName + ": " + message);
            }
            sender.sendMessage("Message send");
        }
        else if (receiver.equals("lobby")) {
            for (Lobby lobby : lobbies) {
                if (lobby.getName().equals(sender.lobbyName)) {
                    for (ClientHandler client : lobby.clients) {
                        if (!client.clientName.equals(sender.clientName)) {
                            client.sendMessage("Message from " + sender.clientName + ": " + message);
                        }
                    }
                    sender.sendMessage("Message send");
                }

            }
        } else {
            ClientHandler clientReceiver = null;
            for (ClientHandler client : clients) if (client.clientName.equals(receiver)) clientReceiver = client;
            if (clientReceiver != null) {
                clientReceiver.sendMessage("Message from " + sender.clientName + ": " + message);
                sender.sendMessage("Message send");
            } else sender.sendMessage("User not found");
        }
    }

    static void handleConsoleInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            System.out.println(input);
            if (input.startsWith("skip")) {
                try {
                    String lobbyName = input.split(" ")[1];
                    for (Lobby lobby : lobbies)
                        if (lobby.getName().equals(lobbyName)) {
                            if (lobby.game != null) {
                                lobby.game.skipRound();
                            }
                        }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
            }
        }
    }
}
