package ck.ia.agents;

import java.util.List;

import ck.graphics.FastAtan2;
import ck.graphics.FastTrig;
import ck.ia.Config;
import ck.ia.Entity;
import ck.ia.EntityType;
import ck.ia.goals.Goal;
import ck.ia.goals.GoalManager;
import ck.ia.goals.GoalStatus;
import ck.ia.goals.GoalType;

import com.threed.jpct.SimpleVector;

public class MovingAgent extends Entity {
	protected volatile Goal intention;
	protected boolean alive;
	protected int currentWaypoint_i;
	protected int team;
	private boolean halt = false;
	private int cycleCounter;
	private List<SimpleVector> path;
	private GoalManager goalManager;

	/**
	 * Constructs a new SoldierAgent on the given team, positioned on the given position, with the given radius, starting with
	 * the given health and set on the given environment.
	 * 
	 * @param team
	 *            The agent's team.
	 * @param position
	 *            It's position on the world / environment.
	 * @param soldierRadius
	 *            It's radius (used for path cornering and action's radius).
	 * @param health
	 *            It's health. If equal or lower than zero, he's dead.
	 * @param environment
	 *            The environment where the agent lives and carries functions.
	 */
	public MovingAgent(int team, SimpleVector position, float soldierRadius, float health, EnvironmentAgent environment) {
		super(health, position, soldierRadius, environment);
		this.goalManager = environment.getGoalManager();
		super.type = EntityType.MOVING_AGENT;
		this.team = team;
		intention = null;
		alive = !this.isDead();
		cycleCounter = 0;
	}

	/**
	 * Executes a single iteration typical of a 'dead' agent. Currently set's the agent turn down and only senses the
	 * environment.
	 * 
	 * @param cycleTime
	 */
	private void behaviourIncapacitated(double cycleTime) {
	}

	/**
	 * Executes a single iteration typical of a 'living' agent. Sets the agent with a normal posture, senses the environment,
	 * executes it's primary intention and randomly chooses a goal.
	 * 
	 * @param cycleTime
	 */
	private void behaviourNormal(double cycleTime) {
		if (intention == null) {
			chooseGoal();
		}

		// if we have an intention, execute it
		if (intention != null) {
			executeIntention(cycleTime);
		}
		cycleCounter++;
		if (cycleCounter >= 2147483519)
			cycleCounter = 0;
	}

	private void chooseGoal() {

		boolean unlocked = false;
		goalManager.lock.lock();
		try {

			// intention chosen from the available goals (not assigned OR shared) as the highest priority incomplete goal
			Goal bestFoundGoal = goalManager.getBestGoal(this);
			// do nothing if there is no better goal or got the same as currently
			if (bestFoundGoal == null)
				return;
			// the best found goal is the current intention
			if (intention != null && bestFoundGoal.getId() == intention.getId())
				return;
			// is the candidate goal more important than the current intention?
			// if it is, we'll set it as the current intention
			if (intention == null || bestFoundGoal.getPriority() > intention.getPriority()) {
				// if we acquire a new goal and the last one was a carry goal, carrying the payload
				if (intention != null && intention.getType() == GoalType.CARRY
						&& intention.getStatus() == GoalStatus.APPROACHING_CAPTURE_POSITION) {
					leavePayload();
				}
				// set the last intention as without an owner
				if (intention != null) {
					intention.reset();
				}
				// -----
				bestFoundGoal.reset();
				bestFoundGoal.assign(this.getId());
				intention = bestFoundGoal;

				goalManager.lock.unlock();
				unlocked = true;

				// get destination according to intention type
				SimpleVector destination = null;
				switch (intention.getType()) {
				case ROAM:
					destination = intention.getDestinationRoam();
					break;
				case CARRY:
					Entity payload = intention.getPayload();
					if (intention.getType() == GoalType.CARRY) {
						destination = payload.getPosition();
					}
					break;
				}
				path = requestPathTo(destination);
				currentWaypoint_i = 0;
			}
		} finally {
			if (!unlocked)
				goalManager.lock.unlock();
		}
	}

	private void died() {
		// if we're incapacitated, let's pretend we are...
		this.setRoll(Config.SOLDIER_DEAD_ROLL);
		// let's move ourselves on the floor
		SimpleVector mypos = this.getPosition();
		mypos.add(Config.SOLDIER_DEAD_OFFSET);
		// we must leave our current intention
		if (intention != null) {
			leaveIntention();
		}
	}

