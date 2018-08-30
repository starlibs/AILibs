package jaicore.planning.graphgenerators.task.ceoctfd;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.tfd.TFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.OCMethod;

@SuppressWarnings("serial")
public class CEOCTFDGraphGenerator<O extends CEOCOperation, M extends OCMethod, A extends CEOCAction> extends TFDGraphGenerator<O, M, A> {

	public CEOCTFDGraphGenerator(CEOCSTNPlanningProblem<O, M, A> problem) {
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
