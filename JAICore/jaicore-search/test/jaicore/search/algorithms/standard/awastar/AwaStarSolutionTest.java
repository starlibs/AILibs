package jaicore.search.algorithms.standard.awastar;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import jaicore.search.graphgenerators.npuzzle.standard.NPuzzleGenerator;
import jaicore.search.graphgenerators.npuzzle.standard.NPuzzleNode;
public class AwaStarSolutionTest {

	@Test
	public void test() {
		
		NPuzzleNode solution = null;
		try {
			AwaStarSearch<NPuzzleNode, String, Double> search = new AwaStarSearch<>(new NPuzzleGenerator(3, 9), n-> (double)n.getPoint().getNumberOfWrongTiles());
			solution = search.search(60);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(solution);
		System.out.println(solution);
	}
}
