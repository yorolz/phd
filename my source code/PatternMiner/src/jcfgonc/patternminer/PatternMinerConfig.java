package jcfgonc.patternminer;

public class PatternMinerConfig {
//	public static final String FILE_PATH = "sapper_world_facts_v2.csv";
	public static final String FILE_PATH = "../ConceptNet5/kb/conceptnet5v5.csv";
	public static final int POPULATION_SIZE = 1024;
	public static final double MUTATION_RATE = 1.0;
	public static final int BLOCK_SIZE = 256; // querykb tool specific
	public static final int PARALLEL_LIMIT = 28; // number of threads for the querykb tool
	public static final boolean FORCE_CYCLES = true; // if true tries to add cycles whenever possible
	public static int QUERY_TIMEOUT_SECONDS = 1 * 5 * 60; // mutable int, used when launching a new query
}
