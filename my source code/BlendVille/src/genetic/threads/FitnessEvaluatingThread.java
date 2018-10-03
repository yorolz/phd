package genetic.threads;

import genetic.Chromosome;
import genetic.operators.FitnessEvaluator;

/**
 * @author CK
 */
public class FitnessEvaluatingThread<T> implements Runnable {
	private Chromosome<T>[] chromosomes;
	private int threadId;
	private int rangeL;
	private int rangeH;
	private FitnessEvaluator<T> fe;

	public FitnessEvaluatingThread(int thread_id, Chromosome<T>[] c, int range_l, int range_h, FitnessEvaluator<T> fe) {
		this.threadId = thread_id;
		this.chromosomes = c;
		this.rangeL = range_l;
		this.rangeH = range_h;
		this.fe = fe;
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
				chromosomes[i].updateFitness(fe);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
