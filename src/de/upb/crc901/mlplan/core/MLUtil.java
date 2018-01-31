package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.upb.crc901.configurationsetting.compositiondomain.CompositionDomain;
import de.upb.crc901.configurationsetting.operation.SequentialComposition;
import jaicore.basic.FileUtil;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;
import jaicore.planning.graphgenerators.task.ceociptfd.OracleTaskResolver;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.OptionHandler;

public class MLUtil {

	private static Logger logger = LoggerFactory.getLogger(MLUtil.class);

	public static MLPipeline extractPipelineFromPlan(List<CEOCAction> plan) {

		if (plan == null)
			return null;

		/* read config */
		StringBuilder creationString = new StringBuilder();
		plan.stream().forEach(a -> creationString.append(a.toString() + "\n"));
		try {

			/* now execute a light version of the plan that does not use any input. This way, we obtain the filter (preprocessor) and classifier objects created by the plan */
			PlanExecutor executor = new PlanExecutor(new Random());
			Map<ConstantParam, Object> variables = executor.executePlan(plan, new HashMap<>());

			/* now extract the filter and the classifier object */
			Optional<Classifier> classifierOpt = variables.keySet().stream().filter(k -> k.getName().equals("classifier")).map(k -> (Classifier) variables.get(k)).findAny();
			if (!classifierOpt.isPresent()) {
				return null;
			}
			Classifier classifier = classifierOpt.get();
			return (MLPipeline) classifier;
		} catch (Throwable e) {
			System.err.println("Could not execute the following code: " + getJavaCodeFromPlan(plan));
			System.err.println("Code created from: ");
			plan.stream().forEach(a -> System.err.println("\t " + a.getEncoding()));
			e.printStackTrace();
			return null;
		}
	}

	public static CEOCIPSTNPlanningProblem getPlanningProblem(File testsetFile, Instances data) {
		CEOCIPSTNPlanningProblem problem = new TaskProblemGenerator().getProblem(testsetFile, data);

		/* TODO: DIRRTYYYYY!!!!! Added an operation here to support oracle */
		Map<CNFFormula, Monom> addLists = new HashMap<>();
		Monom precondition = new Monom();
		addLists = new HashMap<>();
		for (String c : WekaUtil.getClassesDeclaredInDataset(data)) {
			addLists.put(new CNFFormula(new Monom("$contains('" + c + "', p) & $contains('" + c + "',ss)")), new Monom("in('" + c + "', lc)"));
			addLists.put(new CNFFormula(new Monom("$contains('" + c + "', p) & !$contains('" + c + "',ss)")), new Monom("in('" + c + "', rc)"));
		}
		CEOCOperation op = new CEOCOperation("configChildNodes",
				Arrays.asList(new VariableParam[] { new VariableParam("p"), new VariableParam("ss"), new VariableParam("lc"), new VariableParam("rc") }), precondition, addLists,
				new HashMap<>(), Arrays.asList());
		problem.getDomain().getOperations().add(op);

		/* return the problem */
		return problem;
	}

