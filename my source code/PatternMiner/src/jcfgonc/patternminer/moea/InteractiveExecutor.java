package jcfgonc.patternminer.moea;

import java.util.Properties;

import javax.swing.UIManager;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;

public class InteractiveExecutor {
	private Problem problem;
	private String algorithmName;
	private Properties properties;
	private int maxGenerations;
	private NondominatedPopulation finalResult;

	public InteractiveExecutor(Problem problem, String algorithmName, Properties properties, int maxGenerations) {
		this.problem = problem;
		this.algorithmName = algorithmName;
		this.properties = properties;
		this.maxGenerations = maxGenerations;
	}

	public NondominatedPopulation execute() {
		NondominatedPopulation lastResult = null;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// couldn't set system look and feel, continue with default
		}

		EvolutionMonitor evolutionMonitor = new EvolutionMonitor();

		int generation = 0;
		Algorithm algorithm = null;
//		Properties properties = new Properties();
//		properties.setProperty("populationSize", Integer.toString(populationSize));

		// you may want to use this later, do not forget to shutdown the ExecutorService
//		executor = Executors.newFixedThreadPool(numberOfThreads);
//		problem = new DistributedProblem(problem, executor);

		try {
			algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, properties, problem);
			evolutionMonitor.show();

			do {
				algorithm.step();
				generation++;

				// update graphs
				lastResult = algorithm.getResult();
				updateGUI(lastResult, generation, maxGenerations);

				if (algorithm.isTerminated() || generation >= maxGenerations || evolutionMonitor.isCanceled()) {
					break; // break while loop
				}
			} while (true);
		} finally {
			if (algorithm != null) {
				algorithm.terminate();
			}
		}
		finalResult = lastResult;
		evolutionMonitor.dispose();
		showFinalResult();
		return lastResult;
	}

	private void updateGUI(NondominatedPopulation nondominatedPopulation, int generation, int maxGenerations) {
	}

	public void showFinalResult() {
		if (finalResult == null) {
			System.out.println("no results to show");
			return;
		}

		for (Solution solution : finalResult) {
			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				System.out.format("%.7f\t", solution.getObjective(i));
			}
			System.out.format("%s\n", solution.getVariable(0));
		}

	}
}
