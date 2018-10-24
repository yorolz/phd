package com.githhub.aaronbembenek.querykb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestCountNaive.GroundQueries.class, TestCountNaive.NonGroundQueries.class })
public class TestCountNaive {

	public static class GroundQueries extends AbstractTestCountGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.countNaive(q);
		}

	}

	public static class NonGroundQueries extends AbstractTestCountNonGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.countNaive(q);
		}

	}

}
