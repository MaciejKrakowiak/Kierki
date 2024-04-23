package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class Game extends Thread {
    Lobby lobby;
    List<String> players = new ArrayList<>();
    boolean playerMove = false;
    Character playedSuit;
    Character leadingSuit = null;
    Table table;
    int numOfRounds = 7;
    int numOfTurns;
    int turn = 0;
    int round = 0;
    boolean notification = false;
    boolean lewaPenaty = false;
    boolean lastLewaPenalty = false;
    Integer lewaPenaltyValue = -20;
    Integer lastLewaPenaltyValue = -75;
    boolean restrictedKier = false;
    String actualPlayer;
    HashMap<String, Integer> penaltyCards = new HashMap<String, Integer>();
    HashMap<String, Integer> points = new HashMap<String, Integer>();

    public Game(List<String> players, Lobby lobby) {
        this.lobby = lobby;
        this.players = players;
        this.table = new Table(this.players);
        this.numOfTurns = table.deck.getNumOfCards() / lobby.maxPlayers;
    }

    @Override
    public void run() {
        for (; round < numOfRounds; round++) {
            handleStartRound(round + 1);
            Collections.shuffle(players);
            displayTable();
            while (turn < numOfTurns) {
                for (int i = 0; i < players.size(); ) {
                    actualPlayer = players.get(i);
                    if (!notification) {
                        for (ClientHandler client : lobby.clients)
                            if (client.clientName.equals(actualPlayer)) client.sendMessage("Your turn");
                        notification = true;
                    }
                    if (playerMove) {
                        playerMove = false;
                        if (i == 0) leadingSuit = playedSuit;
                        displayTable();
                        i++;
                        notification = false;
                    }
                }
                turn++;
                handleTurn(turn);
            }
            turn = 0;
            handleEndRound();
            rotatePlayers();
        }
        endGame();
    }

    public void makeMove(String player, String card) {
        if (actualPlayer.equals(player)) {
            if (isCardValid(player, card)) {
                table.playCard(player, card);
                playedSuit = card.charAt(0);
                playerMove = true;
                for (ClientHandler client : lobby.clients)
                    if (client.clientName.equals(player)) client.sendMessage("U made move");
            } else for (ClientHandler client : lobby.clients)
                if (client.clientName.equals(player)) {
                    playedSuit = null;
                    client.sendMessage("U cant play this card");
                }
        } else for (ClientHandler client : lobby.clients)
            if (client.clientName.equals(player)) client.sendMessage("Wait for your turn");
    }

    private boolean isCardValid(String player, String card) {
        Character suit = card.charAt(0);
        List<Card> hand = table.seats.get(player);
        boolean containCard = hand.stream().anyMatch(c -> c.name.equals(card));
        if (!containCard) {
            return false;
        }

        if (restrictedKier && suit.equals('H') && !leadingSuit.equals('H')) {
            boolean hasOtherSuits = hand.stream().anyMatch(c -> c.getSuit() != 'H');
            return !hasOtherSuits;
        }

        if ((leadingSuit == null || suit.equals(leadingSuit))) {
            return true;
        } else {
            for (Card playerCard : hand) {
                if (playerCard.getSuit() == leadingSuit) {
                    return false;
                }
            }
        }
        return true;
    }

    private void rotatePlayers() {
        String firstPlayer = players.remove(0);
        players.add(firstPlayer);
    }

    private void displayTable() {
        for (ClientHandler client : lobby.clients) {
            client.sendMessage("Round:" + (round + 1) + '\n');
            client.sendMessage(table.displayHands(client.clientName));
            client.sendMessage(table.displayCurrentCards(players));
        }
    }

    private void handleTurn(int turn) {
        String highestCard = null;
        String leadingPlayer = null;

        for (String player : players) {
            Card playerCard = table.getCurrentCards().get(player);
            if (playerCard != null && playerCard.name.charAt(0) == leadingSuit) {
                if (highestCard == null || compareCardValues(playerCard.name, highestCard) > 0) {
                    highestCard = playerCard.name;
                    leadingPlayer = player;
                }
            }
        }

        if (leadingPlayer != null) {
            int leadingIndex = players.indexOf(leadingPlayer);

            if (lewaPenaty) {
                for (String player : players) {
                    if (player.equals(leadingPlayer)) {
                        points.put(player, points.getOrDefault(player, 0) + lewaPenaltyValue);
                    }
                }
            }

            if (leadingIndex != -1) {
                Collections.rotate(players, -leadingIndex);
            }

            if ((turn == numOfRounds - 1 || turn == 6) && lastLewaPenalty) {
                points.put(leadingPlayer, points.getOrDefault(leadingPlayer, 0) + lastLewaPenaltyValue);
            }

            for (String player : players) {
                Card playerCard = table.getCurrentCards().get(player);
                if (playerCard != null && penaltyCards.containsKey(playerCard.name)) {
                    points.put(leadingPlayer, points.getOrDefault(leadingPlayer, 0) + penaltyCards.get(playerCard.name));
                }
            }
            table.lewa.put(leadingPlayer, table.lewa.getOrDefault(leadingPlayer, 0) + 1);
        }
        leadingSuit = null;
        table.clearCurrentCards();
        displayTable();
        displayPoints();
    }

    private int compareCardValues(String card1, String card2) {
        int value1 = getValueFromString(card1.substring(1));
        int value2 = getValueFromString(card2.substring(1));
        return Integer.compare(value1, value2);
    }

    private int getValueFromString(String valueStr) {
        return switch (valueStr) {
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "A" -> 14;
            default -> Integer.parseInt(valueStr);
        };
    }

    private void handleEndRound() {
        table.lewa.clear();
        table.clearCurrentCards();
        table.clearSeatCards();
    }

    private void handleStartRound(int round) {
        penaltyCards.clear();
        restrictedKier = false;
        lewaPenaty = false;
        lastLewaPenalty = false;
        table.giveCards();
        readXmlFile("C:\\Users\\maciek\\IdeaProjects\\kierki\\src\\main\\java\\org\\example\\rules.xml", round);
        displayPoints();
    }

    private void displayPoints() {
        StringBuilder pointsMessage = new StringBuilder("Points after round:\n");

        for (String player : players) {
            int playerPoints = points.getOrDefault(player, 0);
            pointsMessage.append(player).append(": ").append(playerPoints).append(" points\n");
        }

        for (ClientHandler client : lobby.clients) {
            client.sendMessage(pointsMessage.toString());
        }
    }

    private void readXmlFile(String filePath, int targetRound) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList roundList = doc.getElementsByTagName("round");

            for (int i = 0; i < roundList.getLength(); i++) {
                Node roundNode = roundList.item(i);

                if (roundNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element roundElement = (Element) roundNode;
                    int roundNumber = Integer.parseInt(roundElement.getAttribute("number"));
                    if (roundNumber == targetRound) {
                        NodeList forbiddenCardsList = roundElement.getElementsByTagName("card");
                        for (int j = 0; j < forbiddenCardsList.getLength(); j++) {
                            Node cardNode = forbiddenCardsList.item(j);
                            if (cardNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element cardElement = (Element) cardNode;
                                String cardName = cardElement.getAttribute("name");
                                int penalty = Integer.parseInt(cardElement.getAttribute("penalty"));
                                penaltyCards.put(cardName, penalty);
                            }
                        }
                        lewaPenaty = Boolean.parseBoolean(roundElement.getElementsByTagName("lewa_penalty").item(0).getTextContent());
                        lastLewaPenalty = Boolean.parseBoolean(roundElement.getElementsByTagName("lastlewa_penalty").item(0).getTextContent());
                        restrictedKier = Boolean.parseBoolean(roundElement.getElementsByTagName("restricted_kier").item(0).getTextContent());
                        lastLewaPenaltyValue = Integer.parseInt(roundElement.getElementsByTagName("lastlewa_penalty_value").item(0).getTextContent());
                        lewaPenaltyValue = Integer.parseInt(roundElement.getElementsByTagName("lewa_penalty_value").item(0).getTextContent());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void skipRound() {
        if (round < 6) {
            Random random = new Random();
            for (String player : players) {
                int randomPenalty = -random.nextInt(101);
                points.put(player, points.getOrDefault(player, 0) + randomPenalty);
            }
            table.clearCurrentCards();
            table.clearSeatCards();
            round++;
            handleStartRound(round + 1);
            Collections.shuffle(players);
            displayTable();
            turn = 0;
            notification = false;
            playerMove = false;
        } else if (round == 6) {
            endGame();
        }
    }

    public void endGame() {
        String winner = null;
        int maxScore = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : points.entrySet()) {
            String player = entry.getKey();
            int score = entry.getValue();
            if (score > maxScore) {
                maxScore = score;
                winner = player;
            }
        }
        StringBuilder endGameMessage = new StringBuilder("Game Over!\n");
        endGameMessage.append("Winner: ").append(winner).append("\n\n");
        endGameMessage.append("Final Scores:\n");
        for (String player : players) {
            int playerScore = points.getOrDefault(player, 0);
            endGameMessage.append(player).append(": ").append(playerScore).append(" points\n");
        }
        for (ClientHandler client : lobby.clients) {
            client.sendMessage(endGameMessage.toString());
        }
        lobby.endGame();
        this.interrupt();
    }
}
