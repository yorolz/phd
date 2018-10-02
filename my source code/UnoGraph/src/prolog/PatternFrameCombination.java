package prolog;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import alice.tuprolog.Term;
import structures.MapOfList;

public class PatternFrameCombination {
	private MapOfList<Integer, Term> clausesPerLevel;

	public PatternFrameCombination() {
		clausesPerLevel = new MapOfList<>();
	}

	public void addClause(Term clause, int level) {
		clausesPerLevel.put(level, clause);
	}

	public Term getClauseAtHighestLevel() {
		int maximumLevel = this.getMaximumLevel();
		List<Term> clausesAtLevel = getClausesAtLevel(maximumLevel);
		return clausesAtLevel.iterator().next();
	}

	/**
	 * Returns all the combinations of predicates at the given level.
	 * 
	 * @param level
	 *            > 1, as it represents combinations of n (level) predicates.
	 * @return
	 */
	public List<Term> getClausesAtLevel(int level) {
		return clausesPerLevel.get(level);
	}

	public Set<Integer> getLevels() {
		return clausesPerLevel.keySet();
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
		return clausesPerLevel.keySet();
	}

	@Override
	public String toString() {
		return clausesPerLevel.toString();
	}

}
