package jcfgonc.patternminer;

public class PatternMinerConfig {
//	public static final String FILE_PATH = "sapper_world_facts_v2.csv";
	public static final String FILE_PATH = "../ConceptNet5/kb/conceptnet5v5.csv";
	public static final int POPULATION_SIZE = 256;
	public static final double MUTATION_RATE = 1.0;
	public static final int BLOCK_SIZE = 256;
	public static final int PARALLEL_LIMIT = 32; // number of threads for the querykb tool
	public static int QUERY_TIMEOUT_MS = 1 * 1 * 60 * 1000; // mutable int, used when launching a new query
}
