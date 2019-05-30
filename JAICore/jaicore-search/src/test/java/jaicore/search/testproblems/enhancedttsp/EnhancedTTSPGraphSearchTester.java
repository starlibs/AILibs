package jaicore.search.testproblems.enhancedttsp;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPGenerator;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

public class EnhancedTTSPGraphSearchTester {

	@Test
	public void testUnmodifiabilityOfLists() {
		EnhancedTTSP tsp = new EnhancedTTSPGenerator().generate(5, 5);
		EnhancedTTSPNode n = tsp.getInitalState();
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
