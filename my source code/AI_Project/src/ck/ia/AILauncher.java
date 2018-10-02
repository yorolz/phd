package ck.ia;

import java.util.concurrent.TimeUnit;

import ck.ia.agents.EnvironmentAgent;
import ck.ia.agents.MovingAgent;
import ck.ia.agents.SpawnAgent;

import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class AILauncher {
	private EnvironmentAgent environment;

	/**
	 * Initializes the goal simulator as a stand-alone thread, including the environment inside it. After the call the
	 * environment will be paused, all the agents and entities will have been initialized and ready for execution.
	 */
	public AILauncher(World world, World wireframeWorld) {
		environment = new EnvironmentAgent(world,wireframeWorld);
		initializeEnvironment();
		executeInParallel();
	}

	public void executeInParallel() {
		new Thread("AI main thread") {
			public void run() {
				environment.executeInParallel();
			}
		}.start();
	}

	public void addMovingAgents() {
		float soldiers_initial_health = Config.SOLDIER_HEALTH_INITIAL;
		// soldier agents
		for (int i = 0; i < Config.NUMBER_AGENTS_RED; i++) {
			MovingAgent agent = new MovingAgent(0, new SimpleVector(), Config.SOLDIER_RADIUS, soldiers_initial_health,
					environment);
			environment.addMovingAgent(agent);
		}
		for (int i = 0; i < Config.NUMBER_AGENTS_BLUE; i++) {
			MovingAgent agent = new MovingAgent(1, new SimpleVector(), Config.SOLDIER_RADIUS, soldiers_initial_health,
					environment);
			environment.addMovingAgent(agent);
		}
	}

	public void addSpawnAgents() {
		TimeUnit tu = TimeUnit.SECONDS;
		long spawn_interval_ns = tu.toNanos(Config.SPAWN_INTERVAL_SECONDS);
		long blue_phase_ns = tu.toNanos(Config.SPAWN_PHASE_SECONDS_BLUE);
		long red_phase_ns = tu.toNanos(Config.SPAWN_PHASE_SECONDS_RED);
		environment.addSpawnAgent(new SpawnAgent(0, new SimpleVector(Config.SPAWN_POSITION_RED), Config.SPAWN_RADIUS_RED,
				spawn_interval_ns, red_phase_ns, environment));
		environment.addSpawnAgent(new SpawnAgent(1, new SimpleVector(Config.SPAWN_POSITION_BLUE), Config.SPAWN_RADIUS_BLUE,
				spawn_interval_ns, blue_phase_ns, environment));
	}

	public EnvironmentAgent getEnvironment() {
		return environment;
	}

	public void initializeEnvironment() {
		addSpawnAgents();
		addMovingAgents();
		System.out.println("Goal Simulator's environment initialized");
	}
}
