//  MetricsUtil.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.qualityIndicator.util;

import org.apache.commons.math3.util.FastMath;

/**
 * This class provides some utilities to compute quality indicators. With some alterations by jcfgonc@gmail.com.
 **/
public class MetricsUtil {

	/**
	 * Gets the maximun values for each objectives in a given pareto front
	 *
	 * @param front
	 *            The pareto front
	 * @return double [] An array of noOjectives values whit the maximum values for each objective
	 **/
	public static double[] getMaximumValues(double[][] front) {
		int noObjectives = front[0].length;
		double[] maximumValue = new double[noObjectives];
		for (int i = 0; i < noObjectives; i++)
			maximumValue[i] = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < front.length; i++) {
			for (int j = 0; j < front[i].length; j++) {
				if (front[i][j] > maximumValue[j])
					maximumValue[j] = front[i][j];
			}
		}

		return maximumValue;
	} // getMaximumValues

	/**
	 * Gets the minimun values for each objectives in a given pareto front
	 *
	 * @param front
	 *            The pareto front
	 * @return double [] An array of noOjectives values whit the minimum values for each objective
	 **/
	public static double[] getMinimumValues(double[][] front) {
		int noObjectives = front[0].length;
		double[] minimumValue = new double[noObjectives];
		for (int i = 0; i < noObjectives; i++)
			minimumValue[i] = Double.MAX_VALUE;

		for (int i = 0; i < front.length; i++) {
			for (int j = 0; j < front[i].length; j++) {
				if (front[i][j] < minimumValue[j])
					minimumValue[j] = front[i][j];
			}
		}
		return minimumValue;
	} // getMinimumValues

	public static double[] distanceToReference(double[][] points, double[] reference) {
		int size = points.length;
		double[] distances = new double[size];

		for (int i = 0; i < size; i++) {
			double distance = MetricsUtil.distance(reference, points[i]);
			distances[i] = distance;
		}
		return distances;
	}

	public static double[] distanceToReferenceSpecial(double[][] points, double[] reference) {
		int size = points.length;
		double[] distances = new double[size];

		for (int i = 0; i < size; i++) {
			double distance = distanceHarmonic(reference, points[i]);
			distances[i] = distance;
		}
		return distances;
	}

	/**
	 * This method returns the distance (taken the euclidean distance) between two points given as <code>double []</code>
	 *
	 * @param a
	 *            A point
	 * @param b
	 *            A point
	 * @return The euclidean distance between the points
	 **/
	public static double distance(double[] a, double[] b) {
		return FastMath.sqrt(distanceSquared(a, b));
	} // distance

	public static double distanceSquared(double[] a, double[] b) {
		double distance = 0.0;

		for (int i = 0; i < a.length; i++) {
			double dif = a[i] - b[i];
			distance += dif * dif;
		}
		return distance;
	} // distanceSquared

	public static double distanceAbsolute(double[] a, double[] b) {
		double distance = 0.0;

		for (int i = 0; i < a.length; i++) {
			double dif = a[i] - b[i];
			distance += FastMath.abs(dif);
		}
		return distance;
	}

	public static double distanceHarmonic(double[] a, double[] b) {
		double distance = 0.0;

		for (int i = 0; i < a.length; i++) {
			double dif = a[i] - b[i];
			distance += 1.0 / FastMath.abs(dif);
		}
		distance = (double) a.length / distance;
		return distance;
	}

	/**
	 * Gets the distance between a point and the nearest one in a given front (the front is given as <code>double [][]</code>)
	 *
	 * @param point
	 *            The point
	 * @param front
	 *            The front that contains the other points to calculate the distances
	 * @return The minimun distance between the point and the front
	 **/
	public static double distanceToClosedPoint(double[] point, double[][] front) {
		double minDistance = distance(point, front[0]);

		for (int i = 1; i < front.length; i++) {
			double aux = distance(point, front[i]);
			if (aux < minDistance) {
				minDistance = aux;
			}
		}

		return minDistance;
	} // distanceToClosedPoint

	/**
	 * Gets the distance between a point and the nearest one in a given front, and this distance is greater than 0.0
	 *
	 * @param point
	 *            The point
	 * @param front
	 *            The front that contains the other points to calculate the distances
	 * @return The minimun distances greater than zero between the point and the front
	 */
	public static double distanceToNearestPoint(double[] point, double[][] front) {
		double minDistance = Double.MAX_VALUE;

		for (int i = 0; i < front.length; i++) {
			double aux = distance(point, front[i]);
			if ((aux < minDistance) && (aux > 0.0)) {
				minDistance = aux;
			}
		}

		return minDistance;
	} // distanceToNearestPoint

	/**
	 * This method receives a pareto front and two points, one with maximun values and the other with minimun values allowed, and returns a the
	 * normalized pareto front.
	 *
	 * @param front
	 *            A pareto front.
	 * @param maximumValue
	 *            The maximun values allowed
	 * @param minimumValue
	 *            The mininum values allowed
	 * @return the normalized pareto front
	 **/
	public static double[][] getNormalizedFront(double[][] front, double[] maximumValue, double[] minimumValue) {

		double[][] normalizedFront = new double[front.length][];

		for (int i = 0; i < front.length; i++) {
			normalizedFront[i] = new double[front[i].length];
			for (int j = 0; j < front[i].length; j++) {
				normalizedFront[i][j] = (front[i][j] - minimumValue[j]) / (maximumValue[j] - minimumValue[j]);
			}
		}
		return normalizedFront;
	} // getNormalizedFront

	/**
	 * This method receives a normalized pareto front and return the inverted one. This operation needed for minimization problems
	 *
	 * @param front
	 *            The pareto front to inverse
	 * @return The inverted pareto front
	 **/
	public static double[][] invertedFront(double[][] front) {
		double[][] invertedFront = new double[front.length][];

		for (int i = 0; i < front.length; i++) {
			invertedFront[i] = new double[front[i].length];
			for (int j = 0; j < front[i].length; j++) {
				if (front[i][j] <= 1.0 && front[i][j] >= 0.0) {
					invertedFront[i][j] = 1.0 - front[i][j];
				} else if (front[i][j] > 1.0) {
					invertedFront[i][j] = 0.0;
				} else if (front[i][j] < 0.0) {
					invertedFront[i][j] = 1.0;
				}
			}
		}
		return invertedFront;
	} // invertedFront

} // MetricsUtil
