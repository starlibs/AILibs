package jaicore.planning.hierarchical.testproblems.nesteddichotomies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningDomain;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class CEOCSTNNDProblemGenerator {
	
	public static CEOCSTNPlanningProblem getNestedDichotomyCreationProblem(String rootClusterName, int numClasses, boolean objectCreation, int maxExpRange, int maxRefinement) {
		List<String> classes = new ArrayList<>();
		for (int i = 1; i <= numClasses; i++)
			classes.add("C" + i);
		return getNestedDichotomyCreationProblem(rootClusterName, classes, objectCreation, maxExpRange, maxRefinement);
	}
	
	public static CEOCSTNPlanningProblem getNestedDichotomyCreationProblem(String rootClusterName, Collection<String> classesInit, boolean objectCreation, int maxExpRange, int maxRefinement) {
		
		/* define operations */
		List<String> classes = classesInit.stream().sorted().collect(Collectors.toList());
		if (!classes.equals(classesInit)) {
			System.out.println("Reordered classes!");
//			System.out.println(classesInit);
//			System.out.println(classes);
		}
		List<CEOCOperation> operations = new ArrayList<>();
		Map<CNFFormula,Monom> addLists = new HashMap<>(), deleteLists = new HashMap<>();
		
		/* operations to init child clusters and to close them */
		for (String c : classes) {
			addLists.put(new CNFFormula(new Monom("in('" + c + "',p)")), new Monom("in('" + c + "', lc)"));
			addLists.put(new CNFFormula(new Monom("smallest('" + c + "',p)")), new Monom("smallest('" + c + "', lc)"));
		}
		if (objectCreation) {
			operations.add(new CEOCOperation("initChildClusters", Arrays.asList(new VariableParam[] { new VariableParam("p"), new VariableParam("lc"), new VariableParam("rc") }), new Monom(), addLists, new HashMap<>(), Arrays.asList(new VariableParam[] { new VariableParam("lc"), new VariableParam("rc") })));
		}
		else{
			addLists.put(new CNFFormula(), new Monom("nextVar(next)"));
			deleteLists.put(new CNFFormula(), new Monom("nextVar(lc)"));
			operations.add(new CEOCOperation("initChildClusters", Arrays.asList(new VariableParam[] { new VariableParam("p"), new VariableParam("lc"), new VariableParam("rc"), new VariableParam("next") }), new Monom(), addLists, deleteLists, new ArrayList<>()));
		}
		
		/* add the closed-literal for nodes as post-condition for close clusters */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("closed(l) & closed(r)"));
		operations.add(new CEOCOperation("closeClusters", Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("lw"), new VariableParam("r"), new VariableParam("rw") }), new Monom("in(lw,l) & in(rw,r)"), addLists, new HashMap<>(), new ArrayList<>()));
		
		/* operation to shift the first element from a left cluster to a right one */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("in(x,r) & biggest(x,r) & smallest(x,r)"));
		Monom case1 = new Monom("smallest(x,l)");
		for (String c2ndSmallest : classes) {
			CNFFormula allQuantifiedBiggerRelation = new CNFFormula(case1);
			allQuantifiedBiggerRelation.add(new Clause("in('" + c2ndSmallest + "', l)"));
			for (String cOther : classes) {
				if (!cOther.equals(c2ndSmallest)) {
					Clause c = new Clause("!in('" + cOther + "', l) | x = '" + cOther + "' | bigger('" + cOther + "', '" + c2ndSmallest + "')");
					allQuantifiedBiggerRelation.add(c);
				}
			}
			addLists.put(allQuantifiedBiggerRelation, new Monom("smallest('" + c2ndSmallest + "', l)"));
		}
		deleteLists = new HashMap<>();
			/* case two: some other is smallest */
