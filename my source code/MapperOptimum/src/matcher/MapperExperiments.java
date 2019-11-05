package matcher;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.CSVWriter;
import structures.Ticker;

public class MapperExperiments {
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 10; i++) {
			runMapperBatch();
		}
	}

	private static void runMapperBatch() throws InterruptedException, IOException {
		CSVWriter csvW = new CSVWriter();
		csvW.writeHeader("tau", "bridge", "time", "analogies", "pairs", "numberConcepts", "allocationSize");

		String baseFolder = "..\\DomainSpotter\\generated inputspaces";
		String[] tauFolders = { "tau0.0", "tau0.01", "tau0.1", "tau0.25" };
		String[] bridges = { "beaver_rat", "big_board", "exercise_physiology", "flower_person", "hiram_ohio", "horror_fiction", "motorcycle_drag_race", "redwatch", "vascularity",
				"venography" };

		String[] interesting = { "0.0_exercise_physiology", "0.01_redwatch", "0.01_venography", "0.1_hiram_ohio", "0.1_horror_fiction", "0.25_redwatch", "0.25_vascularity",
				"0.25_venography", };

		for (String tauFolder : tauFolders) {
			for (String bridge : bridges) {
				boolean skip = true;
				String filenameTemplate = baseFolder + "\\" + tauFolder + "\\" + tauFolder + "_" + bridge + "_is";
				// these are the guys which timeout
				for (String p : interesting) {
					if (filenameTemplate.contains(p)) {
						skip = false;
						break;
					}
				}

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

	private static void runMapper(String is0, String is1, CSVWriter csvW) throws IOException, InterruptedException {
		// ..\\DomainSpotter\\generated inputspaces\\tau0.1\\tau0.1_beaver_rat_is0.csv
		String domain = new File(is0).getName();
		domain = domain.substring(0, domain.indexOf(".csv"));
		// domain = tau0.1_beaver_rat_is0
		String tau = domain.substring(3, domain.indexOf('_'));
		String bridge = domain.substring(domain.indexOf('_') + 1, domain.length() - 4);

		// merge all the given files
		StringGraph graph = new StringGraph();

		String domain0 = new File(is0).getName();
		domain0 = domain0.substring(0, domain0.indexOf(".csv"));
		String domain1 = new File(is1).getName();
		domain1 = domain1.substring(0, domain1.indexOf(".csv"));

		System.out.println(domain0 + " VS " + domain1);
		GraphReadWrite.readAutoDetect(is0, graph);
		GraphReadWrite.readAutoDetect(is1, graph);

		MutableInt numberConcepts = new MutableInt();
		MutableInt allocationSize = new MutableInt();
		Ticker ticker = new Ticker();
		Set<AnalogySet> analogies = MapperLauncher.findAllAnalogies(graph, numberConcepts, allocationSize);
		double dt = ticker.getTimeDeltaLastCall();
		System.out.printf("d(t)=%f\n", dt);
		int pairs = 0;
		int numAnalogies = analogies.size();
		if (!analogies.isEmpty()) {
			AnalogySet mapping = analogies.iterator().next();
			pairs = mapping.getMappings().size();
		}

		csvW.writeLine(tau, bridge, Double.toString(dt), Integer.toString(numAnalogies), Integer.toString(pairs), numberConcepts.toString(), allocationSize.toString());
		csvW.flush();
	}

}
