import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by Mark on 11/3/15.
 */
class Player {

    private String name;
    private int pos, money, turnInJail, jailCards, doublesCount;
    private boolean turn, mustRoll, mustAddressProperty, mustPay;
    private Map<String, BiConsumer<Monopoly, String>> commands;

    Player(String name) {
        this.name = name;
        pos = 0;
        money = 1500;
        commands = makeCommands();
        turnInJail = -1;
    }

    public String getName() { return name; }
    void mustAddressProperty() { mustAddressProperty = true; }

    boolean isTurn() { return turn; }
    void initializeTurn() {
        turn = true;
        mustRoll = true;
        mustAddressProperty = false;
        mustPay = false;
        doublesCount = 0;
        //If they are in jail, increase the turnInJail number.
        if (isInJail())
            turnInJail++;
        System.out.printf("%nIt is now %s's turn.%n", getName());
    }
    void forceEndTurn() {
        turn = false;
    }

    boolean isInJail() { return turnInJail > -1; }
    int getTurnInJail() { return turnInJail; }
    void gotoJail() {
        pos = 10;
        mustRoll = false;
        turnInJail = 0;
    }

    int getPos() { return pos; }
    void move(int n, List<BoardSpace> boardSpaces) {
        int oldPos = pos;
        pos = (pos + n) % 40;
        BoardSpace sp = boardSpaces.get(pos);
        //If they passed go, collect $200.
        if (pos < oldPos) {
            gainMoney(200);
            System.out.println("You passed go and collected $200.");
        }
        System.out.printf("You landed on %s.%n", sp.getFormattedName());
    }

    int getMoney() { return money; }
    void gainMoney(int n) { money += n; }
    void loseMoney(int n) { money -= n; }
    void giveMoneyTo(Player p, int n) {
        loseMoney(n);
        p.gainMoney(n);
    }

    void gainJailCard() { jailCards++; }

    void buy(Property p) {
        buy(p, p.getPrice());
    }
    void buy(Property p, int price) {
        loseMoney(price);
        p.setOwner(this);
        System.out.printf("%s bought %s for $%d.%n", getName(), p.getFormattedName(), price);
    }

    List<Property> getProperties(List<BoardSpace> boardSpaces) {
        Property[] p = boardSpaces.stream()
                .filter(b -> b instanceof Property)
                .filter(b -> ((Property)b).getOwner() == this)
                .toArray(Property[]::new);

        return new ArrayList<>(Arrays.asList(p));
    }
    int totalAssets(List<BoardSpace> boardSpaces) {
        int ret = getMoney();
        //Check if they own each property, and if they have any houses/hotel on them.
        for (Property p : getProperties(boardSpaces))
            if (p.isMortgaged())
                ret += p.getPrice() / 2;
            else {
                ret += p.getPrice();
                if (p instanceof ColorProperty)
                    ret += ((ColorProperty)p).getHouses() *
                            ((ColorProperty)p).getGroup().getBuildPrice();
            }
        return ret;
    }

    private Map<String, BiConsumer<Monopoly, String>> makeCommands() {
        Map<String, BiConsumer<Monopoly, String>> ret = new HashMap<>();
        ret.put("roll", this::CMDRoll);
        ret.put("buy", this::CMDBuy);
        ret.put("card", this::CMDCard);
        ret.put("pay", this::CMDPay);
        ret.put("end", this::CMDEnd);
        ret.put("money", this::CMDMoney);
        ret.put("auction", this::CMDAuction);
        ret.put("bid", this::CMDBid);
        ret.put("pass", this::CMDPass);
        ret.put("mortgage", this::CMDMortgage);
        ret.put("unmortgage", this::CMDUnmortgage);
        ret.put("build", this::CMDBuild);
        ret.put("unbuild", this::CMDUnbuild);
        ret.put("forfeit", this::CMDForfeit);
        ret.put("cheat", (m, s) -> gainMoney(1000));
        return ret;
    }

    void executeCommand(Monopoly m, String cmd) {
        Scanner cs = new Scanner(cmd);
        //Check if no command was entered.
        if (!cs.hasNext()) {
            System.out.println("No command entered.");
            return;
        }

        String first = cs.next();
        //Check if command has been defined in "commands".
        if (commands.containsKey(first))
            commands.get(first).accept(m, cs.hasNext() ? cs.nextLine().trim() : null);
        else
            System.out.println("Command not recognized.");
    }

