package jcfgonc.bridging;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

public class BridgingGene {
	public String bridge;
	public int bridgeDegree;
	public Set<String> intersection;
	public int deepness;
	public Set<String> set0;
	public Set<String> set1;

	public BridgingGene() {
	}

	public BridgingGene(BridgingGene other) {
		this.bridge = other.bridge;
		this.bridgeDegree = other.bridgeDegree;
		this.intersection = new HashSet<>(other.intersection);
		this.set0 = new HashSet<>(other.set0);
		this.set1 = new HashSet<>(other.set1);
		this.deepness = other.deepness;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BridgingGene other = (BridgingGene) obj;
		if (bridge != other.bridge)
			return false;
		return true;
	}

	public double getFitness() {

		int size0 = set0.size();
		int size1 = set1.size();
		double size = (double) size0 + size1;

		// we don't want terminal nodes as bridge
		if (bridgeDegree < 2)
			return -Double.MAX_VALUE;
		//
		double minSize = FastMath.min(size0, size1);
		double maxSize = FastMath.max(size0, size1);
		double isratio = minSize / maxSize; // [0...1] higher means equal size
		double deep = deepness + 0; // [0...graph radius-1]
		double bdegree = Math.pow(2.0, 1 * -bridgeDegree + 1); // [0...1] higher means lowest degree
		double issize = size / 1e9;
		//
		double fitness = 0.00 * isratio + 1.0 * deep + 0 * bdegree - 10.0 * issize;
		return fitness;
	}

	@Override
	public int hashCode() {
		return bridge.hashCode();
	}

	@Override
	public String toString() {
		return "deg=" + bridgeDegree + " bridge=" + bridge + " deep=" + deepness + " #s0=" + set0.size() + " #s1=" + set1.size() + " #intersect=" + intersection.size();
	}
}
