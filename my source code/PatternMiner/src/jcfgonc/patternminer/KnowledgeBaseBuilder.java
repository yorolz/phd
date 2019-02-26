package jcfgonc.patternminer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.IndexedPredicate;
import com.githhub.aaronbembenek.querykb.Util;

import gnu.trove.map.hash.TObjectIntHashMap;
import graph.StringEdge;
import graph.StringGraph;

/**
 * Custom KnowledgeBase building class receiving individual facts.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class KnowledgeBaseBuilder {

	private AtomicInteger counter;
	private TObjectIntHashMap<String> constants;
	private Map<String, IndexedPredicate.Builder> builders;
	private boolean built;

	public KnowledgeBaseBuilder() {
		this.built = false;
		this.counter = new AtomicInteger(1);
		this.constants = new TObjectIntHashMap<>();
		this.builders = new HashMap<>();
	}

	public KnowledgeBase build() {
		Map<String, IndexedPredicate> relations = new ConcurrentHashMap<>();
		builders.entrySet().parallelStream().forEach(e -> relations.put(e.getKey(), e.getValue().build()));
		this.built = true;
		return new KnowledgeBase(constants, relations);
	}

	public void addFact(String predicate, String subject, String object) {
		if (this.built)
			throw new RuntimeException("KnowledgeBase already built");
		int sub = Util.lookupOrCreate(constants, subject, () -> counter.getAndIncrement());
		int obj = Util.lookupOrCreate(constants, object, () -> counter.getAndIncrement());
		// assert counter.get() >= 0; //guaranteed (init to 1 and unless an integer overflow occurs)
		Util.lookupOrCreate(builders, predicate, () -> new IndexedPredicate.Builder(predicate)).addEntry(sub, obj);
	}

	public void addFact(StringEdge edge) {
		addFact(edge.getLabel(), edge.getSource(), edge.getTarget());
	}

	public void addFacts(Set<StringEdge> facts) {
		for (StringEdge fact : facts) {
			this.addFact(fact);
		}
	}

	public void addFacts(StringGraph facts) {
		addFacts(facts.edgeSet());
	}
}
