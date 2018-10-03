package genetic.threads;

import java.util.concurrent.Callable;

import org.apache.commons.math3.random.RandomGenerator;

import genetic.Chromosome;
import genetic.operators.GeneInitializer;
import genetic.operators.GeneRepair;

public class PopulationInitializerThread<T> implements Callable<T> {
	private int rangeL;
	private int rangeH;
	private Chromosome<T>[] population;
	private RandomGenerator random;
	private int numberOfGenes;
	private GeneInitializer<T> geneInitializer;
	private GeneRepair<T> repairingOperator;
	private Class<T> geneclass;

	/**
	 * initializes random chromosomes in the range [rangeL, rangeH[ and assigns them to the given population
	 *
	 * @param population
	 * @param rangeL
	 * @param rangeH
	 * @param numberOfGenes
	 * @param random
	 * @param randomLock
	 * @param repairingOperator
	 * @param geneInitializer
	 */
	public PopulationInitializerThread(Chromosome<T>[] population, int rangeL, int rangeH, int numberOfGenes, RandomGenerator random,
			GeneRepair<T> repairingOperator, GeneInitializer<T> geneInitializer, Class<T> geneclass) {
		this.geneclass = geneclass;
		this.rangeL = rangeL;
		this.rangeH = rangeH;
		this.population = population;
		this.numberOfGenes = numberOfGenes;
		this.random = random;
		this.repairingOperator = repairingOperator;
		this.geneInitializer = geneInitializer;
	}

	@Override
	public T call() {
		try {
			for (int pos = rangeL; pos < rangeH; pos++) {
				Chromosome<T> c = new Chromosome<T>(geneclass, numberOfGenes);
				geneInitializer.initializeGenes(c.genes, random);
				if (repairingOperator != null) {
					repairingOperator.repairGenes(c.genes);
				}
				population[pos] = c;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
}
