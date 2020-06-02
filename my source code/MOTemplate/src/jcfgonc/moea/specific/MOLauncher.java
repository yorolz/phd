package jcfgonc.moea.specific;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.util.TypedProperties;

import jcfgonc.moea.InteractiveExecutor;
import jcfgonc.moea.MOConfig;

/**
 * Demonstrates how a new problem is defined and used within the MOEA Framework.
 */
public class MOLauncher {

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException,
			NoSuchFileException, IOException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		// TODO: setup your data structures here

//		String path = MOConfig.FILE_PATH;
//		System.out.println("loading... " + path);
//		StringGraph kbGraph = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
//		Ticker ticker = new Ticker();
//		GraphReadWrite.readCSV(path, kbGraph);
//		kbGraph.showStructureSizes();
//		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
//		System.out.println("-------");
//		KnowledgeBase kb = PatternFinderUtils.buildKnowledgeBase(kbGraph);

//		PatternChromosome.kb = kb;
//		PatternChromosome.kbGraph = kbGraph;
//		PatternChromosome.random = new Well44497a();

		// the following is setup for mutation only
		registerPatternChromosomeMutation();
		Properties properties = new Properties();
		properties.setProperty("operator", "PatternMutation");
		properties.setProperty("PatternMutation.Rate", Double.toString(MOConfig.MUTATION_RATE));
		properties.setProperty("populationSize", Integer.toString(MOConfig.POPULATION_SIZE));
		// TODO: personalize your constructor here
		CustomProblem problem = new CustomProblem();
		InteractiveExecutor ie = new InteractiveExecutor(problem, "NSGAII", properties, Integer.MAX_VALUE);
		ie.execute();
	}

	private static void registerPatternChromosomeMutation() {
		OperatorFactory.getInstance().addProvider(new OperatorProvider() {
			public String getMutationHint(Problem problem) {
				return null;
			}

			public String getVariationHint(Problem problem) {
				return null;
			}

			public Variation getVariation(String name, Properties properties, Problem problem) {
				TypedProperties typedProperties = new TypedProperties(properties);

				if (name.equalsIgnoreCase("CustomMutation")) {
					double probability = typedProperties.getDouble("CustomMutation.Rate", 1.0);
					CustomMutation pm = new CustomMutation(probability);
					return pm;
				}

				// No match, return null
				return null;
			}
		});
	}
}
