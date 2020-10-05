package ai.libs.jaicore.problems.enhancedttsp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EnhancedTTSPGenerator {

	private final ITSPLocationGenerator locationGenerator;

	public EnhancedTTSPGenerator(final ITSPLocationGenerator locationGenerator) {
		super();
		this.locationGenerator = locationGenerator;
	}

	public EnhancedTTSP generate(final int n, final int maxDistance, final int seed) {
		if (n > Short.MAX_VALUE) {
			throw new IllegalArgumentException("Number of locations must not exceed " + Short.MAX_VALUE);
		}

		/* create TTSP problem */
		List<Boolean> blockedHours = Arrays.asList(true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true);
		List<Location> locations = this.locationGenerator.getLocations(n, 0, 0, maxDistance, 0.1);
		Collections.shuffle(locations, new Random(seed));
		return new EnhancedTTSP(locations, (short) 0, blockedHours, 8, 4.5, 1, 10);
	}
}
