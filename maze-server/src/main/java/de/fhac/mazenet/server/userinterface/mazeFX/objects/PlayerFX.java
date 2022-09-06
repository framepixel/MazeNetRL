package de.fhac.mazenet.server.userinterface.mazeFX.objects;

import de.fhac.mazenet.server.userinterface.mazeFX.data.Translate3D;
import de.fhac.mazenet.server.userinterface.mazeFX.util.FakeTranslateBinding;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Created by Richard on 01.06.2016.
 */
public class PlayerFX extends Sphere {

    private static final double RADIUS = 0.15;
    private static final double OFFSET_Y = -0.4;
    private static final double PLAYER_SPECIFIC_OFFSET = 0.15;
	public final int playerId;
	public final Translate3D playerSpecificOffset;

    private CardFX boundCard;
    private FakeTranslateBinding binding;
    private PhongMaterial material;

    private PlayerFX(int id){
        super(RADIUS);
        Color c = playerIdToColor(id);
        material = new PhongMaterial(c);
        setMaterial(material);
	    this.playerId = id;
	    this.playerSpecificOffset = playerIdToOffset(id);
    }

    public PlayerFX(int id, CardFX card){
        this(id);
        bindToCard(card);
    }

    public static String playerIdToColorString(int id){
        //Colors taken from
        //http://materialuicolors.co/?ref=flatuicolors.com
        switch (id) {
            case 1:
                return "#ff5722"; //Deep Orange(Rot)
            case 2:
                return "#009688"; //Teal(Mint)
            case 3:
                return "#ffc107"; //Amber (Orange)
            case 4:
                return "#3f51b5"; //Indigo (Blau)
            default:
                throw new IndexOutOfBoundsException("Spieler-ID nicht vorhanden");
        }
    }

    public static Color playerIdToColor(int id) {
        return Color.valueOf(playerIdToColorString(id));
    }

    public static Translate3D playerIdToOffset(int id) {

        switch (id) {
            case 1:
                return new Translate3D(-PLAYER_SPECIFIC_OFFSET, 0, -PLAYER_SPECIFIC_OFFSET);
            case 2:
                return new Translate3D(+PLAYER_SPECIFIC_OFFSET, 0, +PLAYER_SPECIFIC_OFFSET);
            case 3:
                return new Translate3D(-PLAYER_SPECIFIC_OFFSET, 0, +PLAYER_SPECIFIC_OFFSET);
            case 4:
                return new Translate3D(+PLAYER_SPECIFIC_OFFSET, 0, -PLAYER_SPECIFIC_OFFSET);
            default:
                return new Translate3D(0, 0, 0);
        }
    }

    public CardFX getBoundCard(){
        return boundCard;
    }

    public void bindToCard(CardFX card){
        /*translateXProperty().bind(card.translateXProperty());
        translateYProperty().bind(Bindings.add(OFFSET_Y,card.translateYProperty()));
        translateZProperty().bind(card.translateZProperty());*/
        if(binding!=null) unbindFromCard();
        binding = new FakeTranslateBinding(this,card,getOffset()).bind();
        boundCard=card;
    }

    public void unbindFromCard(){
        boundCard = null;
        binding.unbind();
        binding = null;
        /*translateXProperty().unbind();
        translateYProperty().unbind();
        translateZProperty().unbind();/**/
        /*setTranslateX(getTranslateX());
        setTranslateY(getTranslateY());
        setTranslateZ(getTranslateZ());/**/
    }

    public Translate3D getOffset(){
        return new Translate3D(0,OFFSET_Y,0).translate(playerSpecificOffset);
    }
}
