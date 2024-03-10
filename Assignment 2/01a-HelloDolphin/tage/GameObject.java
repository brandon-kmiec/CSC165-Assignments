package tage;
import org.joml.*;
import java.util.*;
import tage.shapes.*;
import tage.physics.PhysicsObject;

/**
* GameObject holds the data associated with a node in the scenegraph.
* <br>
* Specifically, a GameObject includes the following:
* <ul>
* <li> a link to the associated ObjShape (which includes vertices, texcoords, normals, material)
* <li> a link to the associated TextureImage
* <li> a link to the associated RenderStates
* <li> local and world transforms for translation, rotation, and scale
* <li> boolean flags to control propagation of transforms when building world transforms (default true)
* <li> boolean flags for applying parent rotation/scale to local translation (default false)
* <li> a reference to its parent GameObject (usually the root object)
* <li> references to its child GameObjects, stored in a java HashSet.
* <li> a link to a corresponding PhysicsObject, if applicable
* <li> a boolean flag indicating whether this is a terrain object
* <li> a reference to its height map texture image, if it is a terrain object
* </ul>
* <p>
* An application can change an GameObject's location, size, and orientation by
* modifying its LOCAL transforms.  However, it is actually rendered according to
* its WORLD transforms, which are updated automatically whenever it or its parent
* are changed.  The game application can get the world transforms, but cannot set them directly.
* <p>
* There is one "root" GameObject, that is never rendered. It is stored in a static field and
* static accessors are provided for it.  The root node does not have a parent (the parent is null).
* <p>
* There is one "skybox" GameObject, which if enabled is always rendered first.
* It is the only GameObject that is not included in the scenegraph tree.
* <p>
* A number of other object settings are stored in the attached RenderStates object.
* These are items that affect OpenGL and other settings during rendering.
* See the javadoc for RenderStates for information about these settings.
* <p>
* GameObject has several constructors.  The no-parameter constructor is protected and only used for the root node.
* There is a one-parameter constructor that accepts a GameObject - it is for making an empty GameObject.
* There is also a one-parameter constructor that accepts an ObjShape, that the engine uses for
* constructing the SkyBox node (the game application shouldn't call this constructor).
* In all other constructors, the first parameter is for setting the initial parent node.
* This should be set to Engine.getRoot() in most cases, but may be set to other nodes
* if building a hierarchical object or system.
* <p>
* An application can modify the scenegraph tree by using setParent().
* Note that this method automatically updates the list of the parent's children.
* <p>
* If the game object's shape comes from an OBJ or RKM model that was oriented incorrectly
* (such as facing sideways or not correctly vertical), the application can set the object's
* modelOrientationCorrection render state.  This is a rotation that is applied when rendering,
* but that is not applied to the object's local or world transforms.
* @author Scott Gordon
*/

public class GameObject
{
	//------------------ STATIC AREA -----------------------
	private static GameObject root;

	// createRoot() is called automatically by the
	// engine and should not be called by the game application.
	// All other GameObjects have the root as an ancestor.

	protected static GameObject createRoot() { root = new GameObject(); return root; }

	/** returns a reference to the scenegraph root node */
	public static GameObject root() { return root; }
	//------------------------------------------------------

	private ObjShape shape;
	private TextureImage texture;
	private TextureImage heightMap = (TextureImage) new DefaultHeightMap();
	private RenderStates renderStates = new RenderStates();
	private GameObject parent;
	private HashSet<GameObject> children = new HashSet<GameObject>();
	
	private Matrix4f localTranslation, localRotation, localScale;
	private Matrix4f worldTranslation, worldRotation, worldScale;
	private boolean propagateTranslation, propagateRotation, propagateScale;
	private boolean applyParentRotationToPosition, applyParentScaleToPosition;
	private Vector3f v = new Vector3f(); // utility vector for JOML calls

	private PhysicsObject physicsObject;
	private boolean isTerrain = false;

	//------------------ CONSTRUCTORS -----------------

	// only applicable for creating the root node
	protected GameObject()
	{	shape = null;
		texture = null;
		parent = null;
		initTransforms();
	}

	/** creates an empty GameObject - for building a group or hierarchical system */
	public GameObject(GameObject p)
	{	shape = null;
		texture = null;
		parent = p;
		parent.addChild(this);
		initTransforms();
		Engine.getEngine().getSceneGraph().addGameObject(this);
		renderStates.disableRendering();
	}