    private void CMDRoll(Monopoly m, String s) {
        //Cannot roll if it is not your turn or if you must do something else.
        if (!isTurn()) {
            System.out.println("It is not your turn.");
            return;
        }
        if (!mustRoll || mustAddressProperty || m.isAuctioning()) {
            System.out.println("You cannot roll.");
            return;
        }
        Dice d = m.getDice();
        boolean wasInJail;
        //String will only not be null if they were in jail and had to pay after 3 rounds.
        if (s == null) {
            System.out.printf("You rolled %s.%n", d.rollString());
            wasInJail = isInJail();
        } else {
            wasInJail = true;
            Scanner sc = new Scanner(s);
            d.setCurrent(sc.nextInt(), sc.nextInt());
        }
        //If they are in jail, check if they got doubles, or if they've been in jail for >3 rounds.
        if (isInJail())
            if (d.isDoubles())
                turnInJail = -1;
            else {
                if (getTurnInJail() >= 3) {
                    System.out.println("You must pay.");
                    mustPay = true;
                } else
                    System.out.println("You remain in jail.");
                mustRoll = false;
                return;
            }
        //Only can roll again if they got doubles and the weren't in jail.
        if (wasInJail || !d.isDoubles())
            mustRoll = false;
        else
            doublesCount++;
        //Go to jail if they get doubles three times in a row.
        if (doublesCount >= 3) {
             gotoJail();
             System.out.println("You are sent to jail for rolling doubles thrice in a row.");
            return;
        }

        move(d.total(), m.getBoardSpaces());

        BoardSpace space = m.getBoardSpaces().get(getPos());
        //Check what kind of space they landed on.
        if (space instanceof Property) {
            Property prop = (Property) space;
            Player owner = prop.getOwner();
            //Check whether the property is owned, mortgaged, etc.
            if (owner == null) {
                System.out.printf("This property costs $%d.%n", prop.getPrice());
                mustAddressProperty();
            } else if (owner == this)
                System.out.println("You own this property.");
            else if (!prop.isMortgaged()) {
                int rent = prop.rent(m.getBoardSpaces(), d.total());
                giveMoneyTo(owner, rent);
                System.out.printf("You paid $%d rent to %s.%n", rent, owner.getName());
            }
        }
        else if (space instanceof SpecialSpace) {
            ((SpecialSpace) space).doFunc(m);
        }
        else if (space instanceof CardSpace) {
            CardSpace cdsp = (CardSpace) space;
            Deck deck = cdsp.getType() == CardType.CHANCE ?
                    m.getChance() : m.getCommunityChest();
            Card card = deck.draw();
            System.out.println(card.getText());
            card.doFunc(m);
        }
    }
    private void CMDBuy(Monopoly m, String s) {
        //Make sure its their turn, they can buy something, etc.
        if (!isTurn())
            System.out.println("It is not your turn.");
        else {
            BoardSpace bs = m.getBoardSpaces().get(getPos());
            if (!(mustAddressProperty && bs instanceof Property))
                System.out.println("You cannot buy a property.");
            else {
                Property prop = (Property) bs;
                //Make sure they have enough money to but the property.
                if (getMoney() < prop.getPrice())
                    System.out.println("You cannot afford this property.");
                else {
                    buy(prop);
                    mustAddressProperty = false;
                }
            }
        }
    }
    private void CMDAuction(Monopoly m, String s) {
        //Make sure they can start an auction.
        if (!isTurn())
            System.out.println("It is not your turn.");
        else if (!mustAddressProperty)
            System.out.println("You cannot auction a property.");
        else {
            mustAddressProperty = false;
            m.startAuction(this, (Property) m.getBoardSpaces().get(getPos()));
        }
    }
    private void CMDBid(Monopoly m, String s) {
        //Make sure they can bid in an auction.
        if (!m.isAuctioning())
            System.out.println("There is no auction.");
        else if (m.getAuction().whoseTurn() != this)
            System.out.println("It is not your turn.");
        else if (s == null || !(new Scanner(s).hasNextInt()))
            System.out.println("You must input the price you will bid.");
        else {
            int amt = (new Scanner(s)).nextInt();
            if (getMoney() < amt) {
                System.out.println("You cannot afford to bid this much.");
                return;
            }
            m.getAuction().bid(amt);
        }
    }
    private void CMDPass(Monopoly m, String s) {
        //Make sure there is an auction they are in.
        if (!m.isAuctioning())
            System.out.println("There is no auction.");
        else if (!m.getAuction().getBidders().contains(this))
            System.out.println("You are not in the auction.");
        else m.getAuction().remove(this);
    }
    private void CMDCard(Monopoly m, String s) {
        //Make sure they are in jail, and can use a card, etc.
        if (!isTurn())
            System.out.println("It is not your turn.");
        else if (!isInJail() || !mustRoll || mustPay || jailCards <= 0)
            System.out.println("You cannot use a \"Get Out of Jail Free\" card.");
        else {
            turnInJail = -1;
            jailCards--;
            System.out.println("You used a \"Get Out of Jail Free\" card.");
        }
    }
    private void CMDPay(Monopoly m, String s) {
        //Make sure they are in jail, and can pay to get out, etc.
        if (!isTurn())
            System.out.println("It is not your turn");
        else if (!isInJail() || !(mustRoll || mustPay))
            System.out.println("You cannot pay to get out of jail.");
        else if (getMoney() < 50)
            System.out.println("You cannot afford to pay.");
        else {
            loseMoney(50);
            turnInJail = -1;
            System.out.println("You paid $50 to get out of jail.");
            if (mustPay) {
                mustPay = false;
                mustRoll = true;
                CMDRoll(m, m.getDice().getCurrent()[0] + " " + m.getDice().getCurrent()[1]);
            }
        }
    }
    private void CMDEnd(Monopoly m, String s) {
        //Make sure they can end their turn.
        if (!isTurn())
            System.out.println("It is not your turn.");
        else if (mustRoll || mustAddressProperty || mustPay || m.isAuctioning())
            System.out.println("You cannot end your turn.");
        else {
            if (getMoney() < 0) {
                System.out.println("You forfeit for having debt at the end of your turn.");
                CMDForfeit(m, null);
            }
            turn = false;
        }
    }
    private void CMDMoney(Monopoly m, String s) {
        System.out.println("$" + getMoney());
    }
    private void CMDMortgage(Monopoly m, String s) {
        if (s == null)
            System.out.println("You must input the property you want to mortgage.");
        else {
            Property p = null;
            //Find the property they want to mortgage.
            for (BoardSpace b : m.getBoardSpaces())
                if (b instanceof Property && b.getName().equals(s)) {
                    p = (Property) b;
                    break;
                }
            //Make sure they own the property.
            if (p == null)
                System.out.println("Input is not a property name.");
            else if (p.getOwner() != this)
                System.out.println("You do not own this property.");
            else p.mortgage(m.getBoardSpaces());
        }
    }
    private void CMDUnmortgage(Monopoly m, String s) {
        if (s == null) {
            System.out.println("You must input the property you want to unmortgage.");
            return;
        }
        Property p = null;
        //Find the property they want to unmortgage.
        for (BoardSpace b : m.getBoardSpaces())
            if (b instanceof Property && b.getName().equals(s)) {
                p = (Property) b;
                break;
            }
        //Make sure the own the property.
        if (p == null)
            System.out.println("Input is not a property name.");
        else if (p.getOwner() != this)
            System.out.println("You cannot unmortgage this property.");
        else p.unmortgage();
    }
    private void CMDBuild(Monopoly m, String s) {
        if (s == null) {
            System.out.println("You must input the property you want to build on.");
            return;
        }
        ColorProperty cp = null;
        //Find the property they want to build on.
        for (BoardSpace b : m.getBoardSpaces())
            if (b instanceof ColorProperty && b.getName().equals(s)) {
                cp = (ColorProperty) b;
                break;
            }
        //Make sure they own the property.
        if (cp == null) {
            System.out.println("Input is not a buildable property name.");
            return;
        }
        if (cp.getOwner() != this) {
            System.out.println("You do not own this property.");
            return;
        }
        cp.build(m);
    }
    private void CMDUnbuild(Monopoly m, String s) {
        if (s == null) {
            System.out.println("You must input the property you want to unbuild on.");
            return;
        }
        ColorProperty cp = null;
        //Find the property they want to unbuild from.
        for (BoardSpace b : m.getBoardSpaces())
            if (b instanceof ColorProperty && b.getName().equals(s)) {
                cp = (ColorProperty) b;
                break;
            }
        //Make sure they own the property.
        if (cp == null) {
            System.out.println("Input is not a buildable property name.");
            return;
        }
        if (cp.getOwner() != this) {
            System.out.println("You do not own this property.");
        }
        cp.unbuild(m);
    }
    private void CMDForfeit(Monopoly m, String s) {
        for (Property p : getProperties(m.getBoardSpaces())) {
            if (p instanceof ColorProperty)
                ((ColorProperty) p).resetHouses(m);
            p.setOwner(null);
        }
        m.getPlayers().remove(this);
    }
}