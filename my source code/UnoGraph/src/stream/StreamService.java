package stream;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.OSTools;

public class StreamService {

	private ExecutorService es;
	private int amountThreads;

	public StreamService() {
		this.amountThreads = OSTools.getNumberOfCPUCores() + 0;
		this.es = Executors.newFixedThreadPool(amountThreads);
	}

	public StreamService(int amountThreads) {
		this.amountThreads = amountThreads;
		this.es = Executors.newFixedThreadPool(amountThreads);
	}

	public <I, O> void invoke(int dataSize, StreamProcessor sp) throws InterruptedException {
		ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		int range_size = dataSize / this.amountThreads;
		for (int threadId = 0; threadId < this.amountThreads; threadId++) {
			int rangeL = range_size * threadId;
			int rangeH;
			if (threadId == this.amountThreads - 1)
				rangeH = dataSize - 1;
			else
				rangeH = range_size * (threadId + 1) - 1;

			tasks.add(new StreamInvoker(threadId, rangeL, rangeH, sp, dataSize));
		}

		es.invokeAll(tasks);
	}

	public void shutdown() {
		es.shutdown();
	}
	
	private class StreamInvoker implements Callable<Object> {

		private int threadId;
		private int rangeL;
		private int rangeH;
		private int dataSize;
		private StreamProcessor sp;

		public StreamInvoker(int threadId, int rangeL, int rangeH, StreamProcessor sp, int dataSize) {
			this.threadId = threadId;
			this.rangeL = rangeL;
			this.rangeH = rangeH;
			this.dataSize = dataSize;
			this.sp = sp;
		}

		@Override
		public Object call() throws Exception {
			sp.run(threadId, rangeL, rangeH, dataSize);
			return null;
		}

	}

}
