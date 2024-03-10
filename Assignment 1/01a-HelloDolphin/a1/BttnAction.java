package a1;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class BttnAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av;
	private boolean rideDolphin = false;
	
	public BttnAction(MyGame g) {
		game = g;
	} // end BttnAction Constructor
	
	@Override
	public void performAction(float time, Event e) {
		av = game.getAvatar();
		
		// hop on/off dolphin
		if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._0 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.SPACE) {
			rideDolphin = !rideDolphin;
			game.setRideDolphin(rideDolphin);
		} // end if
		
		// toggle dolphin wireframe
		if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._2 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key._2)
			av.getRenderStates().setWireframe(true);
		else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Button._3 ||
			e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key._3)
			av.getRenderStates().setWireframe(false);
	} // end performAction
} // end BttnAction Class