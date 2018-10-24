package com.githhub.aaronbembenek.querykb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestCount.GroundQueriesSingletonBlockSequential.class,
		TestCount.GroundQueriesSingletonBlockParallel.class, TestCount.GroundQueriesNonSingletonBlockSequential.class,
		TestCount.GroundQueriesNonSingletonBlockParallel.class,
		TestCount.NonGroundQueriesSingletonBlockSequential.class,
		TestCount.NonGroundQueriesSingletonBlockParallel.class,
		TestCount.NonGroundQueriesNonSingletonBlockSequential.class,
		TestCount.NonGroundQueriesNonSingletonBlockParallel.class, })
public class TestCount {

	public static class GroundQueriesSingletonBlockSequential extends AbstractTestCountGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 1, 1, Long.MAX_VALUE);
		}

	}

	public static class GroundQueriesSingletonBlockParallel extends AbstractTestCountGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 1, 4, Long.MAX_VALUE);
		}

	}

	public static class GroundQueriesNonSingletonBlockSequential extends AbstractTestCountGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 3, 1, Long.MAX_VALUE);
		}

	}

	public static class GroundQueriesNonSingletonBlockParallel extends AbstractTestCountGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 3, 4, Long.MAX_VALUE);
		}

	}

	public static class NonGroundQueriesSingletonBlockSequential extends AbstractTestCountNonGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 1, 1, Long.MAX_VALUE);
		}

	}

	public static class NonGroundQueriesSingletonBlockParallel extends AbstractTestCountNonGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 1, 4, Long.MAX_VALUE);
		}

	}

	public static class NonGroundQueriesNonSingletonBlockSequential extends AbstractTestCountNonGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 3, 1, Long.MAX_VALUE);
		}

	}

	public static class NonGroundQueriesNonSingletonBlockParallel extends AbstractTestCountNonGroundQueries {

		@Override
		protected long query(KnowledgeBase kb, Query q) {
			return kb.count(q, 3, 4, Long.MAX_VALUE);
		}

	}

}
