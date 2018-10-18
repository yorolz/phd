package jcfgonc.genetic.threads;

import java.util.concurrent.Callable;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;

public final class PopulationInitializerThread<T> implements Callable<T> {
	private int rangeL;
	private int rangeH;
	private Chromosome<T>[] population;
	private RandomGenerator random;
	private GeneticOperations<T> geneOperator;

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
	public PopulationInitializerThread(Chromosome<T>[] population, int rangeL, int rangeH, RandomGenerator random, GeneticOperations<T> geneOperator) {
		this.rangeL = rangeL;
		this.rangeH = rangeH;
		this.population = population;
		this.random = random;
		this.geneOperator = geneOperator;
	}

	@Override
	public T call() {
		try {
			for (int pos = rangeL; pos < rangeH; pos++) {
				Chromosome<T> c = new Chromosome<T>();
				c.setGenes(geneOperator.initializeGenes(random));
				// geneOperator.initializeGenes(c, random); //OLD, DEPRECATED
				if (geneOperator.useGeneRepair()) {
					geneOperator.repairGenes(c, random);
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