	private void leaveIntention() {
		if (intention.getType() == GoalType.CARRY && intention.getStatus() == GoalStatus.APPROACHING_CAPTURE_POSITION) {
			leavePayload();
		}
		// set the intention as without an owner
		intention.reset();
		// we'll need to start all over again
		intention = null;
	}

	private void leavePayload() {
		// let's re-position the payload
		Entity payload = intention.getPayload();
		SimpleVector payload_position = payload.getPosition();
		// leave it on the floor
		// payload_position.sub(Config.CARRY_GOAL_PAYLOAD_OFFSET);
		// somewhere near us
		payload_position.x += 0;
		payload_position.z += 0;
	}

	/**
	 * Gives a 'cycle' to the agent. Basically it allows him to do it's life for a single instant.
	 */
	public void executeCycle(double cycleTime) {
		// System.out.println(cycleTime);
		if (this.halt) {
			System.out.println("insert breakpoint here");
			this.halt = false;
		}
		super.executeCycle(cycleTime);
		// I'm dead
		if (this.isDead()) {
			// but I was living before
			if (alive) {
				alive = false;
				this.died();
			}
			behaviourIncapacitated(cycleTime);
		} else
		// I'm alive
		{
			// but I was dead before
			if (!alive) {
				alive = true;
				this.revived();
			}
			behaviourNormal(cycleTime);
		}
	}

	/**
	 * Executes what the agent has chosen as the primary intention. According to it, it may move to somewhere, follow some path,
	 * rotate towards something, carry a payload, etc. It also checks for the completeness of the intention and warns the
	 * environment of it's state. If new type of goals are created this function must be updated.
	 * 
	 * @param cycleTime
	 */
	private void executeIntention(double cycleTime) {

		motorControl(cycleTime);

		// check for goal completion
		SimpleVector lastWaypoint = path.get(path.size() - 1);
		if (lastWaypoint.distance(this.getPosition()) < this.getRadius()) {
			// if our goal is the roam, it completes when we arrive at it's destination
			switch (intention.getType()) {
			case ROAM:
				intention.setStatus(GoalStatus.COMPLETE);
				goalManager.resetGoal(intention);
				intention = null;
				break;
			case CARRY:
				if (intention.getStatus() == GoalStatus.APROACHING_PAYLOAD) {
					intention.setStatus(GoalStatus.APPROACHING_CAPTURE_POSITION);
					SimpleVector destination = intention.getDestinationDeliverCargo();
					path = requestPathTo(destination);
					currentWaypoint_i = 0;
					break;
				} else if (intention.getStatus() == GoalStatus.APPROACHING_CAPTURE_POSITION) {
					leavePayload();
					intention.setStatus(GoalStatus.COMPLETE);
					goalManager.resetGoal(intention);
					intention = null;
					break;
				}
				break;
			}
		}
	}

	private void motorControl(double cycleTime) {
		if (path != null) {
			int last_waypoint_i = path.size() - 1;
			SimpleVector currentWaypoint = path.get(currentWaypoint_i);
			double distance_to_waypoint = currentWaypoint.distance(this.getPosition());
			// System.out.println(path.size() + "\t" + currentWaypoint_i + "\t" + distance_to_waypoint);
			if (distance_to_waypoint < Config.WAYPOINT_RADIUS) {
				if (currentWaypoint_i < last_waypoint_i) {
					currentWaypoint_i++;
				}
			}
			// orient myself (rotate) and move towards the destination
			double soldierMovementSpeed = Config.SOLDIER_MOVEMENT_SPEED * cycleTime;
			double soldierRotationSpeed = Config.SOLDIER_ROTATION_SPEED * cycleTime;
			orientAndMoveSmooth(currentWaypoint, soldierMovementSpeed, soldierRotationSpeed);

			// if our intention is the carry and it's status is approaching the capture position with the payload

			if (intention.getType() == GoalType.CARRY && intention.getStatus() == GoalStatus.APPROACHING_CAPTURE_POSITION) {
				// we carry the payload along our movement
				SimpleVector payload_position = intention.getPayload().getPosition();
				payload_position.x = this.getPosition().x + Config.CARRY_GOAL_PAYLOAD_OFFSET.x;
				payload_position.y = this.getPosition().y + Config.CARRY_GOAL_PAYLOAD_OFFSET.y;
				payload_position.z = this.getPosition().z + Config.CARRY_GOAL_PAYLOAD_OFFSET.z;
				intention.getPayload().setYaw(this.getYaw());
			}
		}
	}

