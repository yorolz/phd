package com.githhub.aaronbembenek.querykb;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;

import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Parser;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

public class Example {

	public static void main(String[] args) throws IOException, ParseException {
		// Load the knowledge base. A KnowledgeBase can be loaded from
		// comma-separated triples (where each triple is separated by
		// whitespace)...
		String data = "fluffy,isa,cat rover,isa,dog goldie,isa,dog";
		KnowledgeBase kb = KnowledgeBase.fromCSVTriples(new StringReader(data));
		// ...or from Datalog-style facts.
		data = "isa(fluffy, cat). isa(rover, dog). isa(goldie, dog).";
		kb = KnowledgeBase.fromDatalogFacts(new StringReader(data));
		
		// Create a query. You can do this by hand...
		Query q = Query.make(Arrays.asList(Conjunct.make("isa", "X", "dog")));
		// ...or by turning it into a string and parsing it.
		Tokenizer t = new Tokenizer(new StringReader(":- isa(X, dog)."));
		q = Parser.parseQuery(t);
	
		// Make the query.
		System.out.println("Making query: " + q);
		BigInteger count = kb.count(q);
		System.out.println("Found " + count + " solution(s).");
	}
	
}
