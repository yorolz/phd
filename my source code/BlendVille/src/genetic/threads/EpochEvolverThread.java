package genetic.threads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.apache.commons.math3.random.RandomGenerator;

import genetic.Chromosome;
import genetic.operators.GeneCrossover;
import genetic.operators.GeneMutation;
import genetic.operators.GeneRepair;

/**
 * Thread class wich creates dummy (genes filled with null elements) chromosomes and assigns them to a specific part of the next population.
 *
 * @author ck
 *
 * @param <T>
 */
public class EpochEvolverThread<T> implements Callable<T> {

	private int rangeL;
	private int rangeH;
	private Chromosome<T> population[];
	private GeneCrossover<T> geneCrossover;
	private GeneMutation<T> geneMutator;
	private double mutationProbability;
	private RandomGenerator random;
	private GeneRepair<T> repairingOperator;
	private double crossoverProbability;
	private Chromosome<T>[] nextPopulation;
	private int tournamentSize;
	private double tournamentStrongestProb;

	public EpochEvolverThread(int rangeL, int rangeH, Chromosome<T>[] population, Chromosome<T>[] nextPopulation,
			GeneCrossover<T> geneCrossover, GeneMutation<T> geneMutator, double mutationProbability, RandomGenerator random,
			GeneRepair<T> repairingOperator, double crossoverProbability, int tournamentSize, double tournamentStrongestProb) {
		super();
		this.rangeL = rangeL;
		this.rangeH = rangeH;
		this.population = population;
		this.nextPopulation = nextPopulation;
		this.geneCrossover = geneCrossover;
		this.geneMutator = geneMutator;
		this.mutationProbability = mutationProbability;
		this.random = random;
		this.repairingOperator = repairingOperator;
		this.crossoverProbability = crossoverProbability;
		this.tournamentSize = tournamentSize;
		this.tournamentStrongestProb = tournamentStrongestProb;
	}

	private void mutateChromosome(Chromosome<T> c) {
		if (geneMutator != null) {
			if (random.nextDouble() < mutationProbability) {
				geneMutator.mutateGenes(c.genes, random);
			}
		}
	}

	private void repairChromosome(Chromosome<T> c) {
		if (repairingOperator != null) {
			repairingOperator.repairGenes(c.genes);
		}
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

				Chromosome<T> offspring0;
				Chromosome<T> offspring1;

				if (geneCrossover != null && crossoverProbability > 1e-9 && random.nextDouble() < crossoverProbability) {
					offspring0 = new Chromosome<T>(ancestor0, false);
					offspring1 = new Chromosome<T>(ancestor1, false);

					geneCrossover.crossover(ancestor0, ancestor1, offspring0, offspring1, random);
				} else {
					offspring0 = new Chromosome<T>(ancestor0, true);
					offspring1 = new Chromosome<T>(ancestor1, true);
				}

				mutateChromosome(offspring0);
				mutateChromosome(offspring1);

				repairChromosome(offspring0);
				repairChromosome(offspring1);

				nextPopulation[i + 0] = offspring0;
				if (i + 1 == rangeH)
					break;
				nextPopulation[i + 1] = offspring1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	private Chromosome<T> kTournamentSelection() {
		if (tournamentSize > 1) {
			ArrayList<Chromosome<T>> candidates = new ArrayList<>(randomSubSample(population, tournamentSize));
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

	private HashSet<Chromosome<T>> randomSubSample(Chromosome<T>[] p, int k) {
		HashSet<Chromosome<T>> elements = new HashSet<>(k * 2);
		while (elements.size() < k) {
			Chromosome<T> chromosome = p[random.nextInt(p.length)];
			elements.add(chromosome);
		}
		return elements;
	}
}
