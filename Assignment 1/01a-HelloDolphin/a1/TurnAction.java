package a1;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class TurnAction extends AbstractInputAction {
	private MyGame game;
	private GameObject av;
	private Vector4f oldUp;
	private Matrix4f oldRotation, newRotation, rotAroundAvatarUp;
	
	public TurnAction(MyGame g) {
		game = g;
	} // end TurnAction Constructor
	
	@Override
	public void performAction(float time, Event e) {
		// allow movement if getStopDol is false		
		if(!game.getStopDol()) {
			time /= 1.5f;
		
			float keyValue = e.getValue();

			if(keyValue > -0.2 && keyValue < 0.2)
				return; // deadzone
		
			av = game.getAvatar();
		
			// detect which component is being activated and perform globalYaw or localPitch accordingly
			if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.A)
				av.globalYaw(new Matrix4f(), time, 0);
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.D)
				av.globalYaw(new Matrix4f(), time, 1);
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.UP)
				av.localPitch(new Matrix4f(), time, 0);
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Key.DOWN)
				av.localPitch(new Matrix4f(), time, 1);
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.RY) {
				if(keyValue > 0)
					av.localPitch(new Matrix4f(), time, 1);
				else if(keyValue < 0)
					av.localPitch(new Matrix4f(), time, 0);
			} // end if
			else if(e.getComponent().getIdentifier() == net.java.games.input.Component.Identifier.Axis.X) {
				if(keyValue > 0)
					av.globalYaw(new Matrix4f(), time, 1);
				else if(keyValue < 0)
					av.globalYaw(new Matrix4f(), time, 0);
			} // end else if
		} // end if
	} // end performAction
} // end TurnAction Class