package defaultEval.core;

import java.util.ArrayList;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;

public abstract class Optimizer {

	protected Component preProcessor;
	protected Component classifier;
	protected String dataSet;
	
	protected ArrayList<Parameter> parameterList = new ArrayList<>();
	
	public Optimizer(Component preProcessor, Component classifier, String dataSet) {
		this.preProcessor = preProcessor;
		this.classifier = classifier;		
		
		this.dataSet = dataSet;
		
		if(preProcessor != null) {
			for (Parameter p : preProcessor.getParameters()) {
				parameterList.add(p);
			}
		}
		
		for (Parameter p : classifier.getParameters()) {
			parameterList.add(p);
		}
	}

	
	public abstract ComponentInstance optimize();
	
	
}
