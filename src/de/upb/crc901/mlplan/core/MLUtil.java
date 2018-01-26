package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.upb.crc901.configurationsetting.compositiondomain.CompositionDomain;
import de.upb.crc901.configurationsetting.operation.OperationInvocation;
import de.upb.crc901.configurationsetting.operation.SequentialComposition;
import de.upb.crc901.mlplan.test.IDistSearchConf;
import jaicore.basic.FileUtil;
import jaicore.basic.SetUtil;
import jaicore.basic.StringUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.OptionHandler;

public class MLUtil {

	private static Logger logger = LoggerFactory.getLogger(MLUtil.class);

	public static MLPipeline extractPipelineFromPlan(List<CEOCAction> plan) {

		/* read config */
		IDistSearchConf props = ConfigCache.getOrCreate(IDistSearchConf.class);
//		File folderToStoreASSerializations = new File(props.getASSFolder());
//		logger.info("Use {} as folder for serializations of AttributeSelectors", folderToStoreASSerializations);

		StringBuilder creationString = new StringBuilder();
		plan.stream().forEach(a -> creationString.append(a.toString() + "\n"));
		try {

			/* now execute a light version of the plan that does not use any input. This way, we obtain the filter (preprocessor) and classifier objects created by the plan */
			PlanExecutor executor = new PlanExecutor(new Random());
			// List<CEOCAction> reducedPlan = plan.stream()
			// .filter(a -> !a.getGrounding().values().contains(new ConstantParam("x")) && !(a.getOperation().getName().equals("inheritInstanceProp")
			// || a.getOperation().getName().contains("setInputFormat") || a.getOperation().getName().contains("useFilter")))
			// .collect(Collectors.toList());
			Map<ConstantParam, Object> variables = executor.executePlan(plan, new HashMap<>());

			/* now extract the filter and the classifier object */
			Optional<?> classifier = variables.values().stream().filter(o -> o instanceof Classifier).findFirst();
			Optional<?> as = variables.values().stream().filter(o -> o instanceof AttributeSelection).findFirst();
			// Optional<?> searcher = variables.values().stream().filter(o -> o instanceof ASSearch).findFirst();
			// Optional<?> evaluator = variables.values().stream().filter(o -> o instanceof ASEvaluation).findFirst();
			if (as.isPresent()) {
				// ConstantParam varWithAttributeSelector = variables.keySet().stream().filter(k -> variables.get(k).equals(as.get())).findAny().get();
				ConstantParam varWithSearchSetter = plan.stream().filter(a -> a.getOperation().getName().endsWith("setSearch")).map(a -> a.getParameters().get(1)).findAny().get();
				ConstantParam varWithEvalSetter = plan.stream().filter(a -> a.getOperation().getName().endsWith("setEvaluator")).map(a -> a.getParameters().get(1)).findAny().get();

				ASSearch asSearch = (ASSearch) variables.get(varWithSearchSetter);
				ASEvaluation asEval = (ASEvaluation) variables.get(varWithEvalSetter);
				// Optional<CEOCAction> setterAction = plan.stream().filter(a -> a.getOperation().getName().endsWith("setOptions") && a.getParameters().get(0).equals(varWithEvalSetter)).findAny();
				// if (setterAction.isPresent()) {
				// System.out.println("Actually adopted options for eval: ");
				// System.out.println("\tAction:" + setterAction.get());
				// System.out.println("\tOption variable: " + Arrays.toString((String[])variables.get(setterAction.get().getParameters().get(1))));
				// System.out.println("\tParams used for eval: " + Arrays.toString(((OptionHandler)asEval).getOptions()));
				// }
				// if (classifier.get() instanceof OptionHandler)
				// System.out.println("\tfor classifier: " + Arrays.toString(((OptionHandler)classifier.get()).getOptions()));
				// if (asSearch instanceof OptionHandler)
				// System.out.println("\tfor search: " + Arrays.toString(((OptionHandler)asSearch).getOptions()));
				//
				// asFile = new File(folderToStoreASSerializations + File.separator + asSearch.getClass().getSimpleName() + "_" + asEval.getClass().getSimpleName());
				return new MLPipeline(plan, asSearch, asEval, classifier.isPresent() ? (Classifier) classifier.get() : null);
			} else
				return new MLPipeline(plan, null, null, classifier.isPresent() ? (Classifier) classifier.get() : null);
		} catch (Throwable e) {
			System.err.println("Could not execute the following code: " + getJavaCodeFromPlan(plan));
			System.err.println("Code created from: ");
			plan.stream().forEach(a -> System.err.println("\t " + a.getEncoding()));
			e.printStackTrace();
			return null;
		}
	}

	public static CEOCIPTFDGraphGenerator getGraphGenerator(File testsetFile, File evaluablePredicateFile, Instances dataset) throws IOException {
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
		return new CEOCIPTFDGraphGenerator(new TaskProblemGenerator().getProblem(testsetFile, dataset), evaluablePredicateMap, null);
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
			case "configReduction":
				break;
			case "assignTo":
				codeBuilder.append("String " + outParam.getName());
				codeBuilder.append("= \"");
				codeBuilder.append(inputs.get(0).getName());
				codeBuilder.append("\";\n");
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
		for (SuvervisedFilterSelector pp : pl.getPreprocessors()) {

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

	public static MLPipeline copyPipeline(MLPipeline pl) {
		return extractPipelineFromPlan(pl.getCreationPlan());
	}

	public static SequentialComposition pipelineToComposition(MLPipeline pl, CompositionDomain domain) {
		SequentialComposition comp = new SequentialComposition(domain);
		TaskProblemGenerator tpg = new TaskProblemGenerator();
		for (CEOCAction action : pl.getCreationPlan()) {
			Map<VariableParam, LiteralParam> inputMapping = new HashMap<>(action.getGrounding());
			for (VariableParam output : action.getOperation().getOutputs())
				inputMapping.remove(output);
			Map<VariableParam, VariableParam> outputMapping = new HashMap<>();
			for (VariableParam output: action.getOperation().getOutputs()) {
				ConstantParam vp = action.getGrounding().get(output);
				outputMapping.put(output, new VariableParam(vp.getName(), vp.getType()));
			}

			comp.addOperationInvocation(new OperationInvocation(tpg.operationFromJAICoreToCRCSetting(action.getOperation()), inputMapping, outputMapping));
		}
		return comp;
	}

	public static boolean didLastActionAffectPipeline(List<TFDNode> path) {
		Literal resolvedProblem = path.get(path.size() - 2).getRemainingTasks().get(0);
		String taskName = resolvedProblem.getPropertyName().substring(resolvedProblem.getPropertyName().indexOf("-") + 1).toLowerCase();
		boolean matches = taskName.matches("(addsingleparam|addoption|addvaluedparam|addoptionpair|noaddsingleparam|noaddvaluedparam)");
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
