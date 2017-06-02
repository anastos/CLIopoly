import java.util.*;
import java.util.List;

/**
 * Created by Mark on 11/11/15.
 */
class Deck {

    List<Card> deck;

    Deck(CardType t) {
        String fileName = t == CardType.CHANCE ?
                "chanceCards.txt" : "communityChestCards.txt";
        List<String> cardTexts = importCardTexts(fileName);
        deck = t == CardType.CHANCE ?
                makeChanceCards(cardTexts) :
                makeCommunityChestCards(cardTexts);
        shuffle();
    }

    Card draw() {
        Card ret = deck.remove(0);
        deck.add(ret);
        return ret;
    }
    private void shuffle() {
        List<Card> remaining = deck;
        deck = new ArrayList<>();
        //Add random cards to the deck until there are none left to add.
        while (!remaining.isEmpty())
            deck.add(remaining.remove((int)(Math.random() * remaining.size())));
    }

    List<String> importCardTexts(String fileName) {
        List<String> ret = new ArrayList<>();
        try {
            Scanner s = new Scanner(ClassLoader.getSystemResource(fileName).openStream());
            //Add all the lines from the file into the list of card texts.
            while (s.hasNextLine())
                ret.add(s.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    List<Card> makeChanceCards(List<String> texts) {
        List<Card> ret = new ArrayList<>();
        ret.add(new Card(texts.get(0), m -> {
            Player p = m.getCurrentPlayer();
            p.move(0 - p.getPos(), m.getBoardSpaces());
        }));
        ret.add(new Card(texts.get(1), m -> {
            Player p = m.getCurrentPlayer();
            Property illinoisAve = (Property)m.getBoardSpaces().get(24);
            p.move(24 - p.getPos(), m.getBoardSpaces());
            //Check whether the property is owned, mortgaged, etc.
            if (illinoisAve.isOwned()) {
                if (illinoisAve.getOwner() == m.getCurrentPlayer())
                    System.out.println("You own this property.");
                else if (!illinoisAve.isMortgaged()) {
                    Player owner = illinoisAve.getOwner();
                    int rent = illinoisAve.rent(m.getBoardSpaces(), m.getDice().total());
                    p.giveMoneyTo(owner, rent);
                    System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                }
            } else p.mustAddressProperty();
        }));
        ret.add(new Card(texts.get(2), m -> {
            Player p = m.getCurrentPlayer();
            Property stCharlesPlace = (Property)m.getBoardSpaces().get(24);
            p.move(11 - p.getPos(), m.getBoardSpaces());
            //Check whether the property is owned, mortgaged, etc.
            if (stCharlesPlace.isOwned()) {
                if (!stCharlesPlace.isMortgaged()) {
                    Player owner = stCharlesPlace.getOwner();
                    int rent = stCharlesPlace.rent(m.getBoardSpaces(), m.getDice().total());
                    p.giveMoneyTo(owner, rent);
                    System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                }
            } else p.mustAddressProperty();
        }));
        ret.add(new Card(texts.get(3), m -> {
            Player p = m.getCurrentPlayer();
            int utilityPos = 0;
            Utility utility = null;
            //Go around the board looking for the first Utility.
            for (int i = p.getPos(); i < 40 + p.getPos(); i++) {
                int pos = i % 40;
                BoardSpace sp = m.getBoardSpaces().get(pos);
                if (sp instanceof Utility) {
                    utilityPos = pos;
                    utility = (Utility)m.getBoardSpaces().get(pos);
                    break;
                }
            }
            p.move(utilityPos - p.getPos(), m.getBoardSpaces());
            //Check whether the property is owned, mortgaged, etc.
            if (utility != null && utility.getOwner() != null) {
                if (!utility.isMortgaged()) {
                    Player owner = utility.getOwner();
                    int rent = m.getDice().total() * 10;
                    p.giveMoneyTo(owner, rent);
                    System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                }
            } else p.mustAddressProperty();
        }));
        ret.add(new Card(texts.get(4), m -> {
            Player p = m.getCurrentPlayer();
            int railroadPos = 0;
            Railroad railroad = null;
            //Go around the board looking for the first Railroad.
            for (int i = p.getPos(); i < 40 + p.getPos(); i++) {
                int pos = i % 40;
                BoardSpace sp = m.getBoardSpaces().get(pos);
                if (sp instanceof Railroad) {
                    railroadPos = pos;
                    railroad = (Railroad)m.getBoardSpaces().get(pos);
                    break;
                }
            }
            p.move(railroadPos - p.getPos(), m.getBoardSpaces());
            //Check whether the property is owned, mortgaged, etc.
            if (railroad != null && railroad.getOwner() != null) {
                if (!railroad.isMortgaged()) {
                    Player owner = railroad.getOwner();
                    int rent = railroad.rent(m.getBoardSpaces(), m.getDice().total()) * 2;
                    p.giveMoneyTo(owner, rent);
                    System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                }
            } else p.mustAddressProperty();
        }));
        ret.add(new Card(texts.get(5), m ->
                m.getCurrentPlayer().gainMoney(50)));
        ret.add(new Card(texts.get(6), m ->
                m.getCurrentPlayer().gainJailCard()));
        ret.add(new Card(texts.get(7), m -> {
            Player p = m.getCurrentPlayer();
            p.move(-3, m.getBoardSpaces());
            BoardSpace bs = m.getBoardSpaces().get(p.getPos());
            //Check whether the property is owned, mortgaged, etc.
            if (bs instanceof Property) {
                Property prop = (Property)bs;
                if (prop.isOwned()) {
                    if (!prop.isMortgaged()) {
                        Player owner = prop.getOwner();
                        int rent = prop.rent(m.getBoardSpaces(), m.getDice().total());
                        p.giveMoneyTo(owner, rent);
                        System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                    }
                } else p.mustAddressProperty();
            }
        }));
        ret.add(new Card(texts.get(8), m ->
                m.getCurrentPlayer().gotoJail()));
        ret.add(new Card(texts.get(9), m -> {
            Player p = m.getCurrentPlayer();
            int total = 0;
            //Go through the board searching for properties they own with houses on them.
            for (BoardSpace b : m.getBoardSpaces())
                if (b instanceof ColorProperty) {
                    ColorProperty cp = (ColorProperty)b;
                    if (cp.getOwner() == p) {
                        total += cp.getHouses() < 5 ?
                                cp.getHouses() * 25 : 100;
                    }
                }
            p.loseMoney(total);
            System.out.printf("You paid $%d for your houses and hotels.%n", total);
        }));
        ret.add(new Card(texts.get(10), m ->
                m.getCurrentPlayer().loseMoney(15)));
        ret.add(new Card(texts.get(11), m -> {
            Player p = m.getCurrentPlayer();
            Property readingRailroad = (Property)m.getBoardSpaces().get(5);
            p.move(5 - p.getPos(), m.getBoardSpaces());
            //Check whether the property is owned, mortgaged, etc.
            if (readingRailroad.isOwned()) {
                if (!readingRailroad.isMortgaged()) {
                    Player owner = readingRailroad.getOwner();
                    int rent = readingRailroad.rent(m.getBoardSpaces(), m.getDice().total());
                    p.giveMoneyTo(owner, rent);
                    System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                }
            } else p.mustAddressProperty();
        }));
        ret.add(new Card(texts.get(12), m -> {
            Player p = m.getCurrentPlayer();
            Property boardwalk = (Property)m.getBoardSpaces().get(39);
            p.move(39 - p.getPos(), m.getBoardSpaces());
            //Check whether the property is owned, mortgaged, etc.
            if (boardwalk.isOwned()) {
                if (!boardwalk.isMortgaged()) {
                    Player owner = boardwalk.getOwner();
                    int rent = boardwalk.rent(m.getBoardSpaces(), m.getDice().total());
                    p.giveMoneyTo(owner, rent);
                    System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
                }
            } else p.mustAddressProperty();
        }));
        ret.add(new Card(texts.get(13), m -> {
            //Give each player $50.
            for (Player p : m.getPlayers())
                m.getCurrentPlayer().giveMoneyTo(p, 50);
        }));
        ret.add(new Card(texts.get(14), m ->
                m.getCurrentPlayer().gainMoney(150)));
        ret.add(new Card(texts.get(15), m ->
                m.getCurrentPlayer().gainMoney(100)));
        return ret;
    }
    List<Card> makeCommunityChestCards(List<String> texts) {
        List<Card> ret = new ArrayList<>();
        ret.add(new Card(texts.get(0), m -> {
            Player p = m.getCurrentPlayer();
            p.move(0 - p.getPos(), m.getBoardSpaces());
        }));
        ret.add(new Card(texts.get(1), m ->
                m.getCurrentPlayer().gainMoney(200)));
        ret.add(new Card(texts.get(2), m ->
                m.getCurrentPlayer().loseMoney(50)));
        ret.add(new Card(texts.get(3), m ->
                m.getCurrentPlayer().gainMoney(50)));
        ret.add(new Card(texts.get(4), m ->
                m.getCurrentPlayer().gainJailCard()));
        ret.add(new Card(texts.get(5), m ->
                m.getCurrentPlayer().gotoJail()));
        ret.add(new Card(texts.get(6), m -> {
            //Take $50 from each player.
            for (Player p : m.getPlayers())
                p.giveMoneyTo(m.getCurrentPlayer(), 50);
        }));
        ret.add(new Card(texts.get(7), m ->
                m.getCurrentPlayer().gainMoney(100)));
        ret.add(new Card(texts.get(8), m ->
                m.getCurrentPlayer().gainMoney(20)));
        ret.add(new Card(texts.get(9), m ->
                m.getCurrentPlayer().gainMoney(100)));
        ret.add(new Card(texts.get(10), m ->
                m.getCurrentPlayer().loseMoney(100)));
        ret.add(new Card(texts.get(11), m ->
                m.getCurrentPlayer().loseMoney(150)));
        ret.add(new Card(texts.get(12), m ->
                m.getCurrentPlayer().gainMoney(25)));
        ret.add(new Card(texts.get(13), m -> {
            Player p = m.getCurrentPlayer();
            int total = 0;
            //Go through the board searching for properties they own with houses on them.
            for (BoardSpace b : m.getBoardSpaces())
                if (b instanceof ColorProperty) {
                    ColorProperty cp = (ColorProperty)b;
                    if (cp.getOwner() == p) {
                        total += cp.getHouses() < 5 ?
                                cp.getHouses() * 40 : 115;
                    }
                }
            p.loseMoney(total);
            System.out.printf("You paid $%d for your houses and hotels.%n", total);
        }));
        ret.add(new Card(texts.get(14), m ->
                m.getCurrentPlayer().gainMoney(10)));
        ret.add(new Card(texts.get(15), m ->
                m.getCurrentPlayer().gainMoney(100)));
        return ret;
    }

}