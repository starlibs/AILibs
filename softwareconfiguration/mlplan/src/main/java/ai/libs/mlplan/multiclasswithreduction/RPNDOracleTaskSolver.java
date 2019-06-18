package ai.libs.mlplan.multiclasswithreduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.logic.fol.theories.set.SetTheoryUtil;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCAction;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd.OracleTaskResolver;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import weka.core.Instances;

public class RPNDOracleTaskSolver implements OracleTaskResolver {

	private static final Logger logger = LoggerFactory.getLogger(RPNDOracleTaskSolver.class);
	private final Random rand;
	private final String classifierName;
	private final Instances data;
	private CEOCOperation configChildNodesOp;

	public RPNDOracleTaskSolver(final Random rand, final String classifierName, final Instances data, final CEOCIPSTNPlanningProblem problem) {
		super();
		this.rand = rand;
		this.classifierName = classifierName;
		this.data = data;
		for (CEOCOperation op : problem.getDomain().getOperations()) {
			if (op.getName().equals("configChildNodes")) {
				this.configChildNodesOp = op;
				break;
			}
		}
		if (this.configChildNodesOp == null) {
			throw new IllegalArgumentException("Domain has no operation with name \"configChildNodes\"");
		}
	}

	private interface Splitter {
		Split split(Collection<String> set) throws InterruptedException;
	}

	@SuppressWarnings("serial")
	class Split extends Pair<Set<String>, Set<String>> {

		public Split(final Set<String> x, final Set<String> y) {
			super(x, y);
		}

		@Override
		public String toString() {
			return "Split [getX()=" + this.getX() + ", getY()=" + this.getY() + "]";
		}
	}

	@SuppressWarnings("serial")
	class SplitException extends Exception {
		public SplitException(final Exception e) {
			super(e);
		}
	}

	private class RPNDSplitter implements Splitter {

		private final Instances data;

		public RPNDSplitter(final Instances data) {
			super();
			this.data = data;
		}

		@Override
		public Split split(final Collection<String> set) throws InterruptedException {
			ClassSplit<String> split = NestedDichotomyUtil.createGeneralRPNDBasedSplit(set, RPNDOracleTaskSolver.this.rand, RPNDOracleTaskSolver.this.classifierName, this.data);
			return new Split(new HashSet<>(split.getL()), new HashSet<>(split.getR()));
		}
	}

	@Override
	public Collection<List<Action>> getSubSolutions(final Monom state, final Literal task) throws Exception {

		/* prepare template grounding for actions */
		String nameOfParent = task.getConstantParams().get(0).getName();
		String nameOfLC = task.getConstantParams().get(1).getName();
		String nameOfRC = task.getConstantParams().get(2).getName();
		Map<VariableParam, ConstantParam> groundingTemplate = new HashMap<>();
		groundingTemplate.put(new VariableParam("p"), new ConstantParam(nameOfParent));
		groundingTemplate.put(new VariableParam("lc"), new ConstantParam(nameOfLC));
		groundingTemplate.put(new VariableParam("rc"), new ConstantParam(nameOfRC));

		List<String> set = new ArrayList<>(SetTheoryUtil.getObjectsInSet(state, nameOfParent));
		logger.info("Compute RPND split for {}", set);

		if (set.size() <= 1) {
			return new ArrayList<>();
		}

		/* if no decision is to be made, return the single possible solution */
		if (set.size() == 2) {

			/* determine subsolutions */
			Collection<List<Action>> subsolutions = new ArrayList<>();
			Map<VariableParam, ConstantParam> grounding = new HashMap<>(groundingTemplate);
			grounding.put(new VariableParam("ss"), new ConstantParam("{" + set.get(0) + "}"));
			List<Action> subsolution = new ArrayList<>();
			subsolution.add(new CEOCAction(this.configChildNodesOp, grounding));
			subsolutions.add(subsolution);
			return subsolutions;
		}

		List<Splitter> splitters = new ArrayList<>();
		int max = 1;
		logger.info("Make {} suggestions for {} classes", max, set.size());
		for (int i = 0; i < max; i++) {
			splitters.add(new RPNDSplitter(this.data));
		}

		/* determine subsolutions */
		Collection<List<Action>> subsolutions = new ArrayList<>();
		for (Splitter splitter : splitters) {
			logger.info("Compute next split");
			Split split = splitter.split(set);
			logger.info("Split computed: {}", split);
			Map<VariableParam, ConstantParam> grounding = new HashMap<>(groundingTemplate);
			grounding.put(new VariableParam("ss"), new ConstantParam(SetUtil.serializeAsSet(split.getX())));
			List<Action> subsolution = new ArrayList<>();
			subsolution.add(new CEOCAction(this.configChildNodesOp, grounding));
			subsolutions.add(subsolution);
		}

		logger.info("Ready with RPND computation");
		return subsolutions;
	}

}
