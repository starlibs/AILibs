package jaicore.search.algorithms.standard.awastar;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.graphgenerators.npuzzle.standard.NPuzzleGenerator;
import jaicore.search.graphgenerators.npuzzle.standard.NPuzzleNode;
import jaicore.search.structure.core.Node;
public class AwaStarSolutionTest {

	@Test
	public void test() {
		
		List<Node<NPuzzleNode, Double>> solution = null;
		try {
			AwaStarSearch<NPuzzleNode, String, Double> search = new AwaStarSearch<>(
					new NPuzzleGenerator(3, 4),
					n-> (double)n.getPoint().getNumberOfWrongTiles(),
					new ISolutionEvaluator<NPuzzleNode, Double>() {

						@Override
						public Double evaluateSolution(List<NPuzzleNode> solutionPath) throws Exception {
							if (solutionPath.get(solutionPath.size() - 1).getNumberOfWrongTiles() != 0) {
								return Double.MAX_VALUE;
							} else {
								return 0.0d;
							}
						}

						@Override
						public boolean doesLastActionAffectScoreOfAnySubsequentSolution(
								List<NPuzzleNode> partialSolutionPath) {
							return false;
						}
					}
			);
			solution = search.search(60);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(solution);
		System.out.println(solution);
	}
}
