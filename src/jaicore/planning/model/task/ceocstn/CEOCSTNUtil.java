package jaicore.planning.model.task.ceocstn;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.logic.Monom;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDNodeUtil;
import jaicore.planning.model.ceoc.CEOCAction;

public class CEOCSTNUtil {

	public static List<CEOCAction> extractPlanFromSolutionPath(List<TFDNode> solution) {
		return solution.stream().map(np -> (CEOCAction) np.getAppliedAction()).filter(a -> a != null).collect(Collectors.toList());
	}
	
	public static Monom getStateAfterPlanExecution(Monom init, List<CEOCAction> plan) {
		Monom state = new Monom(init);
		for (CEOCAction a : plan)
			TFDNodeUtil.updateState(state, a);
		return state;
	}
}
