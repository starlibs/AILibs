package ai.libs.jaicore.search.exampleproblems.racetrack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import ai.libs.jaicore.basic.FileUtil;

public class RacetrackReader {

	public RacetrackMDP read(final File file, final double successRate, final Random random, final boolean stopOnCrash) throws IOException {
		List<String> lines = FileUtil.readFileAsList(file);
		String[] dimensions = lines.remove(0).trim().split(",");
		int height = Integer.parseInt(dimensions[0]);
		int width = Integer.parseInt(dimensions[1]);
		if (height != lines.size()) {
			throw new IllegalArgumentException("The file specifies a height of " + height + " but defines a course of " + lines.size() + " lines.");
		}
		boolean[][] track = new boolean[width][height];
		boolean[][] start = new boolean[width][height];
		boolean[][] goal = new boolean[width][height];

		for (int y = 0; y < height; y++)  {
			String line = lines.get(height - 1 - y);
			for (int x = 0; x < width; x++) {
				char c = line.charAt(x);
				if (c == '.') {
					track[x][y] = true;
				}
				if (c == 'S') {
					track[x][y] = true;
					start[x][y] = true;
				}
				if (c == 'F') {
					track[x][y] = true;
					goal[x][y] = true;
				}
			}
		}
		return new RacetrackMDP(track, start, goal, successRate, random, stopOnCrash);
	}
}
