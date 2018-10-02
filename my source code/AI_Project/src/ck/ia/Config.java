package ck.ia;

import java.awt.Color;
import java.io.File;

import ck.navigation.SimpleCustomVector;

import com.threed.jpct.SimpleVector;

public class Config {
	public static final SimpleVector CAMERA_DIRECTION_DEFAULT = new SimpleVector(0.0, -1.0, 0.0);
	public static final float CAMERA_MOVEMENT_CROUCH_JUMP_SPEED = 0.25f;
	public static final float CAMERA_MOVEMENT_FORWARD_BACKWARD_SPEED = 0.25f;
	public static final float CAMERA_MOVEMENT_MULTIPLIER_FAST_RATIO = 4.0f;
	public static final double CAMERA_MOVEMENT_MULTIPLIER_NORMAL = 32.0;
	public static final float CAMERA_MOVEMENT_STRAFE_SPEED = 0.25f;
	public static final String CAMERA_PATH = "camerapath.txt";
	public static final SimpleVector CAMERA_POSITION_DEFAULT = new SimpleVector(-71.67244, 132.92757, 73.75197);
	public static final float CAMERA_ROTATION_PITCH_SPEED = 0.015625f;
	public static final float CAMERA_ROTATION_ROLL_SPEED = 0.015625f;
	public static final float CAMERA_ROTATION_YAW_SPEED = 0.015625f;
	public static final SimpleVector CAMERA_UP_DEFAULT = new SimpleVector(1.0, 0.0, 0.0);
	public static final float CARRY_ENTITY_RADIUS = 0.125f;
	public static final SimpleVector CARRY_GOAL_CAPTURE_POSITION_BLUE_1 = new SimpleVector(-39.05375, 0.0, 70.437256);
	public static final SimpleVector CARRY_GOAL_CAPTURE_POSITION_BLUE_2 = new SimpleVector(-39.05375, 0.0, 70.437256);
	public static final SimpleVector CARRY_GOAL_CAPTURE_POSITION_BLUE_3 = new SimpleVector(-39.05375, 0.0, 70.437256);
	public static final SimpleVector CARRY_GOAL_CAPTURE_POSITION_RED_1 = new SimpleVector(-122.51777, 0.0, 70.00301);
	public static final SimpleVector CARRY_GOAL_CAPTURE_POSITION_RED_2 = new SimpleVector(-122.51777, 0.0, 70.00301);
	public static final SimpleVector CARRY_GOAL_CAPTURE_POSITION_RED_3 = new SimpleVector(-122.51777, 0.0, 70.00301);
	public static final double CARRY_GOAL_INITIAL_POSITION_DISPERSION = 4;
	public static final double CARRY_GOAL_CAPTURE_POSITION_DISPERSION = 4;
	public static final SimpleVector CARRY_GOAL_INITIAL_POSITION_BLUE = new SimpleVector(-36.639427, 0.0, 90.98959);
	public static final SimpleVector CARRY_GOAL_INITIAL_POSITION_RED = new SimpleVector(-96.86457, 0.0, 15.782455);
	public static final SimpleVector CARRY_GOAL_PAYLOAD_OFFSET = new SimpleVector(0.0, 0.01, 0.0);
	public static final Color COLOR_BACKGROUND = java.awt.Color.black;
	public static final String CURRENT_PATH = System.getProperty("user.dir");
	public static final String DIRECTORY_MODELS = "models";
	public static final char DIRECTORY_SEPARATOR = File.separatorChar;
	public static final String DIRECTORY_TEXTURES = "models";
	public static final int ENVIRONMENT_ID = -1;
	public static final int GOAL_PRIORITY_CARRY = 2;
	public static final int GOAL_PRIORITY_ROAM = 1;
	public static final int NUMBER_GOALS_CARRY_BLUE = 800;
	public static final int NUMBER_GOALS_CARRY_RED = NUMBER_GOALS_CARRY_BLUE;
	public static final int NUMBER_GOALS_ROAM_BLUE = 4096;
	public static final int NUMBER_GOALS_ROAM_RED = NUMBER_GOALS_ROAM_BLUE;
	public static final int NUMBER_AGENTS_BLUE = 4096;
	public static final int NUMBER_AGENTS_RED = 4096;
	public static final String OBJECT_AXIS = "axis.3ds";
	public static final String OBJECT_GIFT = "gift.3ds";
	public static final String OBJECT_BLUE_ROBOT = "mousebot_blue.3ds";
	public static final String OBJECT_BLUE_SPAWN = "blue_spawn.3ds";
	public static final String OBJECT_MAP = "block_city2.3ds";
	public static final String OBJECT_RED_ROBOT = "mousebot_red.3ds";
	public static final String OBJECT_RED_SPAWN = "red_spawn.3ds";
	public static final int[] RENDERER_RESOLUTION = { 1920, 1080 };
	protected static final long SOLDIER_AUCTION_TIMEOUT = 100;
	public static final SimpleVector SOLDIER_DEAD_OFFSET = new SimpleVector(0.0, 0.0, 0.0);
	public static final float SOLDIER_DEAD_ROLL = (float) Math.PI;
	public static final float SOLDIER_HEALTH_DEAD = 0.0f;
	public static final float SOLDIER_HEALTH_INITIAL = 0.0f;
	public static final float SOLDIER_HEALTH_NORMAL = 100.0f;
	public static final float SOLDIER_HEALTH_REDUCTION_RATE = 0.00f;
	public static final float SOLDIER_MOVEMENT_SPEED = 4.0f;
	public static final float SOLDIER_RADIUS = 0.5f;
	public static final float SOLDIER_ROTATION_SPEED = (float) (Math.PI * 4.0);
	public static final float SPAWN_HEALTH = Float.MAX_VALUE;
	public static final long SPAWN_INTERVAL_SECONDS = 10;
	public static final int SPAWN_PHASE_SECONDS_BLUE = 7;
	public static final int SPAWN_PHASE_SECONDS_RED = 2;
	public static final SimpleVector SPAWN_POSITION_BLUE = new SimpleVector(-16.221184, 0.0, 16.0);
	public static final SimpleVector SPAWN_POSITION_RED = new SimpleVector(-119.79689, 0.0, 133.0693);
	public static final float SPAWN_RADIUS_BLUE = 1.0f;
	public static final float SPAWN_RADIUS_RED = 1.0f;
	public static final SimpleCustomVector WAYPOINT_CREATOR_STARTING_POINT = new SimpleCustomVector(-16.266878f, 0.0f, 14.0f);
	public static final double WAYPOINT_GRID_DELTA = 3.00;
	public static final double WAYPOINT_RADIUS = 1.135f;
	public static final int NUMBER_THREADS_AI = 16;//(NUMBER_AGENTS_BLUE + NUMBER_AGENTS_RED) / 256;
}
