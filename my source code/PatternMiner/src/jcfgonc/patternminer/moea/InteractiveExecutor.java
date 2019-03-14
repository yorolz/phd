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
	private Properties algorithmProperties;
	private int maxGenerations;
	private NondominatedPopulation lastResult;
	private boolean canceled;

	public InteractiveExecutor(Problem problem, String algorithmName, Properties algorithmProperties, int maxGenerations) {
		this.problem = problem;
		this.algorithmName = algorithmName;
		this.algorithmProperties = algorithmProperties;
		this.maxGenerations = maxGenerations;
	}

	public NondominatedPopulation execute() throws InterruptedException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// couldn't set system look and feel, continue with default
		}

		InteractiveExecutorGUI gui = new InteractiveExecutorGUI(this);

		int generation = 0;
		Algorithm algorithm = null;
		lastResult = null;

		// you may want to use this later, do not forget to shutdown the ExecutorService
//		executor = Executors.newFixedThreadPool(numberOfThreads);
//		problem = new DistributedProblem(problem, executor);

		try {
			algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, algorithmProperties, problem);
			gui.initializeTheRest();
			gui.setVisible(true);
			this.canceled = false;

			do {
				// update graphs
				gui.updateStatus(lastResult, generation, algorithm.getNumberOfEvaluations());

				algorithm.step();
				generation++;
				lastResult = algorithm.getResult();

				if (algorithm.isTerminated() || generation >= maxGenerations || this.canceled) {
					break; // break while loop
				}
			} while (true);
		} finally {
			if (algorithm != null) {
				algorithm.terminate();
			}
		}
		gui.dispose();
		showLastResult();
		return lastResult;
	}

	public Problem getProblem() {
		return problem;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public Properties getAlgorithmProperties() {
		return algorithmProperties;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public NondominatedPopulation getLastResult() {
		return lastResult;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void showLastResult() {
		if (lastResult == null) {
			System.out.println("no results to show");
			return;
		}

		for (Solution solution : lastResult) {
			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				System.out.format("%.7f\t", solution.getObjective(i));
			}
			System.out.format("%s\n", solution.getVariable(0));
		}

	}

	public void abortOptimization() {
		showLastResult();
		System.exit(-1);
	}

	public void stopOptimization() {
		this.canceled = true;
	}
}
