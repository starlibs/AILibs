package defaultEval.core;

import java.io.File;
import java.util.ArrayList;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;

public abstract class Optimizer {

	protected Component searcher;
	protected Component evaluator;
	protected Component classifier;
	protected String dataSet;
	
	protected ArrayList<Parameter> parameterList = new ArrayList<>();
	
	protected ComponentInstance finalSearcher;
	protected ComponentInstance finalEvaluator;
	protected ComponentInstance finalClassifier;
	
	protected File environment;
	
	public Optimizer(Component searcher, Component evaluator, Component classifier, String dataSet, File environment) {
		this.searcher = searcher;
		this.evaluator = evaluator;
		
		this.classifier = classifier;
		
		this.dataSet = dataSet;
		
		if(searcher != null) {
			for (Parameter p : searcher.getParameters()) {
				parameterList.add(p);
			}
			for (Parameter p : evaluator.getParameters()) {
				parameterList.add(p);
			}
		}
		
		for (Parameter p : classifier.getParameters()) {
			parameterList.add(p);
		}
		
		this.environment = environment;
		
	}

	public abstract void optimize();

	
	
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
