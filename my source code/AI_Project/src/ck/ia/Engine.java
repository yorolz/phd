package ck.ia;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import ck.graphics.CameraPathFollower;
import ck.graphics.Ticker;
import ck.ia.agents.EnvironmentAgent;
import ck.ia.agents.MovingAgent;
import ck.ia.agents.SpawnAgent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

public class Engine {
	public static void main(String[] args) throws Exception {
		new Engine();
	}

	private SpawnAgent blue_spawn;
	private FrameBuffer buffer;
	private Camera camera;
	private EnvironmentAgent environment;
	private boolean executing;
	private AILauncher goal_simulator;
	private boolean key_c_pressed, key_f1_pressed, key_z_pressed, key_x_pressed;

	private HashSet<Entity> gifts;
	private Object3D o3d_gift;
	private HashMap<Entity, Object3D> o3d_gifts;

	private Object3D o3d_blue_robot;
	private HashMap<MovingAgent, Object3D> o3d_blue_robots;
	private Object3D o3d_blue_spawn;

	private Object3D o3d_red_robot;
	private HashMap<MovingAgent, Object3D> o3d_red_robots;
	private Object3D o3d_red_spawn;

	private HashSet<MovingAgent> blue_moving_agents;
	private HashSet<MovingAgent> red_moving_agents;
	private SpawnAgent red_spawn;

	private TextureManager texMan;
	private World world, wireframeWorld;
	private CameraPathFollower camerapath;
	private Ticker ticker;
	private boolean automaticCamera;
	private boolean drawWaypoints;

	public Engine() {
		initialize();
		loop();
	}