	/**
	 * Returns the agent's team.
	 * 
	 * @return it's team identifier
	 */
	public int getTeam() {
		return this.team;
	}

	/**
	 * Improved version of orientAndMove(), much more smooth. The agent moves and rotates linearly towards the destination,
	 * changing it's facing direction. The agent moves by some amount (vector) in the direction it's currently facing.
	 * 
	 * @param destination
	 *            The destination of movement.
	 * @param movingSpeed
	 *            The movement's step (movement is actually the vector current direction multiplied by the step).
	 * @param rotationSpeed
	 *            The rotation's step. Higher values makes the agent rotate much faster towards the destination. Huge values
	 *            makes it instant.
	 */
	private void orientAndMoveSmooth(SimpleVector destination, double movingSpeed, double rotationSpeed) {
		// get my direction (from my position to my destination)
		SimpleVector currentPosition = this.getPosition();
		// calculate direction vector (from current to destination)
		double dx = destination.x - currentPosition.x;
		double dy = destination.y - currentPosition.y;
		double dz = destination.z - currentPosition.z;
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		// make sure we don't go beyond destination
		if (movingSpeed > len)
			movingSpeed = len;
		// normalize direction
		dx = dx / len;
		dy = dy / len;
		dz = dz / len;
		// do we have movement?
		if (len > 0.001) {
			double yawCurrent = this.getYaw();
			double pi2 = Math.PI * 2;

			if (yawCurrent < 0)
				yawCurrent += pi2;
			if (yawCurrent > pi2)
				yawCurrent -= pi2;

			// calculate yaw corresponding to required direction
			double yawTarget = 0.0;
			if (dx == 0.0 && dz == 0.0) {
				yawTarget = yawCurrent;
			} else {
				yawTarget = FastAtan2.atan2((float) dx, (float) dz);
			}
			if (yawTarget < 0)
				yawTarget += pi2;
			// if (yawTarget > pi2)
			// yawTarget -= pi2;

			// compute rotation step (yaw delta) ie rotate a little
			double rotationDelta = calculateRotationStep(rotationSpeed, yawCurrent, yawTarget);
			// rotate (add angle step to current yaw)
			double newYaw = yawCurrent + rotationDelta;
			// if (newYaw > 2 * Math.PI)
			// newYaw -= 2 * Math.PI;

			this.setYaw(newYaw);
			dx = FastTrig.sin(newYaw);
			dz = FastTrig.cos(newYaw);

			dx *= movingSpeed;
			dy *= movingSpeed;
			dz *= movingSpeed;

			// move
			currentPosition.x += dx;
			currentPosition.y += dy;
			currentPosition.z += dz;
		}
	}

	/**
	 * Calculates the best rotation step from the agent's current yaw (rotation xOz), the target yaw and some step as maximum.
	 * 
	 */
	private double calculateRotationStep(double rotateStep, double yawCurrent, double yawTarget) {
		// how much do we need to rotate?
		double yawDif = yawTarget - yawCurrent;
		double absYawDif = Math.abs(yawDif);
		if (absYawDif < rotateStep)
			rotateStep = absYawDif;
		boolean reverse = false;
		if (yawDif < 0)
			rotateStep = -rotateStep; // should be false
		if (absYawDif > Math.PI)
			reverse = !reverse; // reverse the direction because |angle| is > 180º
		if (reverse)
			rotateStep = -rotateStep;

		// System.out.println(yawCurrent + "\t" + yawTarget + "\t" + rotateStep);
		return rotateStep;
	}

	/**
	 * Requests the environment the shortest path from this agent's position to the given destination.
	 * 
	 * @param to
	 *            Where the path leads to.
	 * @return
	 */
	private List<SimpleVector> requestPathTo(SimpleVector to) {
		SimpleVector from = this.getPosition();
		return environment.requestPathTo(from, to);
	}

	private void revived() {
		// if we're normal, let's have a normal posture
		this.setRoll(0.0f);
		// let's move ourselves on the floor (the roll moves us up & down)
		SimpleVector mypos = this.getPosition();
		mypos.sub(Config.SOLDIER_DEAD_OFFSET);
	}

	/**
	 * asked by the spawn agent to check for re-spawns
	 * 
	 * @return if this agents wants to re-spawn (be positioned at the spawn point)
	 */
	public boolean waitingToSpawn() {
		return this.isDead();
	}

	public void halt() {
		this.halt = true;
	}

}
