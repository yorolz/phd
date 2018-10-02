package matcher;

import structures.Ticker;

public class StaticTimer {

	private static Ticker ticker;
	private static double timeout;

	public static void resetTicker() {
		ticker.resetTicker();
	}

	public static void initializeTicker() {
		ticker = new Ticker();
	}

	public static double getElapsedTime() {
		return ticker.getElapsedTime();
	}

	public static void setTimeout(double timeout) {
		StaticTimer.timeout = timeout;
	}

	public static boolean timedOut() {
		double t = getElapsedTime();
		if (t >= timeout) {
			return true;
		}
		return false;
	}

}
