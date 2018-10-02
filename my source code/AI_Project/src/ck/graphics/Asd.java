package ck.graphics;

public class Asd {

	public static double calculateRotationStep(double rotateStep, double yaw_current, double yaw_target) {
		// how much do we need to rotate?
		double yawDif = yaw_target - yaw_current;
		boolean reverse = false;
		if (yawDif < 0)
			reverse = !reverse; // should be false
		double absYawDif = Math.abs(yawDif);
		if (absYawDif > 180)
			reverse = !reverse; // reverse the direction because |angle| is > 180º
		if (absYawDif < rotateStep)
			rotateStep = absYawDif;
		if (reverse)
			rotateStep = -rotateStep;

		return rotateStep;
	}

	public static void main(String[] args) {
		System.out.println(calculateRotationStep(1, 270, 0));
		System.out.println(calculateRotationStep(1, 270, 90));
		System.out.println(calculateRotationStep(1, 270, 180));
		System.out.println(calculateRotationStep(1, 270, 270));
		System.out.println(calculateRotationStep(1, 270, 360));
	}

}
