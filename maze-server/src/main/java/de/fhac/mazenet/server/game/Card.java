package de.fhac.mazenet.server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import de.fhac.mazenet.server.generated.CardData;
import de.fhac.mazenet.server.generated.Treasure;
import de.fhac.mazenet.server.tools.Messages;

public class Card extends CardData {

    public Card(CardData c) {
        super();
        this.setOpenings(new Openings());
        this.getOpenings().setBottom(c.getOpenings().isBottom());
        this.getOpenings().setLeft(c.getOpenings().isLeft());
        this.getOpenings().setRight(c.getOpenings().isRight());
        this.getOpenings().setTop(c.getOpenings().isTop());
        this.setTreasure(c.getTreasure());
        this.setPin(new Pin());
        if (c.getPin() != null) {
            this.pin.getPlayerID().addAll(c.getPin().getPlayerID());
        } else {
            this.setPin(null);
        }
    }

    public Card(CardShape shape, Orientation orientation, Treasure treasure) {
        super();
        this.setOpenings(new Openings());
        this.setPin(new Pin());
        this.pin.getPlayerID();
        switch (shape) {
        case I:
            switch (orientation) {
            case D180:
            case D0:
                this.openings.setBottom(true);
                this.openings.setTop(true);
                this.openings.setLeft(false);
                this.openings.setRight(false);
                break;
            case D270:
            case D90:
                this.openings.setBottom(false);
                this.openings.setTop(false);
                this.openings.setLeft(true);
                this.openings.setRight(true);
                break;
            default:
                throw new IllegalArgumentException("Ungültige Drehung. Erlaub sind nur (D0,D90,D180,D270)");
            }
            break;
        case L:
            switch (orientation) {
            case D180:
                this.openings.setBottom(true);
                this.openings.setTop(false);
                this.openings.setLeft(true);
                this.openings.setRight(false);
                break;
            case D270:
                this.openings.setBottom(false);
                this.openings.setTop(true);
                this.openings.setLeft(true);
                this.openings.setRight(false);
                break;
            case D90:
                this.openings.setBottom(true);
                this.openings.setTop(false);
                this.openings.setLeft(false);
                this.openings.setRight(true);
                break;
            case D0:
                this.openings.setBottom(false);
                this.openings.setTop(true);
                this.openings.setLeft(false);
                this.openings.setRight(true);
                break;
            default:
                throw new IllegalArgumentException("Ungültige Drehung. Erlaub sind nur (D0,D90,D180,D270)");
            }
            break;
        case T:
            switch (orientation) {
            case D180:
                this.openings.setBottom(false);
                this.openings.setTop(true);
                this.openings.setLeft(true);
                this.openings.setRight(true);
                break;
            case D270:
                this.openings.setBottom(true);
                this.openings.setTop(true);
                this.openings.setLeft(false);
                this.openings.setRight(true);
                break;
            case D90:
                this.openings.setBottom(true);
                this.openings.setTop(true);
                this.openings.setLeft(true);
                this.openings.setRight(false);
                break;
            case D0:
                this.openings.setBottom(true);
                this.openings.setTop(false);
                this.openings.setLeft(true);
                this.openings.setRight(true);
                break;
            default:
                throw new IllegalArgumentException("Ungültige Drehung. Erlaub sind nur (D0,D90,D180,D270)");
            }
            break;
        default:
            throw new IllegalArgumentException("Ungültige Form. Erlaub sind nur (I,L,T)");
        }
        this.treasure = treasure;
    }

