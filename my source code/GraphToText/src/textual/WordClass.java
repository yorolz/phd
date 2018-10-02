package textual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class WordClass {
	
	public static final int UNDEFINED=0;
	public static final int NOUN=1;
	public static final int VERB=2;
	
	public static Object2IntOpenHashMap<String> readFile(File file) throws IOException {
		Object2IntOpenHashMap<String> wordClasses = new Object2IntOpenHashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while (br.ready()) {
			String line = br.readLine().trim();
			if (!line.isEmpty()) {
				String[] tokens = line.split("\t");
				String name = tokens[0];
				String type = tokens[1];
				int wc = WordClass.UNDEFINED;
				switch (type) {
				case "noun":
					wc = WordClass.NOUN;
					break;
				case "verb":
					wc = WordClass.VERB;
					break;
				}
				wordClasses.put(name, wc);
			}
		}
		br.close();
		return wordClasses;
	}

	public static Object2IntOpenHashMap<String> readFile(String filename) throws IOException {
		return readFile(new File(filename));
	}
}
