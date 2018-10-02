package jcfgonc.genetic.threads;

import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;

/**
 * @author CK
 */
public final class FitnessEvaluatingThread<T> implements Runnable {
	private Chromosome<T>[] chromosomes;
	private int threadId;
	private int rangeL;
	private int rangeH;
	private GeneticOperations<T> go;

	public FitnessEvaluatingThread(int thread_id, Chromosome<T>[] c, int range_l, int range_h, GeneticOperations<T> geneOperator) {
		this.threadId = thread_id;
		this.chromosomes = c;
		this.rangeL = range_l;
		this.rangeH = range_h;
		this.go = geneOperator;
	}

	public Chromosome<T>[] getChromosomes() {
		return chromosomes;
	}

	public int getRangeH() {
		return rangeH;
	}

	public int getRangeL() {
		return rangeL;
	}

	public int getThreadId() {
		return threadId;
	}

	@Override
	public void run() {
		try {
			for (int i = rangeL; i <= rangeH; i++) {
				chromosomes[i].updateFitness(go);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
