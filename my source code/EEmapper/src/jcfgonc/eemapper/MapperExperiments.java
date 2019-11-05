package jcfgonc.eemapper;

import java.io.File;
import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.CSVWriter;
import structures.Ticker;

public class MapperExperiments {
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 10; i++) {
			runMapperBatch();
		}
		System.exit(0);
	}

	private static void runMapperBatch()
			throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		CSVWriter csvW = new CSVWriter();
		csvW.writeHeader("tau", "bridge", "time", "filename", "pairs", "numberConcepts");

		String baseFolder = "..\\DomainSpotter\\generated inputspaces";
		String[] tauFolders = { "tau0.0", "tau0.01", "tau0.1", "tau0.25" };
		String[] bridges = { "beaver_rat", "big_board", "exercise_physiology", "flower_person", "hiram_ohio", "horror_fiction", "motorcycle_drag_race", "redwatch", "vascularity",
				"venography" };

		String[] interesting = { "0.0_exercise_physiology", "0.01_redwatch", "0.01_venography", "0.1_hiram_ohio", "0.1_horror_fiction", "0.25_redwatch", "0.25_vascularity",
				"0.25_venography", };

		for (String tauFolder : tauFolders) {
			for (String bridge : bridges) {
				String filenameTemplate = baseFolder + "\\" + tauFolder + "\\" + tauFolder + "_" + bridge + "_is";

				boolean skip = !stringContainsAnyFromArray(interesting, filenameTemplate);

				if (skip)
					continue;

				String filename0 = filenameTemplate + "0.csv";
				String filename1 = filenameTemplate + "1.csv";

				runMapper(filename0, filename1, csvW);

				System.out.println("cleaning up garbage (GC)...");
				System.gc();
				Thread.sleep(60000);

				System.out.println("-------------------------------------------------------");
			}
		}
		csvW.close();
	}

	private static boolean stringContainsAnyFromArray(String[] array, String str) {
		for (String a : array) {
			if (str.contains(a)) {
				return true;
			}
		}
		return false;
	}

	private static void runMapper(String is0, String is1, CSVWriter csvW)
			throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		// ..\\DomainSpotter\\generated inputspaces\\tau0.1\\tau0.1_beaver_rat_is0.csv
		String domain = new File(is0).getName();
		domain = domain.substring(0, domain.indexOf(".csv"));
		// domain = tau0.1_beaver_rat_is0
		String tau = domain.substring(3, domain.indexOf('_'));
		String bridge = domain.substring(domain.indexOf('_') + 1, domain.length() - 4);

		// merge all the given files
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);

		String domain0 = new File(is0).getName();
		domain0 = domain0.substring(0, domain0.indexOf(".csv"));
		String domain1 = new File(is1).getName();
		domain1 = domain1.substring(0, domain1.indexOf(".csv"));

		System.out.println(domain0 + " VS " + domain1);
		GraphReadWrite.readAutoDetect(is0, inputSpace);
		GraphReadWrite.readAutoDetect(is1, inputSpace);

		Ticker ticker = new Ticker();
		String inner = domain.substring(0, domain.indexOf("_is0")) + "_" + CSVWriter.generateFilenameWithTimestamp();
		String innerCsvwFilename = inner + ".csv";
		CSVWriter innerCsvw = new CSVWriter(innerCsvwFilename);

		MappingStructure<String, String> best = MapperEnhanced.executeGeneticAlgorithm(inputSpace, innerCsvw);

		innerCsvw.close();
		double dt = ticker.getTimeDeltaLastCall();
		System.out.printf("d(t)=%f\n", dt);

		best.writeTGF(inner + ".tgf");

		int pairs = best.getMapping().size();
		csvW.writeLine(tau, bridge, Double.toString(dt), innerCsvw.getFilename(), Integer.toString(pairs), Integer.toString(inputSpace.getVertexSet().size()));
		csvW.flush();
	}

}
