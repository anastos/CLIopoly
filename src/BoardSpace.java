import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Mark on 11/5/15.
 */
class BoardSpace {

    private String name;

    BoardSpace(String n) {
        name = n;
    }

    String getName() { return name; }
    String getFormattedName() {
        return "\u001B[1m" + name + "\u001B[0m";
    };
    int getNameExtraLength() {
        return getFormattedName().length() - getName().length();
    }

    static List<BoardSpace> importBoardSpaces(String fileName) {
        List<BoardSpace> ret = new ArrayList<>();
        try {
            Scanner s = new Scanner(ClassLoader.getSystemResource(fileName).openStream());
            //Add a space on the board for each line in the file.
            while (s.hasNextLine())
                ret.add(createBoardSpace(s.nextLine()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    private static BoardSpace createBoardSpace(String in) {
        Scanner s = new Scanner(in);
        String type = s.next();
        String name = s.findInLine("\".*\"");
        switch (type) {
            case "cd":
                CardType cardType = CardType.valueOf(s.next());
                return new CardSpace(name, cardType);
            case "cp":
                Group group = Group.valueOf(s.next());
                int price = s.nextInt();
                int[] rents = new int[5];
                //Add the recorded rents to the Property for 0 houses -> hotel.
                for (int i = 0; i < 5; i++)
                    rents[i] = s.nextInt();
                return new ColorProperty(name, group, price, rents);
            case "nl":
                return new NullSpace(name);
            case "rr":
                return new Railroad(name);
            case "sp":
                SpecialType specialType = SpecialType.valueOf(s.next());
                return new SpecialSpace(name, specialType);
            case "ut":
                return new Utility(name);
            default:
                return null;
        }
    }

}



