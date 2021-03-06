package com.githhub.aaronbembenek.querykb;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;

import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Parser;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

public abstract class AbstractTestCount {

	private final String data;
	
	public AbstractTestCount(String data) {
		this.data = data;
	}
	
	protected BigInteger query(String query) throws IOException, ParseException {
		KnowledgeBase kb = KnowledgeBase.fromDatalogFacts(new StringReader(data));
		Tokenizer t = new Tokenizer(new StringReader(query));
		return query(kb, Parser.parseQuery(t));
	}

	protected abstract BigInteger query(KnowledgeBase kb, Query q);
}
