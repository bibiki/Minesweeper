package com.gagi;

import picocli.CommandLine;

/**
 * Hello world!
 *
 */
public class App {
	
	public static void main(String[] args) {
//		args = new String[3];
////		args = new String[1];
////		args[0] = "new";
////		args[0] = "show";
////		args[0] = "opentile";
//		args[0] = "opentile";
//		args[1] = "11";
//		args[2] = "0";
		int exitCode = new CommandLine(new Minesweeper()).execute(args);
		System.out.println(exitCode);
	}
}
