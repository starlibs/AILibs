package jaicore.planning.hierarchical.problems.ceocstn;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.algorithms.strips.forward.StripsUtil;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public class CEOCSTNUtil {

	public static List<CEOCAction> extractPlanFromSolutionPath(List<TFDNode> solution) {
		return solution.stream().map(np -> (CEOCAction) np.getAppliedAction()).filter(a -> a != null).collect(Collectors.toList());
	}
	
	public static Monom getStateAfterPlanExecution(Monom init, List<CEOCAction> plan) {
		Monom state = new Monom(init);
		for (CEOCAction a : plan)
			StripsUtil.updateState(state, a);
		return state;
	}
}
