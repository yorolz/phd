package jcfgonc.genetic.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;

/**
 * Thread class wich creates dummy (genes filled with null elements) chromosomes and assigns them to a specific part of the next population.
 *
 * @author ck
 *
 * @param <T>
 */
public final class EpochEvolverThread<T> implements Callable<T> {

	private int rangeL;
	private int rangeH;
	private Chromosome<T> population[];
	private double mutationProbability;
	private RandomGenerator random;
	private double crossoverProbability;
	private Chromosome<T>[] nextPopulation;
	private int tournamentSize;
	private double tournamentStrongestProb;
	private GeneticOperations<T> geneOperator;

	public EpochEvolverThread(int rangeL, int rangeH, Chromosome<T>[] population, Chromosome<T>[] nextPopulation, double mutationProbability, RandomGenerator random,
			double crossoverProbability, int tournamentSize, double tournamentStrongestProb, GeneticOperations<T> geneOperator) {
		super();
		this.rangeL = rangeL;
		this.rangeH = rangeH;
		this.population = population;
		this.nextPopulation = nextPopulation;
		this.mutationProbability = mutationProbability;
		this.random = random;
		this.crossoverProbability = crossoverProbability;
		this.tournamentSize = tournamentSize;
		this.tournamentStrongestProb = tournamentStrongestProb;
		this.geneOperator = geneOperator;
	}

	/**
	 * Returns one parent selected randomly using the tournament method from the population.
	 *
	 * @return
	 */
	private Chromosome<T> binaryTournamentSelection() {
		Chromosome<T> challenger0;
		Chromosome<T> challenger1;
		Chromosome<T> strongest;
		Chromosome<T> weakest;
		// get the challenger's indexes
		int i0 = random.nextInt(population.length);
		int i1 = random.nextInt(population.length);
		challenger0 = population[i0];
		challenger1 = population[i1];
		// get the strongest and weakest from both
		if (challenger0.compareTo(challenger1) > 0) {
			strongest = challenger0;
			weakest = challenger1;
		} else {
			strongest = challenger1;
			weakest = challenger0;
		}
		// according to the tournament probability, select from both who will win.
		// the greater the tournament probability, the greater the possibility of selecting the weakest
		if (random.nextDouble() < tournamentStrongestProb) {
			// select the strongest
			return strongest;
		} else {
			// select the weakest
			return weakest;
		}
	}

	@Override
	public T call() {
		try {
			// reproduce until we have the desired amount of chromosomes
			for (int i = rangeL; i < rangeH; i += 2) {
				Chromosome<T> ancestor0;
				Chromosome<T> ancestor1;
				if (tournamentSize == 2) {
					ancestor0 = binaryTournamentSelection();
					ancestor1 = binaryTournamentSelection();
				} else {
					ancestor0 = kTournamentSelection();
					ancestor1 = kTournamentSelection();
				}

				boolean useMutation = geneOperator.useMutation();
				boolean mutateOffspring0 = random.nextDouble() < mutationProbability && useMutation;
				boolean mutateOffspring1 = random.nextDouble() < mutationProbability && useMutation;

				T offGenes0;
				T offGenes1;

				if (geneOperator.useCrossover() && crossoverProbability > 1e-9 && random.nextDouble() < crossoverProbability) {
					// do crossover
					offGenes0 = geneOperator.createGeneCopy(ancestor0.getGenes(), true);
					offGenes1 = geneOperator.createGeneCopy(ancestor1.getGenes(), true);

					geneOperator.crossover(ancestor0.getGenes(), ancestor1.getGenes(), offGenes0, offGenes1, random);
					
				} else {
					// do not crossover, copy only from ancestor0/1 to offspring0/1
					offGenes0 = geneOperator.createGeneCopy(ancestor0.getGenes(), mutateOffspring0);
					offGenes1 = geneOperator.createGeneCopy(ancestor1.getGenes(), mutateOffspring1);
				}

				if (mutateOffspring0) {
					offGenes0 = geneOperator.mutateGenes(offGenes0, random);
				}
				if (mutateOffspring1) {
					offGenes1 = geneOperator.mutateGenes(offGenes1, random);
				}

				if (geneOperator.useGeneRepair()) {
					offGenes0 = geneOperator.repairGenes(offGenes0, random);
					offGenes1 = geneOperator.repairGenes(offGenes1, random);
				}

				nextPopulation[i + 0] = new Chromosome<>(offGenes0);
				if (i + 1 == rangeH)
					break;
				nextPopulation[i + 1] = new Chromosome<>(offGenes1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private Chromosome<T> kTournamentSelection() {
		if (tournamentSize > 1) {
			HashSet<Chromosome<T>> subset = createChromosomeRandomSubSet(population, tournamentSize);
			ArrayList<Chromosome<T>> candidates = new ArrayList<>(subset);
			candidates.sort(null);
			// choose the nth best individual with probability p*((1-p)^n)
			for (int i = 0; i < tournamentSize; i++) {
				// final double selectionProbability = p * (Math.pow(1 - p, i));
				final double selectionProbability = tournamentStrongestProb;
				if (random.nextDouble() <= selectionProbability) {
					Chromosome<T> best = candidates.get(tournamentSize - i - 1);
					return best;
				}
			}
			Chromosome<T> best = candidates.get(0);
			return best;
		} else {
			return population[random.nextInt(population.length)];
		}
	}

	private HashSet<Chromosome<T>> createChromosomeRandomSubSet(Chromosome<T>[] p, int k) {
		HashSet<Chromosome<T>> elements = new HashSet<>(k * 2);
		while (elements.size() < k) {
			Chromosome<T> chromosome = p[random.nextInt(p.length)];
			elements.add(chromosome);
		}
		return elements;
	}
}
