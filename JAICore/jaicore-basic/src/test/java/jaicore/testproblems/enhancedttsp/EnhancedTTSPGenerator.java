package jaicore.testproblems.enhancedttsp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EnhancedTTSPGenerator {
	public EnhancedTTSP generate(final int n, final int maxDistance) {

		/* create TTSP problem */
		Random r = new Random(0);
		List<Boolean> blockedHours = Arrays.asList(true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true);
		List<Location> locations = new LinkedList<>();
		for (short i = 0; i < n; i++) {
			locations.add(new Location(i, r.nextDouble() * maxDistance, r.nextDouble() * maxDistance));

		}
		return new EnhancedTTSP(locations, (short) 0, blockedHours, 8, 4.5, 1, 10);
	}
}
