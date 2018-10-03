package matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

import graph.StringGraph;

/**
 * @author CK
 */
public class AnalogyEvaluatingThread implements Callable<Object> {
	final private int threadId;
	final private int rangeL;
	final private int rangeH;
	private ArrayList<Mapping<String>> rootMappings;
	private StringGraph graph;
	private AnalogySet[] analogies;

	public AnalogyEvaluatingThread(int thread_id, int rangeL, int rangeH, ArrayList<Mapping<String>> rootMappings, StringGraph graph, AnalogySet[] analogies) {
		this.threadId = thread_id;
		this.rangeL = rangeL;
		this.rangeH = rangeH;

		this.rootMappings = rootMappings;
		this.analogies = analogies;
		this.graph = graph;
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
	public Object call() {
		try {
			// -----------------------------------------------------
			{
				Thread currentThread = Thread.currentThread();
				currentThread.setName("AnalogyEvaluatingThread:" + threadId);
				currentThread.setPriority(Thread.MIN_PRIORITY);
			}

			// -----------------------------------------------------
			for (int i = rangeL; i <= rangeH; i++) {
				if (StaticTimer.timedOut())
					break;

				Mapping<String> rootMapping = rootMappings.get(i);
				String leftConcept = rootMapping.getLeftConcept();
				String rightConcept = rootMapping.getRightConcept();
				Set<Mapping<String>> bestAnalogy = MapperLauncher.getAnalogy(graph, leftConcept, rightConcept, false);
				analogies[i] = new AnalogySet(bestAnalogy);
			}
			// -----------------------------------------------------

		} catch (IOException e) {
			e.printStackTrace();
			// this is serious, an I/O error has occurred (probably writing to a file)
			System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); // at least someone is going to now there was a problem
		}
		return null;
	}

}