    /**
     * gibt alle moeglichen Rotationen der Karte zurueck (Convenience fuer Clients)
     *
     * @return Liste aller rotierten Karten
     */
    public List<Card> getPossibleRotations() {
        List<Card> cards = new ArrayList<>();
        CardShape shape = getShape();
        Treasure treasure = getTreasure();
        // Reihenfolge und fehlende breaks wichtig
        switch (shape) {
        case L:
        case T:
            cards.add(new Card(shape, Orientation.D180, treasure));
            cards.add(new Card(shape, Orientation.D270, treasure));
        case I:
            cards.add(new Card(shape, Orientation.D0, treasure));
            cards.add(new Card(shape, Orientation.D90, treasure));
            break;
        default:
            System.err.print(Messages.getString("Card.invalidShape"));
            break;
        }
        return cards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CardData))
            return false;
        Card card = new Card((CardData) o);
        return this.getShape() == card.getShape()
                && Objects.equals(this.getPin().getPlayerID(), card.getPin().getPlayerID())
                && this.getTreasure() == card.getTreasure();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getShape(), this.getPin().getPlayerID(), this.getTreasure());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ------ \n");
        StringBuilder line1 = new StringBuilder("|");
        StringBuilder line2 = new StringBuilder("|");
        StringBuilder line3 = new StringBuilder("|");
        StringBuilder line4 = new StringBuilder("|");
        StringBuilder line5 = new StringBuilder("|");
        StringBuilder line6 = new StringBuilder("|");
        Card card = new Card(this);
        if (card.getOpenings().isTop()) {
            line1.append("##  ##|");
            line2.append("##  ##|");
        } else {
            line1.append("######|");
            line2.append("######|");
        }
        if (card.getOpenings().isLeft()) {
            line3.append("  ");
            line4.append("  ");
        } else {
            line3.append("##");
            line4.append("##");
        }
        if (card.getPin().getPlayerID().size() != 0) {
            line3.append("S");
        } else {
            line3.append(" ");
        }
        if (card.getTreasure() != null) {
            String name = card.getTreasure().name();
            switch (name.charAt(1)) {
            case 'Y':
                // Symbol
                line3.append("T");
                break;
            case 'T':
                // Startpunkt
                line3.append("S");
                break;
            }
            line4.append(name.substring(name.length() - 2));
        } else {
            line3.append(" ");
            line4.append("  ");
        }
        if (card.getOpenings().isRight()) {
            line3.append("  |");
            line4.append("  |");
        } else {
            line3.append("##|");
            line4.append("##|");
        }
        if (card.getOpenings().isBottom()) {
            line5.append("##  ##|");
            line6.append("##  ##|");
        } else {
            line5.append("######|");
            line6.append("######|");
        }

        sb.append(line1.append("\n"));
        sb.append(line2.append("\n"));
        sb.append(line3.append("\n"));
        sb.append(line4.append("\n"));
        sb.append(line5.append("\n"));
        sb.append(line6.append("\n"));
        sb.append(" ------ \n");

        return sb.toString();
    }

    public CardShape getShape() {
        boolean[] open = new boolean[4];
        open[0] = getOpenings().isTop();
        open[1] = getOpenings().isRight();
        open[2] = getOpenings().isBottom();
        open[3] = getOpenings().isLeft();

        int indsum = 0;
        int numberOfOpenings = 0;

        for (int i = 0; i < open.length; i++) {
            if (open[i]) {
                indsum += i;
                ++numberOfOpenings;
            }
        }
        if (numberOfOpenings == 2 && indsum % 2 == 0) {
            return CardShape.I;
        } else if (numberOfOpenings == 2 && indsum % 2 == 1) {
            return CardShape.L;
        } else if (numberOfOpenings == 3) {
            return CardShape.T;
        } else {
            throw new NoSuchElementException("Die Karte entspricht keiner gültigen Form");
        }
    }

    public Orientation getOrientation() {
        switch (getShape()) {
        case I:
            if (getOpenings().isTop()) {
                return Orientation.D0;
            }
            return Orientation.D90;
        case L:
            if (getOpenings().isTop() && getOpenings().isRight()) {
                return Orientation.D0;
            } else if (getOpenings().isRight() && getOpenings().isBottom()) {
                return Orientation.D90;
            } else if (getOpenings().isBottom() && getOpenings().isLeft()) {
                return Orientation.D180;
            } else { // if(getOpenings().isLeft() && getOpenings().isTop()){
                return Orientation.D270;
            }
        case T:
            if (!getOpenings().isTop()) {
                return Orientation.D0;
            } else if (!getOpenings().isRight()) {
                return Orientation.D90;
            } else if (!getOpenings().isBottom()) {
                return Orientation.D180;
            } else {// if(!getOpenings().isLeft()){
                return Orientation.D270;
            }
        default:
            throw new NoSuchElementException("Die Karte kann nur im rechten Winkel gedreht werden");
        }

    }

    public enum CardShape {
        L, T, I
    }

    public enum Orientation {

        D0(0), D90(90), D180(180), D270(270);

        final int value;

        Orientation(int value) {
            this.value = value;
        }

        public static Orientation fromValue(int v) {
            for (Orientation c : Orientation.values()) {
                if (c.value == v) {
                    return c;
                }
            }
            throw new IllegalArgumentException(String.valueOf(v));
        }

        public int value() {
            return value;
        }
    }

}
