package com.githhub.aaronbembenek.querykb;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Parser;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class KnowledgeBase {

	private final TObjectIntMap<String> constants;
	private final Map<String, IndexedPredicate> relations;
	private final ForkJoinPool fjp = ForkJoinPool.commonPool();

	public KnowledgeBase(TObjectIntMap<String> constants, Map<String, IndexedPredicate> relations) {
		this.constants = constants;
		this.relations = relations;
	}

	public static KnowledgeBase fromCSVTriples(Reader csv) throws IOException, ParseException {
		return make(csv, csvReader);
	}

	public static KnowledgeBase fromDatalogFacts(Reader datalog) throws IOException, ParseException {
		return make(datalog, datalogReader);
	}

	private static KnowledgeBase make(final Reader r, final TupleReader tupleReader)
			throws IOException, ParseException {
		AtomicInteger counter = new AtomicInteger(1);
		TObjectIntMap<String> constants = new TObjectIntHashMap<>();
		Map<String, IndexedPredicate.Builder> builders = new HashMap<>();
		Tokenizer t = new Tokenizer(r);
		while (t.hasNext()) {
			tupleReader.read(t);
			int sub = Util.lookupOrCreate(constants, tupleReader.getSubject(), () -> counter.getAndIncrement());
			int obj = Util.lookupOrCreate(constants, tupleReader.getObject(), () -> counter.getAndIncrement());
			assert counter.get() >= 0;
			String pred = tupleReader.getPredicate();
			Util.lookupOrCreate(builders, pred, () -> new IndexedPredicate.Builder(pred)).addEntry(sub, obj);
		}
		Map<String, IndexedPredicate> relations = new ConcurrentHashMap<>();
		builders.entrySet().parallelStream().forEach(e -> relations.put(e.getKey(), e.getValue().build()));
		return new KnowledgeBase(constants, relations);
	}

	private static interface TupleReader {

		void read(Tokenizer t) throws IOException, ParseException;

		String getPredicate();

		String getSubject();

		String getObject();

	}

	private static final TupleReader csvReader = new TupleReader() {

		private String predicate;
		private String subject;
		private String object;

		@Override
		public void read(final Tokenizer t) throws IOException, ParseException {
			subject = Parser.parseConstant(t);
			t.consume(",");
			predicate = Parser.parseConstant(t);
			t.consume(",");
			object = Parser.parseConstant(t);
		}

		@Override
		public String getPredicate() {
			return predicate;
		}

		@Override
		public String getSubject() {
			return subject;
		}

		@Override
		public String getObject() {
			return object;
		}

	};

	private static final TupleReader datalogReader = new TupleReader() {

		private String predicate;
		private String subject;
		private String object;

		@Override
		public void read(final Tokenizer t) throws IOException, ParseException {
			predicate = Parser.parseConstant(t);
			t.consume("(");
			subject = Parser.parseConstant(t);
			t.consume(",");
			object = Parser.parseConstant(t);
			t.consume(").");
		}

		@Override
		public String getPredicate() {
			return predicate;
		}

		@Override
		public String getSubject() {
			return subject;
		}

		@Override
		public String getObject() {
			return object;
		}

	};

	public long countNaive(Query q) {
		NaiveQueryEvaluator eval = new NaiveQueryEvaluator(q);
		return eval.run();
	}

	public long count(Query q) {
		CountingQueryEvaluator eval = new CountingQueryEvaluator(q);
		return eval.run();
	}

	public long count(Query q, int blockSize, int parallelLimit, long solutionLimit) {
		CountingQueryEvaluator eval = new CountingQueryEvaluator(q, blockSize, parallelLimit, solutionLimit);
		return eval.run();
	}

	///////////////////////////////////////////////////////////////////////////
	
	private class NaiveQueryEvaluator {

		private final static int parallelThreshold = 1;

		private final Map<String, Boolean> varSet = new HashMap<>();
		private final TObjectIntMap<String> varMap = new TObjectIntHashMap<>();
		private int varCounter = -1;
		private final Set<Conjunct> conjuncts;
		private int tupleLength = 0;

		public NaiveQueryEvaluator(Query q) {
			conjuncts = new HashSet<>(q.getConjuncts());
		}

		public synchronized long run() {
			int[] res = new int[0];
			while (!conjuncts.isEmpty()) {
				IntConjunct c = intize(chooseNextConjunct());
				if (c == null) {
					return 0;
				}
				res = query(c, res);
				if (res == null) {
					return 0;
				}
			}
			if (tupleLength == 0) {
				return 1;
			}
			assert res.length != 0;
			return res.length / tupleLength;
		}

		private boolean isVariable(String s) {
			return Util.lookupOrCreate(varSet, s, () -> Parser.isVariable(s));
		}

		public IntConjunct intize(Conjunct c) {
			Integer subject = intizeTerm(c.getSubject());
			Integer object = intizeTerm(c.getObject());
			if (subject == null || object == null) {
				return null;
			}
			if (!relations.containsKey(c.getPredicate())) {
				return null;
			}
			return new IntConjunct(c.getPredicate(), subject, object);
		}

		private Integer intizeTerm(String term) {
			if (isVariable(term)) {
				return Util.lookupOrCreate(varMap, term, () -> varCounter--);
			}
			if (!constants.containsKey(term)) {
				return null;
			}
			return constants.get(term);
		}

		private Conjunct chooseNextConjunct() {
			Conjunct best = null;
			int bestSize = 0;
			for (Conjunct c : conjuncts) {
				String subject = c.getSubject();
				int sub;
				if (isVariable(subject) && !varMap.containsKey(subject)) {
					sub = -1;
				} else {
					sub = 0;
				}
				String object = c.getObject();
				int obj;
				if (object.equals(subject)) {
					obj = sub;
				} else {
					if (isVariable(object) && !varMap.containsKey(object)) {
						obj = -2;
					} else {
						obj = 1;
					}
				}
				int size = relations.get(c.getPredicate()).getIndexKeySetSize(sub, obj);
				if (size < bestSize || best == null) {
					best = c;
					bestSize = size;
				}
			}
			assert best != null;
			conjuncts.remove(best);
			return best;
		}

		private int[] query(IntConjunct c, int[] rel) {
			if (rel.length == 0) {
				return query(c);
			}
			int subject = c.getSubject();
			int object = c.getObject();
			int copySize = isUnboundVar(subject) ? 1 : 0;
			copySize += isUnboundVar(object) && subject != object ? 1 : 0;
			if (rel.length < parallelThreshold) {
				return sequentialQuery(c, rel, copySize);
			}
			return parallelQuery(c, rel, copySize);
		}

		private int[] parallelQuery(IntConjunct c, int[] rel, int copySize) {
			List<Callable<TIntList>> tasks = new ArrayList<>();
			int blockSize = parallelThreshold / tupleLength;
			blockSize = Math.max(blockSize, tupleLength);
			for (int i = 0; i < rel.length; i += blockSize) {
				final int start = i;
				final int end = Math.min(i + blockSize, rel.length);
				tasks.add(() -> query(c, rel, start, end, copySize));
			}
			List<Future<TIntList>> futures = fjp.invokeAll(tasks);
			List<TIntList> results = new ArrayList<>();
			int size = 0;
			for (Future<TIntList> future : futures) {
				try {
					TIntList r = future.get();
					if (r != null) {
						results.add(r);
						size += r.size();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					throw new AssertionError();
				}
			}
			if (results.isEmpty()) {
				return null;
			}
			int[] res = new int[size];
			int pos = 0;
			for (TIntList r : results) {
				int len = r.size();
				r.toArray(res, 0, pos, len);
				pos += len;
			}
			tupleLength += copySize;
			return res;
		}

		private int[] sequentialQuery(IntConjunct c, int[] rel, int copySize) {
			TIntList r = query(c, rel, 0, rel.length, copySize);
			if (r == null) {
				return null;
			}
			tupleLength += copySize;
			return r.toArray();
		}

		private TIntList query(IntConjunct c, int[] rel, int start, int end, int copySize) {
			TIntList res = new TIntArrayList();
			int subject = c.getSubject();
			int object = c.getObject();
			boolean ok = false;
			for (int i = start; i < end; i += tupleLength) {
				int sub = lookupTerm(subject, i, rel);
				int obj = lookupTerm(object, i, rel);
				int[] q = relations.get(c.getPredicate()).query(sub, obj);
				if (q == null) {
					continue;
				}
				ok = true;
				if (q.length == 0) {
					for (int j = i; j < i + tupleLength; j++) {
						res.add(rel[j]);
					}
				} else {
					for (int k = 0; k < q.length; k += copySize) {
						for (int j = i; j < i + tupleLength; j++) {
							res.add(rel[j]);
						}
						for (int j = k; j < k + copySize; j++) {
							res.add(q[j]);
						}
					}
				}
			}
			return ok ? res : null;
		}

		@SuppressWarnings("unused")
		private String tupleToString(int[] rel, int start, int length) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = start; i < start + length; i++) {
				sb.append(rel[i]);
				sb.append(",");
			}
			sb.append(")");
			return sb.toString();
		}

		private boolean isUnboundVar(int term) {
			return KnowledgeBase.isVariable(term) && varToIndex(term) >= tupleLength;
		}

		private int varToIndex(int var) {
			return -(var + 1);
		}

		private int lookupTerm(int term, int tupleStart, int[] rel) {
			if (KnowledgeBase.isVariable(term) && varToIndex(term) < tupleLength) {
				return rel[tupleStart + varToIndex(term)];
			}
			return term;
		}

		private int[] query(IntConjunct c) {
			IndexedPredicate rel = relations.get(c.getPredicate());
			int subject = c.getSubject();
			int object = c.getObject();
			int[] res = rel.query(subject, object);
			if (KnowledgeBase.isVariable(subject)) {
				tupleLength++;
			}
			if (object != subject && KnowledgeBase.isVariable(object)) {
				tupleLength++;
			}
			return res;
		}

	}
	
	///////////////////////////////////////////////////////////////////////////

	private class CountingQueryEvaluator {

		private final TObjectIntMap<String> varMap = new TObjectIntHashMap<>();
		private final Map<String, Boolean> varSet = new HashMap<>();
		private int varCounter = -1;
		private final IntConjunct[] conjuncts;
		private final TIntIntMap finalLocForVar = new TIntIntHashMap();

		private final static int defaultBlockSize = 4096;
		private final static int defaultParallelLimit = 1;
		private final static long defaultSolutionLimit = Long.MAX_VALUE;
		private final int blockSize;
		private final int parallelLimit;
		private final long solutionLimit;
	
		private final AtomicLong solutionCount = new AtomicLong();
		
		private volatile boolean cancelled;
		
		private final AtomicInteger taskCounter = new AtomicInteger();

		public CountingQueryEvaluator(Query q) {
			this(q, defaultBlockSize, defaultParallelLimit, defaultSolutionLimit);
		}

		public CountingQueryEvaluator(Query q, int blockSize, int parallelLimit, long solutionLimit) {
			this.blockSize = blockSize;
			this.parallelLimit = parallelLimit;
			this.solutionLimit = solutionLimit;
			conjuncts = optimizeQuery(q);
			if (conjuncts != null) {
				int i = 0;
				for (IntConjunct c : conjuncts) {
					int subject = c.getSubject();
					if (KnowledgeBase.isVariable(subject)) {
						finalLocForVar.put(subject, i);
					}
					int object = c.getObject();
					if (KnowledgeBase.isVariable(object)) {
						finalLocForVar.put(object, i);
					}
					i++;
				}
			}
		}

		public long run() {
			if (conjuncts == null) {
				return 0;
			}
			TLongList l = new TLongArrayList();
			l.add(1);
			Block initBlock = new Block(new TIntArrayList(), new TIntArrayList(), l);
			QueryTask task = new QueryTask(initBlock, 0);
			taskCounter.getAndIncrement();
			(new QueryTaskWrapper(task, taskCounter)).fork();
			synchronized (taskCounter) {
				while (taskCounter.get() > 0) {
					try {
						taskCounter.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return solutionCount.get();
		}

		@SuppressWarnings("serial")
		private class QueryTaskWrapper extends RecursiveAction {

			private QueryTask inner;
			private final AtomicInteger taskCounter;
		
			public QueryTaskWrapper(QueryTask inner, AtomicInteger taskCounter) {
				this.inner = inner;
				this.taskCounter = taskCounter;
			}
			
			@Override
			protected void compute() {
				inner.compute();
				inner = null;
				if (taskCounter.decrementAndGet() <= 0) {
					synchronized (taskCounter) {
						taskCounter.notify();
					}
				}
			}
			
		}
		
		@SuppressWarnings("serial")
		private class QueryTask extends RecursiveAction {

			private final Block block;
			private final int pos;
			private final IntConjunct c;
			private final TIntList newSchema = new TIntArrayList();
			private final TIntIntMap schemaIndex = new TIntIntHashMap();
			private final TIntList newVarsToSave = new TIntArrayList();
			private final int[] scratchTup;
			private final boolean[] attrsToCopy;
			private final int nAttrsToCopy;

			public QueryTask(Block block, int pos) {
				this.block = block;
				this.pos = pos;
				c = pos < conjuncts.length ? conjuncts[pos] : null;
				attrsToCopy = new boolean[block.arity];
				if (c != null) {
					setup();
				}
				int n = 0;
				for (boolean b : attrsToCopy) {
					if (b) {
						n++;
					}
				}
				nAttrsToCopy = n;
				scratchTup = new int[newSchema.size()];
			}

			@Override
			protected void compute() {
				if (cancelled) {
					return;
				}
				long res;
				if (c == null) {
					assert block.getSchema().size() == 0;
					assert block.getCardinality() == 1;
					res = block.readTuple(0, new int[0]);
					if (solutionCount.addAndGet(res) > solutionLimit) {
						cancelled = true;
					}
				} else {
					doQuery();
				}
			}

			private void doQuery() {
				assert block.getCardinality() > 0;
				Block b = new Block(newSchema);
				int[] tup = new int[block.getArity()];
				for (int relIdx = 0; relIdx < block.getCardinality() && !cancelled; relIdx++) {
					long cnt = block.readTuple(relIdx, tup);
					b = doQuery(tup, cnt, b);
				}
				if (b.getCardinality() != 0 && !cancelled) {
					subquery(b);
				}
			}

			private Block doQuery(int[] tup, long cnt, Block b) {
				int subject = c.getSubject();
				int object = c.getObject();
				if (schemaIndex.containsKey(subject)) {
					subject = tup[schemaIndex.get(subject)];
				}
				if (schemaIndex.containsKey(object)) {
					object = tup[schemaIndex.get(object)];
				}
				for (int i = 0, j = 0; i < block.arity; i++) {
					if (attrsToCopy[i]) {
						scratchTup[j] = tup[i];
						j++;
					}
				}
				if (newVarsToSave.size() == 0) {
					return doQuery0(tup, cnt, b, subject, object);
				}
				return doQuery1(tup, cnt, b, subject, object);
			}

			private Block doQuery0(int[] tup, long cnt, Block b, int subject, int object) {
				IndexedPredicate pred = relations.get(c.getPredicate());
				int[] res = pred.query(subject, object);
				int k = 0;
				if (KnowledgeBase.isVariable(subject)) {
					k++;
				}
				if (KnowledgeBase.isVariable(object) && subject != object) {
					k++;
				}
				if (res != null) {
					int n = k == 0 ? 1 : res.length / k;
					b = writeScratchTupleToBlock(b, cnt * n);
				}
				return b;
			}

			private Block doQuery1(int[] tup, long cnt, Block b, int subject, int object) {
				IndexedPredicate pred = relations.get(c.getPredicate());
				IndexedPredicate.Entry[] es;
				int other;
				if (newVarsToSave.indexOf(subject) == 0) {
					es = pred.forwardIndex;
					other = object;
				} else {
					assert newVarsToSave.indexOf(object) == 0;
					es = pred.backwardIndex;
					other = subject;
				}
				int nVarsToSave = newVarsToSave.size();
				for (IndexedPredicate.Entry e : es) {
					scratchTup[nAttrsToCopy] = e.key;
					if (nVarsToSave == 1) {
						long newCnt;
						if (KnowledgeBase.isVariable(other)) {
							newCnt = cnt * e.vals.length;
							assert newCnt != 0;
						} else {
							if (Arrays.binarySearch(e.vals, other) >= 0) {
								newCnt = cnt;
							} else {
								newCnt = 0;
							}
						}
						b = writeScratchTupleToBlock(b, newCnt);
					} else {
						for (int i : e.vals) {
							scratchTup[nAttrsToCopy + 1] = i;
							b = writeScratchTupleToBlock(b, cnt);
						}
					}
				}
				return b;
			}

			private Block writeScratchTupleToBlock(Block b, long cnt) {
				if (b.getCardinality() >= blockSize) {
					subquery(b);
					b = new Block(newSchema);
				}
				b.addTuple(scratchTup, cnt);
				return b;
			}

			private void subquery(Block b) {
				QueryTask q = new QueryTask(b, pos + 1);
				if (taskCounter.incrementAndGet() > parallelLimit) {
					int count = taskCounter.decrementAndGet();
					assert count > 0;
					q.compute();
				} else {
					QueryTaskWrapper qq = new QueryTaskWrapper(q, taskCounter);
					qq.fork();
				}
			}

			private void setup() {
				int subject = c.getSubject();
				int object = c.getObject();
				int i = 0;
				for (TIntIterator it = block.getSchema().iterator(); it.hasNext();) {
					int var = it.next();
					if (finalLocForVar.get(var) > pos) {
						newSchema.add(var);
						attrsToCopy[i] = true;
					} else {
						attrsToCopy[i] = false;
					}
					if (var == subject || var == object) {
						schemaIndex.put(var, i);
					}
					i++;
				}
				findNewVarsToSave();
				newSchema.addAll(newVarsToSave);
			}

			private void findNewVarsToSave() {
				int subject = c.getSubject();
				int object = c.getObject();
				boolean saveSubject = finalLocForVar.get(subject) > pos && !schemaIndex.containsKey(subject);
				boolean saveObject = subject != object && finalLocForVar.get(object) > pos
						&& !schemaIndex.containsKey(object);
				int[] vars;
				if (saveSubject) {
					if (saveObject) {
						if (finalLocForVar.get(object) > finalLocForVar.get(subject)) {
							vars = new int[] { object, subject };
						} else {
							vars = new int[] { subject, object };
						}
					} else {
						vars = new int[] { subject };
					}
				} else if (saveObject) {
					vars = new int[] { object };
				} else {
					vars = new int[0];
				}
				newVarsToSave.addAll(vars);
			}

		}

		public IntConjunct[] optimizeQuery(Query q) {
			List<IntConjunct> l = intizeConjuncts(new HashSet<>(q.getConjuncts()));
			if (l == null) {
				return null;
			}
			QueryOptimizer o = new QueryOptimizer(l);
			return o.run().toArray(new IntConjunct[0]);
		}

		private class QueryOptimizer {

			TIntObjectMap<Set<IntConjunct>> conjunctsForVar = new TIntObjectHashMap<>();
			Deque<IntConjunct> stack = new ArrayDeque<>();
			private final TIntIterator vars;
			List<IntConjunct> out = new ArrayList<>();
			TIntIntMap varOrder = new TIntIntHashMap();
			int varRankCounter = 0;

			public QueryOptimizer(Collection<IntConjunct> conjuncts) {
				vars = setup(conjuncts);
			}

			public List<IntConjunct> run() {
				while (vars.hasNext()) {
					if (stack.isEmpty()) {
						loadNextVar();
					} else if (stack.size() == 1) {
						/*
						 * If the stack only has one thing left, can always push it to output.
						 */
						pushToOutput(popFromStack());
					} else {
						IntConjunct next = peekInStack();
						int subject = next.getSubject();
						int object = next.getObject();
						if (!isSafe(subject)) {
							assert isSafe(object);
							loadVar(subject);
						} else if (!isSafe(object)) {
							loadVar(object);
						} else {
							pushToOutput(popFromStack());
						}
					}
				}
				unloadStack();
				return out;
			}

			private boolean isSafe(int term) {
				if (KnowledgeBase.isVariable(term)) {
					return varOrder.containsKey(term) || conjunctsForVar.get(term).isEmpty();
				}
				return true;
			}

			private void loadNextVar() {
				assert stack.isEmpty();
				loadVar(vars.next());
			}

			private void loadVar(int var) {
				Util.lookupOrCreate(varOrder, var, () -> varRankCounter++);
				List<IntConjunct> conjuncts = new ArrayList<>(conjunctsForVar.get(var));
				for (IntConjunct c : conjuncts) {
					int subject = c.getSubject();
					if (KnowledgeBase.isVariable(subject)) {
						conjunctsForVar.get(subject).remove(c);
					}
					int object = c.getObject();
					if (KnowledgeBase.isVariable(object)) {
						conjunctsForVar.get(object).remove(c);
					}
				}
				if (!conjuncts.isEmpty()) {
					boolean pushed = false;
					List<IntConjunct> notPushed = new ArrayList<>();
					for (IntConjunct c : conjuncts) {
						boolean safe = !KnowledgeBase.isVariable(c.getSubject())
								|| conjunctsForVar.get(c.getSubject()).isEmpty();
						safe &= !KnowledgeBase.isVariable(c.getSubject())
								|| conjunctsForVar.get(c.getSubject()).isEmpty();
						if (safe) {
							pushed = true;
							pushToOutput(c);
						} else {
							notPushed.add(c);
						}
					}
					if (pushed) {
						for (IntConjunct c : notPushed) {
							pushToStack(c);
						}
						return;
					}
					Iterator<IntConjunct> it = notPushed.iterator();
					IntConjunct c = it.next();
					while (it.hasNext()) {
						pushToStack(it.next());
					}
					pushToOutput(c);
					int other;
					if (c.getSubject() == var) {
						other = c.getObject();
					} else {
						assert c.getObject() == var;
						other = c.getSubject();
					}
					if (KnowledgeBase.isVariable(other)) {
						Util.lookupOrCreate(varOrder, other, () -> varRankCounter++);
						if (!conjunctsForVar.get(other).isEmpty()) {
							loadVar(other);
						}
					}
				}
			}

			private IntConjunct peekInStack() {
				return stack.peekFirst();
			}

			private void pushToStack(IntConjunct c) {
				stack.addFirst(c);
			}

			private IntConjunct popFromStack() {
				return stack.removeFirst();
			}

			private void pushToOutput(IntConjunct c) {
				int subject = c.getSubject();
				int object = c.getObject();
				if (KnowledgeBase.isVariable(subject) && !varOrder.containsKey(subject)) {
					varOrder.put(subject, varRankCounter++);
				}
				if (KnowledgeBase.isVariable(object) && !varOrder.containsKey(object)) {
					varOrder.put(object, varRankCounter++);
				}
				out.add(c);
			}

			private void unloadStack() {
				List<IntConjunct> l = new ArrayList<>(stack);
				/*
				 * Need to push conjuncts from stack onto output in an order that preserves
				 * stack-like invariant of schema.
				 */
				l.sort(new Comparator<IntConjunct>() {

					@Override
					public int compare(IntConjunct l, IntConjunct r) {
						Integer l1 = getRank(l.getSubject());
						Integer l2 = getRank(l.getObject());
						Integer r1 = getRank(r.getSubject());
						Integer r2 = getRank(r.getObject());

						/*
						 * If lmin is less than rmin, then l has a variable that appears earlier in the
						 * schema, and so l must come after r.
						 */
						int lmin = Math.min(l1, l2);
						int rmin = Math.min(r1, r2);
						if (lmin < rmin) {
							return 1;
						} else if (lmin > rmin) {
							return -1;
						}

						/*
						 * Compare on second attribute if necessary.
						 */
						int lmax = Math.max(l1, l2);
						int rmax = Math.max(r1, r2);
						return Integer.compare(rmax, lmax);
					}

					private int getRank(int var) {
						if (KnowledgeBase.isVariable(var)) {
							return Util.lookupOrCreate(varOrder, var, () -> varRankCounter++);
						}
						return Integer.MAX_VALUE;
					}

				});
				out.addAll(l);
			}

			private TIntIterator setup(Collection<IntConjunct> conjuncts) {
				TIntList varList = new TIntArrayList();
				TIntSet seen = new TIntHashSet();
				for (IntConjunct c : conjuncts) {
					int subject = c.getSubject();
					boolean subjectIsVar = KnowledgeBase.isVariable(subject);
					if (subjectIsVar) {
						Util.lookupOrCreate(conjunctsForVar, subject, () -> new HashSet<>()).add(c);
						if (!seen.contains(subject)) {
							varList.add(subject);
							seen.add(subject);
						}
					}
					int object = c.getObject();
					boolean objectIsVar = KnowledgeBase.isVariable(object);
					if (objectIsVar) {
						Util.lookupOrCreate(conjunctsForVar, object, () -> new HashSet<>()).add(c);
						if (!seen.contains(object)) {
							varList.add(object);
							seen.add(object);
						}
					}
					if (!subjectIsVar && !objectIsVar) {
						out.add(c);
					}
				}
				varList.reverse();
				return varList.iterator();
			}

		}

		public List<IntConjunct> intizeConjuncts(Iterable<Conjunct> conjuncts) {
			List<IntConjunct> l = new ArrayList<>();
			for (Conjunct c : conjuncts) {
				IntConjunct ic = intizeConjunct(c);
				if (ic == null) {
					return null;
				}
				l.add(ic);
			}
			return l;
		}

		public IntConjunct intizeConjunct(Conjunct c) {
			Integer subject = intizeTerm(c.getSubject());
			Integer object = intizeTerm(c.getObject());
			if (subject == null || object == null) {
				return null;
			}
			String pred = c.getPredicate();
			if (!relations.containsKey(pred)) {
				return null;
			}
			return new IntConjunct(pred, subject, object);
		}

		public Integer intizeTerm(String t) {
			if (isVariable(t)) {
				return Util.lookupOrCreate(varMap, t, () -> varCounter--);
			}
			if (!constants.containsKey(t)) {
				return null;
			}
			return constants.get(t);
		}

		private boolean isVariable(String s) {
			return Util.lookupOrCreate(varSet, s, () -> Parser.isVariable(s));
		}

		private class Block {

			private final TIntList schema;
			private final TIntList rel;
			private final TLongList cnts;
			private final int arity;

			public Block(TIntList schema, TIntList rel, TLongList cnts) {
				this.schema = schema;
				this.rel = rel;
				this.cnts = cnts;
				this.arity = schema.size();
				assert schema.size() == 0 || rel.size() % schema.size() == 0;
				assert schema.size() == 0 || rel.size() / schema.size() == cnts.size();
			}

			public Block(TIntList schema) {
				this(schema, new TIntArrayList(), new TLongArrayList());
			}

			public void addTuple(int[] tuple, long cnt) {
				assert tuple.length == arity;
				assert cnt >= 0;
				if (getCardinality() == 0) {
					addTuple2(tuple, cnt);
					return;
				}
				int prevIdx = (getCardinality() - 1) * arity;
				for (int i = 0; i < arity; i++) {
					if (rel.get(prevIdx + i) != tuple[i]) {
						addTuple2(tuple, cnt);
						return;
					}
				}
				int last = cnts.size() - 1;
				cnts.set(last, cnt + cnts.get(last));
			}

			private void addTuple2(int[] tuple, long cnt) {
				rel.addAll(tuple);
				cnts.add(cnt);
			}

			public int getArity() {
				return arity;
			}

			public long readTuple(int idx, int[] tuple) {
				assert tuple.length == arity;
				int offset = idx * arity;
				for (int i = 0; i < arity; i++) {
					tuple[i] = rel.get(offset + i);
				}
				return cnts.get(idx);
			}

			public TIntList getSchema() {
				return schema;
			}

			public int getCardinality() {
				return cnts.size();
			}

		}

	}

	///////////////////////////////////////////////////////////////////////////

	private class IntConjunct {

		private final String predicate;
		private final int subject;
		private final int object;

		public IntConjunct(String predicate, int subject, int object) {
			this.predicate = predicate;
			this.subject = subject;
			this.object = object;
		}

		public String getPredicate() {
			return predicate;
		}

		public int getSubject() {
			return subject;
		}

		public int getObject() {
			return object;
		}

		@Override
		public String toString() {
			return predicate + "(" + subject + ", " + object + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + object;
			result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
			result = prime * result + subject;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntConjunct other = (IntConjunct) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (object != other.object)
				return false;
			if (predicate == null) {
				if (other.predicate != null)
					return false;
			} else if (!predicate.equals(other.predicate))
				return false;
			if (subject != other.subject)
				return false;
			return true;
		}

		private KnowledgeBase getOuterType() {
			return KnowledgeBase.this;
		}

	}

	private static boolean isVariable(int i) {
		return i < 0;
	}

	public static class IndexedPredicate {

		private final String name;
		private final int[] all;
		private final Entry[] forwardIndex;
		private final Entry[] backwardIndex;
		private final int[] identical;

		private IndexedPredicate(String name, int[] all, Entry[] forwardIndex, Entry[] backwardIndex, int[] identical) {
			this.name = name;
			this.all = all;
			this.forwardIndex = forwardIndex;
			this.backwardIndex = backwardIndex;
			this.identical = identical;
		}

		private static final int[] empty = new int[0];

		public int[] query(int subject, int object) {
			if (isVariable(subject)) {
				if (subject == object) {
					return queryIdentical();
				} else if (isVariable(object)) {
					return queryAll();
				} else {
					return queryByObject(object);
				}
			} else if (isVariable(object)) {
				return queryBySubject(subject);
			} else {
				return queryBySubjectAndObject(subject, object);
			}
		}

		private int[] queryBySubject(int subject) {
			return queryByKey(forwardIndex, subject);
		}

		private int[] queryByObject(int object) {
			return queryByKey(backwardIndex, object);
		}

		private int[] queryByKey(Entry[] index, int key) {
			Entry e = getEntry(index, key);
			if (e == null) {
				return null;
			}
			return e.getVals();
		}

		private int[] queryBySubjectAndObject(int subject, int object) {
			Entry e = getEntry(forwardIndex, subject);
			if (e == null) {
				return null;
			}
			return e.hasVal(object) ? empty : null;
		}

		private int[] queryIdentical() {
			return identical.length == 0 ? null : identical;
		}

		private int[] queryAll() {
			return all.length == 0 ? null : all;
		}

		public int getIndexKeySetSize(int subject, int object) {
			if (isVariable(subject)) {
				if (subject == object) {
					return identical.length;
				}
				if (isVariable(object)) {
					return all.length / 2;
				}
				return backwardIndex.length;
			}
			if (isVariable(object)) {
				return forwardIndex.length;
			}
			return 0;
		}

		private Entry getEntry(Entry[] es, int key) {
			int idx = binSearch(es, key);
			if (idx >= 0) {
				return es[idx];
			}
			return null;
		}
		
		private int binSearch(Entry[] es, int key) {
			int low = 0;
			int hi = es.length;
			while (low < hi) {
				int mid = (hi - low) / 2 + low;
				int key2 = es[mid].key;
				if (key < key2) {
					hi = mid;
				} else if (key > key2) {
					low = mid + 1;
				} else {
					return mid;
				}
			}
			return -1;
		}

		@Override
		public String toString() {
			return name + Arrays.toString(all);
		}

		public static class Builder {

			private final String name;

			public Builder(String name) {
				this.name = name;
			}

			TIntObjectMap<TIntList> forwardIndex = new TIntObjectHashMap<>();
			TIntObjectMap<TIntList> backwardIndex = new TIntObjectHashMap<>();
			TIntList all = new TIntArrayList();
			TIntList identical = new TIntArrayList();

			public void addEntry(int subject, int object) {
				all.add(subject);
				all.add(object);
				if (subject == object) {
					identical.add(subject);
				}
				addEntry(subject, object, forwardIndex);
				addEntry(object, subject, backwardIndex);
			}

			private void addEntry(int subject, int object, TIntObjectMap<TIntList> index) {
				Util.lookupOrCreate(index, subject, () -> new TIntArrayList()).add(object);
			}

			public IndexedPredicate build() {
				int[] allArr = all.toArray();
				int[] idArr = identical.toArray();
				return new IndexedPredicate(name, allArr, buildIndex(forwardIndex), buildIndex(backwardIndex), idArr);
			}

			private Entry[] buildIndex(TIntObjectMap<TIntList> index) {
				Entry[] es = new Entry[index.size()];
				int i = 0;
				for (TIntIterator it = index.keySet().iterator(); it.hasNext();) {
					int key = it.next();
					TIntList v = index.get(key);
					es[i] = new Entry(key, v.toArray());
					i++;
				}
				Arrays.sort(es, Entry.compareByKey);
				return es;
			}

		}

		private static class Entry {

			private final int key;
			private final int[] vals;

			public Entry(int key, int[] vals) {
				this.key = key;
				this.vals = vals;
			}

			public static final Comparator<Entry> compareByKey = new Comparator<Entry>() {

				@Override
				public int compare(Entry o1, Entry o2) {
					return Integer.compare(o1.key, o2.key);
				}

			};

			int[] getVals() {
				return vals;
			}

			boolean hasVal(int val) {
				return Arrays.binarySearch(vals, val) >= 0;
			}

			@Override
			public String toString() {
				return "(" + key + "," + Arrays.toString(vals) + ")";
			}

		}

	}

}
