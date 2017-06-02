import java.util.List;

/**
 * Created by Mark on 11/7/15.
 */
class Railroad extends Property {

    Railroad(String n) {
        super(n, 200);
    }

    @Override
    int rent(List<BoardSpace> allProperties, int roll) {
        int count = (int)allProperties.stream()
                .filter(p -> p instanceof Railroad)
                .filter(p -> ((Railroad)p).getOwner() == getOwner())
                .count();

        return 25 * (int)Math.pow(2, count - 1);
    }

    @Override
    String getFormattedName() {
        return "\u001B[38;5;8m" + super.getFormattedName();
    }
}
