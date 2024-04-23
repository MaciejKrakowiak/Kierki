package org.example;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    public final List<ClientHandler> clients = new ArrayList<>();
    private final String name;
    public boolean isfull=false;
    public boolean started = false;
    public Game game = null;
    int maxPlayers = 4;
    public Lobby(ClientHandler client,String name) {
        this.name = name;
        this.clients.add(client);
    }
    public String getName() {
        return name;
    }
    public void addClient(ClientHandler client)
    {
        int exist=0;
        for (ClientHandler cli:clients)
        {
            if (cli.clientName.equals(client.clientName)) {
                exist = 1;
                break;
            }
        }
        if (exist==0)clients.add(client);
    }
    public int numOfClients()
    {
        return clients.size();
    }

    public void startGame()
    {
        for (ClientHandler client:clients) client.lobbyStarted = true;
        List<String> clientNames = new ArrayList<>();
        for (ClientHandler client : clients) {
            clientNames.add(client.clientName);
        }
        started = true;
        this.game = new Game(clientNames,this);
        new Thread(game).start();
    }

    public void endGame()
    {
        for (ClientHandler client:clients) client.lobbyStarted = false;
        started = false;
        this.game = null;
    }
}