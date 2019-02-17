package jcfgonc.patternminer.launcher.moea;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.patternminer.KnowledgeBaseBuilder;
import jcfgonc.patternminer.PatternMinerConfig;
import structures.Ticker;

/**
 * Demonstrates how a new problem is defined and used within the MOEA Framework.
 */
public class PatternMinerMOEALauncher {

	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, NoSuchFileException, IOException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String path = PatternMinerConfig.FILE_PATH;

		System.out.println("loading... " + path);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(path, inputSpace);
		inputSpace.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");

		System.out.println("vertices\t" + inputSpace.getVertexSet().size());
		System.out.println("edges   \t" + inputSpace.edgeSet().size());
		System.out.println("-------");

		// removeEdges(inputSpace);

		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		ticker.resetTicker();
		System.out.println("kbb.addFacts(inputSpace)");
		kbb.addFacts(inputSpace);
		System.out.println("kbb.build()");
		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");

		// -----------------
		NondominatedPopulation result = new Executor()// ---------
				.withProblemClass(PatternMinerProblem.class)// ---
				.withAlgorithm("NSGAII")// -----------------------
				.withMaxEvaluations(10000)// ---------------------
				.run(); // ---------------------------------------

		// display the results
		System.out.format("Objective1  Objective2%n");

		for (Solution solution : result) {
			System.out.format("%.4f      %.4f%n", solution.getObjective(0), solution.getObjective(1));
		}
	}

}
