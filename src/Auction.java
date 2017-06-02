import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 11/21/15.
 */
class Auction {
    private Property property;
    private Player auctioneer;
    private List<Bidder> bidders;
    private Bidder turn;
    private boolean running;

    Auction(Player pl, Property pr, List<Player> players) {
        auctioneer = pl;
        property = pr;
        bidders = new ArrayList<>();
        //Go through all players and add to auction if they are not the one selling.
        players.forEach(p -> {
            if (p != auctioneer)
                bidders.add(new Bidder(p));
        });
        turn = bidders.get(0);
        running = true;

        System.out.printf("%nAn auction has begun for %s.%n", property.getFormattedName());
        //Check if anyone won the auction already, otherwise start it.
        if (!winCheck())
            System.out.printf("%nIt is %s's turn to bid or pass.%n", turn.getPlayer().getName());
    }

    List<Player> getBidders() {
        List<Player> ret = new ArrayList<>();
        //add the Player within each Bidder to the list.
        bidders.forEach(b -> ret.add(b.getPlayer()));
        return ret;
    }
    Player whoseTurn() {
        return turn.getPlayer();
    }
    boolean isRunning() { return running; }

    void stop() {
        property = null;
        auctioneer = null;
        bidders = null;
        turn = null;
        running = false;
    }

    void bid(int amt) {
        //Don't allow bids less than the current bid.
        if (amt <= currentBid())
            System.out.println("You must bid more than the current bid.");
        else if (amt < property.getPrice() / 2)
            System.out.println("You must bid at least half of the normal price.");
        else {
            turn.setBid(amt);
            System.out.printf("%s bid $%s.%n%n", turn.getPlayer().getName(), amt);
            nextBidder();
        }
    }
    void remove(Player p) {
        System.out.printf("%s exited the auction.%n%n", p.getName());
        Bidder next = getNextBidder();
        //Go through the list of bidders; find and remove the player who exited the auction.
        for (Bidder b : bidders)
            if (b.getPlayer() == p) {
                bidders.remove(b);
                break;
            }
        //Check if someone won the auction; otherwise continue with the next bidder.
        if (!winCheck())
            nextBidder(next);
    }

    private int currentBid() {
        return bidders.stream()
                .max((a, b) -> Math.max(a.getBid(), b.getBid()))
                .get().getBid();
    }
    private Bidder getNextBidder() {
        return bidders.get((bidders.indexOf(turn) + 1) % bidders.size());
    }
    private void nextBidder() {
        nextBidder(getNextBidder());
    }
    private void nextBidder(Bidder b) {
        turn = b;
        System.out.printf("It is %s's turn to bid or pass.%n", turn.getPlayer().getName());
    }

    private boolean winCheck() {
        //If there's only 1 bidder left, than they have won.
        if (bidders.size() == 1) {
            Bidder winner = bidders.get(0);
            System.out.printf("%s won the auction.%n", winner.getPlayer().getName());
            winner.getPlayer().buy(property, winner.getBid());
            System.out.println();
            stop();
            return true;
        }
        return false;
    }
}

class Bidder {
    private Player player;
    private int bid;

    Bidder(Player p) {
        player = p;
        bid = 0;
    }

    Player getPlayer() { return player; }
    void setBid(int bid) { this.bid = bid; }
    int getBid() { return bid; }
}