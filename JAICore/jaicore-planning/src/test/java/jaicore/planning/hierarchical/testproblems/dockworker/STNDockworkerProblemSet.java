package jaicore.planning.hierarchical.testproblems.dockworker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.strips.StripsPlanningDomain;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class STNDockworkerProblemSet extends AAlgorithmTestProblemSet<STNPlanningProblem> {
	public STNDockworkerProblemSet() {
		super("Dockworker Problem");
	}

	public static STNPlanningProblem getDockworkerProblem() {

		/* retrieve STRIPS operations of the planning problem */
		StripsPlanningDomain dwrStripsDomain = jaicore.planning.classical.problems.strips.StandardProblemFactory.getDockworkerProblem().getDomain();

		/* define non-primitive STN task literals for the domain */
		Literal taskMoveTopmostContainer = new Literal("move-topmost-container(p1,p2)");
		Literal taskMoveStack = new Literal("move-stack(p,q)");
		//		Literal taskMoveAllStacks = new Literal("move-all-stacks()");

		/* define STN methods for the domain */
		List<Method> methods = new ArrayList<>();
		Monom p1 = new Monom("top(c,p1) & on(c,x1) & attached(p1,l1) & belong(k,l1) & attached(p2,l2) & top(x2,p2)");
		methods.add(new Method("take-and-put", Arrays.asList(new VariableParam[] { new VariableParam("k"), new VariableParam("c"), new VariableParam("p1"), new VariableParam("p2"), new VariableParam("l1"), new VariableParam("l2"), new VariableParam("x1"), new VariableParam("x2") }), taskMoveTopmostContainer, p1,
				new TaskNetwork("take(k,l1,c,x1,p1) -> put(k,l2,c,x2,p2)"), false));
		methods.add(new Method("recursive-move", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("p"), new VariableParam("q"), new VariableParam("x") }), taskMoveStack, new Monom("top(c,p) & on(c,x)"),
				new TaskNetwork("move-topmost-container(p,q) -> move-stack(p,q)"), false));
		methods.add(new Method("do-nothing", Arrays.asList(new VariableParam[] { new VariableParam("p") }), taskMoveStack, new Monom("top('pallet',p)"),
				new TaskNetwork(), false));

		/* create STN domain */
		STNPlanningDomain domain = new STNPlanningDomain(dwrStripsDomain.getOperations(), methods);

		/* define init situation, w.r.t. example in Ghallab, Nau, Traverso; p. 230 */
		Monom init = new Monom(""
				+ "attached('p1a','l1') & attached('p1b','l1') & attached('p1c','l1') & belong('crane1','l1') & empty('crane1') & in('c11','p1a') & in('c12','p1a') & top('c11','p1a') & top('pallet','p1b') & top('pallet','p1c') & on('c11','c12') & on('c12','pallet') &"
				+ "attached('p2a','l2') & attached('p2b','l2') & attached('p2c','l2') & belong('crane2','l2') & empty('crane2') & in('c21','p2a') & in('c22','p2a') & in('c23','p2a')  & top('c21','p2a') & top('pallet','p2b') & top('pallet','p2c') & on('c21','c22') & on('c22','c23') & on('c23','pallet') &"
				+ "attached('p3a','l3') & attached('p3b','l3') & attached('p3c','l3') & belong('crane3','l3') & empty('crane3') & in('c31','p3a') & in('c32','p3a') & in('c33','p3a') & in('c34','p3a') & top('c31','p3a') & top('pallet','p3b') & top('pallet','p3c') & on('c31','c32') & on('c32','c33') & on('c33', 'c34') & on('c34','pallet')"
				);
		TaskNetwork network = new TaskNetwork("move-stack('p1a', 'p1c') -> move-stack('p1c','p1b') -> move-stack('p2a','p2c') -> move-stack('p2c','p2b') -> move-stack('p3a','p3c') -> move-stack('p3c','p3b')");
		return new STNPlanningProblem(domain, null, init, network);
	}

	@Override
	public STNPlanningProblem getSimpleProblemInputForGeneralTestPurposes() {
		return getDockworkerProblem();
	}

	@Override
	public STNPlanningProblem getDifficultProblemInputForGeneralTestPurposes() {
		return getDockworkerProblem();
	}
}
