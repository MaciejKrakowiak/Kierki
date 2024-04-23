package org.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Deck {
    public int getNumOfCards() {
        return numOfCards;
    }

    private List<Card> deck = new ArrayList<>();
    private final int numOfCards = 52;
    private int valueOfGeneratedCard = 2;
    public Deck()
    {
        generateDeck();
    }

    void generateDeck()
    {
        for(int i =0; i < numOfCards;i++)
        {
            switch (i%4)
            {
                case 0:
                    deck.add(new Card('H',valueOfGeneratedCard));
                    break;
                case 1:
                    deck.add(new Card('D',valueOfGeneratedCard));
                    break;
                case 2:
                    deck.add(new Card('S',valueOfGeneratedCard));
                    break;
                case 3:
                    deck.add(new Card('C',valueOfGeneratedCard));
                    valueOfGeneratedCard++;
                    break;
            }
        }
    }

    void shuffleDeck()
    {
        Collections.shuffle(deck);
    }

    public List<List<Card>> dealCards(int numberOfPlayers) {
        List<List<Card>> playerHands = new ArrayList<>();

        for (int i = 0; i < numberOfPlayers; i++) {
            playerHands.add(new ArrayList<>());
        }

        int currentPlayer = 0;
        for (Card card : deck) {
            playerHands.get(currentPlayer).add(card);
            currentPlayer = (currentPlayer + 1) % numberOfPlayers;
        }

        return playerHands;
    }
}
