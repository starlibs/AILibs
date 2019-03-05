package jaicore.testproblems.npuzzle;

import java.util.concurrent.atomic.AtomicInteger;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;

public class NPuzzleProblemSet extends AAlgorithmTestProblemSet<NPuzzleProblem> {

	public NPuzzleProblemSet(final String name) {
		super("N-Puzzle");
	}

	private final static int SEED = 0;
	private int max_n = 4;
	private int max_solutions = 100;
	private AtomicInteger seenSolutions = new AtomicInteger(0);
	private boolean showGraphs = false;

	@Override
	public NPuzzleProblem getSimpleProblemInputForGeneralTestPurposes() {
		return new NPuzzleProblem(3, 0);
	}
	@Override
	public NPuzzleProblem getDifficultProblemInputForGeneralTestPurposes() {
		return new NPuzzleProblem(1000, 0);
	}

	//	@Override
	//	public void testThatIteratorReturnsEachPossibleSolution() {
	//		for (int n = 3; n <= this.max_n; n++) {
	//			System.out.print("Checking first 100 solutions of " + n + "-puzzle ... ");
	//			IGraphSearch<I,O,NPuzzleNode, String> search = this.getSearch(n, SEED);
	//			assertNotNull("The factory has not returned any search object.", search);
	//			boolean initialized = false;
	//			boolean terminated = false;
	//			int solutions = 0;
	//			Iterator<AlgorithmEvent> iterator = search.iterator();
	//			assertNotNull("The search algorithm does return NULL as an iterator for itself.", iterator);
	//			while (iterator.hasNext()) {
	//				AlgorithmEvent e = search.next();
	//				assertNotNull("The search iterator has returned NULL even though hasNext suggested that more event should come.", e);
	//				if (!initialized) {
	//					assertTrue(e instanceof AlgorithmInitializedEvent);
	//					initialized = true;
	//				} else if (e instanceof AlgorithmFinishedEvent) {
	//					terminated = true;
	//				} else {
	//					assertTrue(!terminated);
	//					if (e instanceof GraphSearchSolutionCandidateFoundEvent) {
	//						solutions++;
	//						@SuppressWarnings("unchecked")
	//						List<NPuzzleNode> solutionPath = ((EvaluatedSearchSolutionCandidateFoundEvent<NPuzzleNode, String, Double>) e).getSolutionCandidate().getNodes();
	//						NPuzzleNode finalNode = solutionPath.get(solutionPath.size() - 1);
	//						assertTrue("Number of wrong tiles in solution " + finalNode.toString() + " is " + finalNode.getNumberOfWrongTiles(), finalNode.getNumberOfWrongTiles() == 0);
	//						if (solutions >= this.max_solutions) {
	//							return;
	//						}
	//					}
	//				}
	//
	//				System.out.println("done");
	//			}
	//		}
	//	}
}
