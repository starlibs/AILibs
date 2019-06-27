package ai.libs.jaicore.basic.sets;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests added for the functionalities getMultiplyContainedItems() and getInvertedCopyOfList().
 *
 * @author Suganya
 */

@RunWith(Parameterized.class)

public class SetUtilTest2<T> {
	private List<T> input;
	private Collection<T> output;
	private Type type;

	enum Type {
		INVERTED, DUPLICATE
	};

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
			{ Type.DUPLICATE, Arrays.asList(3.0, "3.0", Double.parseDouble("3.0"), 2, 3, 3),
				new HashSet<>(Arrays.asList(3, 3.0)) },
			{ Type.DUPLICATE, Arrays.asList(3.0, "3.0", Double.parseDouble("3.0"), 2, 3, '3', '3'),
					new HashSet<>(Arrays.asList('3', 3.0)) },
			{ Type.DUPLICATE, Arrays.asList(), new HashSet<>(Arrays.asList()) },
			{ Type.INVERTED, Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1) },
			{ Type.INVERTED, Arrays.asList("a", "b", "c", "d"), Arrays.asList("d", "c", "b", "a") },
			{ Type.INVERTED, Arrays.asList("a"), Arrays.asList("a") },
			{ Type.INVERTED, Arrays.asList(), Arrays.asList() },

		});
	}

	public SetUtilTest2(final Type type, final List<T> input, final Collection<T> output) {

		this.type = type;
		this.input = input;
		this.output = output;

	}

	@Test
	public void testgetMultiplyContainedItems() {
		Assume.assumeTrue(this.type == Type.DUPLICATE);
		assertEquals(this.output, SetUtil.getMultiplyContainedItems(this.input));
	}

	@Test
	public void testgetInvertedCopyOfList() {
		Assume.assumeTrue(this.type == Type.INVERTED);
		assertEquals(this.output, SetUtil.getInvertedCopyOfList(this.input));

	}

}
