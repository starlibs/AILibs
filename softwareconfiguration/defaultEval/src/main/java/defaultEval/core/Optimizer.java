package defaultEval.core;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import defaultEval.core.Util.ParamType;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;

public abstract class Optimizer {

	protected Component searcher;
	protected Component evaluator;
	protected Component classifier;
	protected String dataSet;
	
	protected ArrayList<Pair<Parameter, ParamType>> parameterList = new ArrayList<>();
	
	
	protected ComponentInstance finalSearcher;
	protected ComponentInstance finalEvaluator;
	protected ComponentInstance finalClassifier;
	
	protected File environment;
	protected File dataSetFolder;
	
	int seed = 0;
	
	protected int maxRuntimeParam;
	protected int maxRuntime;
	
	
	public Optimizer(Component searcher, Component evaluator, Component classifier, String dataSet, File environment, File dataSetFolder, int seed, int maxRuntimeParam, int maxRuntime) {
		this.searcher = searcher;
		this.evaluator = evaluator;
		
		this.classifier = classifier;
		
		this.dataSet = dataSet;
		
		if(searcher != null) {
			for (Parameter p : searcher.getParameters()) {
				parameterList.add(new Pair<>(p, ParamType.searcher));
			}
			for (Parameter p : evaluator.getParameters()) {
				parameterList.add(new Pair<>(p, ParamType.evaluator));
			}
		}
		
		for (Parameter p : classifier.getParameters()) {
			parameterList.add(new Pair<>(p, ParamType.classifier));
		}
		
		this.environment = environment;
		this.dataSetFolder = dataSetFolder;
		this.seed = seed;
		this.maxRuntimeParam = maxRuntimeParam;
		this.maxRuntime = maxRuntime;
		
	}

	public abstract void optimize();

	
	protected String buildFileName() {
		StringBuilder sb = new StringBuilder((searcher != null) ? (searcher.getName()+"_"+evaluator.getName()) : "null");
		sb.append("_");
		sb.append(classifier.getName());
		sb.append("_");
		sb.append(dataSet);
		sb.append("_");
		sb.append(seed);
		return sb.toString().replaceAll("\\.", "").replaceAll("-", "_");
	}
	
	
	public int getDoubleStringAsInt(String str) {
		return (int)Double.valueOf(str).doubleValue();
	}
	
	
	public String correctParameterSyntax(String input, ParameterDomain pd) {
		if(pd instanceof NumericParameterDomain) {
			NumericParameterDomain d = (NumericParameterDomain) pd;
			if(d.isInteger()) {
				return Integer.toString(getDoubleStringAsInt(input));
			}
		}
		return input;
	}
	
	protected String getUniqueParamName(Parameter p, ParamType t) {
		return Util.convertToUniqueParamName(p.getName(), t);
	}
	
	protected String getUniqueParamName(Pair<Parameter, ParamType> p) {
		return Util.convertToUniqueParamName(p.getFirst().getName(), p.getSecond());
	}
	
	
	public ComponentInstance getFinalClassifier() {
		return finalClassifier;
	}
	
	public ComponentInstance getFinalEvaluator() {
		return finalEvaluator;
	}
	
	public ComponentInstance getFinalSearcher() {
		return finalSearcher;
	}
	
}
