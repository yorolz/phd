
/**
 * Class to manage elapsed time
 * 
 * @author CK
 * 
 */
public class Ticker {

	private long reference;
	private double time_lastcall;

	/**
	 * Creates a new ticker, sets its timer to zero and starts counting.
	 */
	public Ticker() {
		this.resetTicker();
		getTimeDeltaLastCall();
	}

	/**
	 * Returns the elapsed time (in seconds) since the creation (or reset) of this ticker.
	 * 
	 * @return
	 */
	public double getElapsedTime() {
		double dif = getNanoTime() - reference;
		double el = dif * 1e-9;
		return el;
	}

	private long getNanoTime() {
		return System.nanoTime();
	}

	/**
	 * Returns the elapsed time (in seconds) this last call.
	 * 
	 * @return
	 */
	public double getTimeDeltaLastCall() {
		double t1 = getElapsedTime();
		double tdif = t1 - time_lastcall;
		time_lastcall = t1;
		return tdif;
	}

	/**
	 * Sets this ticker timer to zero and starts counting again.
	 */
	public void resetTicker() {
		reference = getNanoTime();
	}

	public void showTimeDeltaLastCall() {
		System.out.printf("d(t)=%f\n", getTimeDeltaLastCall());
	}

}
