package jcfgonc.patternminer;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;
import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Parser;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.Ticker;

public class QueryKBTest {
	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, NoSuchFileException, IOException, ParseException {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String path = "../ConceptNet5/kb/conceptnet5v43_no_invalid_chars.csv";

		System.out.println("loading... " + path);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(path, inputSpace);
		inputSpace.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");

		System.out.println("vertices\t" + inputSpace.getVertexSet().size());
		System.out.println("edges   \t" + inputSpace.edgeSet().size());
		System.out.println("-------");

		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		ticker.resetTicker();
		System.out.println("kbb.addFacts(inputSpace)");
		kbb.addFacts(inputSpace);
		System.out.println("kbb.build()");
		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");

		Tokenizer t = new Tokenizer(new StringReader(":- isa(X2,X3),isa(X1,X0),synonym(X1,X3)."));
		// Tokenizer t = new Tokenizer(new StringReader(":- isa(X3,X0),isa(X3,X1),isa(X2,X1)."));
		Query q = Parser.parseQuery(t);
//		for (int blockSize = 1; blockSize < 8192; blockSize *= 2) {
			for (int parallelLimit = 0; parallelLimit < 8; parallelLimit++) {
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for (int i = 0; i < 20; i++) {
				ticker.resetTicker();
			//	 System.out.println("Making query: " + q);
				int blockSize=256;
				@SuppressWarnings("unused")
				BigInteger count = kb.count(q, blockSize, parallelLimit, 8000000); 
			//	 System.out.println("Found " + count + " solution(s).");
				double time = ticker.getElapsedTime();
				ds.addValue(time);
			//	 System.out.println("query took " + time + " s");
			}
			System.out.println(parallelLimit + "\t" + ds.getMin());
		}
	}
}
