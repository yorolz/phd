package ck.ia;

import ck.ia.agents.EnvironmentAgent;

import com.threed.jpct.SimpleVector;

public class Entity implements Comparable<Entity> {

	private static int id_counter = 0;
	protected EnvironmentAgent environment;
	protected double health;
	protected int id;
	protected double pitch, roll, yaw;
	protected SimpleVector position;
	protected double radius;
	protected EntityType type;

	/**
	 * Constructs a new entity.
	 * 
	 * @param health
	 *            The initial health of this entity.
	 * @param position
	 *            The initial position of this entity.
	 * @param soldierRadius
	 *            The initial radius of this entity.
	 * @param type
	 *            The type of this entity.
	 * @param environment
	 *            The environment where this entity resides.
	 */
	public Entity(double health, SimpleVector position, double soldierRadius, EnvironmentAgent environment) {
		this.position = position;
		this.environment = environment;
		this.radius = soldierRadius;
		this.id = id_counter;
		this.health = health;
		this.type = EntityType.UNDEFINED;
		id_counter++;
		this.pitch = 0.0f;
		this.roll = 0.0f;
		this.yaw = 0.0f;
	}

	/**
	 * Compares this entity's ID to the given one.
	 */
	public int compareTo(Entity o) {
		int my_id = this.getId();
		int others_id = o.getId();
		if (my_id < others_id)
			return -1;
		else if (my_id == others_id)
			return 0;
		else
			return +1;
	}

	/**
	 * Damages this entity, i.e., it's health is reduced by the given amount.
	 * 
	 * @param damage
	 *            The amount of damage to be caused.
	 */
	public void damage(double damage) {
		this.health -= damage;
		if (this.health < 0) {
			this.health = 0.0f;
		}
	}

	/**
	 * Checks if this entity and another one are the same.
	 * 
	 * @param e
	 *            The entity to compare to.
	 * @return True if the entities are the same (only if they have the same ID).
	 */
	public boolean equals(Entity e) {
		return this.getId() == e.getId();
	}

	/**
	 * Executes a single moment of this entity. Basically does nothing, because this entity has no activity.
	 * 
	 * @param cycleTime
	 */
	public void executeCycle(double cycleTime) {
	}

	/**
	 * Returns the environment where this entity lives.
	 * 
	 * @return The environment.
	 */
	public EnvironmentAgent getEnvironment() {
		return environment;
	}

	/**
	 * Gets this entity's health.
	 * 
	 * @return The health.
	 */
	public double getHealth() {
		return health;
	}

	/**
	 * Returns this entity's identifier. The identifier is a unique integer for each entity.
	 * 
	 * @return the entity's ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns this entity's pitch (rotation on plane xOy).
	 * 
	 * @return the pitch on radians.
	 */
	public double getPitch() {
		return pitch;
	}

	/**
	 * Returns this entity's position on the environment.
	 * 
	 * @return the position.
	 */
	public SimpleVector getPosition() {
		return position;
	}

	/**
	 * Returns this entity's radius.
	 * 
	 * @return the radius.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Returns this entity's roll (rotation on plane yOz).
	 * 
	 * @return the roll on radians
	 */
	public double getRoll() {
		return roll;
	}

	/**
	 * Returns this entity (specialization) type.
	 * 
	 * @return it's type.
	 */
	public EntityType getType() {
		return type;
	}

	/**
	 * Returns this entity's yaw (rotation on plane xOz).
	 * 
	 * @return the yaw on radians.
	 */
	public double getYaw() {
		return yaw;
	}

	/**
	 * Returns a unique hashing code for each entity, so it returns this entity's ID.
	 * 
	 * @return this entity's ID.
	 */
	public int hashcode() {
		return this.getId();
	}

	/**
	 * Returns true if this entity is has health lower or equal to zero.
	 * 
	 * @return true if this entity has no health.
	 */
	public boolean isDead() {
		return this.getHealth() <= 0.0f;
	}

	/**
	 * Sets this entity's health to the given value.
	 * 
	 * @param health
	 *            the health to set to.
	 */
	public void setHealth(double health) {
		this.health = health;
	}

	/**
	 * Sets this entity's pitch (rotation on plane xOy).
	 * 
	 * @param pitch
	 *            the new pitch on radians.
	 */
	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

	/**
	 * Sets this entity's position on the environment.
	 * 
	 * @param position
	 *            the new position.
	 */
	public void setPosition(SimpleVector position) {
		this.position = position;
	}

	/**
	 * Sets this entity's radius.
	 * 
	 * @param radius
	 *            the radius.
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * Sets this entity's roll (rotation on plane yOz).
	 * 
	 * @param roll
	 *            the roll on radians
	 */
	public void setRoll(double roll) {
		this.roll = roll;
	}

	/**
	 * Sets this entity (specialization) type.
	 * 
	 * @param type
	 *            it's type.
	 */
	public void setType(EntityType type) {
		this.type = type;
	}

	/**
	 * Sets this entity's yaw (rotation on plane xOz).
	 * 
	 * @param yaw
	 *            the yaw on radians.
	 */
	public void setYaw(double yaw) {
		this.yaw = yaw;
	}

	public String toString() {
		SimpleVector position2 = this.getPosition();
		String string = position2 == null ? "" : position2.toString();
		return "Entity:" + this.getId() + " type:" + this.getType() + " radius:" + this.getRadius() + " position:" + string;
	}
}
