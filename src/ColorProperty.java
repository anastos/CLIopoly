import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mark on 11/5/15.
 */
class ColorProperty extends Property {

    private Group group;
    private int[] rents;
    private int houses;

    private static final Map<Group, String> COLORS =
            new HashMap<Group, String>() {{
                put(Group.PURPLE, "\u001B[38;5;57m");
                put(Group.LIGHT_BLUE, "\u001B[38;5;14m");
                put(Group.PINK, "\u001B[38;5;13m");
                put(Group.ORANGE, "\u001B[38;5;208m");
                put(Group.RED, "\u001B[38;5;9m");
                put(Group.YELLOW, "\u001B[38;5;11m");
                put(Group.GREEN, "\u001B[38;5;10m");
                put(Group.BLUE, "\u001B[38;5;12m");
            }};

    ColorProperty(String n, Group g, int p, int[] r) {
        super(n, p);
        group = g;
        rents = r;
        houses = 0;
    }

    Group getGroup() { return group; }
    int getHouses() { return houses; }

    void build(Monopoly m) {
        //Don't allow building more than 5 houses (1 hotel).
        if (getHouses() > 4) {
            System.out.println("This property is already built to its maximum capacity.");
            return;
        }
        //Cannot build on a mortgaged property.
        if (isMortgaged()) {
            System.out.println("You cannot build on a mortgaged property.");
            return;
        }
        //Search through all the spaces on the board and find any that are of the same color group as this.
        for (BoardSpace b : m.getBoardSpaces())
            if (b instanceof ColorProperty) {
                ColorProperty cp = (ColorProperty) b;
                if (getGroup() == cp.getGroup()) {
                    //Cannot build unless you own the entire color group.
                    if (cp.getOwner() != getOwner()) {
                        System.out.println("You do not own the entire color group.");
                        return;
                    }
                    //Cannot build unevenly or when other properties in the group are mortgaged.
                    if (cp.isMortgaged() || cp.getHouses() < getHouses()) {
                        System.out.println("You must build evenly throughout the color group.");
                        return;
                    }
                }
            }
        //Check if player has enough money to build, and if there are any house pieces left to build with.
        if (getOwner().getMoney() < getGroup().getBuildPrice())
            System.out.println("You cannot afford to build on this property.");
        else if (houses < 4 && !m.useHouse())
            System.out.println("There are not any houses left.");
        else if (houses == 4 && !m.useHotel())
            System.out.println("There are not any hotels left.");
        else {
            getOwner().loseMoney(getGroup().getBuildPrice());
            houses++;
            System.out.printf("You built a %s on %s for $%d.%n",
                    getHouses() == 5 ? "hotel" : "house", getFormattedName(), getGroup().getBuildPrice());
        }
    }
    void unbuild(Monopoly m) {
        //Check if there are any houses to unbuild on the property.
        if (getHouses() == 0) {
            System.out.println("This property has no houses or hotels.");
            return;
        }
        //Search through all the spaces on the board and find any that are of the same color group as this.
        for (BoardSpace b : m.getBoardSpaces())
            if (b instanceof ColorProperty) {
                ColorProperty cp = (ColorProperty) b;
                if (getGroup() == cp.getGroup())
                    //Cannot build unevenly in a color group.
                    if (getHouses() < cp.getHouses()) {
                        System.out.println("You must unbuild evenly throughout the color group.");
                        return;
                    }
            }
        //Check if there are any house pieces left to go from 1 hotel to 4 houses.
        if (houses == 5 && !m.unuseHotel()) {
            System.out.println("There are not any houses left.");
            return;
        }
        //Put a house back in the pile if there isn't a hotel on the property.
        if (houses < 5)
            m.unuseHouse();

        houses--;
        getOwner().gainMoney(getGroup().getUnbuildPrice());
        System.out.printf("You unbuilt a %s on %s for $%d.%n",
                getHouses() == 4 ? "hotel" : "house", getFormattedName(), getGroup().getUnbuildPrice());
    }

    void resetHouses(Monopoly m) {
        if (houses == 5)
            m.setHotels(m.getHotels() + 1);
        else
            m.setHouses(m.getHouses() + getHouses());
        houses = 0;
    }

    @Override
    int rent(List<BoardSpace> allProperties, int roll) {
        //If there are houses/hotels on the property, get rent from the rents array.
        if (houses > 0)
            return rents[houses];

        int owned = (int)allProperties.stream()
                .filter(p -> p instanceof ColorProperty)
                .filter(p -> ((ColorProperty)p).getGroup() == group)
                .filter(p -> ((ColorProperty)p).getOwner() == getOwner())
                .count();

        return owned == group.getNumberOfProperties() ?
                2 * rents[0] : rents[0];
    }

    @Override
    String getFormattedName() {
        return COLORS.get(getGroup()) + super.getFormattedName();
    }
}

enum Group {

    PURPLE      (2, 50),
    LIGHT_BLUE  (3, 50),
    PINK        (3, 100),
    ORANGE      (3, 100),
    RED         (3, 150),
    YELLOW      (3, 150),
    GREEN       (3, 200),
    BLUE        (2, 200);

    private int numberOfProperties;
    private int housePrice;
    Group(int num, int housePrice) {
        numberOfProperties = num;
        this.housePrice = housePrice;
    }

    int getBuildPrice() {
        return housePrice;
    }
    int getUnbuildPrice() {
        return housePrice / 2;
    }
    int getNumberOfProperties() {
        return numberOfProperties;
    }

}
