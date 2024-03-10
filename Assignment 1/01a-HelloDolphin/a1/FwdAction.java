package a1;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class FwdAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av;
	private Vector3f oldPosition, newPosition;
	private Vector3f fwdDirection;
	
	public FwdAction(MyGame g) {
		game = g;
	} // end FwdAction Constructor
	
	@Override
	public void performAction(float time, Event e) {
		// allow movement if getStopDol is false
		if(!game.getStopDol()) {
			time /= 1.5f;
		
			float keyValue = e.getValue();
			if(keyValue > -0.2 && keyValue < 0.2)
				return; // deadzone

			av = game.getAvatar();
			oldPosition = av.getWorldLocation();
			fwdDirection = av.getWorldForwardVector();
			
			// detect which component is being activated and modify newPosition accordingly
			if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.W)
				newPosition = oldPosition.add(fwdDirection.mul(time));
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.S)
				newPosition = oldPosition.add(fwdDirection.mul(-time));
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.Y) {
				if(keyValue > 0)
					newPosition = oldPosition.add(fwdDirection.mul(-time));
				else if(keyValue < 0)
					newPosition = oldPosition.add(fwdDirection.mul(time));
			} // end else if 
		
			av.setLocalLocation(newPosition);
		} // end if
	} // end performAction
} // end FwdAction Class