	// only applicable for creating the skybox node
	protected GameObject(ObjShape sh)
	{	shape = sh;
		parent = null;
		initTransforms();
	}

	/** Builds a GameObject with specified parent node and ObjShape, with texture set to null. */
	public GameObject(GameObject p, ObjShape s)
	{	shape = s;
		texture = null;
		parent = p;
		parent.addChild(this);
		initTransforms();
		Engine.getEngine().getSceneGraph().addGameObject(this);
	}

	/** Builds a GameObject with specified parent node, ObjShape, and TextureImage. */
	public GameObject(GameObject p, ObjShape s, TextureImage t)
	{	shape = s;
		texture = t;
		parent = p;
		parent.addChild(this);
		initTransforms();
		Engine.getEngine().getSceneGraph().addGameObject(this);
	}

	private void initTransforms()
	{	localTranslation = new Matrix4f().identity();
		localRotation = new Matrix4f().identity();
		localScale = new Matrix4f().identity();
		worldTranslation = new Matrix4f().identity();
		worldRotation = new Matrix4f().identity();
		worldScale = new Matrix4f().identity();
		propagateTranslation = true;
		propagateRotation = true;
		propagateScale = true;
		applyParentRotationToPosition = false;
		applyParentScaleToPosition = false;
	}

	// ---------------------------------------------------

	/** returns a reference to the ObjShape associated with this GameObject */
	public ObjShape getShape() { return shape; }

	/** assigns an ObjShape to this GameObject */
	public void setShape(ObjShape sh) { shape = sh; }

	/** returns the TextureImage associated with this GameObject */
	public TextureImage getTextureImage() { return texture; }

	/** assigns a TextureImage to this GameObject */
	public void setTextureImage(TextureImage tex) { texture = tex; }

	/** returns a reference to the TextureImage height map associated with this GameObject - applicable to terrain planes */
	public TextureImage getHeightMap() { return heightMap; }

	/** assigns a TextureImage height map to this GameObject - applicable to terrain planes */
	public void setHeightMap(TextureImage tex) { heightMap = tex; isTerrain = true; }

	/** returns a reference to the RenderStates associated with this GameObject */
	public RenderStates getRenderStates() { return renderStates; }

	/** returns a boolean value that is true if this GameObject is a terrain plane */
	public boolean isTerrain() { return isTerrain; }

	/** sets this GameObject to be a terrain plane */
	public void setIsTerrain(boolean t) { isTerrain = t; }

	/** returns a reference to this GameObject's parent GameObject */
	public GameObject getParent() { return parent; }

	/** 
	* Specify a parent for this node when building a scenegraph (tree).
	* It automatically updates the list of the parent's children.
	*/
	public void setParent(GameObject g)
	{	parent.removeChild(this);
		parent = g;
		parent.addChild(this);
	}
	
	protected void setFirstParent(GameObject g)
	{	parent = g;
		parent.addChild(this);
	}

	protected void setParentToNull() { parent = null; }

	/** returns a boolean that is true if this GameObject has any children nodes */
	public boolean hasChildren() { return !(children.isEmpty()); }

	protected void addChild(GameObject g) { children.add(g); }
	protected void removeChild(GameObject g) { children.remove(g); }
	protected Iterator getChildrenIterator() { return children.iterator(); }

	// ------------------ Look At methods ------------------------------

	/** Orients this GameObject so that it faces a specified GameObject */
	public void lookAt(GameObject go) { lookAt(go.getWorldLocation()); }

	/** Orients this GameObject so that it faces a location specified in a Vector3f */
	public void lookAt(Vector3f target) { lookAt(target.x(), target.y(), target.z()); }

	/** Orients this GameObject so that it faces a specified (x,y,z) location */
	public void lookAt(float x, float y, float z)
	{	Vector3f right, up, fwd, l, copyFwd, copyRight;
		l = getWorldLocation();
		fwd = (new Vector3f(x-l.x(), y-l.y(), z-l.z())).normalize();
		copyFwd = new Vector3f(fwd);
		if ((fwd.equals(0,1,0)) || (fwd.equals(0,-1,0)))
			right = new Vector3f(1f,0f,0f);
		else
			right = (new Vector3f(copyFwd.cross(0f,1f,0f))).normalize();
		copyRight = new Vector3f(right);
		up = (new Vector3f(copyRight.cross(fwd))).normalize();
		localRotation.identity();
		localRotation.setColumn(0, new Vector4f(right.negate(), 0f));
		localRotation.setColumn(1, new Vector4f(up, 0f));
		localRotation.setColumn(2, new Vector4f(fwd, 0f));
		update();
	}
	
