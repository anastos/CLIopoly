import java.util.Random;

/**
 * Created by Mark on 11/3/15.
 */
class Dice {

    private Random r = new Random();
    private int[] current = new int[2];

    int[] getCurrent() { return current; }
    void setCurrent(int x, int y) {
        current[0] = x;
        current[1] = y;
    }

    int[] roll() {
        //Roll each dice in the set.
        for (int i = 0; i < current.length; i++)
            current[i] = r.nextInt(6) + 1;
        return current;
    }

    String rollString() {
        roll();
        return toString();
    }
    int rollTotal() {
        roll();
        return total();
    }

    int total() {
        return current[0] + current[1];
    }

    boolean isDoubles() {
        return current[0] == current[1];
    }

    @Override
    public String toString() {
        String ret = "(";
        //This loop only matters if there are more than 2 dice.
        for (int i = 0; i < current.length - 1; i++)
            ret += current[i] + ", ";
        ret += current[current.length - 1] + ") - " + total();
        return ret;
    }
}
