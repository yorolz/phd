package com.githhub.aaronbembenek.querykb;

import java.math.BigInteger;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestCountNaive.GroundQueries.class, TestCountNaive.NonGroundQueries.class })
public class TestCountNaive {

	public static class GroundQueries extends AbstractTestCountGroundQueries {

		@Override
		protected BigInteger query(KnowledgeBase kb, Query q) {
			return BigInteger.valueOf(kb.countNaive(q));
		}

	}

	public static class NonGroundQueries extends AbstractTestCountNonGroundQueries {

		@Override
		protected BigInteger query(KnowledgeBase kb, Query q) {
			return BigInteger.valueOf(kb.countNaive(q));
		}

	}

}