	private void checkKeyboard(double timeDeltaLastCall) {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			executing = false;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
			if (!key_x_pressed) {
				drawWaypoints = !drawWaypoints;
				key_x_pressed = true;
			}
		} else {
			key_x_pressed = false;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			if (!key_z_pressed) {
				automaticCamera = !automaticCamera;
				key_z_pressed = true;
			}
		} else {
			key_z_pressed = false;
		}
		float movement_multiplier = (float) (Config.CAMERA_MOVEMENT_MULTIPLIER_NORMAL * timeDeltaLastCall);
		// RUN
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			movement_multiplier *= Config.CAMERA_MOVEMENT_MULTIPLIER_FAST_RATIO;
		}
		// JUMP
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			camera.moveCamera(Camera.CAMERA_MOVEUP, Config.CAMERA_MOVEMENT_CROUCH_JUMP_SPEED * movement_multiplier);
		}
		// CROUCH
		if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
			camera.moveCamera(Camera.CAMERA_MOVEDOWN, Config.CAMERA_MOVEMENT_CROUCH_JUMP_SPEED * movement_multiplier);
		}
		// FORWARD
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			camera.moveCamera(Camera.CAMERA_MOVEOUT, Config.CAMERA_MOVEMENT_FORWARD_BACKWARD_SPEED * movement_multiplier);
		}
		// BACKWARD
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			camera.moveCamera(Camera.CAMERA_MOVEIN, Config.CAMERA_MOVEMENT_FORWARD_BACKWARD_SPEED * movement_multiplier);
		}
		// STRAFE LEFT
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			camera.moveCamera(Camera.CAMERA_MOVELEFT, Config.CAMERA_MOVEMENT_STRAFE_SPEED * movement_multiplier);
		}
		// STRAFE RIGHT
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			camera.moveCamera(Camera.CAMERA_MOVERIGHT, Config.CAMERA_MOVEMENT_STRAFE_SPEED * movement_multiplier);
		}
		// ROLL LEFT
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			camera.rotateZ(Config.CAMERA_ROTATION_ROLL_SPEED * movement_multiplier);
		}
		// ROLL RIGHT
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			camera.rotateZ(-Config.CAMERA_ROTATION_ROLL_SPEED * movement_multiplier);
		}
		// PITCH UP
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			camera.rotateX(Config.CAMERA_ROTATION_PITCH_SPEED * movement_multiplier);
		}
		// PITCH DOWN
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			camera.rotateX(-Config.CAMERA_ROTATION_PITCH_SPEED * movement_multiplier);
		}
		// YAW LEFT
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			camera.rotateY(Config.CAMERA_ROTATION_YAW_SPEED * movement_multiplier);
		}
		// YAW RIGHT
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			camera.rotateY(-Config.CAMERA_ROTATION_YAW_SPEED * movement_multiplier);
		}
		// CAMERA DEBUG
		if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			// if before this key press check the key wasn't pressed, show the debug
			if (!key_c_pressed) {
				debugCamera();
				key_c_pressed = true;
			}
		} else {
			key_c_pressed = false;
		}
		// CAMERA RESET
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			camera.setPosition(Config.CAMERA_POSITION_DEFAULT);
			camera.setOrientation(Config.CAMERA_DIRECTION_DEFAULT, Config.CAMERA_UP_DEFAULT);
		}
		// CAMERA REDIRECT
		if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
			camera.setOrientation(Config.CAMERA_DIRECTION_DEFAULT, Config.CAMERA_UP_DEFAULT);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
			if (!key_f1_pressed) {
				try {
					Display.setFullscreen(!Display.isFullscreen());
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				key_f1_pressed = true;
			}
		} else {
			key_f1_pressed = false;
		}
	}

	private void cloneGifts() {
		log("cloneGifts()");
		log("cloneGifts() done");
		for (Entity gift : this.gifts) {
			Object3D o3d = o3d_gift.cloneObject();
			addObject3DToWorld(o3d);
			// o3d.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
			o3d_gifts.put(gift, o3d);
		}
	}

	private void addObject3DToWorld(Object3D o3d) {
		world.addObject(o3d);
	}

	private void cloneRobots() {
		log("cloneRobots()");
		Object3D cloneObject;
		for (MovingAgent rsa : red_moving_agents) {
			cloneObject = o3d_red_robot.cloneObject();
			addObject3DToWorld(cloneObject);
			cloneObject.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
			o3d_red_robots.put(rsa, cloneObject);
		}
		for (MovingAgent bsa : blue_moving_agents) {
			cloneObject = o3d_blue_robot.cloneObject();
			addObject3DToWorld(cloneObject);
			cloneObject.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
			o3d_blue_robots.put(bsa, cloneObject);
		}
		log("cloneRobots() done");
	}

	private void debugCamera() {
		System.out.println("Camera: position = " + camera.getPosition() + " direction = " + camera.getDirection() + " up = "
				+ camera.getUpVector());
	}

	private void initialize() {
		log("initialize()");
		System.out.println("working directory: " + Config.CURRENT_PATH);
		executing = true;

		initializeRenderer();
		initializeScene();
		initializeSimulator();
		initializeDynamicObjects();
		initializeKeyboard();

		// createWaypointsFromWorld();
		world.buildAllObjects();

		ticker = new Ticker();
		log("initialize() done");
	}

	private void initializeDynamicObjects() {
		loadAndSetupModels();
		o3d_blue_robots = new HashMap<MovingAgent, Object3D>();
		o3d_red_robots = new HashMap<MovingAgent, Object3D>();
		o3d_gifts = new HashMap<Entity, Object3D>();
		cloneRobots();
		cloneGifts();
	}

	private void initializeKeyboard() {
		log("initializeKeyboard()");
		synchronized (FrameBuffer.SYNCHRONIZER) {
			if (!Keyboard.isCreated())
				if (Display.isCreated())
					try {
						Keyboard.create();
					} catch (LWJGLException e) {
						e.printStackTrace();
					}
		}
		key_c_pressed = false;
		key_f1_pressed = false;
		key_z_pressed = false;
		key_x_pressed = false;
		log("initializeKeyboard() done");
	}

	private void initializeRenderer() {
		log("initializeRenderer()");
		log("jPCT version: " + com.threed.jpct.Config.getVersion());
		buffer = new FrameBuffer(Config.RENDERER_RESOLUTION[0], Config.RENDERER_RESOLUTION[1],
				FrameBuffer.SAMPLINGMODE_HARDWARE_ONLY);

		com.threed.jpct.Config.glTrilinear = true;
		com.threed.jpct.Config.maxPolysVisible = 1 << 17;
		com.threed.jpct.Config.farPlane = 2048.0f;
		com.threed.jpct.Config.nearPlane = 0.0625f;
		com.threed.jpct.Config.glUseVBO = false;
		com.threed.jpct.Config.useMultipleThreads = true;
		com.threed.jpct.Config.useNormalsFromOBJ = true;

		// jPCT says this is good so...
		World.setDefaultThread(Thread.currentThread());
		buffer.enableRenderer(IRenderer.RENDERER_OPENGL);
		buffer.disableRenderer(IRenderer.RENDERER_SOFTWARE);
		Display.setVSyncEnabled(false);

		// wait until window is created
		while (!Display.isCreated()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
		log("initializeRenderer() done");
	}

	private void pollMouse(double timeDeltaLastCall) {
		if (Mouse.isButtonDown(0)) {
			int x = this.getMouseX();
			int y = this.getMouseY();
			Object3D clickedO3d = processClickEvent(x, y);
			if (clickedO3d != null) {
				int id = clickedO3d.getID();
				MovingAgent sa = this.getMovingAgentFromObject3D(id);
				if (sa == null)
					return;
				sa.halt();
				System.out.println("processClickEvent: " + id);
			}
		}
	}

	private MovingAgent getMovingAgentFromObject3D(int id) {
		for (MovingAgent sa : o3d_blue_robots.keySet()) {
			Object3D o3d = o3d_blue_robots.get(sa);
			if (o3d.getID() == id) {
				return sa;
			}
		}
		for (MovingAgent sa : o3d_red_robots.keySet()) {
			Object3D o3d = o3d_red_robots.get(sa);
			if (o3d.getID() == id) {
				return sa;
			}
		}
		return null;
	}

	public int getMouseX() {
		return Mouse.getX();
	}

	public int getMouseY() {
		return buffer.getOutputHeight() - Mouse.getY();
	}

	private Object3D processClickEvent(int x, int y) {
		SimpleVector dir = Interact2D.reproject2D3DWS(camera, buffer, x, y);
		dir = dir.normalize();
		Object[] res = world.calcMinDistanceAndObject3D(camera.getPosition(), dir, 10000);
		Object3D picked = (Object3D) res[1];
		return picked;
	}

	private void initializeScene() {
		log("initializeScene()");
		world = new World();
		wireframeWorld = new World();
		// world.setWorldProcessor(new WorldProcessor(8));
		loadTextures();
		automaticCamera = false;
		drawWaypoints = false;

		loadMap();

		// world.setAmbientLight(32, 32, 32);
		world.addLight(new SimpleVector(0, 24, 0), Color.WHITE);
		world.setLightAttenuation(0, 2048.0f);

		camera = world.getCamera();
		camera.setPosition(Config.CAMERA_POSITION_DEFAULT);
		camera.setOrientation(Config.CAMERA_DIRECTION_DEFAULT, Config.CAMERA_UP_DEFAULT);
		camerapath = new CameraPathFollower(camera);
		camerapath.loadPath(Config.CURRENT_PATH + Config.DIRECTORY_SEPARATOR + Config.CAMERA_PATH);
		wireframeWorld.setCameraTo(camera);
		log("initializeScene() done");
	}

	private void loadMap() {
		// map
		log("loading " + Config.OBJECT_MAP);
		// where are we loading from?
		String parent_dir = Config.DIRECTORY_MODELS + Config.DIRECTORY_SEPARATOR;
		Object3D o3d_map = load3ds(parent_dir + Config.OBJECT_MAP, 1.0f, true);

		// o3d_map.setLighting(Object3D.LIGHTING_NO_LIGHTS);
		o3d_map.setAdditionalColor(Color.DARK_GRAY);
		o3d_map.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		addObject3DToWorld(o3d_map);

		// // transparent section of the map
		// // this section is transparent
		// o3d_map_transparent.setTransparency(2);

		// map's stationary
		o3d_map.enableLazyTransformations();
	}

	private void initializeSimulator() {
		log("initializeSimulator()");
		goal_simulator = new AILauncher(world, wireframeWorld);
		environment = goal_simulator.getEnvironment();
		red_spawn = environment.getSpawnAgent(0);
		blue_spawn = environment.getSpawnAgent(1);
		red_moving_agents = environment.getMovingAgents(0);
		blue_moving_agents = environment.getMovingAgents(1);
		gifts = environment.getCarriedEntities();
		log("initializeSimulator() done");
	}

	private void loadAndSetupModels() {
		// where are we loading from?
		String parent_dir = Config.DIRECTORY_MODELS + Config.DIRECTORY_SEPARATOR;

		// axis
		Object3D o3d_axis = load3ds(parent_dir + Config.OBJECT_AXIS, 1.0f, true);
		addObject3DToWorld(o3d_axis);

		// red spawn
		o3d_red_spawn = load3ds(parent_dir + Config.OBJECT_RED_SPAWN, 1.0f, true);
		o3d_red_spawn.setAdditionalColor(Color.DARK_GRAY);
		addObject3DToWorld(o3d_red_spawn);

		// blue spawn
		o3d_blue_spawn = load3ds(parent_dir + Config.OBJECT_BLUE_SPAWN, 1.0f, true);
		o3d_blue_spawn.setAdditionalColor(Color.DARK_GRAY);
		addObject3DToWorld(o3d_blue_spawn);

		// red bot
		o3d_red_robot = load3ds(parent_dir + "mousebot_simple_r.3ds", 1.0f, false);
		o3d_red_robot.setAdditionalColor(Color.WHITE);

		// blue bot
		o3d_blue_robot = load3ds(parent_dir + "mousebot_simple_b.3ds", 1.0f, false);
		o3d_blue_robot.setAdditionalColor(Color.WHITE);

		// gift
		o3d_gift = load3ds(parent_dir + Config.OBJECT_GIFT, 1.0f, true);
		o3d_gift.setAdditionalColor(Color.WHITE);
	}

	private Object3D load3ds(String filename, float scale, boolean correct) {
		log("load3ds() " + filename);
		Object3D[] parts = Loader.load3DS(filename, scale);
		if (parts == null || parts.length == 0) {
			System.err.println("could not read file " + filename);
			System.exit(-1);
		}
		Object3D obj = new Object3D(0);
		for (int i = 0; i < parts.length; i++) {
			Object3D part = parts[i];
			if (correct) {
				part.setCenter(SimpleVector.ORIGIN);
			}
			obj = Object3D.mergeObjects(obj, part);
		}
		// obj.createTriangleStrips();
		obj.build();
		obj.compile();
		// obj.compileAndStrip();
		log("loadObj() done");
		return obj;
	}

	private void loadTextures() {
		texMan = TextureManager.getInstance();
		String texture_dir_absolute = Config.CURRENT_PATH + Config.DIRECTORY_SEPARATOR + Config.DIRECTORY_TEXTURES;
		File dir = new File(texture_dir_absolute);
		String[] files = dir.list();
		for (int i = 0; i < files.length; i++) {
			String name = files[i];
			String filter = name.toLowerCase();
			if (filter.endsWith(".jpg") || filter.endsWith(".png")) {
				String texture_filename = texture_dir_absolute + Config.DIRECTORY_SEPARATOR + name;
				// boolean alpha = filter.contains("alpha");
				Texture texture = new Texture(texture_filename, true);
				texMan.addTexture(name, texture);
			}
		}
	}

	private void log(String string) {
		System.out.println(string);
	}

	public void pollInput(double timeDeltaLastCall) {
		checkKeyboard(timeDeltaLastCall);
		pollMouse(timeDeltaLastCall);
	}

	private void loop() {
		while (!Display.isCloseRequested() && executing) {
			if (automaticCamera) {
				double elapsedTime = ticker.getElapsedTime();
				camerapath.interpolateMotion(elapsedTime * 0.333333);
				// System.out.println((elapsedTime+1) );
			}
			renderScene();
			double timeDeltaLastCall = ticker.getTimeDeltaLastCall();
			pollInput(timeDeltaLastCall);
			// Thread.yield();
		}
		buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
		buffer.dispose();
		System.exit(0);
	}

	private void renderScene() {
		setupGifts();
		setupMovingAgents();
		setupSpawnAgents();
		buffer.clear(Color.WHITE);

		world.renderScene(buffer);
		world.draw(buffer);

		if (drawWaypoints) {
			wireframeWorld.renderScene(buffer);
			wireframeWorld.draw(buffer);
		}

		buffer.update();
		buffer.displayGLOnly();
	}

	private void setupGifts() {
		for (Entity gift : gifts) {
			Object3D model = o3d_gifts.get(gift);
			if (model != null) {
				model.getTranslationMatrix().setIdentity();
				model.getRotationMatrix().setIdentity();
				// model.rotateX((float) gift.getPitch());
				model.rotateY((float) gift.getYaw());
				SimpleVector position = gift.getPosition();
				model.translate(position.x, position.y, position.z);
			}
		}
	}

	private void setupMovingAgents() {
		for (MovingAgent agent : red_moving_agents) {
			Object3D model = o3d_red_robots.get(agent);
			model.getRotationMatrix().setIdentity();
			// model.rotateX((float) agent.getPitch());
			model.rotateY((float) agent.getYaw());
			// model.rotateZ((float) agent.getRoll());
			// agent.setRoll((float) (agent.getRoll() + 0.1));
			SimpleVector position = agent.getPosition();
			model.getTranslationMatrix().setIdentity();
			model.translate(position.x, position.y, position.z);
		}

		for (MovingAgent agent : blue_moving_agents) {
			Object3D model = o3d_blue_robots.get(agent);
			// orient
			model.getRotationMatrix().setIdentity();
			// model.rotateX((float) agent.getPitch());
			model.rotateY((float) agent.getYaw());
			// model.rotateZ((float) agent.getRoll());
			// position the blue moving agent
			SimpleVector position = agent.getPosition();
			// nullify past translations
			model.getTranslationMatrix().setIdentity();
			model.translate(position.x, position.y, position.z);
		}
	}

	private void setupSpawnAgents() {
		// revert translation matrices
		o3d_red_spawn.getTranslationMatrix().setIdentity();
		o3d_blue_spawn.getTranslationMatrix().setIdentity();
		// scale to show progress
		float scale = (float) red_spawn.getSpawnProgress();
		if (scale > 0)
			o3d_red_spawn.setScale(scale);
		scale = (float) blue_spawn.getSpawnProgress();
		if (scale > 0)
			o3d_blue_spawn.setScale(scale);
		// translate to agent's center
		SimpleVector redPosition = red_spawn.getPosition();
		SimpleVector bluePosition = blue_spawn.getPosition();
		o3d_red_spawn.translate(redPosition.x, redPosition.y, redPosition.z);
		o3d_blue_spawn.translate(bluePosition.x, bluePosition.y, bluePosition.z);
	}
}
