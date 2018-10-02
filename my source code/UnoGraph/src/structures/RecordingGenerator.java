package structures;

import org.apache.commons.math3.random.RandomGenerator;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class RecordingGenerator implements RandomGenerator {

	private RandomGenerator random;
	private BooleanArrayList booleans;
	private IntArrayList ints;
	private DoubleArrayList doubles;
	private FloatArrayList floats;
	private LongArrayList longs;
	private DoubleArrayList gaussians;
	/**
	 * if not recording, it is playing from history
	 */
	private boolean recording;
	private int ints_index;

	public RecordingGenerator(RandomGenerator random) {
		this.random = random;
		this.booleans = new BooleanArrayList();
		this.ints = new IntArrayList();
		this.doubles = new DoubleArrayList();
		this.floats = new FloatArrayList();
		this.longs = new LongArrayList();
		this.gaussians = new DoubleArrayList();
		this.recording = true;
		this.ints_index = 0;
	}

	public BooleanArrayList getBooleanHistory() {
		return booleans;
	}

	public IntArrayList getIntHistory() {
		return ints;
	}

	public DoubleArrayList getDoubleHistory() {
		return doubles;
	}

	public FloatArrayList getFloatHistory() {
		return floats;
	}

	public LongArrayList getLongHistory() {
		return longs;
	}

	public DoubleArrayList getGaussianHistory() {
		return gaussians;
	}

	@Override
	public void setSeed(int seed) {
		random.setSeed(seed);
	}

	@Override
	public void setSeed(int[] seed) {
		random.setSeed(seed);
	}

	@Override
	public void setSeed(long seed) {
		random.setSeed(seed);
	}

	@Override
	public void nextBytes(byte[] bytes) {
		System.err.println("nextBytes");
		random.nextBytes(bytes);
	}

	@Override
	public int nextInt() {
		System.err.println("nextInt()");
		int nextInt = random.nextInt();
		ints.add(nextInt);
		return nextInt;
	}

	@Override
	public int nextInt(int n) {
		// --protection
		if (ints_index >= ints.size() && recording == false) {
			ints_index = ints.size();
			recording = true;
		}
		// -----REC or PLAY
		if (recording) {
			int nextInt = random.nextInt(n);
			ints.add(nextInt);
			return nextInt;
		} else {
			int nextInt = ints.getInt(ints_index);
			ints_index++;
			return nextInt;
		}
	}

	public IntArrayList getInts() {
		return ints;
	}

	public void setInts(IntArrayList ints) {
		this.ints = ints;
		this.ints_index = 0;
	}

	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	@Override
	public long nextLong() {
		System.err.println("nextLong");
		long nextLong = random.nextLong();
		longs.add(nextLong);
		return nextLong;
	}

	@Override
	public boolean nextBoolean() {
		System.err.println("nextBoolean");
		boolean nextBoolean = random.nextBoolean();
		booleans.add(nextBoolean);
		return nextBoolean;
	}

	@Override
	public float nextFloat() {
		System.err.println("nextFloat");
		float nextFloat = random.nextFloat();
		floats.add(nextFloat);
		return nextFloat;
	}

	@Override
	public double nextDouble() {
		System.err.println("nextDouble");
		double nextDouble = random.nextDouble();
		doubles.add(nextDouble);
		return nextDouble;
	}

	@Override
	public double nextGaussian() {
		System.err.println("nextGaussian");
		double nextGaussian = random.nextGaussian();
		gaussians.add(nextGaussian);
		return nextGaussian;
	}

}
