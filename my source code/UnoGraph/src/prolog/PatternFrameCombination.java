package prolog;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.githhub.aaronbembenek.querykb.Query;

import structures.MapOfList;

public class PatternFrameCombination {
	private MapOfList<Integer, Query> queriesPerLevel;

	public PatternFrameCombination() {
		queriesPerLevel = new MapOfList<>();
	}

	public void addQuery(Query q, int level) {
		queriesPerLevel.put(level, q);
	}

	public Query getQueryAtHighestLevel() {
		int maximumLevel = this.getMaximumLevel();
		List<Query> queriesAtLevel = getQueriesAtLevel(maximumLevel);
		return queriesAtLevel.iterator().next();
	}

	/**
	 * Returns all the combinations of predicates at the given level.
	 * 
	 * @param level > 1, as it represents combinations of n (level) predicates.
	 * @return
	 */
	public List<Query> getQueriesAtLevel(int level) {
		return queriesPerLevel.get(level);
	}

	public Set<Integer> getLevels() {
		return queriesPerLevel.keySet();
	}

	/**
	 * This actually corresponds to number of predicates in the frame.
	 * 
	 * @return
	 */
	public int getMaximumLevel() {
		return Collections.max(keySet()).intValue();
	}

	public int getMinimumLevel() {
		return Collections.min(keySet()).intValue();
	}

	public Set<Integer> keySet() {
		return queriesPerLevel.keySet();
	}

	@Override
	public String toString() {
		return queriesPerLevel.toString();
	}

}
