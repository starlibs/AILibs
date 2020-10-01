package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HOMERLeafTest {

	private static final int LABEL_INDEX = 1;
	private static HOMERLeaf leaf;

	@BeforeAll
	public static void setup() {
		leaf = new HOMERLeaf(LABEL_INDEX);
	}

	@Test
	public void testGetLabels() {
		Collection<Integer> col = leaf.getLabels();
		assertEquals("Size must be 1", 1, col.size());
		assertTrue("Must contain the label index itself", col.contains(LABEL_INDEX));
	}

}
