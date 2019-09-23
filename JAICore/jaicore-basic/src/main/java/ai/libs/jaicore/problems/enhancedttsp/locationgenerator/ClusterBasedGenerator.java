package ai.libs.jaicore.problems.enhancedttsp.locationgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import ai.libs.jaicore.problems.enhancedttsp.ITSPLocationGenerator;
import ai.libs.jaicore.problems.enhancedttsp.Location;

public class ClusterBasedGenerator implements ITSPLocationGenerator {

	private ITSPLocationGenerator clusterLocationGenerator;
	private ITSPLocationGenerator inClusterLocationGenerator;
	private double density; // relative number of locations per cluster
	private int clusterRadius;
	private double minDistanceBetweenClusters;
	private Random random;

	public ClusterBasedGenerator(final ITSPLocationGenerator clusterLocationGenerator, final ITSPLocationGenerator inClusterLocationGenerator, final double density, final int clusterRadius, final double minDistanceBetweenClusters, final Random random) {
		super();
		this.clusterLocationGenerator = clusterLocationGenerator;
		this.inClusterLocationGenerator = inClusterLocationGenerator;
		this.density = density;
		this.clusterRadius = clusterRadius;
		this.minDistanceBetweenClusters = minDistanceBetweenClusters;
		this.random = random;
	}

	@Override
	public List<Location> getLocations(final int n, final double centerX, final double centerY, final double radius, final double minDistance) {
		if (minDistance > 2 * this.clusterRadius) {
			throw new IllegalArgumentException("");
		}
		int locationsPerCluster = (int)Math.ceil(this.density * n);
		int numClusters = (int)Math.ceil(n * 1.0 / locationsPerCluster);
		List<Location> clusterCentroids = this.clusterLocationGenerator.getLocations(numClusters, centerX, centerY, radius, 2 * this.clusterRadius + this.minDistanceBetweenClusters);
		List<Location> locations = new ArrayList<>(n);
		AtomicInteger ai = new AtomicInteger(0);
		for (int i = 0; i < numClusters; i++) {
			Location cl = clusterCentroids.get(i);
			List<Location> localLocs = this.inClusterLocationGenerator.getLocations(locationsPerCluster, cl.getX(), cl.getY(), this.clusterRadius, minDistance);
			localLocs.forEach(l -> locations.add(new Location((short)ai.getAndIncrement(), l.getX(), l.getY())));
		}
		Collections.shuffle(locations, this.random);
		while (locations.size() > n) {
			locations.remove(0);
		}
		return locations;
	}

}
