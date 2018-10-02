package ck.ia.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ck.graphics.Ticker;
import ck.ia.Config;
import ck.ia.Entity;
import ck.ia.EntityExecutor;
import ck.ia.EntityType;
import ck.ia.goals.GoalManager;
import ck.navigation.SimpleCustomVector;
import ck.navigation.WaypointCreator;
import ck.navigation.WaypointNavigation;

import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class EnvironmentAgent {
	public static final int ENVIROMENT_ID = Config.ENVIRONMENT_ID;
	private HashSet<Entity> carried_entities;
	private HashMap<Integer, Entity> entities;
	private ArrayList<Entity> entitiesListExceptMoving;
	private GoalManager goalManager;
	private HashSet<Integer> moving_agents_ids;
	private HashMap<Integer, HashSet<MovingAgent>> moving_agents_teams;
	private ArrayList<Entity> movingEntitiesList;
	private HashMap<Integer, Integer> spawn_agents_ids;
	private ArrayList<SpawnAgent> spawnAgents;
	private Ticker ticker;
	private WaypointNavigation waypointNavigation;
	private final int CYCLE_MAX_MS = 20;
	private final double TIME_DELTA = (double) CYCLE_MAX_MS / 1000.0;

	public EnvironmentAgent(World world, World wireframeWorld) {
		createWaypoints(world, wireframeWorld);
		moving_agents_ids = new HashSet<Integer>();
		moving_agents_teams = new HashMap<Integer, HashSet<MovingAgent>>();
		entities = new HashMap<Integer, Entity>();
		entitiesListExceptMoving = new ArrayList<>();
		movingEntitiesList = new ArrayList<>();
		carried_entities = new HashSet<Entity>();
		spawn_agents_ids = new HashMap<Integer, Integer>();
		spawnAgents=new ArrayList<>();
		this.goalManager = new GoalManager(this);
		this.ticker = new Ticker();
	}

	public void addCarryable(Entity payload) {
		carried_entities.add(payload);
	}

	public void addEntity(Entity entity) {
		entities.put(entity.getId(), entity);
		if (entity.getType() == EntityType.MOVING_AGENT) {
			if (!movingEntitiesList.contains(entity)) {
				movingEntitiesList.add(entity);
			}
		} else {
			if (!entitiesListExceptMoving.contains(entity)) {
				entitiesListExceptMoving.add(entity);
			}
		}
	}

	public void addMovingAgent(MovingAgent agent) {
		int id = agent.getId();
		moving_agents_ids.add(id);
		int agent_team = agent.getTeam();
		HashSet<MovingAgent> team = moving_agents_teams.get(agent_team);
		if (team == null) {
			team = new HashSet<MovingAgent>();
			moving_agents_teams.put(agent_team, team);
		}
		team.add(agent);
		addEntity(agent);
	}

	public void addSpawnAgent(SpawnAgent agent) {
		spawn_agents_ids.put(agent.getTeam(), agent.getId());
		spawnAgents.add(agent);
		addEntity(agent);
	}

	private void createWaypoints(World world, World wireframeWorld) {
		SimpleCustomVector testPosition = Config.WAYPOINT_CREATOR_STARTING_POINT;
		WaypointCreator wc = new WaypointCreator(world, testPosition, wireframeWorld);
		wc.createWaypoints();
		waypointNavigation = new WaypointNavigation(wc.getLines(), wc.getVertices());
		System.out.println(this.toString() + " navigating waypoints");
	}

	public void forkEntityExecuters() {
		int n = movingEntitiesList.size();
		int numberThreads = Config.NUMBER_THREADS_AI;
		if (n < 64)
			numberThreads = 1;

		ExecutorService pool = Executors.newFixedThreadPool(numberThreads);

		int range_size = n / numberThreads;

		for (int thread_id = 0; thread_id < numberThreads; thread_id++) {
			int range_l = range_size * thread_id;
			int range_h = range_size * (thread_id + 1);
			if (thread_id == numberThreads - 1)
				range_h = n;
			pool.execute(new EntityExecutor(movingEntitiesList, range_l, range_h));
		}
		// pool.shutdown();
	}

	public void executeInParallel() {
		forkEntityExecuters();

		ticker.getTimeDeltaLastCall();
		while (true) {
			// give cycles to corresponding entities
			for (Entity entity : spawnAgents) {
				entity.executeCycle(TIME_DELTA);
			}
			double loopTimeSec = ticker.getTimeDeltaLastCall();
			double deltaMS = loopTimeSec * 1000.0;
			double sleepMS = CYCLE_MAX_MS - deltaMS;
			if (sleepMS > 0) {
				// time slice
				try {
					Thread.sleep((long) sleepMS);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public HashSet<Entity> getCarriedEntities() {
		return this.carried_entities;
	}

	public Entity getEntity(int id) {
		return entities.get(id);
	}

	public GoalManager getGoalManager() {
		return goalManager;
	}

	public MovingAgent getMovingAgent(int id) {
		return (MovingAgent) this.getEntity(id);
	}

	public HashSet<MovingAgent> getMovingAgents(int teamID) {
		HashSet<MovingAgent> team = moving_agents_teams.get(teamID);
		if (team == null) {
			team = new HashSet<MovingAgent>();
			moving_agents_teams.put(teamID, team);
		}
		return team;
	}

	public SimpleVector getRandomWaypoint() {
		return waypointNavigation.getRandomWaypoint();
	}

	public SpawnAgent getSpawnAgent(int team) {
		return (SpawnAgent) this.getEntity(getSpawnAgentID(team));
	}

	public int getSpawnAgentID(int team) {
		return spawn_agents_ids.get(team);
	}

	public List<SimpleVector> requestPathTo(SimpleVector from, SimpleVector to) {
		List<SimpleVector> path = waypointNavigation.getPathAsArrayList(from, to);
		return path;
	}

	public String toString() {
		return "environment:" + ENVIROMENT_ID;
	}
}
