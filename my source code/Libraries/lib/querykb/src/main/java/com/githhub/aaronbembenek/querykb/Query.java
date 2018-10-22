package com.githhub.aaronbembenek.querykb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Query {

	private final List<Conjunct> conjuncts;
	
	private Query(List<Conjunct> conjuncts) {
		this.conjuncts = new ArrayList<>(conjuncts);
	}
	
	public static Query make(List<Conjunct> conjuncts) {
		return new Query(conjuncts);
	}
	
	
	public List<Conjunct> getConjuncts() {
		return Collections.unmodifiableList(conjuncts);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(":- ");
		for (Iterator<Conjunct> it = conjuncts.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(".");
		return sb.toString();
	}

}
