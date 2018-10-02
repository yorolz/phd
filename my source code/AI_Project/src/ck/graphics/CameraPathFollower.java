package ck.graphics;

import java.io.BufferedReader;
import java.io.FileReader;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

/*
 * (C) C3R14L.K1L4 / João Carlos Ferreira Gonçalves
 * further contact: jcgonc@student.dei.uc.pt
 * started on 09-04-2007
 * checked on 14-04-2007: OK, clean
 */

public class CameraPathFollower {
	Camera cam;
	SimpleVectorSplineInterpolator pathposition;
	SimpleVectorSplineInterpolator pathdirection;
	SimpleVectorSplineInterpolator pathup;

	public CameraPathFollower(Camera cam) {
		pathposition = new SimpleVectorSplineInterpolator();
		pathdirection = new SimpleVectorSplineInterpolator();
		pathup = new SimpleVectorSplineInterpolator();

		this.cam = cam;
	}

	public void addPathPoint(SimpleVector position0, SimpleVector direction0, SimpleVector up0) {
		// add data
		pathposition.addPathPoint(position0);
		pathdirection.addPathPoint(direction0);
		pathup.addPathPoint(up0);
	}

	public void interpolateMotion(int startingpoint, double n) {
		pathposition.interpolateVector(startingpoint, n);
		pathdirection.interpolateVector(startingpoint, n);
		pathup.interpolateVector(startingpoint, n);

		SimpleVector position = pathposition.getCurrentVector();
		SimpleVector direction = pathdirection.getCurrentVector();
		SimpleVector up = pathup.getCurrentVector();
		this.cam.setPosition(position);
		this.cam.setOrientation(direction, up);
}

	public void interpolateMotion(double n) {
		int pos = (int) Math.floor(n);
		double coef = n - pos;
		interpolateMotion(pos, coef);
	}

	private SimpleVector parseVector(String text) {
		String[] scalar = text.split(",");
		float x = Float.parseFloat(scalar[0]);
		float y = Float.parseFloat(scalar[1]);
		float z = Float.parseFloat(scalar[2]);
		return new SimpleVector(x, y, z);
	}

	public void loadPath(String filename) {
		int lineType = 0;
		SimpleVector pos = null, dir = null, up;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			while (br.ready()) {
				String line = br.readLine();
				switch (lineType) {
				case 0:
					pos = parseVector(line);
					break;
				case 1:
					dir = parseVector(line);
					break;
				case 2:
					up = parseVector(line);
					addPathPoint(pos, dir, up);
					lineType = -1;
					break;
				}
				lineType++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
};