//			Monom case2 = new Monom("smallest('" + cSmallest + "',p) & '" + cSmallest + "' != x");
//			addLists.put(new CNFFormula(case2), new Monom("smallest('" + cSmallest + "', lc)"));
		deleteLists.put(new CNFFormula(), new Monom("in(x,l) & smallest(x,l)"));
		operations.add(new CEOCOperation("shiftFirstElement", Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), new Monom("in(x,l)"), addLists, deleteLists, new ArrayList<>()));
		
		/* operation to shift a new biggest element from a left cluster to a right one */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("in(x,r) & biggest(x,r)"));
		deleteLists = new HashMap<>();
		deleteLists.put(new CNFFormula(), new Monom("in(x,l) & biggest(y,r)"));
		operations.add(new CEOCOperation("shiftElement", Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("y"), new VariableParam("l"), new VariableParam("r") }), new Monom("in(x,l) & biggest(y,r)"), addLists, deleteLists, new ArrayList<>()));
		
		/* add primitive action to select a representative of a cluster */
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("represents(x,c)"));
		deleteLists = new HashMap<>();
		deleteLists.put(new CNFFormula(), new Monom("in(x,c) & smallest(x,c) & biggest(x,c)"));
		operations.add(new CEOCOperation("declareClusterRepresentant", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("x") }), new Monom("in(x,c)"), addLists, deleteLists, new ArrayList<>()));
		
		/* add primitive action to assert emptyness of a cluster */
		Monom precondition = new Monom();
		for (String c : classes) {
			precondition.add(new Literal("!in('" + c + "', c)"));
		}
		addLists = new HashMap<>();
		addLists.put(new CNFFormula(), new Monom("empty(c)"));
		operations.add(new CEOCOperation("assertEmptyness", Arrays.asList(new VariableParam[] { new VariableParam("c") }), precondition, addLists, new HashMap<>(), new ArrayList<>()));
		
		/* operation to set exponent value for SVM c parameter */
//		addLists = new HashMap<>();
//		operations.add(new CEOCOperation("setExpVal", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("x") }), new Monom(), addLists, new HashMap<>(), new ArrayList<>()));
//		addLists = new HashMap<>();
//		operations.add(new CEOCOperation("setSVMCVal", Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("k"), new VariableParam("x") }), new Monom(), addLists, new HashMap<>(), new ArrayList<>()));
		
		/* define STN methods for the domain */
		Literal taskRefine = new Literal("refine(c)");
		Literal taskConfigure = new Literal("configureClusters(l,r)");
