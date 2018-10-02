package ck.textstorm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Launches a new process in the given path, with the given arguments. Taken from
 * http://stackoverflow.com/questions/1410741/want-to-invoke-a-linux-shell-command-from-java
 * 
 * @author João Carlos Ferreira Gonçalves - jcfgonc@gmail.com
 *
 */
public class ProcessLauncher {

	/**
	 * Launches this process, blocking until it completes. When finished, it returns the process' exit value.
	 * 
	 * 
	 * @param path
	 *            The path were to run the process.
	 * @param arguments
	 *            The list of arguments to the process. The first element is the name of the process. The remaining are its arguments.
	 * @return
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int launch(String path, List<String> arguments) throws IOException, InterruptedException {
		// Run macro on target
		ProcessBuilder pb = new ProcessBuilder(arguments);
		pb.directory(new File(path));
		pb.redirectErrorStream(true);
		System.out.println("Starting process: " + pb.command() + " at " + pb.directory());
		Process process = pb.start();

		// for building the process' output
		StringBuilder out = new StringBuilder();
		// for reading from the process' output
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null, previous = null;
		while ((line = br.readLine()) != null)
			if (!line.equals(previous)) {
				previous = line;
				out.append(line).append('\n');
			}

		// Check result
		int processResult = process.waitFor();
		System.err.println(out.toString());
		return processResult;
	}

}
