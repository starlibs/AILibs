package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import defaultEval.core.Util.ParamType;
import hasco.model.BooleanParameterDomain;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import hasco.serialization.ComponentLoader;
import jaicore.processes.ProcessUtil;
import scala.annotation.elidable;

public class HyperbandOptimizer extends Optimizer{
	

	public HyperbandOptimizer(Component searcher, Component evaluator, Component classifier, String dataSet, File environment, File dataSetFolder, int seed, int maxRuntimeParam, int maxRuntime) {
		super(searcher, evaluator, classifier, dataSet, environment, dataSetFolder, seed, maxRuntimeParam, maxRuntime);
	}

	@Override
	public void optimize() {
		Locale.setDefault(Locale.ENGLISH);
		
		generatePyMain();
		generatePyWrapper();
		
		// run hyperband
		// start SMAC
		try {
			Runtime rt = Runtime.getRuntime();
			
			ArrayList<String> cmd = new ArrayList<>();
			cmd.add("python");
			cmd.add("-u");
			cmd.add(String.format("%s/optimizer/hyperband/main_%s.py", environment.getAbsolutePath(), buildFileName()));
			
			
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(environment.getAbsoluteFile());
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.PIPE);
			final Process proc = pb.start();
			
			InputStream i = proc.getInputStream();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					long start_time = System.currentTimeMillis();
					
					while (System.currentTimeMillis() - start_time < maxRuntime*1000) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO 
							e.printStackTrace();
						}
					}
					
					int id = ProcessUtil.getPID(proc);
					try {
						ProcessUtil.killProcess(id);
					} catch (IOException e) {
						// TODO
						e.printStackTrace();
					}
				}
			}).start();
			
			int r = 0;
			
			
			while ((r = i.read()) != -1) {
				System.out.write(r);
			}
			
			int exitValue = proc.exitValue();
			
			createFinalInstances();
			
			
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		System.out.println("final-searcher: " + finalSearcher);
		System.out.println("final-evaluator: " + finalEvaluator);
		System.out.println("final-classifier: " + finalClassifier);
	}

	private void createFinalInstances() throws FileNotFoundException, IOException, ParseException {
		Scanner sc = new Scanner(new File(environment.getAbsolutePath() + "/hyperband-output/" + buildFileName() + ".json"));
		StringBuilder sb = new StringBuilder();
		while (sc.hasNextLine()) {
			sb.append(sc.nextLine());
			sb.append("\r\n");
		}
		sc.close();
		
		JSONArray resultList = new JSONArray(sb.toString());
		JSONObject bestResult = null;
		for (int i = 0; i < resultList.length(); i++) {
			JSONObject resultInstance = resultList.getJSONObject(i);
			if(bestResult == null || resultInstance.getDouble("loss") < bestResult.getDouble("loss")) {
				bestResult = resultInstance;
			}
		}
		
		JSONObject params = bestResult.getJSONObject("params");
		
		HashMap<String, String> searcherParameter = new HashMap<>();
		HashMap<String, String> evaluatorParameter = new HashMap<>();
		HashMap<String, String> classifierParameter = new HashMap<>();
		
		if(searcher != null) {
			// get preprocessor config
			for (Parameter parameter : searcher.getParameters()) {
				searcherParameter.put(parameter.getName(), params.get(getUniqueParamName(parameter, ParamType.searcher)).toString());
			}
			for (Parameter parameter : evaluator.getParameters()) {
				evaluatorParameter.put(parameter.getName(), params.get(getUniqueParamName(parameter, ParamType.evaluator)).toString());
			}
		}
		for (Parameter parameter : classifier.getParameters()) {
			classifierParameter.put(parameter.getName(), params.get(getUniqueParamName(parameter, ParamType.classifier)).toString());
		}
		
		finalSearcher = new ComponentInstance(searcher, searcherParameter, new HashMap<>());
		finalEvaluator = new ComponentInstance(evaluator, evaluatorParameter, new HashMap<>());
		finalClassifier = new ComponentInstance(classifier, classifierParameter, new HashMap<>());
		
	}

	private void generatePyWrapper() {
		// generate py-wrapper file
		PrintStream pyWrapperStream = null;
		
		try {
			pyWrapperStream = new PrintStream(new FileOutputStream(new File(environment.getAbsolutePath() + "/optimizer/hyperband/generated/" + "wrapper_" + buildFileName() + ".py")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		pyWrapperStream.println("from common_defs import *");
		pyWrapperStream.println("import sys, math");
		pyWrapperStream.println("from subprocess import call");
		
		pyWrapperStream.println("space = { ");
		for (Pair<Parameter, ParamType> parameterPair : parameterList) {
			pyWrapperStream.println("\t" + getSpaceEntryByDomain(parameterPair) + ",");	
		}
		pyWrapperStream.println("}");
		
		pyWrapperStream.println("def get_params():");
		pyWrapperStream.println("\tparams = sample( space )");
		pyWrapperStream.println("\treturn handle_integers( params )");
		
		
		pyWrapperStream.println("def try_params( n_iterations, params ):");
		pyWrapperStream.println(String.format("\tcall(\"java -jar %s/PipelineEvaluator.jar %s %d %s %s %s %s %s %s %s\")",
				environment.getAbsolutePath(),
				dataSetFolder.getAbsolutePath() + "/" + dataSet + ".arff",
				seed,
				(searcher != null) ? searcher.getName() : "null",
				(searcher != null) ? generateParamList(searcher, ParamType.searcher) : "",
				(searcher != null) ? evaluator.getName() : "",
				(searcher != null) ? generateParamList(evaluator, ParamType.evaluator) : "",
				classifier.getName(),
				generateParamList(classifier, ParamType.classifier),
				environment.getAbsolutePath() + "/results/" + buildFileName() + ".txt"
				));
		pyWrapperStream.println(String.format("\tfile = open(\"%s/results/%s.txt\", \"r\")", environment.getAbsolutePath(), buildFileName()));
		
		pyWrapperStream.println("\treturn {'loss': float(file.read())}");
		
		pyWrapperStream.close();
		
	}
	
	private void generatePyMain() {
		// generate py-wrapper file
		PrintStream pyWrapperStream = null;
		
		try {
			pyWrapperStream = new PrintStream(new FileOutputStream(new File(environment.getAbsolutePath() + "/optimizer/hyperband/" + "main_" + buildFileName() + ".py")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		pyWrapperStream.println("#!/usr/bin/env python");
		pyWrapperStream.println("from hyperband import Hyperband");
		pyWrapperStream.println("import json");
		pyWrapperStream.println(String.format("from generated.wrapper_%s import get_params, try_params", buildFileName()));
		
		pyWrapperStream.println("hb = Hyperband( get_params, try_params )");
		pyWrapperStream.println(String.format("results = hb.run(output_path=\"%s\", timeout=%d)", environment.getAbsolutePath() + "/hyperband-output/" + buildFileName() + ".json", maxRuntimeParam));
		
		pyWrapperStream.close();
	}
	
	private String createDomainWrapper(String input, ParameterDomain pd) {
		if(pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;
			return n_pd.isInteger() ? input : "\"{:.9f}\".format("+input+")";
		}
		
		return input;
	}
	
	
	
	public static void main(String[] args) {
		
		ComponentLoader cl_p = new ComponentLoader();
		ComponentLoader cl_c = new ComponentLoader();
		
		try {
			Util.loadClassifierComponents(cl_c, "F:\\Data\\Uni\\PG\\DefaultEvalEnvironment");
			Util.loadPreprocessorComponents(cl_p, "F:\\Data\\Uni\\PG\\DefaultEvalEnvironment");	
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Component searcher = null;
		Component evaluator = null;
		Component classifier = null;
		
		for (Component c : cl_p.getComponents()) {
			if(c.getName().equals("weka.attributeSelection.BestFirst")) {
				searcher = c;
			}
		}
		
		for (Component c : cl_p.getComponents()) {
			if(c.getName().equals("weka.attributeSelection.CorrelationAttributeEval")) {
				evaluator = c;
			}
		}
		
		for (Component c : cl_c.getComponents()) {
			if(c.getName().equals("weka.classifiers.functions.Logistic")) {
				classifier = c;
			}
		}
		
		HyperbandOptimizer o = new HyperbandOptimizer(searcher, evaluator, classifier, "breast-cancer", new File("F:\\Data\\Uni\\PG\\DefaultEvalEnvironment"),new File("F:\\Data\\Uni\\PG\\DefaultEvalEnvironment\\datasets"), 0, 200, 10);
		o.optimize();
	}
	
	
	private String generateParamList(Component c, ParamType t) {
		StringBuilder sb = new StringBuilder();
		
		for (Parameter parameter : c.getParameters()) {
			sb.append(" " + parameter.getName());
			sb.append(String.format(" \"+ %s + \"", createDomainWrapper(String.format("params['%s']", getUniqueParamName(parameter, t)), parameter.getDefaultDomain())));
		}
		
		return sb.toString();
	}
	
	
	private String getSpaceEntryByDomain(Pair<Parameter, ParamType> p) {
		ParameterDomain pd = p.getFirst().getDefaultDomain();

		// Numeric (integer or real/double)
		if(pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;
			
			if(n_pd.isInteger()) {
				// int
				return String.format("'%s': hp.choice( '%s', range(%d, %d, 1))", getUniqueParamName(p), getUniqueParamName(p), n_pd.getMin(), n_pd.getMax()+1);
			}else {
				// float
				return String.format("'%s': hp.uniform( '%s', %f, %f)", getUniqueParamName(p),getUniqueParamName(p), n_pd.getMin(), n_pd.getMax());
			}	
		}
		
		// Boolean (categorical)
		else if(pd instanceof BooleanParameterDomain) {
			BooleanParameterDomain b_pd = (BooleanParameterDomain) pd;
			return String.format("'%s': hp.choice( '%s', (True, False))",getUniqueParamName(p),getUniqueParamName(p));
		}
		
		//categorical
		else if(pd instanceof CategoricalParameterDomain) {
			CategoricalParameterDomain c_pd = (CategoricalParameterDomain) pd;
			
			StringBuilder sb = new StringBuilder(String.format("'%s': hp.choice( '%s', (", getUniqueParamName(p), getUniqueParamName(p)));
			for (int i = 0; i < c_pd.getValues().length; i++) {
				sb.append("'" + c_pd.getValues()[i] + "'");
				
				if(i != c_pd.getValues().length - 1) {
					sb.append(",");
				}
			}
			sb.append("))");

			return sb.toString();
		}
		return "";
	}
	
	
	
}
