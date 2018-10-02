package ck.ia.goals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import ck.ia.Config;
import ck.ia.Entity;
import ck.ia.agents.EnvironmentAgent;
import ck.ia.agents.MovingAgent;

import com.threed.jpct.SimpleVector;

public class GoalManager {

	private EnvironmentAgent ea;
	private ArrayList<Goal> goals;
	private HashMap<Integer, Goal> goals_id;
	public ReentrantLock lock=new ReentrantLock(true);

	public GoalManager(EnvironmentAgent ea) {
		this.ea = ea;
		goals = new ArrayList<Goal>();
		goals_id = new HashMap<Integer, Goal>();
		addAllGoals();

	}

	private void addAllGoals() {
		addRoamGoals(0, Config.NUMBER_GOALS_ROAM_RED);
		addRoamGoals(1, Config.NUMBER_GOALS_ROAM_BLUE);
		addCarryGoals(0, Config.NUMBER_GOALS_CARRY_RED, Config.CARRY_GOAL_INITIAL_POSITION_RED);
		addCarryGoals(1, Config.NUMBER_GOALS_CARRY_BLUE, Config.CARRY_GOAL_INITIAL_POSITION_BLUE);
	}

	private void addCarryGoals(int team, int number_of_goals, SimpleVector initial_position_) {
		// carry goals, for each team
		for (int i = 0; i < number_of_goals; i++) {
			// random position the goal near their default position
			// setup the goal's entity
			Entity payload = new Entity(Float.MAX_VALUE, null, Config.CARRY_ENTITY_RADIUS, ea);
			ea.addEntity(payload);
			ea.addCarryable(payload);
			Goal goal = new Goal(GoalType.CARRY, Config.GOAL_PRIORITY_CARRY, team);
			goal.setPayload(payload);
			setupCarryGoal(goal, initial_position_, true);
			this.addGoal(goal);
		}
	}

	private void addGoal(Goal goal) {
		int goal_id = goal.getId();
		goals_id.put(goal_id, goal);
		goals.add(goal);
	}

	private void addRoamGoals(int team, int number_of_goals) {
		for (int i = 0; i < number_of_goals; i++) {
			Goal goal = new Goal(GoalType.ROAM, Config.GOAL_PRIORITY_ROAM, team);
			setupRoamGoal(goal);
			this.addGoal(goal);
		}
	}

	/**
	 * Computes the 'cost' (currently a simplified version of traveling distances) of the given goal.
	 * 
	 * @param g
	 *            The goal to calculate the cost from.
	 * @return The cost of the goal or -1 if the cost can't be calculated (because that goal type does not exist).
	 * @throws Exception
	 */
	private double calculateGoalCost(Goal g, SimpleVector agentPosition) {
		double cost = Double.MAX_VALUE;
		switch (g.getType()) {
		case ROAM:
			// roam's value is just the distance from the agent to goal destination
			cost = agentPosition.distance(g.getDestinationRoam());
			break;
		case CARRY:
			cost = agentPosition.distance(g.getPayload().getPosition());
		}
		return cost;
	}

	public ArrayList<Goal> getAllGoals() {
		return goals;
	}

	/**
	 * Gets the best (higher priority and in case of goals of same higher priority, the nearest) goal from the desires.
	 * 
	 * @return the best goal.
	 */
	public Goal getBestGoal(MovingAgent agent) {
		int agentTeam = agent.getTeam();
		SimpleVector agentPosition = agent.getPosition();

		Goal candidate = null;
		double candidateGoalCost = Double.POSITIVE_INFINITY;
		// scan all desires (available goals)
		for (Goal testGoal : goals) {
			// we only want goals assigned to our team
			if (testGoal.getTeam() != agentTeam)
				continue;
			boolean assigned = testGoal.isAssigned();
			if (assigned)
				continue;
			boolean complete = testGoal.getStatus() == GoalStatus.COMPLETE;
			if (complete)
				continue;

			// rate the goal according to it's 'cost'
			double goalCost = calculateGoalCost(testGoal, agentPosition);
			// if our current candidate is null, any goal is better
			if (candidate == null) {
				candidate = testGoal;
				candidateGoalCost = goalCost;
			} else {
				// if the analyzed goal has higher priority
				if (testGoal.getPriority() > candidate.getPriority()) {
					// higher priority goals are always preferred
					candidate = testGoal;
					candidateGoalCost = goalCost;
				} else
				// the analyzed goal has the same priority as the candidate
				if (testGoal.getPriority() == candidate.getPriority()) {
					// it's chosen if it's cost is lower than the candidate's one
					if (goalCost < candidateGoalCost) {
						candidate = testGoal;
						candidateGoalCost = goalCost;
					}
				}
			}
		}
		return candidate;
	}

	public Goal getGoal(int id) {
		return goals_id.get(id);
	}

	public void resetGoal(Goal g) {
		switch (g.getType()) {
		case CARRY:
			this.setupCarryGoal(g, ea.getRandomWaypoint(), false);
			break;
		case ROAM:
			this.setupRoamGoal(g);
			break;
		}
	}

	private void setupCarryGoal(Goal goal, SimpleVector initial_position_, boolean disperseInitial) {
		SimpleVector initial_position = new SimpleVector(initial_position_);
		goal.getPayload().setPosition(initial_position);
		if (disperseInitial) {
			initial_position.x += Config.CARRY_GOAL_INITIAL_POSITION_DISPERSION * (Math.random() * 2 - 1);
			initial_position.z += Config.CARRY_GOAL_INITIAL_POSITION_DISPERSION * (Math.random() * 2 - 1);
		}
		// choose a random capture point (according to the team)
		int position = (int) (Math.random() * 3);
		SimpleVector capture_position;
		if (goal.getTeam() == 0) {
			if (position == 0) {
				capture_position = Config.CARRY_GOAL_CAPTURE_POSITION_RED_1;
			} else if (position == 1) {
				capture_position = Config.CARRY_GOAL_CAPTURE_POSITION_RED_2;
			} else {
				capture_position = Config.CARRY_GOAL_CAPTURE_POSITION_RED_3;
			}
		} else {
			if (position == 0) {
				capture_position = Config.CARRY_GOAL_CAPTURE_POSITION_BLUE_1;
			} else if (position == 1) {
				capture_position = Config.CARRY_GOAL_CAPTURE_POSITION_BLUE_2;
			} else {
				capture_position = Config.CARRY_GOAL_CAPTURE_POSITION_BLUE_3;
			}
		}
		capture_position = new SimpleVector(capture_position);
		// shake capture position a little bit
		capture_position.x += Config.CARRY_GOAL_CAPTURE_POSITION_DISPERSION * (Math.random() * 2 - 1);
		capture_position.z += Config.CARRY_GOAL_CAPTURE_POSITION_DISPERSION * (Math.random() * 2 - 1);
		// link the goal and his entity
		goal.setDestinationDeliverCargo(capture_position);
		goal.reset();
	}

	private void setupRoamGoal(Goal goal) {
		SimpleVector randomWaypoint = ea.getRandomWaypoint();
		goal.setDestinationRoam(new SimpleVector(randomWaypoint));
		goal.reset();
	}

}
