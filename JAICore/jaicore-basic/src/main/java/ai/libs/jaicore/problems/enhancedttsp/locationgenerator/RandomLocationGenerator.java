package ai.libs.jaicore.problems.enhancedttsp.locationgenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPUtil;
import ai.libs.jaicore.problems.enhancedttsp.ITSPLocationGenerator;
import ai.libs.jaicore.problems.enhancedttsp.Location;

public class RandomLocationGenerator implements ITSPLocationGenerator {

	private final Random random;

	public RandomLocationGenerator(final Random random) {
		super();
		this.random = random;
	}

	@Override
	public List<Location> getLocations(final int n, final double centerX, final double centerY, final double radius, final double minDistance) {
		List<Location> locations = new LinkedList<>();
		for (short i = 0; i < n; i++) {
			Location nl;
			do {
				nl = new Location(i, centerX + (this.random.nextDouble() * radius * (this.random.nextBoolean() ? 1 : -1)), centerY + (this.random.nextDouble() * radius * (this.random.nextBoolean() ? 1 : -1)));
			}
			while (!EnhancedTTSPUtil.getLocationsInDistance(locations, nl, minDistance).isEmpty());
			locations.add(nl);
		}
		return locations;
	}

}
