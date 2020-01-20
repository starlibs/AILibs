package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

/**
 * This is the single player UCT variant proposed in
	@inproceedings{schadd2008single,
	  title={Single-player monte-carlo tree search},
	  author={Schadd, Maarten PD and Winands, Mark HM and Van Den Herik, H Jaap and Chaslot, Guillaume MJ-B and Uiterwijk, Jos WHM},
	  booktitle={International Conference on Computers and Games},
	  pages={1--12},
	  year={2008},
	  organization={Springer}
	}
 *
 * In this algorithm, the exploration constant, which is typically sqrt(2) in UCT, must be set as a parameter
 *
 * @author fmohr
 *
 */
public class SPUCTPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public SPUCTPathSearch(final I problem, final boolean maximization, final int seed, final double evaluationFailurePenalty, final double explorationC, final double bigD) {
		super(problem, new SPUCBPolicy<>(maximization, bigD), new UniformRandomPolicy<>(new Random(seed)), evaluationFailurePenalty);
		((SPUCBPolicy<N, A>)this.getTreePolicy()).setExplorationConstant(explorationC);
	}

	public SPUCTPathSearch(final I problem, final int seed, final double evaluationFailurePenalty, final double explorationC, final double bigD) {
		this(problem, false, seed, evaluationFailurePenalty, explorationC, bigD);
	}
}
