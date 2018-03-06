package de.upb.crc901.mlplan.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

/**
 * 
 * A class that helps 'plan' a ML-pipeline.
 * 
 * Currently a ML-pipeline only exists of a series of AttributeSelection's (0 or more) followed by a single classifier.
 * 
 * @author aminfaez
 *
 */
public class MLPipelinePlan implements Serializable {
	
	
	/** TODO UGLY HACK! */
	public static String hostJASE;
	public static String hostPASE;
	
	// list of preprocessors
	private List<MLPipe> atrPipes = new LinkedList<>();
	
	// Points to the end of the pipeline.
	private MLPipe cPipe;

	// contains the host name of the next added pipeline.
	private String nextHost;
	
	public MLPipelinePlan onHost(String hostname) {
		this.nextHost = Objects.requireNonNull(hostname);
		return this;
	}
	
	public MLPipelinePlan onHost(String host, int port) {
		return onHost(host + ":" + port);
	}
	
	public MLPipe addAttributeSelection(String classname) {

		/* we assume, by default, that this method is only called for python instances */
		this.onHost(hostPASE);
		
		Objects.requireNonNull(this.nextHost, "Host needs to be specified before adding pipes to the pipeline.");
		MLPipe asPipe =  new MLPipe(this.nextHost, Objects.requireNonNull(classname));
		atrPipes.add(asPipe); // add to pipe list before returning.
		return asPipe;
	}
	
	public void addOptions(MLPipe pipe, String option, Object value) {
		pipe.addOptions(option + " " + value.toString());
	}
	

	public WekaAttributeSelectionPipe addWekaAttributeSelection(ASSearch searcher, ASEvaluation eval) {
		Objects.requireNonNull(searcher);
		Objects.requireNonNull(eval);
		WekaAttributeSelectionPipe asPipe =  new WekaAttributeSelectionPipe(hostJASE);
		asPipe.withSearcher(searcher.getClass().getName());
		if(searcher instanceof OptionHandler) {
			String searchOptions[] = ((OptionHandler) searcher).getOptions();
			asPipe.addSearchOptions(searchOptions);
		}
		asPipe.withEval(eval.getClass().getName());
		if(eval instanceof OptionHandler) {
			String evalOptions[] = ((OptionHandler) eval).getOptions();
			asPipe.addEvalOptions(evalOptions);
		}
		atrPipes.add(asPipe);
		return asPipe;
	}
	
	
	public MLPipe setClassifier(String classifierName) {
		
		/* we assume, by default, that this method is only called for python instances */
		this.onHost(hostPASE);
				
		Objects.requireNonNull(this.nextHost, "Host needs to be specified before adding pipes to the pipeline.");
		this.cPipe = new MLPipe(this.nextHost, classifierName); // set cPipe field.
		return cPipe;
	}
	
	public MLPipe setClassifier(Classifier wekaClassifier) {
		Objects.requireNonNull(wekaClassifier);
		String classname = wekaClassifier.getClass().getName();
		this.cPipe = new MLPipe(hostJASE, classname);
		if(wekaClassifier instanceof OptionHandler) {
			String[] options = ((OptionHandler) wekaClassifier).getOptions();
			cPipe.addOptions(options);
		}
		return cPipe;
	}
	
	/**
	 * Returns True if the plan is 'valid' in the sense that a classifier was set.
	 */
	public boolean isValid() {
		if(cPipe == null) { // if classifier is null return false immediately
			return false;
		}
		for(MLPipe pipe : atrPipes) { 
			if(!pipe.isValid()) {
				return false;
			}
		}
		return true;
	}
	
	public List<MLPipe> getAttrSelections(){
		return atrPipes;
	}
	
	public MLPipe getClassifierPipe() {
		return cPipe;
	}
	
	
	
	// CLASSES for pipe creation.
	abstract class AbstractPipe implements Serializable {
		private final String host;
		
		protected AbstractPipe(String hostname) {
			this.host = Objects.requireNonNull(hostname);
		}
		
		protected String getHost() {
			return this.host;
		}

		protected boolean isValid() {
			return true;
		}
	}
	
	public class MLPipe extends AbstractPipe {
		private final String classifierName;
		private final Set<String> classifierOptions = new TreeSet<>();
		private final List<Object> constructorArgs = new ArrayList<>(); 
		
		protected MLPipe(String hostname, String classifierName) {
			super(hostname);
			this.classifierName = Objects.requireNonNull(classifierName);
		}
		

		public MLPipe addOptions(String...additionalOptions) {
			Objects.requireNonNull(additionalOptions);
			for(String newOption : additionalOptions) {
				classifierOptions.add(newOption);
			}
			return this;
		}
		
		public MLPipe addConstructorArgs(Object... args) {
			Objects.requireNonNull(args);
			for(Object newArg : args) {
				this.constructorArgs.add(newArg);
			}
			return this;
		}
		
		public String getName() {
			return classifierName;
		}
		
		public String getQualifiedName() {
			return classifierName;
		}
		
		public ArrayList<String> /*ArrayList was explicitly used*/ getOptions(){
			ArrayList<String> options = new ArrayList<>();
			options.addAll(classifierOptions);
			return options;
		}
		public Object[] getArguments() {
			return constructorArgs.toArray();
		}
		
	}

	class WekaAttributeSelectionPipe extends MLPipe {
		private String searcherName, evalName; 
		public static final String classname = "weka.attributeSelection.AttributeSelection";
		protected WekaAttributeSelectionPipe(String host) {
			super(host, classname);
		}
		private List<String> 	searcherOptions = new ArrayList<>(), 
							evalOptions = new ArrayList<>();
		
		public WekaAttributeSelectionPipe withSearcher(String searcherName) {
			this.searcherName = Objects.requireNonNull(searcherName);
			return this;
		} 
		
		public WekaAttributeSelectionPipe withEval(String evaluator) {
			this.evalName = Objects.requireNonNull(evaluator);
			return this;
		}

		public WekaAttributeSelectionPipe addSearchOptions(String... additionalOptions) {
			addToOptionList(searcherOptions, additionalOptions);
			return this;
		}
		
		public WekaAttributeSelectionPipe addEvalOptions(String... additionalOptions) {
			addToOptionList(evalOptions, additionalOptions);
			return this;
		}
		
		private void addToOptionList(List<String> optionList, String[] additionalOptions) {
			Objects.requireNonNull(additionalOptions);
			for(String newOption : additionalOptions) {
				optionList.add(newOption);
			}
		}
		
		public String getSearcher() {
			return searcherName;
		}
		public String getEval() {
			return evalName;
		}
		
		public String getQualifiedName() {
			return searcherName + "/" + evalName;
		}
		
		public ArrayList<String> getSearcherOptions(){
			ArrayList<String> options = new ArrayList<>();
			options.addAll(searcherOptions);
			return options;
		}
		
		public ArrayList<String> getEvalOptions(){
			ArrayList<String> options = new ArrayList<>();
			options.addAll(evalOptions);
			return options;
		}

		protected boolean isValid() {
			if(isWekaAS() && (searcherName == null || evalName == null)) {
				return false;
			}
			return true;
		}
		
		public boolean isWekaAS() {
			return "weka.attributeSelection.AttributeSelection".equals(getName());
		}

	}
}
