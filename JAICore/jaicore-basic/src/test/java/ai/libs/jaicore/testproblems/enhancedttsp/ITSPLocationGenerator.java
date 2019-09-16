package ai.libs.jaicore.testproblems.enhancedttsp;

import java.util.List;

public interface ITSPLocationGenerator {
	public List<Location> getLocations(int n, double centerX, double centerY, double radius, double minDistance);
}
