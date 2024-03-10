package a1;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;

	private boolean paused=false;
	private int counter=0;
	private double lastFrameTime, currFrameTime, elapsTime, timeElapsed;
	
	private boolean rideDolphin = false, spacePushed = false;
	private boolean stopDol = false;
	private boolean visitedSphere = false, visitedCube = false, visitedTorus = false, visitedPlane = false;
	private boolean powerUpActive = false;
	
	private Camera cam;
	
	private	Vector3f loc, fwd, up, right, newLocation; 
	private Vector3f localPitch, globalYaw;

	private GameObject dol, sphere, cube, torus, plane; 
	private GameObject xLine, yLine, zLine; 
	private GameObject octBin, spherePc, cubePc, torusPc, planePc;
	private GameObject powerUp;
	
	private ObjShape dolS, sphereShape, cubeShape, torusShape, planeShape;
	private ObjShape xLineS, yLineS, zLineS;
	private ObjShape octBinS, spherePcS, cubePcS, torusPcS, planePcS;
	private ObjShape powerUpS;
	
	private TextureImage doltx, sphereTx, cubeTx, torusTx, planeTx;
	private TextureImage octBinTX, spherePcTx, cubePcTx, torusPcTx, planePcTx;
	private TextureImage powerUpTx;
	
	private Light light1, light2;
	
	private float camDolDistance = 0.0f;
	
	private int score = 0;
	
	private InputManager im;
	
	public MyGame() { super(); } // end MyGame

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	} // end main

	@Override
	public void loadShapes()
	{
		// load dolphin shape
		dolS = new ImportedModel("dolphinHighPoly.obj");
		
		// load visit objects shapes
		cubeShape = new Cube();
		sphereShape = new Sphere();
		torusShape = new Torus();
		planeShape = new Plane();

		// load xyz world axes shapes
		xLineS = new Line((new Vector3f(0, 0, 0)), (new Vector3f(1, 0, 0)));
		yLineS = new Line((new Vector3f(0, 0, 0)), (new Vector3f(0, 1, 0)));
		zLineS = new Line((new Vector3f(0, 0, 0)), (new Vector3f(0, 0, 1)));
		
		// load octBin manual object
		octBinS = new ManualOctBin();
		
		// load visit shapes postcards shapes
		spherePcS = new Plane();
		cubePcS = new Plane();
		torusPcS = new Plane();
		planePcS = new Plane();
		
		// load powerUp shape
		powerUpS = new Sphere();
	} // end loadShapes

	@Override
	public void loadTextures()
	{
		// dolphin texture
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
	
		// visit objects textures
		cubeTx = new TextureImage("medieval-brick-wall.jpg");
		sphereTx = new TextureImage("customTexture2.png");
		torusTx = new TextureImage("customTexture.png");
		planeTx = new TextureImage("blackboard-green-old.jpg");
		
		// octBin manual object texture
		octBinTX = new TextureImage("silver.png");
		
		// textures for the visit object postcards
		spherePcTx = new TextureImage("customSpherePostcard.png");
		cubePcTx = new TextureImage("brickCubePostcard.png");
		torusPcTx = new TextureImage("customTorusPostcard.png");
		planePcTx = new TextureImage("blackboardPostcard.png");
		
		// powerUp texture
		powerUpTx = new TextureImage("sun.png");
	} // end loadTextures

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale;

		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
		initialScale = (new Matrix4f()).scaling(3.0f);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);
		
		// build cube visit object
		cube = new GameObject(GameObject.root(), cubeShape, cubeTx);
		initialTranslation = (new Matrix4f()).translation(-7, -5, 2);
		initialScale = (new Matrix4f()).scaling(0.5f);
		cube.setLocalTranslation(initialTranslation);
		cube.setLocalScale(initialScale);
		
		// build sphere visit object
		sphere = new GameObject(GameObject.root(), sphereShape, sphereTx);
		initialTranslation = (new Matrix4f()).translation(-10, 4, -5);
		initialScale = (new Matrix4f()).scaling(2.0f);
		sphere.setLocalTranslation(initialTranslation);
		sphere.setLocalScale(initialScale);
		
		// build torus visit object
		torus = new GameObject(GameObject.root(), torusShape, torusTx);
		initialTranslation = (new Matrix4f()).translation(6, -3, 3);
		initialScale = (new Matrix4f()).scaling(1.5f);
		torus.setLocalTranslation(initialTranslation);
		torus.setLocalScale(initialScale);
		
		// build plane visit object
		plane = new GameObject(GameObject.root(), planeShape, planeTx);
		initialTranslation = (new Matrix4f()).translation(5, 5, 5);
		initialScale = (new Matrix4f()).scaling(1.0f);
		plane.setLocalTranslation(initialTranslation);
		plane.setLocalScale(initialScale);
		plane.setLocalRotation((new Matrix4f()).rotation((float)Math.toRadians(90), 1, 0, 0));
		
		// build xyz world axes
		xLine = new GameObject(GameObject.root(), xLineS, null);
		xLine.getRenderStates().setColor(new Vector3f(1, 0, 0));
		yLine = new GameObject(GameObject.root(), yLineS, null);
		yLine.getRenderStates().setColor(new Vector3f(0, 1, 0));
		zLine = new GameObject(GameObject.root(), zLineS, null);
		zLine.getRenderStates().setColor(new Vector3f(0, 0, 1));
		
		// build octagon bin manual object
		octBin = new GameObject(GameObject.root(), octBinS, octBinTX);
		initialTranslation = (new Matrix4f()).translation(0, -5, 0);
		initialScale = (new Matrix4f()).scaling(1.0f);
		octBin.setLocalTranslation(initialTranslation);
		octBin.setLocalScale(initialScale);
		
		// build sphere postcard located inside octBin
		spherePc = new GameObject(GameObject.root(), spherePcS, spherePcTx);
		initialTranslation = (new Matrix4f()).translation(0.5f, -5, 0);
		initialScale = (new Matrix4f()).scaling(0.0f);
		spherePc.setLocalTranslation(initialTranslation);
		spherePc.setLocalScale(initialScale);
		
		// build cube postcard located inside octBin
		cubePc = new GameObject(GameObject.root(), cubePcS, cubePcTx);
		initialTranslation = (new Matrix4f()).translation(-0.5f, -5, 0);
		initialScale = (new Matrix4f()).scaling(0.0f);
		cubePc.setLocalTranslation(initialTranslation);
		cubePc.setLocalScale(initialScale);
		
		// build torus postcard located inside octBin
		torusPc = new GameObject(GameObject.root(), torusPcS, torusPcTx);
		initialTranslation = (new Matrix4f()).translation(0, -5, 0.5f);
		initialScale = (new Matrix4f()).scaling(0.0f);
		torusPc.setLocalTranslation(initialTranslation);
		torusPc.setLocalScale(initialScale);
		
		// build plane postcard located inside octBin
		planePc = new GameObject(GameObject.root(), planePcS, planePcTx);
		initialTranslation = (new Matrix4f()).translation(0, -5, -0.5f);
		initialScale = (new Matrix4f()).scaling(0.0f);
		planePc.setLocalTranslation(initialTranslation);
		planePc.setLocalScale(initialScale);
		
		// build powerup
		powerUp = new GameObject(GameObject.root(), powerUpS, powerUpTx);
		initialTranslation = (new Matrix4f()).translation(5, 6, -3);
		initialScale = (new Matrix4f()).scaling(0.5f);
		powerUp.setLocalTranslation(initialTranslation);
		powerUp.setLocalScale(initialScale);
	} // end buildObjects

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
		
		// spotlight pointing down on octBin
		light2 = new Light();
		light2.setLocation(new Vector3f (0.0f, 0.0f, 0.0f));
		light2.setType(Light.LightType.SPOTLIGHT);
		(engine.getSceneGraph()).addLight(light2);
	} // end initializeLights

	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		timeElapsed = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,5));
		
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		
		// ------------- inputs -------------
		im = engine.getInputManager();
		
		// IAction actions
		FwdAction fwdAction = new FwdAction(this);
		TurnAction turnAction = new TurnAction(this);
		BttnAction bttnAction = new BttnAction(this);
		
		// gamepad movement actions
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RY, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		// gamepad/keyboard button actions
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._0, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._2, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._2, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._3, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key._3, bttnAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		// keyboard movement actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	} // end initializeGame

	@Override
	public void update()
	{	
		// change elapsTime based on if powerUp has been activated or not
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		if(powerUpActive)
			elapsTime = (currFrameTime - lastFrameTime) / 150.0;
		else
			elapsTime = (currFrameTime - lastFrameTime) / 500.0;
	
		// update the input manager
		im.update((float)elapsTime);
	
		// check the distance between the dolphin and other objects
		checkDolDistance();
		

		// build and set HUD
		buildSetHUD();
		
		// place the camera on the back of the dolphin
		if(rideDolphin) {
			loc = dol.getWorldLocation();
			fwd = dol.getWorldForwardVector();
			up = dol.getWorldUpVector();
			right = dol.getWorldRightVector();
			cam.setU(right);
			cam.setV(up);
			cam.setN(fwd);
			cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-2.5f)));
		} // end if
	} // end update
	
	private void checkDolDistance() {
		// get the distance between the dolphin and the camera.  Stop the dolphin if the distance if >10.0f, allow to move if
		//	<10.0f.  Dolphin will resume moving if the player hops on the back of the dolphin, making the distance < 10.0f.
		camDolDistance = dol.getWorldLocation().distance(cam.getLocation());
		if(camDolDistance > 10.0f)
			stopDol = true;
		else if(camDolDistance < 10.0f)
			stopDol = false;
		
		// check if the dolphin is close enough to each visit location, if the distance is less than the value specified for 
		//  each object, set the object scale to 0.0f, the postcard scale for that object to 0.25f, increment the score, and 
		//  the visited boolean to true.
		if(!visitedSphere && dol.getWorldLocation().distance(sphere.getWorldLocation()) < 2.5f) {
			sphere.setLocalScale((new Matrix4f()).scaling(0.0f));
			spherePc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedSphere = true;
			score++;
		} // end if
		if(!visitedCube && dol.getWorldLocation().distance(cube.getWorldLocation()) < 1.25f) {
			cube.setLocalScale((new Matrix4f()).scaling(0.0f));
			cubePc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedCube = true;
			score++;
		} // end if
		if(!visitedTorus && dol.getWorldLocation().distance(torus.getWorldLocation()) < 2.25f) {
			torus.setLocalScale((new Matrix4f()).scaling(0.0f));
			torusPc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedTorus = true;
			score++;
		} // end if
		if(!visitedPlane && dol.getWorldLocation().distance(plane.getWorldLocation()) < 1.5f) {
			plane.setLocalScale((new Matrix4f()).scaling(0.0f));
			planePc.setLocalScale((new Matrix4f()).scaling(0.25f));
			visitedPlane = true;
			score++;
		} // end if
		
		// check if the dolphin is close enough to the powerUp, if the distance is less than 1.25f, set the scale of the 
		//	powerUp to 0.0f and set the powerUpActive boolean to true.
		if(!powerUpActive && dol.getWorldLocation().distance(powerUp.getWorldLocation()) < 1.25f) {
			powerUp.setLocalScale((new Matrix4f()).scaling(0.0f));
			powerUpActive = true;
		} // end if
	} // end checkDolDistance
	
	private void buildSetHUD() {
		// time elapsed hud
		int elapsTimeSec = Math.round((float)timeElapsed);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String dispStr1 = "Time = " + elapsTimeStr;
		Vector3f hud1Color = new Vector3f(1,0,0);
		
		// score hud
		String dispStrScore = "Score = " + score;
		Vector3f hudScoreColor = new Vector3f(1, 1, 1);
		if(score >= 4) {
			dispStrScore = "You Win!";
			(engine.getHUDmanager()).setHUD2(dispStrScore, hudScoreColor, 900, 515);
			
			(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 900, 485);
		} // end if
		else {
			timeElapsed += (currFrameTime - lastFrameTime) / 1000.0;
			(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
			
			(engine.getHUDmanager()).setHUD2(dispStrScore, hudScoreColor, 15, 45);
		} // end else
	} // end buildSetHUD

	// replaced with input manager
	@Override
	public void keyPressed(KeyEvent e)
	{	
		switch(e.getKeyCode())
		{
			/*
			case KeyEvent.VK_C:
				counter++;
				break;
			case KeyEvent.VK_1:	// pause the game
				paused = !paused;
				break;
			*/
			/*
			case KeyEvent.VK_W:	// move dolphin forward
				if(!stopDol) {
					fwd = dol.getWorldForwardVector();
					loc = dol.getWorldLocation();
					newLocation = loc.add(fwd.mul((float)elapsTime));
					dol.setLocalLocation(newLocation);
				}// end if
				break;
			case KeyEvent.VK_S: // move dolphin backwards
				if(!stopDol) {
					fwd = dol.getWorldForwardVector();
					loc = dol.getWorldLocation();
					newLocation = loc.add(fwd.mul(-(float)elapsTime));
					dol.setLocalLocation(newLocation);
				} // end if
				break;
			case KeyEvent.VK_A:	// turn (yaw) left
				if(!stopDol)
					dol.globalYaw(new Matrix4f(), elapsTime, 0);
				break;
			case KeyEvent.VK_D:	// turn (yaw) right
				if(!stopDol)
					dol.globalYaw(new Matrix4f(), elapsTime, 1);
				break;
			case KeyEvent.VK_UP:	// turn (pitch) up
				if (!stopDol)
					dol.localPitch(new Matrix4f(), elapsTime, 0);
				break;
			case KeyEvent.VK_DOWN: // turn (pitch) down
				if(!stopDol)
					dol.localPitch(new Matrix4f(), elapsTime, 1);
				break;
			*/
			/*
			case KeyEvent.VK_SPACE:	// hop on/off dolphin
				setRideDolphin(!rideDolphin);
				break;
			*/
			/*
			case KeyEvent.VK_2:
				dol.getRenderStates().setWireframe(true);
				break;
			case KeyEvent.VK_3:
				dol.getRenderStates().setWireframe(false);
				break;
			case KeyEvent.VK_4:
				(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,0));
				break;
			*/
		} // end switch
		super.keyPressed(e);
	} // end keyPressed
	
	// disconnect the camera from the dolphin (not riding the dolphin)
	public void disconnectDolCam() {
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		loc = dol.getWorldLocation();
		fwd = dol.getWorldForwardVector();
		up = dol.getWorldUpVector();
		right = dol.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.setLocation(loc.add(up.mul(0.5f)).add(fwd.mul(-0.5f)).add(right.mul(1.0f)));
	} // end disconnectDolCam
	
	// return the avatar (dolphin)
	public GameObject getAvatar() {
		return dol;
	} // end getAvatar
	
	// set the status of rideDolphin and call disconnectDolCam if rideDolphin = false
	public void setRideDolphin(boolean ride) {
		rideDolphin = ride;
		
		if(!rideDolphin)
			disconnectDolCam();
	} // end setRideDolphin
	
	// return value of stopDol
	public boolean getStopDol() {
		return stopDol;
	} // end getStopDol
} // end MyGame