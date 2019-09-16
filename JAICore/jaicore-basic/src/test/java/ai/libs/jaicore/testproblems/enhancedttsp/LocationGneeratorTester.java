package ai.libs.jaicore.testproblems.enhancedttsp;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import ai.libs.jaicore.testproblems.enhancedttsp.locationgenerator.ClusterBasedGenerator;
import ai.libs.jaicore.testproblems.enhancedttsp.locationgenerator.RandomLocationGenerator;

public class LocationGneeratorTester {

	@Test
	public void test() {
		int n = 100;
		RandomLocationGenerator gen1 = new RandomLocationGenerator(new Random(0));
		RandomLocationGenerator gen2 = new RandomLocationGenerator(new Random(0));

		ClusterBasedGenerator cGen = new ClusterBasedGenerator(gen1, gen2, 0.1, 1, 2);
		List<Location> locations = cGen.getLocations(n, 0, 0, 20, 0.5);

		EnhancedTTSP tsp = new EnhancedTTSPGenerator(cGen).generate(n, 20);
	}
}
