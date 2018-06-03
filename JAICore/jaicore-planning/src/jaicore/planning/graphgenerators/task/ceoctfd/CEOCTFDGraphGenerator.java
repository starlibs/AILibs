package jaicore.planning.graphgenerators.task.ceoctfd;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.tfd.TFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;

@SuppressWarnings("serial")
public class CEOCTFDGraphGenerator extends TFDGraphGenerator {

	public CEOCTFDGraphGenerator(CEOCSTNPlanningProblem problem) {
		super(problem);
	}

	@Override
	protected TFDNode postProcessComplexTaskNode(TFDNode node) {
		Monom state = node.getState();
		state.getParameters().stream().filter(p -> p.getName().startsWith("newVar") && !state.contains(new Literal("def('" + p.getName() + "')")))
				.forEach(p -> state.add(new Literal("def('" + p.getName() + "')")));
		return node;
	}
}