	public static CEOCIPTFDGraphGenerator getGraphGenerator(File testsetFile, File evaluablePredicateFile, Map<String, OracleTaskResolver> oracleResolvers, Instances dataset)
			throws IOException {
		Map<String, EvaluablePredicate> evaluablePredicateMap = new HashMap<>();
		if (evaluablePredicateFile != null) {
			List<String> evaluablePredicates = FileUtil.readFileAsList(evaluablePredicateFile.getAbsolutePath());

			for (String evaluablePredicate : evaluablePredicates) {
				String[] split = evaluablePredicate.split("=");
				String predicateName = split[0].trim();
				String className = split[1].trim();
				try {
					Class<?> clazz = Class.forName(className);
					if (!EvaluablePredicate.class.isAssignableFrom(clazz))
						throw new IllegalArgumentException("Class " + className + " does not implement the " + EvaluablePredicate.class.getName() + " interface.");
					evaluablePredicateMap.put(predicateName, (EvaluablePredicate) clazz.newInstance());
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return new CEOCIPTFDGraphGenerator(getPlanningProblem(testsetFile, dataset), evaluablePredicateMap, oracleResolvers);
	}

	static public String getJavaCodeFromPlan(List<CEOCAction> plan) {

		StringBuilder codeBuilder = new StringBuilder();

		for (CEOCAction a : plan) {

			/* create local copies of the operation, its inputs and outputs. Using this, we then match the operation with a concrete java method */
			CEOCOperation op = a.getOperation();
			Map<VariableParam, ConstantParam> grounding = a.getGrounding();
			ConstantParam outParam = op.getOutputs().isEmpty() ? null : grounding.get(op.getOutputs().get(0));
			List<ConstantParam> inputs = new ArrayList<>(
					SetUtil.difference(op.getParams(), op.getOutputs()).stream().map(v -> a.getGrounding().get(v)).collect(Collectors.toList()));

			switch (a.getOperation().getName()) {
			case "noop":
			case "noaddSingleParam":
			case "noaddValuedParam":
				break;
			case "defSet":
				codeBuilder.append("Collection<String> " + inputs.get(0) + " = SetUtil.unserializeSet(\"" + inputs.get(1) + "\");\n");
				break;
			case "configChildNodes":
				String add1 = "Collection<String> " + inputs.get(2) + " = SetUtil.unserializeSet(\"" + inputs.get(1) + "\");\n";
				String add2 = "Collection<String> " + inputs.get(3) + " = SetUtil.difference(SetUtil.unserializeSet(\"" + inputs.get(0) + "\"), " + inputs.get(2) + ");\n";
				codeBuilder.append(add1);
				codeBuilder.append(add2);
				break;
			case "assignTo":
				codeBuilder.append("String " + outParam.getName());
				codeBuilder.append("= \"");
				codeBuilder.append(inputs.get(0).getName());
				codeBuilder.append("\";\n");
				break;
			case "setNull":
				codeBuilder.append("Object " + inputs.get(0).getName() + " = null;\n");
				break;
			case "getOptionList": {
				codeBuilder.append("List<String> ");
				codeBuilder.append(outParam);
				codeBuilder.append(" = new ArrayList<>();\n");
				break;
			}
			case "addSingleParam": {
				codeBuilder.append(CodePlanningUtil.getListAppendCode(inputs.get(0).getName(), inputs.get(1).getName()));
				break;
			}
			case "addValuedParam": {
				codeBuilder.append(CodePlanningUtil.getListAppendCode(inputs.get(0).getName(), inputs.get(1).getName()));
				codeBuilder.append(CodePlanningUtil.getListAppendCode(inputs.get(0).getName(), inputs.get(2).getName()));
				break;
			}
			case "appendOptions": {
				codeBuilder.append(CodePlanningUtil.getListAppendAllCode(inputs.get(0).getName(), inputs.get(1).getName()));
				break;
			}
			case "concatenate":
			case "concatenateWithName": {
				codeBuilder.append("Concatenate/Concatenatewithname - DUMMY");
				break;
			}
			case "compileOptionListToArray": {
				String oList = inputs.get(0).getName();
				codeBuilder.append("String[] " + outParam + " = new String[" + oList + ".size()];\n");
				codeBuilder.append(oList + ".toArray(" + outParam + ");\n");
				break;
			}
			case "scale": {
				String input = a.getParameters().get(0).getName();
				codeBuilder.append("new Catalano.Imaging.Filters.Crop(0, 0, min, min).ApplyInPlace(" + input + ");\n");
				codeBuilder.append("new Catalano.Imaging.Filters.Resize(250, 250).applyInPlace(" + input + ");\n");
				break;
			}
			default: {
				String codeForAction = CodePlanningUtil.getCodeForAction(a);
				codeBuilder.append(codeForAction);
				codeBuilder.append("\n");

				/*
				 * append code snippet to assign the binary pattern stuff to the respective variable in the code
				 */
				if (a.getOperation().getName().startsWith("Catalano.Imaging.Texture.BinaryPattern.")) {
					String[] split = codeForAction.split(" ");
					codeBuilder.append("bp = " + split[1] + ";\n");
				}
			}
			}
		}

		/* return code */
		return codeBuilder.toString();
	}

	/**
	 * This is a helper that is needed when buildClassifier is supposed to be executed in an outsourced process since no call-by-reference is supported then.
	 **/
	public static Classifier buildClassifier(Classifier c, Instances data) {
		try {
			c.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public static JsonNode serializePipelineToJson(MLPipeline pl) {
		ArrayNode array = JsonNodeFactory.instance.arrayNode();

		/* insert first element of pl */
		for (SupervisedFilterSelector pp : pl.getPreprocessors()) {

			ObjectNode on = JsonNodeFactory.instance.objectNode();
			on.put("type", "preprocessor");
			ObjectNode details = JsonNodeFactory.instance.objectNode();

			/* describe searcher */
			ObjectNode searcherNode = JsonNodeFactory.instance.objectNode();
			searcherNode.put("algorithm", pp.getSearcher().getClass().getName());
			ArrayNode optArray = JsonNodeFactory.instance.arrayNode();
			for (String opt : ((OptionHandler) pp.getSearcher()).getOptions())
				optArray.add(opt);
			searcherNode.set("parameters", optArray);
			details.set("searcher", searcherNode);

			/* describe evaluator */
			ObjectNode evaluatorNode = JsonNodeFactory.instance.objectNode();
			evaluatorNode.put("algorithm", pp.getEvaluator().getClass().getName());
			ArrayNode evalOptArray = JsonNodeFactory.instance.arrayNode();
			for (String opt : ((OptionHandler) pp.getEvaluator()).getOptions())
				evalOptArray.add(opt);
			evaluatorNode.set("parameters", evalOptArray);
			details.set("evaluator", evaluatorNode);

			/* add preprocessing description */
			on.set("description", details);
			array.add(on);
		}

		/* create classifier node */
		ObjectNode on = JsonNodeFactory.instance.objectNode();
		on.put("type", "classifier");
		ObjectNode classifierNode = JsonNodeFactory.instance.objectNode();
		classifierNode.put("algorithm", pl.getBaseClassifier().getClass().getName());
		ArrayNode classifierOptArray = JsonNodeFactory.instance.arrayNode();
		for (String opt : ((OptionHandler) pl.getBaseClassifier()).getOptions())
			classifierOptArray.add(opt);
		classifierNode.set("parameters", classifierOptArray);
		on.set("description", classifierNode);
		array.add(on);
		return array;
	}

	public static SequentialComposition pipelineToComposition(MLPipeline pl, CompositionDomain domain) {
		throw new UnsupportedOperationException("Currently no transformation of pipelines to sequential compositions possible");
		// SequentialComposition comp = new SequentialComposition(domain);
		// TaskProblemGenerator tpg = new TaskProblemGenerator();
		// for (CEOCAction action : pl.getCreationPlan()) {
		// Map<VariableParam, LiteralParam> inputMapping = new HashMap<>(action.getGrounding());
		// for (VariableParam output : action.getOperation().getOutputs())
		// inputMapping.remove(output);
		// Map<VariableParam, VariableParam> outputMapping = new HashMap<>();
		// for (VariableParam output: action.getOperation().getOutputs()) {
		// ConstantParam vp = action.getGrounding().get(output);
		// outputMapping.put(output, new VariableParam(vp.getName(), vp.getType()));
		// }
		//
		// comp.addOperationInvocation(new OperationInvocation(tpg.operationFromJAICoreToCRCSetting(action.getOperation()), inputMapping, outputMapping));
		// }
		// return comp;
	}

	public static boolean didLastActionAffectPipeline(List<TFDNode> path) {
		Literal resolvedProblem = path.get(path.size() - 2).getRemainingTasks().get(0);
		String taskName = resolvedProblem.getPropertyName().substring(resolvedProblem.getPropertyName().indexOf("-") + 1).toLowerCase();
		boolean matches = taskName.matches("(addsingleparam|addoption|addvaluedparam|addoptionpair|noaddsingleparam|noaddvaluedparam|configchildnodest)");
		if (matches)
			return true;
		if (taskName.contains("__construct"))
			return true;
		return false;
	}

	public static List<String> getObjectsInSet(Monom state, String setDescriptor) {
		if (setDescriptor.startsWith("{") && setDescriptor.endsWith("}")) {
			return new ArrayList<>(SetUtil.unserialize(setDescriptor));
		}
		return state.stream().filter(l -> l.getPropertyName().equals("in") && l.getParameters().get(1).getName().equals(setDescriptor))
				.map(l -> l.getConstantParams().get(0).getName()).collect(Collectors.toList());
	}
}
