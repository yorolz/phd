package ck.ia.goals;

import ck.ia.Entity;

import com.threed.jpct.SimpleVector;

public class Goal implements Comparable<Goal> {
	private static int id_counter = 0;
	private SimpleVector destinationDeliverCargo;
	/**
	 * positions where goal changes state/type
	 */
	private SimpleVector destinationRoam;

	/**
	 * unique identifier for each goal
	 */
	private int id;

	/**
	 * the entity which owns (executes or plans to execute) this goal
	 */
	private int owner_id;

	/**
	 * the entity associated with this goal, eg, a box (entity) which must be delivered somewhere (destination)
	 */
	private Entity payload;
	/**
	 * an associated priority with this goal
	 */
	private int priority;

	private GoalStatus status;

	/**
	 * the team associated with this goal
	 */
	private int team;

	private GoalType type;

	public Goal(Goal g) {
		this.destinationDeliverCargo = g.destinationDeliverCargo;
		this.destinationRoam = g.destinationRoam;
		this.id = g.id;
		this.owner_id = g.owner_id;
		this.payload = g.payload;
		this.priority = g.priority;
		this.status = g.status;
		this.team = g.team;
		this.type = g.type;
	}

	/**
	 * 
	 * @param type
	 * @param related
	 * @param priority
	 * @param team
	 * @param status
	 * @param shared
	 * @param default_owner
	 */
	public Goal(GoalType type, int priority, int team) {
		this.id = id_counter;
		id_counter++;
		this.type = type;
		this.priority = priority;
		this.team = team;
		this.reset();
	}

	public void assign(int owner_id) {
		this.owner_id = owner_id;
	}

	public int compareTo(Goal o) {
		int my_id = this.getId();
		int others_id = o.getId();
		if (my_id < others_id)
			return -1;
		else if (my_id == others_id)
			return 0;
		else
			return +1;
	}

	public boolean equals(Goal g) {
		return this.getId() == g.getId();
	}

	public SimpleVector getDestinationDeliverCargo() {
		return destinationDeliverCargo;
	}

	public SimpleVector getDestinationRoam() {
		return destinationRoam;
	}

	public int getId() {
		return id;
	}

	public int getOwnerID() {
		return owner_id;
	}

	public Entity getPayload() {
		return payload;
	}

	public float getPriority() {
		return priority;
	}

	public GoalStatus getStatus() {
		return status;
	}

	public int getTeam() {
		return team;
	}

	public GoalType getType() {
		return type;
	}

	public int hashCode() {
		return this.getId();
	}

	public boolean isAssigned() {
		return this.getOwnerID() >= 0;
	}

	public void reset() {
		switch (this.getType()) {
		case CARRY:
			this.setStatus(GoalStatus.APROACHING_PAYLOAD);
			break;
		case ROAM:
			this.setStatus(GoalStatus.ROAMING);
			break;
		}
		this.unassign();
	}

	public void setDestinationDeliverCargo(SimpleVector destinationDeliverCargo) {
		this.destinationDeliverCargo = destinationDeliverCargo;
	}

	public void setDestinationRoam(SimpleVector destinationRoam) {
		this.destinationRoam = destinationRoam;
	}

	public void setPayload(Entity entity) {
		this.payload = entity;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setStatus(GoalStatus status) {
		this.status = status;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public String toString() {
		return "Goal:" + this.getId() + " Type:" + this.getType() + " Priority:" + this.getPriority() + " Team:"
				+ this.getTeam() + " Status:" + this.getStatus() + " Assigned:" + this.isAssigned() + " Owner:"
				+ this.getOwnerID();
	}

	public void unassign() {
		this.owner_id = -1;
	}
}
