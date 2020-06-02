package jcfgonc.blender;

import java.math.BigInteger;

public class BlenderMoConfig {
	public static final int POPULATION_SIZE = 4096;
	public static final double MUTATION_RATE = 1.0;
	public static final int BLOCK_SIZE = 256; // querykb tool specific
	public static final int PARALLEL_LIMIT = 28; // number of threads for the querykb tool
	public static final BigInteger SOLUTION_LIMIT = BigInteger.ONE; // one solution is enough to check for a frame match
	public static int QUERY_TIMEOUT_SECONDS = 1 * 1 * 60; // mutable int, used when launching a new query
}
