package jaicore.planning.hierarchical.testproblems.nesteddichotomies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd.OracleTaskResolver;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningDomain;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocipstn.OCIPMethod;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class CEOCIPSTNNDProblemGenerator {

	public static CEOCIPSTNPlanningProblem getNestedDichotomyCreationProblem(final String rootClusterName, final Collection<String> classesInit, final boolean objectCreation, final int maxExpRange, final int maxRefinement, final Map<String, EvaluablePredicate> evaluablePredicates, final Map<String, OracleTaskResolver> oracleResolvers) {

		/* define operations */
		List<String> classes = classesInit.stream().sorted().collect(Collectors.toList());
		if (!classes.equals(classesInit)) {
			System.out.println("Reordered classes!");
		}
		List<CEOCOperation> operations = new ArrayList<>();
		Map<CNFFormula,Monom> addLists = new HashMap<>();

		/* operation to close a */
		addLists.put(new CNFFormula(), new Monom("cluster(lc) & cluster(rc) & parent(c,lc) & parent(c,rc) & rgt(lc,rc)"));
		operations.add(new CEOCOperation("createclusters", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), new Monom(), addLists, new HashMap<>(), Arrays.asList()));

		Monom precondition = new Monom();
		addLists = new HashMap<>();
		for (String c : classes) {
			addLists.put(new CNFFormula(new Monom("$contains('" + c + "', p) & $contains('" + c + "',ss)")), new Monom("in('" + c + "', lc)"));
			addLists.put(new CNFFormula(new Monom("$contains('" + c + "', p) & !$contains('" + c + "',ss)")), new Monom("in('" + c + "', rc)"));
		}
		operations.add(new CEOCOperation("configChildNodes", Arrays.asList(new VariableParam[] { new VariableParam("p"), new VariableParam("ss"), new VariableParam("lc"), new VariableParam("rc") }), precondition, addLists, new HashMap<>(), Arrays.asList()));

		/* operation to set exponent value for SVM c parameter */
		addLists = new HashMap<>();
		operations.add(new CEOCOperation("setExpVal", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("x") }), new Monom(), addLists, new HashMap<>(), new ArrayList<>()));
		addLists = new HashMap<>();
		operations.add(new CEOCOperation("setSVMCVal", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("k"), new VariableParam("x") }), new Monom(), addLists, new HashMap<>(), new ArrayList<>()));


		/* define STN methods for the domain */
		Literal taskRefine = new Literal("refine(c)");
		Literal taskConfigureSVM = new Literal("configureSVM(c,l,r)");
		Literal taskConfigureChildNodes = new Literal("configChildNodesT(c,l,r)");

		List<OCIPMethod> methods = new ArrayList<>();
		if (objectCreation) {
			methods.add(new OCIPMethod("refineNode",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("createclusters(c,lc,rc) -> configChildNodesT(c,lc,rc) -> refine(lc) -> refine(rc)"), false, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")}), new Monom("notempty(c)")));
		}
		else {
			methods.add(new OCIPMethod("refineNode",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc"), new VariableParam("next") }), taskRefine, new Monom("nextVar(lc) & succ(lc,rc) & succ(rc,next)"), new TaskNetwork("configChildNodesT(c,lc,rc) -> refine(lc) -> refine(rc)"), false, new ArrayList<>(), new Monom("notempty(c)")));
		}
		methods.add(new OCIPMethod("configChildNodesM",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"),new VariableParam("rc"), new VariableParam("ss")}), taskConfigureChildNodes, new Monom("cluster(c) & parent(c,lc) & parent(c,rc) & cluster(lc) & cluster(rc) & rgt(lc,rc)"), new TaskNetwork("configChildNodes(c,ss,lc,rc)"), false, new ArrayList<>(), new Monom("validRefinementChoice(ss,c)")));
		methods.add(new OCIPMethod("closeNode",  Arrays.asList(new VariableParam[] { new VariableParam("c") }), taskRefine, new Monom("cluster(c)"), new TaskNetwork(""), false, new ArrayList<>(), new Monom("oneitem(c)")));

		for (int i = -1 * maxExpRange; i <= maxExpRange; i++) {
			methods.add(new OCIPMethod("setupSVMCForValue" + i,  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("l"), new VariableParam("r")}), taskConfigureSVM, new Monom(), new TaskNetwork("configureSVM1thPlace(c,l,r)"), false, new ArrayList<>(), new Monom()));
		}
		for (int i = 1; i <= maxRefinement; i++) {
			if (i < maxRefinement) {
				methods.add(new OCIPMethod("setPlace" + i + "ofSVMCParam", 		 	Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("l"), new VariableParam("r"), new VariableParam("x") }), new Literal("configureSVM" + i + "thPlace(c,l,r)"), new Monom("!configClosed(c) & digit(x)"), new TaskNetwork("setSVMCVal(c, '" + i + "',x) -> configureSVM" + (i + 1) + "thPlace(c,l,r)"), false, new ArrayList<>(), new Monom()));
			}
			methods.add(new OCIPMethod("setPlaceAndClose" + i + "ofSVMCParam",	Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("l"), new VariableParam("r"), new VariableParam("x") }), new Literal("configureSVM" + i + "thPlace(c,l,r)"), new Monom("!configClosed(c) & digit(x)"), new TaskNetwork("setSVMCVal(c, '" + i + "',x)"), false, new ArrayList<>(), new Monom()));
		}

		/* create STN domain */
		CEOCIPSTNPlanningDomain domain = new CEOCIPSTNPlanningDomain(operations, methods);

		Monom init = new Monom();
		init.add(new Literal("cluster('" + rootClusterName + "')"));
		for (String c : classes) {
			init.add(new Literal("in('" + c + "','" + rootClusterName + "')"));
		}

		/* for SVM config */
		for (int exp = -5; exp <= 5; exp++) {
			init.add(new Literal("exponent('" + exp + "')"));
		}
		for (int i = 0; i <= 9; i+=2) {
			init.add(new Literal("digit('" + i + "')"));
		}

		/* if no constant creation is allowed, add all successor objects */
		if (!objectCreation) {
			init.add(new Literal("nextVar('var0')"));
			String currentVar = "var0";
			for (int i = 1; i < 2 * classes.size(); i++) {
				String nextVar = "var" + i;
				init.add(new Literal("succ('" + currentVar + "', '" + nextVar + "')"));
				currentVar = nextVar;
			}
		}

		TaskNetwork network = new TaskNetwork("refine('" + rootClusterName + "')");
		return new CEOCIPSTNPlanningProblem(domain, null, init, network, evaluablePredicates, oracleResolvers);
	}
}
