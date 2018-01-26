package de.upb.crc901.mlplan.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;

public class CodePlanningUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(CodePlanningUtil.class);

	public static String getCodeForAction(CEOCAction a) {

		CEOCOperation op = a.getOperation();
		Map<VariableParam, ConstantParam> grounding = a.getGrounding();
		ConstantParam outParam = op.getOutputs().isEmpty() ? null : grounding.get(op.getOutputs().get(0));
		List<ConstantParam> inputs = new ArrayList<>(SetUtil.difference(op.getParams(), op.getOutputs()).stream().map(v -> a.getGrounding().get(v)).collect(Collectors.toList()));

		String[] opSplit = a.getOperation().getName().split(":");
		if (opSplit.length != 2) {
			throw new IllegalArgumentException("Cannot generate code for action " + a);
		}
		String className = opSplit[0];
		String methodName = opSplit[1];

		/* if this is a constructor call, create the object */
		if (methodName.equals("__construct")) {
			return className + " " + outParam + " = new " + className + "();\n";
		} else {
			if (inputs.size() > 0) {
				if (outParam != null) {
					return "Object " + outParam + " = " + CodePlanningUtil.getCodeForMethodInvocation(methodName, inputs);
				} else {
					return CodePlanningUtil.getCodeForMethodInvocation(methodName, inputs);
				}
			} else
				logger.info("Ignoring {}", a.getEncoding());
		}
		throw new IllegalArgumentException("Cannot generate code for action " + a);
	}

	public static String getCodeForMethodInvocation(String methodName, List<ConstantParam> inputs) {
		StringBuilder codeBuilder = new StringBuilder();
		codeBuilder.append(inputs.get(0) + "." + methodName + "(");
		for (int input = 1; input < inputs.size(); input++) {
			if (input >= 2)
				codeBuilder.append(", ");
			codeBuilder.append(inputs.get(input));
		}
		codeBuilder.append(");\n");
		return codeBuilder.toString();
	}

	public static String getListAppendCode(String list, String value) {
		return list + ".add(\"" + value + "\");\n";
	}
	
	public static String getListAppendAllCode(String list, String collectionName) {
		return list + ".addAll(\"" + collectionName + "\");\n";
	}

	public static String getPreprocessorEvaluatorFromPipelineGenerationCode(List<String> code) {
		Pattern p = Pattern.compile("new weka\\.attributeSelection\\.(.*)\\(");
		List<Matcher> ppMatchers = code.stream().map(line -> p.matcher(line)).filter(m -> m.find()).collect(Collectors.toList());
		String ppSeq = "";
		for (Matcher m : ppMatchers) {
			String pp = m.group(1);
			if (pp.equals("AttributeSelection"))
				continue;
			if (ppSeq.isEmpty())
				ppSeq += pp;
			else {
				ppSeq += ("&" + pp);
				return ppSeq;
			}
		}
		return ppSeq;
	}
	
	public static String getClassifierFromPipelineGenerationCode(List<String> code) {
		Optional<String> classifierLine = code.stream().filter(line -> line.contains("new") && line.contains("classifiers")).findAny();
		if (!classifierLine.isPresent())
			return "";
		Pattern pattern = Pattern.compile("new weka\\.classifiers\\.[^\\.]+\\.(.*)\\(");
		Matcher m = pattern.matcher(classifierLine.get());
		if (!m.find())
			return "";
		String classifier = m.group(1);
		return classifier;
	}
}
