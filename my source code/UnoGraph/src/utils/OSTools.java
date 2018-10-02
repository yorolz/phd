package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSTools {

	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static void main(String[] args){
		System.out.println(getNumberOfCPUCores());
	}

	public static int getNumberOfCPUCores() {
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
		if (OSTools.isUnix()) {
			return numberOfCores * sockets;
		}
		return numberOfCores;
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
}