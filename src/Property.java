import java.awt.*;
import java.util.List;

/**
 * Created by Mark on 11/5/15.
 */
abstract class Property extends BoardSpace {

    private int price;
    private Player owner;
    private boolean mortgaged;

    Property(String n, int p) {
        super(n);
        price = p;
        owner = null;
        mortgaged = false;
    }

    int getPrice() { return price; }
    boolean isOwned() { return owner != null; }
    Player getOwner() { return owner; }
    boolean isMortgaged() { return mortgaged; }
    int mortgagePrice() { return price / 2; }

    void setOwner(Player p) { owner = p; }

    void mortgage(List<BoardSpace> boardSpaces) {
        //Cannot mortgage a mortgaged property.
        if (isMortgaged()) {
            System.out.println("This property is already mortgaged..");
            return;
        }
        //Check whether the property or any in its color group have houses.
        if (this instanceof ColorProperty) {
            ColorProperty cp = (ColorProperty)this;
            for (BoardSpace b : boardSpaces)
                if (b instanceof ColorProperty) {
                    ColorProperty cp1 = (ColorProperty) b;
                    if (cp.getGroup() == cp1.getGroup() && cp1.getHouses() > 0) {
                        System.out.println("You cannot mortgage this property.");
                        return;
                    }
                }
        }
        mortgaged = true;
        owner.gainMoney(mortgagePrice());
        System.out.printf("You gained $%d for mortgaging %s.%n",
                mortgagePrice(), getFormattedName());
    }
    void unmortgage() {
        //Check if already unmortgaged, or not enough money
        if (!isMortgaged())
            System.out.println("This property is not mortgaged.");
        else if (owner.getMoney() < mortgagePrice())
            System.out.println("You cannot unmortgage this property.");
        else {
            owner.loseMoney(mortgagePrice());
            mortgaged = false;
            System.out.printf("You paid $%d to unmortgage %s.%n",
                    mortgagePrice(), getFormattedName());
        }
    }

    abstract int rent(List<BoardSpace> allProperties, int roll);
}