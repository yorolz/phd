package com.githhub.aaronbembenek.querykb.jcfgonc;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Util;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.IndexedPredicate;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.IndexedPredicate.Builder;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.TupleReader;
import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

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
		// TODO: replace by normal map and check performance
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
}
