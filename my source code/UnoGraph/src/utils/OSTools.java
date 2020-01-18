package utils;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSTools {
	public static void main(String[] args) throws NumberFormatException, IOException {
		System.out.println(OSTools.hasHyperThreading());
	}

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static int getCoreCount() {
		String command = "";
		if (OSTools.isMac()) {
			command = "sysctl -n machdep.cpu.core_count";
		} else if (OSTools.isUnix()) {
			command = "lscpu";
		} else if (OSTools.isWindows()) {
			command = "cmd /C WMIC CPU Get /Format:List";
		}
		Process process = null;
		int numberOfCores = 0;
		int sockets = 0;
		System.out.print("querying OS for CPU number of cores...");
		try {
			if (OSTools.isMac()) {
				String[] cmd = { "/bin/sh", "-c", command };
				process = Runtime.getRuntime().exec(cmd);
			} else {
				process = Runtime.getRuntime().exec(command);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		try {
			while ((line = reader.readLine()) != null) {
				if (OSTools.isMac()) {
					numberOfCores = line.length() > 0 ? Integer.parseInt(line) : 0;
				} else if (OSTools.isUnix()) {
					if (line.contains("Core(s) per socket:")) {
						numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
					}
					if (line.contains("Socket(s):")) {
						sockets = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
					}
				} else if (OSTools.isWindows()) {
					if (line.contains("NumberOfCores")) {
						numberOfCores = Integer.parseInt(line.split("=")[1]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(" done.");
		if (OSTools.isUnix()) {
			return numberOfCores * sockets;
		}
		return numberOfCores;
	}

	public static boolean hasHyperThreading() throws NumberFormatException, IOException {
		assert OSTools.isWindows();
		String command = "cmd /C wmic CPU Get NumberOfCores,NumberOfLogicalProcessors /Format:List";
		System.out.print("querying OS for CPU Hyper-Threading...");
		Process process = Runtime.getRuntime().exec(command);

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		int numberOfCores = 0;
		int NumberOfLogicalProcessors = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().isEmpty())
				continue;
			String[] split;
			if (line.startsWith("NumberOfCores")) {
				split = line.split("=");
				numberOfCores += Integer.parseInt(split[1]);
			} else if (line.startsWith("NumberOfLogicalProcessors")) {
				split = line.split("=");
				NumberOfLogicalProcessors += Integer.parseInt(split[1]);
			}
		}
		System.out.println(" done.");
		boolean ht = NumberOfLogicalProcessors > numberOfCores;
		return ht;
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static int getDPI() {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	/**
	 * Only works for windowze as it is my OS and I don't know how to convert to other OSes.
	 * 
	 * @return
	 */
	public static double getScreenScale() {
		if (isWindows()) {
			double dpi = (double) getDPI();
			double defaultDPI = 96;
			double scale = dpi / defaultDPI;
			return scale;
		} else
			return 1; // yeah...
	}
}