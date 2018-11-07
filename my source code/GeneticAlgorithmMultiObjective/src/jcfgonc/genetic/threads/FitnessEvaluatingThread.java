package jcfgonc.genetic.threads;

import java.util.concurrent.ConcurrentLinkedQueue;

import jcfgonc.genetic.Chromosome;
import jcfgonc.genetic.operators.GeneticOperations;

/**
 * @author jcfgonc@gmail.com
 */
public final class FitnessEvaluatingThread<T> implements Runnable {
	private Chromosome<T>[] chromosomeArray;
	private ConcurrentLinkedQueue<Chromosome<T>> chromosomeQueue;
	private int threadId;
	private int rangeL;
	private int rangeH;
	private GeneticOperations<T> geneOperator;

	public FitnessEvaluatingThread(int thread_id, ConcurrentLinkedQueue<Chromosome<T>> chromosomeQueue, int range_l, int range_h, GeneticOperations<T> geneOperator) {
		this.threadId = thread_id;
		this.chromosomeArray = null;
		this.chromosomeQueue = chromosomeQueue;
		this.rangeL = range_l;
		this.rangeH = range_h;
		this.geneOperator = geneOperator;
	}

	public Chromosome<T>[] getChromosomes() {
		return chromosomeArray;
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
			Chromosome<T> c;
			while ((c = chromosomeQueue.poll()) != null) {
				c.updateFitness(geneOperator);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
