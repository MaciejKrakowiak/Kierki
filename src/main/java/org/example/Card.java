package org.example;

public class Card {
    private final Character suit;
    private final int value;
    public String name;

    Card(Character suit,int value)
    {
        this.suit=suit;
        this.value=value;
        if (value >= 2 && value <= 10) {
            this.name = suit + String.valueOf(value);
        } else if (value == 11) {
            this.name = suit + "J";
        } else if (value == 12) {
            this.name = suit + "Q";
        } else if (value == 13) {
            this.name = suit + "K";
        } else {
            this.name = suit + "A";
        }
    }


//    public static Card createCardFromString(String cardString) {
//        char suit = cardString.charAt(0);
//        String valueStr = cardString.substring(1);
//
//        int value;
//        if (valueStr.equals("J")) {
//            value = 11;
//        } else if (valueStr.equals("Q")) {
//            value = 12;
//        } else if (valueStr.equals("K")) {
//            value = 13;
//        } else if (valueStr.equals("A")) {
//            value = 14;
//        } else {
//            value = Integer.parseInt(valueStr);
//        }
//
//        return new Card(suit, value);
//    }

    public String displayCard()
    {
        return this.name;
    }

    public int getValue() {
        return value;
    }

    public Character getSuit() {
        return suit;
    }
}
