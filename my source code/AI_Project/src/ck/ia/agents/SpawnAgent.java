package ck.ia.agents;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import ck.ia.Config;
import ck.ia.Entity;
import ck.ia.EntityType;

import com.threed.jpct.SimpleVector;

public class SpawnAgent extends Entity {
	/**
	 * next time at which this agent will activate
	 */
	private long triggerTime;
	/**
	 * the time between successive triggers
	 */
	private long spawnPeriod;
	/**
	 * the team associated with this agent
	 */
	private int team;
	private static Timer timer = new Timer();

	public SpawnAgent(int team, SimpleVector position, float radius, long spawn_interval_ns, long phase_ns,
			EnvironmentAgent environment) {
		super(Config.SPAWN_HEALTH, position, radius, environment);
		super.type = EntityType.SPAWN_AGENT;
		this.triggerTime = getElapsedTime() + phase_ns;
		this.spawnPeriod = spawn_interval_ns;
		this.team = team;
		this.environment = environment;

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				spawnCheck();
				// set last spawn as current
				triggerTime = triggerTime + spawnPeriod;
			}
		}, phase_ns / 1000000, spawn_interval_ns / 1000000);
	}

	public void executeCycle(double cycleTime) {
	}

	public float getInterval() {
		return this.spawnPeriod;
	}

	/**
	 * Returns the progress of the spawn timer in the range 0...1, being 0 recently spawn and 1 about to spawn.
	 * 
	 * @return The progress
	 */
	public double getSpawnProgress() {
		long currentTime = getElapsedTime();
		long dif = triggerTime - currentTime;
		double v = ((double) (dif)) / ((double) (spawnPeriod));
		if (v < 0) {
			v = 0;
		}
		return v;
	}

	public int getTeam() {
		return this.team;
	}

	private long getElapsedTime() {
		return System.nanoTime();
	}

	/**
	 * request the environment the positioning of the moving agents
	 */
	public void positionMovingAgent(int id) {
		MovingAgent movingAgent = environment.getMovingAgent(id);
		SimpleVector agentPosition = movingAgent.getPosition();
		SimpleVector spawnPosition = new SimpleVector(this.getPosition());
		agentPosition.x = (float) (spawnPosition.x + this.getRadius() * (Math.random() * 2 - 1));
		agentPosition.y = spawnPosition.y;
		agentPosition.z = (float) (spawnPosition.z + this.getRadius() * (Math.random() * 2 - 1));
		double yaw = Math.PI * (Math.random() * 2.0f - 1);
		movingAgent.setYaw((float) yaw);
	}

	/**
	 * request the environment the revival of the moving agents
	 */
	private void informMovingRevival(int id) {
		MovingAgent movingAgent = this.environment.getMovingAgent(id);
		movingAgent.setHealth(Config.SOLDIER_HEALTH_NORMAL);
	}

	/**
	 * checks for this spawn's team agents need to spawn
	 */
	private void spawnCheck() {
		HashSet<MovingAgent> movingAgents = this.environment.getMovingAgents(this.getTeam());
		for (MovingAgent sa : movingAgents) {
			// if some agent is from this spawn's team and he wants to respawn
			if (sa.waitingToSpawn()) {
				// spawn him near within this spawn's radius
				int saID = sa.getId();
				informMovingRevival(saID);
				positionMovingAgent(saID);
			}
		}
	}

	public String toString() {
		return super.toString() + " team:" + this.getTeam() + " interval:" + this.getInterval();
	}

}
