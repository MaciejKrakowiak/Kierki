package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {
    Deck deck = new Deck();
    HashMap<String, Integer> lewa = new HashMap<String, Integer>();
    HashMap<String, List<Card>> seats = new HashMap<String, List<Card>>();
    HashMap<String, Card> currentCards = new HashMap<>();

    public Table(List<String> players) {
        initialize(players);
    }

    private void initialize(List<String> players) {
        for (String player : players) {
            seats.put(player, new ArrayList<Card>());
        }
    }


    void giveCards() {
        deck.shuffleDeck();
        int numberOfPlayers = seats.size();
        List<List<Card>> playerHands = deck.dealCards(numberOfPlayers);
        int i = 0;
        for (String player : seats.keySet()) {
            List<Card> hand = playerHands.get(i);
            seats.put(player, hand);
            i++;
        }
    }

    public String displayHands(String player) {
        StringBuilder result = new StringBuilder();
            List<Card> hand = seats.get(player);
            result.append(player).append("'s hand: ");
            for (Card card : hand) {
                result.append(card.displayCard()).append(" ");
            }
            result.append("\n");
        return result.toString();
    }

    public Map<String, Card> getCurrentCards() {
        return currentCards;
    }

    public void addToCurrentCards(String player, Card card) {
        currentCards.put(player, card);
    }

    public void clearCurrentCards() {
        currentCards.clear();
    }

    public void clearSeatCards() {
        for (String player : seats.keySet()) {
            seats.get(player).clear();
        }
    }

    public String displayCurrentCards(List<String> players) {
        StringBuilder result = new StringBuilder("Current cards on the table:\n");

        for (String player : players) {
            Card card = currentCards.get(player);
            if (card != null) {
                result.append(player).append(" played: ").append(card.displayCard()).append("\n");
            }
        }

        return result.toString();
    }

    public void removeCardFromHand(String player, Card card) {
        List<Card> hand = seats.get(player);
        if (hand != null && hand.contains(card)) {
            hand.remove(card);
        }
    }

    public void playCard(String player, String card) {
        List<Card> hand = seats.get(player);
        for(Card playercard:hand)
        {
            boolean containCard = playercard.name.equals(card);
            if(containCard)
            {
                addToCurrentCards(player, playercard);
                removeCardFromHand(player, playercard);
                break;
            }
        }
    }
}