	// ------------ SCENE GRAPH TRAVERSAL for MATRICES -----------------

	// This function does scenegraph traversal for game object matrix transforms.
	// It is called automatically by the engine whenever any object transform is modified,
	// and should NOT be called directly by the game application.
	// It works by concatenating local transforms to parent world transforms.
	// It may also apply parent rotation and scale to this objects translation, if a
	// hierarchical object is desired.  Recursively calls update() on children node
	// for each transform that has propagation enabled, if children are present.

	protected void update()
	{	if (this != root)
		{	if (propagateTranslation)
			{	Vector4f loc = (new Vector4f(0,0,0,1)).mul(localTranslation);
				if (applyParentRotationToPosition) loc.mul(parent.getWorldRotation());
				if (applyParentScaleToPosition)	loc.mul(parent.getWorldScale());
				loc.mul(parent.getWorldTranslation());
				worldTranslation.translation(loc.x(), loc.y(), loc.z());
			}
			else
			{	worldTranslation = new Matrix4f(localTranslation);
			}
			if (propagateRotation)
			{	worldRotation = new Matrix4f(parent.getWorldRotation());
				worldRotation.mul(localRotation);
			}
			else
			{	worldRotation = new Matrix4f(localRotation);
			}
			if (propagateScale)
			{	worldScale = new Matrix4f(parent.getWorldScale());
				worldScale.mul(localScale);
			}
			else
			{	worldScale = new Matrix4f(localScale);
			}
		}
		Iterator<GameObject> i = children.iterator();
		while (i.hasNext()) (i.next()).update();
	}

	// ---------------- ACCESSORS FOR MATRICES ---------------------

	/** copies a specified Matrix4f into this GameObject's local translation matrix */
	public void setLocalTranslation(Matrix4f m) { localTranslation = new Matrix4f(m); update(); }

	/** copies a specified Matrix4f into this GameObject's local rotation matrix */
	public void setLocalRotation(Matrix4f l) { localRotation = new Matrix4f(l); update(); }

	/** copies a specified Matrix4f into this GameObject's local scale matrix */
	public void setLocalScale(Matrix4f s) { localScale = new Matrix4f(s); update(); }

	/** returns a copy of this GameObject's local translation matrix */
	public Matrix4f getLocalTranslation() { return new Matrix4f(localTranslation); }

	/** returns a copy of this GameObject's local rotation matrix */
	public Matrix4f getLocalRotation() { return new Matrix4f(localRotation); }

	/** returns a copy of this GameObject's local scale matrix */
	public Matrix4f getLocalScale() { return new Matrix4f(localScale); }

	/** returns a copy of this GameObject's world translation matrix */
	public Matrix4f getWorldTranslation() { return new Matrix4f(worldTranslation); }

	/** returns a copy of this GameObject's world rotation matrix */
	public Matrix4f getWorldRotation() { return new Matrix4f(worldRotation); }

	/** returns a copy of this GameObject's world scale matrix */
	public Matrix4f getWorldScale() { return new Matrix4f(worldScale); }

	/** returns a forward-facing Vector3f based on the local rotation matrix */
	public Vector3f getLocalForwardVector() { return new Vector3f(localRotation.getColumn(2, v)); }

	/** returns a upward-facing Vector3f based on the local rotation matrix */
	public Vector3f getLocalUpVector() { return new Vector3f(localRotation.getColumn(1, v)); }

	/** returns a right-facing Vector3f based on the local rotation matrix */
	public Vector3f getLocalRightVector() { return (new Vector3f(localRotation.getColumn(0, v))).negate(); }

	/** returns a forward-facing Vector3f based on the world rotation matrix */
	public Vector3f getWorldForwardVector() { return new Vector3f(worldRotation.getColumn(2, v)); }

	/** returns a upward-facing Vector3f based on the world rotation matrix */
	public Vector3f getWorldUpVector() { return new Vector3f(worldRotation.getColumn(1, v)); }

	/** returns a right-facing Vector3f based on the world rotation matrix */
	public Vector3f getWorldRightVector() { return (new Vector3f(worldRotation.getColumn(0, v))).negate(); }

	/** returns the location of this object relative to its parent node */
	public Vector3f getLocalLocation() { return new Vector3f(localTranslation.getTranslation(v)); }

	/** returns the location of this object in world space */
	public Vector3f getWorldLocation() { return new Vector3f(worldTranslation.getTranslation(v)); }

