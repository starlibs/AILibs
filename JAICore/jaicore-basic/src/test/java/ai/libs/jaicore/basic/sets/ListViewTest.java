package ai.libs.jaicore.basic.sets;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ListViewTest {

	@Test
	public void testNumberList() {
		List<Number> l = Arrays.asList(2, 4, 6, 8, 10);
		List<Integer> l2 = new ListView<>(l);
		for (int i : l2) {
			System.out.println(i);
		}
	}
}
