package ck.navigation;

import com.threed.jpct.SimpleVector;

public class SimpleCustomVector extends SimpleVector implements Comparable<SimpleCustomVector> {

	private static final long serialVersionUID = -434758101150937957L;

	public SimpleCustomVector(double x, double y, double z) {
		super((float) x, (float) y, (float) z);
	}

	public SimpleCustomVector(float x, float y, float z) {
		super(x, y, z);
	}

	public SimpleCustomVector(SimpleCustomVector z) {
		super((SimpleVector) z);
	}

	@Override
	public int compareTo(SimpleCustomVector other) {
		if (this.x < other.x)
			return -1;
		else if (this.x > other.x) {
			return +1;
		} else {
			if (this.y < other.y)
				return -1;
			else if (this.y > other.y)
				return +1;
			else {
				if (this.z < other.z)
					return -1;
				else if (this.z > other.z)
					return +1;
				else {
					return 0;
				}
			}
		}
	}

	public float distanceAbsolute(SimpleVector to) {
		double x_dif = this.x - to.x;
		double y_dif = this.y - to.y;
		double z_dif = this.z - to.z;
		return (float) (Math.abs(x_dif) + Math.abs(y_dif) + Math.abs(z_dif));
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (getClass() != other.getClass())
			return false;
		final float tolerance = 0.00390625f;
		SimpleCustomVector s = (SimpleCustomVector) other;
		return fequal(s.x, this.x, tolerance) && fequal(s.y, this.y, tolerance) && fequal(s.z, this.z, tolerance);
	}

	private boolean fequal(float V1, float V2, float tolerance) {
		return ((V1 - tolerance) < V2) && ((V1 + tolerance) > V2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		int temp;
		temp = Float.hashCode(this.x * 100.f);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Float.hashCode(this.y * 100.f);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Float.hashCode(this.z * 100.f);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;

	}
}
