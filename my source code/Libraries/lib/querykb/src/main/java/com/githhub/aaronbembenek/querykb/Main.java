package com.githhub.aaronbembenek.querykb;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Parser;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

public class Main {

	private static class KBWrapper {

		private final KnowledgeBase kb;
		private int blockSize = 4096;
		private int parallelLimit = 1; 
		private long solutionLimit = Long.MAX_VALUE;

		public KBWrapper(KnowledgeBase kb) {
			this.kb = kb;
		}

		public long count(Query q) {
			return kb.count(q, blockSize, parallelLimit, solutionLimit);
		}

		public void setBlockSize(int blockSize) {
			this.blockSize = blockSize;
		}

		public void setParallelLimit(int parallelLimit) {
			this.parallelLimit = parallelLimit;
		}

		public void setSolutionLimit(long solutionLimit) {
			this.solutionLimit = solutionLimit;
		}

	}

	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		System.out.println("Welcome to the querykb REPL.\n");
		if (args.length != 1) {
			printUsage();
			System.out.println("\nExiting...");
			return;
		}
		KBWrapper kb = new KBWrapper(loadKB(args[0]));
		System.out.println();
		printOptions();
		System.out.println();

		Scanner sc = new Scanner(System.in);
		printPrompt();
		while (sc.hasNextLine()) {
			String s = sc.nextLine();
			try {
				handleInput(s, kb);
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
			}
			System.out.println();
			printPrompt();
		}
		sc.close();
	}

	private static void handleInput(String s, KBWrapper kb) throws Exception {
		Tokenizer t = new Tokenizer(new StringReader(s));
		while (t.hasNext()) {
			if (t.peek().equals("set")) {
				t.next();
				String option = t.next();
				switch (option) {
				case "blockSize":
					t.consume("=");
					int size = Integer.parseInt(t.next());
					kb.setBlockSize(size);
					t.consume(".");
					break;
				case "parallelLimit":
					t.consume("=");
					int parallelLimit = Integer.parseInt(t.next());
					kb.setParallelLimit(parallelLimit);
					t.consume(".");
					break;
				case "solutionLimit":
					t.consume("=");
					long solutionLimit = Long.parseLong(t.next());
					kb.setSolutionLimit(solutionLimit);
					t.consume(".");
					break;
				default:
					throw new ParseException("Unrecognized option " + option);
				}
			} else {
				long start = System.currentTimeMillis();
				System.out.print("Making query... ");
				long res = kb.count(Parser.parseQuery(t));
				long end = System.currentTimeMillis();
				System.out.println((end - start) / 1000.0 + " seconds, " + res + " solutions.");
			}
		}
	}

	private static void printOptions() {
		System.out.println("Queries are of the form \":- p(X,Y), q(Y,Z).\".");
		System.out.println("\nOther commands are:");
		System.out.println("\t\"set blockSize=[N].\"");
		System.out.println("\t\tmax size of a subtask (default=4096 tuples)");
		System.out.println("\t\"set parallelLimit=[N].\"");
		System.out.println("\t\tmax number of subtasks to run in parallel (default=1)");
		System.out.println("\t\"set solutionLimit=[N].\"");
		System.out.println("\t\tmax number of solutions to find (default=Long.MAX_VALUE)");
	}

	private static void printPrompt() {
		System.out.print("> ");
	}

	private static void printUsage() {
		String msg = "You need to supply, as an argument, the filename of a KB to load. "
				+ "The KB should be a list of comma-separated triples of the form \"subject,predicate,object\".";
		int cnt = 0;
		for (String s : msg.split(" ")) {
			if (cnt + s.length() >= 80) {
				System.out.println();
				cnt = 0;
			}
			if (cnt != 0) {
				System.out.print(" ");
			}
			System.out.print(s);
			cnt += s.length();
		}
		System.out.println();
	}

	private static KnowledgeBase loadKB(String filename) throws FileNotFoundException, IOException, ParseException {
		long start = System.currentTimeMillis();
		System.out.print("Loading knowledge base " + filename + "... ");
		KnowledgeBase kb = KnowledgeBase.fromCSVTriples(new FileReader(filename));
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000.0 + " seconds.");
		return kb;
	}

}
