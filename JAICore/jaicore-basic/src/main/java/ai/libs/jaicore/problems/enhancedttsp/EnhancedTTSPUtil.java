package ai.libs.jaicore.problems.enhancedttsp;

import java.util.Collection;
import java.util.stream.Collectors;

public class EnhancedTTSPUtil {

	private EnhancedTTSPUtil() {
		/* avoid instantiation */
	}

	public static Collection<Location> getLocationsInDistance(final Collection<Location> locations, final Location location, final double distance) {
		return locations.stream().filter(l -> Math.sqrt(Math.pow(l.getX() - location.getX(), 2) + Math.pow(l.getY() - location.getY(), 2)) <= distance).collect(Collectors.toList());
	}
}
