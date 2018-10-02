package ck.graphics;

public class FastTrig {

	private static float[] cosTable = null;
	private static double invStep;
	private final static double PI2 = Math.PI * 2;
	private static float[] sinTable = null;
	private static int size = 0;
	private static double step;

	static {
		size = 1024;
		sinTable = new float[size];
		cosTable = new float[size];
		step = 2d * Math.PI / size;
		invStep = 1.0f / step;
		for (int i = 0; i < size; ++i) {
			sinTable[i] = (float) Math.sin(step * i);
			cosTable[i] = (float) Math.cos(step * i);
		}
	}

	final private static double cropAngle(double ang) {
		while (ang < 0)
			ang += PI2;
		while (ang > PI2)
			ang -= PI2;
		return ang;
	}

	final public static double cos(double ang) {
		double t = cropAngle(ang);
		int indexA = (int) (t * invStep);
		int indexB = indexA + 1;
		if (indexB >= size)
			return cosTable[size - 1];
		double a = cosTable[indexA];
		return a + (cosTable[indexB] - a) * (t - (indexA * step)) * invStep;
	}

	final public static double sin(double ang) {
		double t = cropAngle(ang);
		int indexA = (int) (t * invStep);
		int indexB = indexA + 1;
		if (indexB >= size)
			return sinTable[size - 1];
		double a = sinTable[indexA];
		return a + (sinTable[indexB] - a) * (t - (indexA * step)) * invStep;
	}

}
