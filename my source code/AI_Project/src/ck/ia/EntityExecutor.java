package ck.ia;

import java.util.ArrayList;

import ck.graphics.Ticker;

public class EntityExecutor extends Thread {

	private ArrayList<Entity> entities;
	private int index1;
	private int index0;
	private Ticker ticker;
	private final int CYCLE_MAX_MS = 20;
	private final double TIME_DELTA = (double) CYCLE_MAX_MS / 1000.0;

	public EntityExecutor(ArrayList<Entity> entities, int index0, int index1) {
		super("EntityExecutor:" + index0 + ":" + index1);
		this.entities = entities;
		this.index0 = index0;
		this.index1 = index1;
		this.ticker = new Ticker();
	}

	@Override
	public void run() {
		ticker.getTimeDeltaLastCall();
		while (true) {
			// give cycles to corresponding entities
			for (int i = index0; i < index1; i++) {
				Entity entity = entities.get(i);
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
}
