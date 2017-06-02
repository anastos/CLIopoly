/**
 * Created by Mark on 11/7/15.
 */
class CardSpace extends BoardSpace {

    private CardType type;

    CardSpace(String n, CardType t) {
        super(n);
        type = t;
    }

    CardType getType() { return type; }
}

enum CardType {

    CHANCE, COMMUNITY_CHEST

}