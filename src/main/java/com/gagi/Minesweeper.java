package com.gagi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "minesweeper", aliases = { "ms" }, mixinStandardHelpOptions = true)
public class Minesweeper implements Runnable {

	private String MINE = "M";
	private String SPACE = " ";

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Command(name = "new")
	public void newGame() {
		System.out.println("starting new game");
		List<Integer> mines = setUpMines();
		String[][] grid = setUpGrid(mines);
		setUpMineCounts(grid);
		String oneLine = putInOneLine(grid);
		try {
			String userPerspective = initUserPerspective();
			userPerspective += "\n";
			writeLineInFile(oneLine, false);
			writeLineInFile(userPerspective, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Command(name = "show")
	public void show() {
		System.out.println("showing game");
		String gameInOneLine;
		String userPerspective;
		try {
			gameInOneLine = getGameFromFile();
			userPerspective = getUserPesrpectiveFromFile();
			String[][] grid = getGridFromOneLineString(userPerspective);
			printGrid(grid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Command(name = "opentile")
	public void opentile(@Parameters(index = "0") int row, @Parameters(index = "1") int column) {
		System.out.println("opening tile " + row + " " + column);
		try {
			String game = getGameFromFile();
			String userPerspective = getUserPesrpectiveFromFile();
			String[][] grid = getGridFromOneLineString(game);
			String[][] gridUserPerspective = getGridFromOneLineString(userPerspective);
			String tileContent = grid[row][column];
			if("M".equals(tileContent)) {
				System.out.println("kabooom, game ended");
				return;
			}
			gridUserPerspective[row][column] = tileContent;
			if(" ".equals(tileContent)) {
				List<String> coords = new ArrayList<>();
				coords.add(row+"x"+column);
				Set<String> emptySpaces = findAllAdjecantEmptySpaces(grid, coords, new HashSet<>(), new HashSet<>());
				for(String es : emptySpaces) {
					String[] esCoords = es.split("x");
					int esRow = Integer.parseInt(esCoords[0]);
					int esColumn = Integer.parseInt(esCoords[1]);
					gridUserPerspective[esRow][esColumn] = grid[esRow][esColumn];
				}
			}
			String gameLine = putInOneLine(grid);
			String userPerspectiveLine = putInOneLine(gridUserPerspective);
			writeLineInFile(gameLine, false);
			writeLineInFile(userPerspectiveLine, true);
			if(isSolved(grid, gridUserPerspective)) {
				System.out.println("CONGRATULATIONS, YOU FOUND ALL THE MINES!");
			}
			printGrid(gridUserPerspective);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Command(name = "marktile")
	public void marktile() {
		System.out.println("marking a tile");
	}

	private List<Integer> setUpMines() {
		List<Integer> numbers = IntStream.range(0, 1000).boxed().collect(Collectors.toList());
		Collections.shuffle(numbers);
		return numbers.subList(0, 100);
	}

	private String[][] setUpGrid(List<Integer> mines) {
		String[][] grid = new String[20][50];
		for (Integer mine : mines) {
			int row = mine / 50;
			int col = mine % 50;
			grid[row][col] = MINE;
		}
		
		for(int row = 0; row < grid.length; row++) {
			for(int col = 0; col < grid[0].length; col++) {
				if (grid[row][col] == null) grid[row][col] = SPACE;
			}
		}
		return grid;
	}

	private void setUpMineCounts(String[][] grid) {
		int north, east, south, west, northeast, southeast, southwest, northwest = 0;
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				if (grid[row][col].equals(MINE))
					continue;
				west = 0;
				if (col > 0 && grid[row][col-1].equals(MINE))
					west = 1;
				east = 0;
				if (col < grid[0].length - 1 && grid[row][col+1].equals(MINE))
					east = 1;
				north = 0;
				if ((row > 0) && grid[row - 1][col].equals(MINE)) {
					north = 1;
				}
				south = 0;
				if (row < grid.length - 1 && grid[row + 1][col].equals(MINE))
					south = 1;
				northeast = 0;
				if (row > 0 && col < grid[0].length - 1 && grid[row - 1][col + 1].equals(MINE))
					northeast = 1;
				southeast = 0;
				if (row < grid.length - 1 && col < grid[0].length - 1 && grid[row + 1][col + 1].equals(MINE))
					southeast = 1;
				southwest = 0;
				if (row < grid.length - 1 && col > 0 && grid[row + 1][col - 1].equals(MINE))
					southwest = 1;
				northwest = 0;
				if (row > 0 && col > 0 && grid[row - 1][col - 1].equals(MINE))
					northwest = 1;
				int count = north + east + south + west
						+ northeast + southeast + southwest + northwest;
				System.out.println(row + " " + col + " " + north + " " + east + " " + south + " " + west + " " + northeast + " " + southeast + " " + southwest + " " + northwest + " " + count);
				if(count > 0) grid[row][col] = String.valueOf(count);
			}
		}
	}

	private String putInOneLine(String[][] grid) {
		String result = "";
		for (String[] row : grid) {
			for (String col : row) {
				result = result + col + ",";
			}
		}
		return result+"\n";
	}

	private void writeLineInFile(String game, boolean append) throws IOException {
		Path fileName = Path.of("game.txt");
		if(append) {
			Files.writeString(fileName, game, StandardOpenOption.APPEND);
		}
		else {
			Files.writeString(fileName, game);
		}
	}
	
	private String getGameFromFile() throws IOException {
		Path path = Paths.get("game.txt");
	    String game = Files.readAllLines(path).get(0);
	    return game;
	}
	
	private String[][] getGridFromOneLineString(String game) {
		String[] tiles = game.split(",");
		String[][] grid = new String[20][50];
		for(int i = 0; i < 20; i++) {
			for(int j = 0; j < 50; j++) {
				int k = 50*i + j;
				grid[i][j] = tiles[k];
			}
		}
		return grid;
	}
	
	private void printGrid(String[][] grid) {
		System.out.println("        0         1         2         3         4");
		System.out.println("   01234567890123456789012345678901234567890123456789");
		int i = 0;
		for(String[] row : grid) {
			if(i < 10) {
				System.out.print(" " + i + " ");
			}
			else {
				System.out.print(i + " ");
			}
			i++;
			for(String column : row) {
				System.out.print(column);
			}
			System.out.println();
		}
	}
	
	private String getUserPesrpectiveFromFile() throws IOException {
		Path path = Paths.get("game.txt");
	    String game = Files.readAllLines(path).get(1);
	    return game;	
	}
	
	private String initUserPerspective() {
		return "H,".repeat(1000);
	}
	
	private boolean isGameOver(String gameInOneLine, String userPerspective) {
		return userPerspective.contains("M");
	}
	
	private Set<String> findAllAdjecantEmptySpaces(String[][] grid, List<String> candidates, Set<String> result, Set<String> visited) {
		if(candidates.isEmpty()) {
			return result;
		}
		String c = candidates.remove(0);
		visited.add(c);
		String[] coordinates = c.split("x");
		int row = Integer.parseInt(coordinates[0]);
		int column = Integer.parseInt(coordinates[1]);
		if(!" ".equals(grid[row][column])) {
			return findAllAdjecantEmptySpaces(grid, candidates, result, visited);
		}
		result.add(c);
//		north
		if(row > 0) {
			result.add((row - 1) + "x" + column);
			if(" ".equals(grid[row - 1][column])) {
				candidates.add((row - 1) + "x" + column);
			}
		}
//		east
		if(column < 49) {
			result.add(row + "x" + (column + 1));
			if(" ".equals(grid[row][column + 1])) {
				candidates.add(row + "x" + (column + 1));
			}
		}
//		south
		if(row < 19) {
			result.add((row + 1) + "x" + column);
			if(" ".equals(grid[row + 1][column])) {
				candidates.add((row + 1) + "x" + column);
			}
		}
//		west
		if(column > 0) {
			result.add(row + "x" + (column - 1));
			if(" ".equals(grid[row][column - 1])) {
				candidates.add(row + "x" + (column - 1));
			}
		}
//		northeast
		if(row > 0 && column < 49) {
			result.add((row - 1) + "x" + (column + 1));
			if(" ".equals(grid[row - 1][column + 1])) {
				candidates.add((row - 1) + "x" + (column + 1));
			}
		}
//		southeast
		if(row < 19 && column < 49) {
			result.add((row + 1) + "x" + (column + 1));
			if(" ".equals(grid[row + 1][column + 1])) {
				candidates.add((row + 1) + "x" + (column + 1));
			}
		}
//		southwest
		if(row < 19 && column > 0) {
			result.add((row + 1) + "x" + (column - 1));
			if(" ".equals(grid[row + 1][column - 1])) {
				candidates.add((row + 1) + "x" + (column - 1));
			}
		}
//		northwest
		if(row > 0 && column > 0) {
			result.add((row - 1) + "x" + (column - 1));
			if(" ".equals(grid[row - 1][column - 1])) {
				candidates.add((row - 1) + "x" + (column - 1));
			}
		}
		List<String> nextCandidates = candidates.stream().distinct().collect(Collectors.toList());
		nextCandidates.removeAll(visited);
		return findAllAdjecantEmptySpaces(grid, nextCandidates, result, visited);
	}
	
	private boolean isSolved(String[][] grid, String[][] userPerspective) {
		for(int row = 0; row < userPerspective.length; row++) {
			for(int column = 0; column < userPerspective[0].length; column++) {
				if("H".equals(userPerspective[row][column]) && !"M".equals(grid[row][column])) {
					return false;
				}
			}
		}
		return true;
	}
}
