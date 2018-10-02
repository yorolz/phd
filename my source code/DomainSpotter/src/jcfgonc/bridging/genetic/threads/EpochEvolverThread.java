package jcfgonc.bridging.genetic.threads;

import java.util.concurrent.Callable;

import org.apache.commons.math3.random.RandomGenerator;

import jcfgonc.bridging.genetic.Chromosome;
import jcfgonc.bridging.genetic.GeneticAlgorithmConfig;
import jcfgonc.bridging.genetic.operators.GeneCrossover;
import jcfgonc.bridging.genetic.operators.GeneMutation;
import jcfgonc.bridging.genetic.operators.GeneRepair;

/**
 * Thread class wich creates dummy (genes filled with null elements) chromosomes and assigns them to a specific part of the next population.
 * 
 * @author ck
 *
 * @param <T>
 */
public class EpochEvolverThread<T> implements Callable<T> {

	private final int rangeL;
	private final int rangeH;
	private final Chromosome<T> population[];
	private final GeneCrossover<T> geneCrossover;
	private final GeneMutation<T> geneMutator;
	private final RandomGenerator random;
	private final GeneRepair<T> repairingOperator;
	private Chromosome<T>[] nextPopulation;

	public EpochEvolverThread(int rangeL, int rangeH, Chromosome<T>[] population, Chromosome<T>[] nextPopulation, GeneCrossover<T> geneCrossover, GeneMutation<T> geneMutator,
			RandomGenerator random, GeneRepair<T> repairingOperator) {
		super();
		this.rangeL = rangeL;
		this.rangeH = rangeH;
		this.population = population;
		this.nextPopulation = nextPopulation;
		this.geneCrossover = geneCrossover;
		this.geneMutator = geneMutator;
		this.random = random;
		this.repairingOperator = repairingOperator;
	}

	private void mutateChromosome(Chromosome<T> c) {
		if (this.random.nextDouble() < GeneticAlgorithmConfig.GA_MUTATION_PROBABILITY) {
			this.geneMutator.mutateGenes(c.genes, random);
		}
	}

	private void repairChromosome(Chromosome<T> c) {
		if (repairingOperator != null) {
			this.repairingOperator.repairGenes(c.genes);
		}
	}

	/**
	 * Returns one parent selected randomly using the tournament method from the population.
	 * 
	 * @return
	 */
	private Chromosome<T> getRandomParentFromPopulationUsingTournament() {
		final int i0;
		int i1;
		Chromosome<T> challenger0;
		Chromosome<T> challenger1;
		Chromosome<T> strongest;
		Chromosome<T> weakest;
		// get the challenger's indexes
		// first challenger
		i0 = random.nextInt(population.length);
		challenger0 = this.population[i0];
		// second challenger (must be different from the other, duh)
		do {
			i1 = random.nextInt(population.length);
		} while (i1 == i0);
		challenger1 = this.population[i1];
		// get the strongest and weakest from both
		if (challenger0.getFitness() > challenger1.getFitness()) {
			strongest = challenger0;
			weakest = challenger1;
		} else {
			strongest = challenger1;
			weakest = challenger0;
		}
		// according to the tournament probability, select from both who will win.
		// the greater the tournament probability, the greater the possibility of selecting the weakest
		if (random.nextDouble() < GeneticAlgorithmConfig.GA_TOURNAMENT_WEAKEST_PROBABILITY) {
			// select the weakest
			return weakest;
		} else {
			// select the strongest
			return strongest;
		}
	}

	private void doCrossover(Chromosome<T> ancestor0, Chromosome<T> ancestor1, Chromosome<T> offspring0, Chromosome<T> offspring1) {
		if (this.random.nextDouble() < GeneticAlgorithmConfig.GA_CROSSOVER_PROBABILITY) {
			this.geneCrossover.crossover(ancestor0, ancestor1, offspring0, offspring1, random);
		} else {
			offspring0.copyGenes(ancestor0);
			offspring1.copyGenes(ancestor1);
		}
	}

	@Override
	public T call() {
		try {
			// reproduce until we have the desired amount of chromosomes
			for (int i = rangeL; i < rangeH; i += 2) {
				/** [Selection] Select two parent chromosomes from a population according to their fitness using tournament selection first ancestor **/
				Chromosome<T> ancestor0 = getRandomParentFromPopulationUsingTournament();
				Chromosome<T> ancestor1;
				// second ancestor
				do {
					// logically, this one must be different from the other
					ancestor1 = getRandomParentFromPopulationUsingTournament();
				} while (ancestor1.equals(ancestor0));

				/**
				 * [Crossover] With a crossover probability cross over the parents to form a new offspring (children). If no crossover was performed, offspring is an exact copy of
				 * parents.
				 **/

				Chromosome<T> offspring0 = new Chromosome<T>(ancestor0);// nextPopulation[i];
				Chromosome<T> offspring1 = new Chromosome<T>(ancestor1);// nextPopulation[i + 1];

				// [Crossover] mix the salad
				doCrossover(ancestor0, ancestor1, offspring0, offspring1);

				/** [Mutation] With a mutation probability mutate new offspring at each locus (position in chromosome). **/
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
}
