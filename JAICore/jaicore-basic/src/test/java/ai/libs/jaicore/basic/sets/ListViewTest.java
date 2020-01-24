package ai.libs.jaicore.basic.sets;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ListViewTest {

	@Test
	public void testNumberList() {
		List<Number> l = Arrays.asList(2, 4, 6, 8, 10);
		List<Integer> l2 = new ListView<>(l);
		int index = 0;
		for (int i : l2) {
			assertEquals(i, l.get(index));
			index ++;
		}
	}
}
