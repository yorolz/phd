package jcfgonc.bridging.genetic.operators;

public interface GeneRepair<T> {

	/**
	 * Given an array of type T genes, reparing algorithm to each gene (element of the array) or all of them.
	 * 
	 * @param genes
	 */
	public void repairGenes(T[] genes);
}
