import java.util.function.Consumer;

/**
 * Created by Mark on 11/11/15.
 */
class Card {

    private String text;
    private Consumer<Monopoly> func;

    Card(String text, Consumer<Monopoly> func) {
        this.text = text;
        this.func = func;
    }

    String getText() {
        return text;
    }

    void doFunc(Monopoly m) {
        func.accept(m);
    }
}
