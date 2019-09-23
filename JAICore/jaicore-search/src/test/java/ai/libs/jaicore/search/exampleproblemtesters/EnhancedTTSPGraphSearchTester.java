package ai.libs.jaicore.search.exampleproblemtesters;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPGenerator;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.problems.enhancedttsp.locationgenerator.RandomLocationGenerator;

public class EnhancedTTSPGraphSearchTester {

	@Test
	public void testUnmodifiabilityOfLists() {
		EnhancedTTSP tsp = new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(5, 5, 0);
		EnhancedTTSPState n = tsp.getInitalState();
		boolean exceptionSeen = false;
		try {
			int hashCode1 = n.hashCode();
			n.getCurTour().add((short)1);
			int hashCode2 = n.hashCode();
			assertNotSame(hashCode1, hashCode2);
		}
		catch (UnsupportedOperationException e) {
			exceptionSeen = true;
		}
		assertTrue("Modifying the list from the outside was possible!", exceptionSeen);
	}

}
