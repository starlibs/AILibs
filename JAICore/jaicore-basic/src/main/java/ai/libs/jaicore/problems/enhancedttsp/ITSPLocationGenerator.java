package ai.libs.jaicore.problems.enhancedttsp;

import java.util.List;

public interface ITSPLocationGenerator {
	public List<Location> getLocations(int n, double centerX, double centerY, double radius, double minDistance);
}
