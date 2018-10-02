package ck.graphics;

import java.util.ArrayList;

import com.threed.jpct.SimpleVector;

/*
 * (C) C3R14L.K1L4 / João Carlos Ferreira Gonçalves
 * further contact: jcfgonc@gmail.com
 * started on 09-04-2007
 * checked on 14-04-2007: OK, clean
 * converted from C++ to Java on 06-11-2014
 */

public class SimpleVectorSplineInterpolator {
	private ArrayList<SimpleVector> path;
	private SimpleVector position;

	public SimpleVectorSplineInterpolator() {
		this.path = new ArrayList<SimpleVector>();
		this.position = new SimpleVector();
	}

	public void addPathPoint(SimpleVector newposition) {
		// add data
		path.add(newposition);
	}

	private double catmullRomSpline(double p1, double p2, double p3, double p4, double t) {
		return (0.5 * ((-p1 + 3 * p2 - 3 * p3 + p4) * t * t * t + (2 * p1 - 5 * p2 + 4 * p3 - p4) * t * t + (-p1 + p3) * t + 2 * p2));
	}

	public SimpleVector getCurrentVector() {
		return position;
	}

	public int getNumberOfPathPoints() {
		return path.size();
	}

	public void interpolateVector(double n) {
		int pos = (int) Math.floor(n);
		double coef = n - pos;
		interpolateVector(pos, coef);
	}

	public void interpolateVector(int startingpoint, double n) {
		// control points, s1 & s2 are the interpolated ones
		int s0, s1, s2, s3;
		// cycle path loop
		int pathsize = getNumberOfPathPoints();
		if (startingpoint >= pathsize) {
			startingpoint = startingpoint % pathsize;
		}
		// check for loop positions, each one has it's own control points
		if (startingpoint == 0) {
			s0 = pathsize - 1;
			s1 = 0;
			s2 = 1;
			s3 = 2;
		} else if (startingpoint == pathsize - 2) {
			s0 = pathsize - 3;
			s1 = pathsize - 2;
			s2 = pathsize - 1;
			s3 = 0;
		} else if (startingpoint == pathsize - 1) {
			s0 = pathsize - 2;
			s1 = pathsize - 1;
			s2 = 0;
			s3 = 1;
		} else {
			s0 = startingpoint - 1;
			s1 = startingpoint;
			s2 = startingpoint + 1;
			s3 = startingpoint + 2;
		}
		
		SimpleVector pos0 = path.get(s0);
		SimpleVector pos1 = path.get(s1);
		SimpleVector pos2 = path.get(s2);
		SimpleVector pos3 = path.get(s3);

		double x1 = pos0.x;
		double x2 = pos1.x;
		double x3 = pos2.x;
		double x4 = pos3.x;

		double y1 = pos0.y;
		double y2 = pos1.y;
		double y3 = pos2.y;
		double y4 = pos3.y;

		double z1 = pos0.z;
		double z2 = pos1.z;
		double z3 = pos2.z;
		double z4 = pos3.z;

		position.x = (float) catmullRomSpline(x1, x2, x3, x4, n);
		position.y = (float) catmullRomSpline(y1, y2, y3, y4, n);
		position.z = (float) catmullRomSpline(z1, z2, z3, z4, n);
	};

};