	/** sets the location of this object relative to its parent node */
	public void setLocalLocation(Vector3f location) { localTranslation.setTranslation(location); update(); }

	// ------------------- accessors for hierarchical systems and hierarchical objects

	/** applies the parent translation when building this GameObject's world translation matrix */
	public void propagateTranslation(boolean p) { propagateTranslation = p; }

	/** applies the parent rotation when building this GameObject's world rotation matrix */
	public void propagateRotation(boolean p) { propagateRotation = p; }

	/** applies the parent scale when building this GameObject's world scale matrix */
	public void propagateScale(boolean p) { propagateScale = p; }

	// Set these to true if building a hierarchical object.
	// Note: scaling of a hierarchical object only works if the parent scale factor on all three axes are equal

	/** applies the parent rotation to this GameObject's world translation matrix */
	public void applyParentRotationToPosition(boolean a) { applyParentRotationToPosition = a; }

	/** applies the parent scale to this GameObject's world translation matrix */
	public void applyParentScaleToPosition(boolean a) { applyParentScaleToPosition = a; }

	/** returns a boolean that is true if translation propagation is enabled */
	public boolean propagatesTranslation() { return propagateTranslation; }

	/** returns a boolean that is true if rotation propagation is enabled */
	public boolean propagatesRotation() { return propagateRotation; }

	/** returns a boolean that is true if scale propagation is enabled */
	public boolean propagatesScale() { return propagateScale; }

	/** returns a boolean that is true if the object applies its parent's rotation to its position */
	public boolean appliesParentRotationToPosition() { return applyParentRotationToPosition; }

	/** returns a boolean that is true if the object applies its parent's scale to its position */
	public boolean appliesParentScaleToPosition() { return applyParentScaleToPosition; }

	// ------------------ accessors for physics object ------------------------------

	/** associates a specified PhysicsObject with this GameObject */
	public void setPhysicsObject(PhysicsObject p) { physicsObject = p; }

	/** gets the physics object associated with this GameObject */
	public PhysicsObject getPhysicsObject() { return physicsObject; }

	// -------------- accessor for height of terrain map at specified position ------------

	/** gets the height at (x,z) if this is a terrain plane -- returns 0 if not terrain, only works if flat on y=0 plane. */
	public float getHeight(float x, float z)
	{	x = x - getLocalLocation().x;
		z = z - getLocalLocation().z;

		Matrix4f rot = getLocalRotation().transpose();
		Vector4f vec = new Vector4f(x,0,z,1f);
		vec.mul(rot);
		x = vec.x; z = vec.z;

		x = (x / localScale.m00() + 1.0f) / 2.0f;
		z = 1.0f - (z / localScale.m00() + 1.0f) / 2.0f;
		
		return localScale.m11() * Engine.getEngine().getRenderSystem().getHeightAt(heightMap.getTexture(), x, z);
	}
	
	// ---------------------- yaw and pitch functions ----------------------
		
	/** Sets the world rotation equal to matrix a rotated on the y-axis multiplied by the world rotation matrix.
		Then sets local rotation equal to world rotation.
	*/
	public void globalYaw(Matrix4f a, double elapsTime, int direction) {
		if(direction == 0) {
			worldRotation = (a.rotation((float)elapsTime, 0, 1, 0)).mul(getWorldRotation());
			setLocalRotation(worldRotation);
		}
		if(direction == 1) {
			worldRotation = (a.rotation(-(float)elapsTime, 0, 1, 0)).mul(getWorldRotation());
			setLocalRotation(worldRotation);
		}
	} // end globalYaw
	
	/** Sets the local rotation equal to the world rotation matrix multiplied by matrix a rotated on the x-axis.
	*/
	public void localPitch(Matrix4f a, double elapsTime, int direction) {
		if(direction == 0)
			setLocalRotation(getWorldRotation().mul(a.rotation(-(float)elapsTime, 1, 0, 0)));
		if(direction == 1)
			setLocalRotation(getWorldRotation().mul(a.rotation((float)elapsTime, 1, 0, 0)));
	} // end localPitch

	// --------------- private class for default height map ----------------

	private class DefaultHeightMap extends TextureImage
	{	private String textureFile = "assets/defaultAssets/defaultHeightMap.JPG";
		private int texture;
		private DefaultHeightMap()
		{	setTextureFile(textureFile);
			Engine.getEngine().getRenderSystem().addTexture((TextureImage)this);
		}
	}
}