//		Literal taskConfigureSVM = new Literal("configureSVM(c,l,r)");
		Literal taskShiftFirst = new Literal("selectAndShiftFirstElement(l,r)");
		Literal taskShiftFirstNonSmallest = new Literal("selectAndShiftFirstNonSmallestElement(l,r)");
		Literal taskShiftFurther = new Literal("selectAndShiftFurtherElement(l,r)");
		Literal taskShiftAny = new Literal("shiftAnyElement(l,r)");
		Literal taskCheckLeaf = new Literal("checkLeaf(c)");
		
		List<OCMethod> methods = new ArrayList<>();
		if (objectCreation) {
			methods.add(new OCMethod("assertbinarycluster",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("initChildClusters(c,lc,rc) -> shiftAnyElement(lc,rc) -> checkLeaf(lc) -> checkLeaf(rc)"), true, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
			methods.add(new OCMethod("refineandclosetheotherunarily",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("initChildClusters(c,lc,rc) -> selectAndShiftFirstElement(lc,rc) -> refine(lc)"), false, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
			methods.add(new OCMethod("refinebothwithatleasttwoonbothsides",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc") }), taskRefine, new Monom(), new TaskNetwork("initChildClusters(c,lc,rc) -> selectAndShiftFirstNonSmallestElement(lc,rc) -> selectAndShiftFurtherElement(lc,rc) -> configureClusters(lc,rc) -> refine(lc) -> refine(rc)"), false, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
		}
		else {
			methods.add(new OCMethod("assertbinarycluster",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc"), new VariableParam("next") }), taskRefine, new Monom("nextVar(lc) & succ(lc,rc) & succ(rc,next)"), new TaskNetwork("initChildClusters(c,lc,rc,next) -> shiftAnyElement(lc,rc) -> checkLeaf(lc) -> checkLeaf(rc)"), true, Arrays.asList()));
			methods.add(new OCMethod("refineandclosetheotherunarily",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc"), new VariableParam("next") }), taskRefine, new Monom("nextVar(lc) & succ(lc,rc) & succ(rc,next)"), new TaskNetwork("initChildClusters(c,lc,rc,next) -> selectAndShiftFirstElement(lc,rc) ->  refine(lc)"), false, Arrays.asList()));
			methods.add(new OCMethod("refinebothwithatleasttwoonbothsides",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("lc"), new VariableParam("rc"), new VariableParam("next") }), taskRefine, new Monom("nextVar(lc) & succ(lc,rc) & succ(rc,next)"), new TaskNetwork("initChildClusters(c,lc,rc,next) -> selectAndShiftFirstNonSmallestElement(lc,rc) -> selectAndShiftFurtherElement(lc,rc) -> configureClusters(lc,rc) -> refine(lc) -> refine(rc)"), false, Arrays.asList(new VariableParam[]{ new VariableParam("lc"), new VariableParam("rc")})));
		}
//		for (int i = -1 * maxExpRange; i <= maxExpRange; i++)
//			methods.add(new OCMethod("setupSVMCForValue" + i,  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("l"), new VariableParam("r")}), taskConfigureSVM, new Monom(), new TaskNetwork("configureSVM1thPlace(c,l,r)"), false, new ArrayList<>()));
//		for (int i = 1; i <= maxRefinement; i++) {
//			if (i < maxRefinement)
//				methods.add(new OCMethod("setPlace" + i + "ofSVMCParam", 		 	Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("l"), new VariableParam("r"), new VariableParam("x") }), new Literal("configureSVM" + i + "thPlace(c,l,r)"), new Monom("!configClosed(c) & digit(x)"), new TaskNetwork("setSVMCVal(c, '" + i + "',x) -> configureSVM" + (i + 1) + "thPlace(c,l,r)"), false, new ArrayList<>()));
//			methods.add(new OCMethod("setPlaceAndClose" + i + "ofSVMCParam",	Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("l"), new VariableParam("r"), new VariableParam("x") }), new Literal("configureSVM" + i + "thPlace(c,l,r)"), new Monom("!configClosed(c) & digit(x)"), new TaskNetwork("setSVMCVal(c, '" + i + "',x)"), false, new ArrayList<>()));
//		}
		methods.add(new OCMethod("shiftElementAndConfigure",  Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("r") }), taskConfigure, new Monom(), new TaskNetwork("selectAndShiftFurtherElement(l,r) -> configureClusters(l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftFirstElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), taskShiftFirst, new Monom("in(x,l)"), new TaskNetwork("shiftFirstElement(x,l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftFirstNonSmallestElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), taskShiftFirstNonSmallest, new Monom("in(x,l) & !smallest(x,l)"), new TaskNetwork("shiftFirstElement(x,l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftFurtherElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("y"), new VariableParam("l"), new VariableParam("r") }), taskShiftFurther, new Monom("in(x,l) & biggest(y,r) & bigger(x,y)"), new TaskNetwork("shiftElement(x,y,l,r)"), false, new ArrayList<>()));
		methods.add(new OCMethod("selectAndShiftAnyElementMethod",  Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("l"), new VariableParam("r") }), taskShiftAny, new Monom("in(x,l)  & !smallest(x,l) "), new TaskNetwork("shiftFirstElement(x,l,r)"), true, new ArrayList<>()));
		methods.add(new OCMethod("closeSetup",  Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("lw"), new VariableParam("r"), new VariableParam("rw") }), taskConfigure, new Monom("in(lw,l) & in(rw,r)"), new TaskNetwork("closeClusters(l,lw,r,rw)"), true, new ArrayList<>()));
		methods.add(new OCMethod("checkLeafNode",  Arrays.asList(new VariableParam[] { new VariableParam("c"), new VariableParam("x") }), taskCheckLeaf, new Monom("in(x,c)"), new TaskNetwork("declareClusterRepresentant(c,x) -> assertEmptyness(c)"), true, new ArrayList<>()));
		
		/* create STN domain */
		CEOCSTNPlanningDomain domain = new CEOCSTNPlanningDomain(operations, methods);
		
		Monom init = new Monom();
		for (String c : classes) {
//			init.add(new Literal("class('" + c + "')"));
			init.add(new Literal("in('" + c + "','" + rootClusterName + "')"));
		}
		init.add(new Literal("smallest('" + classes.get(0) + "', '" + rootClusterName + "')"));
		for (int i = 0; i < classes.size(); i++) {
			for (int j = i + 1; j < classes.size(); j++) {
				init.add(new Literal("bigger('" + classes.get(j) + "', '" + classes.get(i) + "')"));
			}
		}
		
		/* for SVM config */
//		for (int exp = -5; exp <= 5; exp++)
//			init.add(new Literal("exponent('" + exp + "')"));
//		for (int i = 0; i <= 9; i++)
//			init.add(new Literal("digit('" + i + "')"));
		
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
		return new CEOCSTNPlanningProblem(domain, null, init, network);
	}
}
