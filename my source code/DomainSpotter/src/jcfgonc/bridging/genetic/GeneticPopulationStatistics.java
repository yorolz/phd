package jcfgonc.bridging.genetic;

public class GeneticPopulationStatistics<T> {

	public double populationMedian(Chromosome<T>[] list) {
		double median;
		final int size = list.length;
		if (size % 2 == 0)
			median = ((double) list[size / 2].getFitness() + (double) list[size / 2 - 1].getFitness()) / 2;
		else
			median = (double) list[size / 2].getFitness();
		return median;
	}

	public double populationMax(Chromosome<T>[] list) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < list.length; i++) {
			if (list[i].getFitness() > max)
				max = list[i].getFitness();
		}
		return max;
	}

	public double populationMean(Chromosome<T>[] list) {
		double sum = populationSum(list);
		return sum / list.length;
	}

	public double populationMin(Chromosome<T>[] list) {
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < list.length; i++) {
			if (list[i].getFitness() < min)
				min = list[i].getFitness();
		}
		return min;
	}

	public double populationStandardDeviation(Chromosome<T>[] list, double mean) {
		double sum = 0.0;
		for (int i = 0; i < list.length; i++) {
			sum += (list[i].getFitness() - mean) * (list[i].getFitness() - mean);
		}
		return Math.sqrt(sum / (list.length - 1));
	}

	/**
	 * 
	 * @param list
	 * @return { min, max, mean, std }
	 */
	public double[] populationStats(Chromosome<T>[] list) {
		double min = populationMin(list);
		double max = populationMax(list);
		double mean = populationMean(list);
		double std = populationStandardDeviation(list, mean);
		double[] stat = { min, max, mean, std };
		return stat;
	}

	public double populationSum(Chromosome<T>[] list) {
		double sum = 0.0;
		for (int i = 0; i < list.length; i++) {
			sum += list[i].getFitness();
		}
		return sum;
	}

}
