package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.commons.math3.util.Pair;
import org.json.simple.parser.ParseException;

import defaultEval.core.Util.ParamType;
import hasco.model.BooleanParameterDomain;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import hasco.serialization.ComponentLoader;

public class GGAOptimizer extends Optimizer {

	public GGAOptimizer(Component searcher, Component evaluator, Component classifier, String dataSet, File environment,
			File dataSetFolder, int seed, int maxRuntimeParam, int maxRuntime) {
		super(searcher, evaluator, classifier, dataSet, environment, dataSetFolder, seed, maxRuntimeParam, maxRuntime);
	}

	@Override
	public void optimize() {
		Locale.setDefault(Locale.ENGLISH);

		generateXML();

		generatePyWrapper();

		// run gga TODO
		/*try {
			Runtime rt = Runtime.getRuntime();

			String command = String.format("");

			final Process proc = rt.exec(command);

			InputStream i = proc.getInputStream();

			new Thread(new Runnable() {

				@Override
				public void run() {
					int r = 0;
					try {
						while ((r = proc.getErrorStream().read()) != -1) {
							System.err.write(r);
						}
					} catch (IOException e) {
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
		}*/

		System.out.println("final-searcher: " + finalSearcher);
		System.out.println("final-evaluator: " + finalEvaluator);
		System.out.println("final-classifier: " + finalClassifier);
	}

	private void createFinalInstances() throws FileNotFoundException, IOException, ParseException {

	}

	private void generatePyWrapper() {
		// generate py-wrapper file
		PrintStream pyWrapperStream = null;
		
		try {
			pyWrapperStream = new PrintStream(new FileOutputStream(new File(environment.getAbsolutePath() + "/optimizer/dgga/dgga-src/generated/" + "wrapper_" + buildFileName() + ".py")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		pyWrapperStream.println("#!/usr/bin/python");
		pyWrapperStream.println("import sys, os");
		pyWrapperStream.println("args = sys.argv");
		
		pyWrapperStream.println("argDict = {}");
		
		pyWrapperStream.println("index = 3");
		pyWrapperStream.println("while index < len(args)");
		pyWrapperStream.println("\tpre,post = args[index].split('=')");
		pyWrapperStream.println("\targDict[pre] = post");
		
		
		pyWrapperStream.println("eargs = []");
		pyWrapperStream.println("eargs.append(\"-jar\")");
		pyWrapperStream.println(String.format("eargs.append(\"%s/PipelineEvaluator.jar\")", environment.getAbsolutePath()));
		pyWrapperStream.println(String.format("eargs.append(\"%s/%s.arff\")", dataSetFolder.getAbsolutePath(), dataSet));
		
		
		if(searcher != null) {
			pyWrapperStream.println(String.format("eargs.append(\"%s\")", searcher.getName()));
			
			for (Parameter p : searcher.getParameters()) {
				pyWrapperStream.println(String.format("eargs.append(\"%s\")", p.getName()));
				pyWrapperStream.println(String.format("eargs.append(argdict[\"%s\"])", getUniqueParamName(p, ParamType.searcher)));
			}
			
			pyWrapperStream.println(String.format("eargs.append(\"%s\")", evaluator.getName()));
			
			for (Parameter p : evaluator.getParameters()) {
				pyWrapperStream.println(String.format("eargs.append(\"%s\")", p.getName()));
				pyWrapperStream.println(String.format("eargs.append(argdict[\"%s\"])", getUniqueParamName(p, ParamType.evaluator)));
			}
		}else {
			pyWrapperStream.println("eargs.append(\"null\")");
		}
		
		pyWrapperStream.println(String.format("eargs.append(\"%s\")", classifier.getName()));
		
		for (Parameter p : classifier.getParameters()) {
			pyWrapperStream.println(String.format("eargs.append(\"%s\")", p.getName()));
			pyWrapperStream.println(String.format("eargs.append(argdict[\"%s\"])", getUniqueParamName(p, ParamType.classifier)));
		}
		
		pyWrapperStream.println(String.format("eargs.append(\"%s/results/%s.txt\")", environment.getAbsolutePath(), buildFileName()));
		
		
		pyWrapperStream.println("eargs.append(args[1])");
		
		pyWrapperStream.println("os.execvp(\"java\", eargs)");
		pyWrapperStream.close();		
	}

	private void generateXML() {
		PrintStream xmlStream = null;

		try {
			xmlStream = new PrintStream(new FileOutputStream(new File(
					environment.getAbsolutePath() + "/optimizer/dgga/dgga-src/generated/" + buildFileName() + ".xml")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		xmlStream.println("<algtune>");

		xmlStream.println("\t<cmd>");
		xmlStream.print(String.format("\t\t./wrapper_%s.py $instances $seed", buildFileName()));
		for (Pair<Parameter, ParamType> pair : parameterList) {
			xmlStream.print(" $" + getUniqueParamName(pair));
		}
		xmlStream.println();
		xmlStream.println("</cmd>");

		xmlStream.println("<seedgenome>");
		xmlStream.println("<variable name=\"root\" value=\"0\" />");
		for (Pair<Parameter, ParamType> pair : parameterList) {
			xmlStream.println(String.format("<variable name=\"%s\" value=\"%s\" />", getUniqueParamName(pair),
					pair.getFirst().getDefaultValue()));
		}
		xmlStream.println("</seedgenome>");

		xmlStream.println("<node type=\"and\" name=\"root\" start=\"0\" end=\"0\">");

		for (Pair<Parameter, ParamType> pair : parameterList) {
			xmlStream.println(String.format("<node type=\"and\" name=\"%s\" prefix=\"%s=\" %s />",
					getUniqueParamName(pair), getUniqueParamName(pair), generateParameterSpace(pair)));
		}

		xmlStream.println("</node>");
		xmlStream.println("</algtune>");

		xmlStream.close();
	}

	private String generateParameterSpace(Pair<Parameter, ParamType> pair) {
		ParameterDomain pd = pair.getFirst().getDefaultDomain();

		// Numeric (integer or real/double)
		if (pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;

			if (n_pd.isInteger()) {
				// int
				return String.format("start=\"%d\" end=\"&d\"", n_pd.getMin(), n_pd.getMax() + 1);
			} else {
				// float
				return String.format("start=\"%f\" end=\"%f\"", n_pd.getMin(), n_pd.getMax());
			}
		}

		// Boolean (categorical)
		else if (pd instanceof BooleanParameterDomain) {
			BooleanParameterDomain b_pd = (BooleanParameterDomain) pd;
			return "categories=\"true,false\"";
		}

		// categorical
		else if (pd instanceof CategoricalParameterDomain) {
			CategoricalParameterDomain c_pd = (CategoricalParameterDomain) pd;

			StringBuilder sb = new StringBuilder("categories=\"");
			for (int i = 0; i < c_pd.getValues().length; i++) {
				sb.append(c_pd.getValues()[i]);

				if (i != c_pd.getValues().length - 1) {
					sb.append(",");
				}
			}
			sb.append("\"");
			return sb.toString();
		}
		return "";

	}

	

//	public static void main(String[] args) {
//
//		ComponentLoader cl_p = new ComponentLoader();
//		ComponentLoader cl_c = new ComponentLoader();
//
//		try {
//			Util.loadClassifierComponents(cl_c, "F:\\Data\\Uni\\PG\\DefaultEvalEnvironment");
//			Util.loadPreprocessorComponents(cl_p, "F:\\Data\\Uni\\PG\\DefaultEvalEnvironment");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		Component searcher = null;
//		Component evaluator = null;
//		Component classifier = null;
//
//		for (Component c : cl_p.getComponents()) {
//			if (c.getName().equals("weka.attributeSelection.BestFirst")) {
//				searcher = c;
//			}
//		}
//
//		for (Component c : cl_p.getComponents()) {
//			if (c.getName().equals("weka.attributeSelection.CorrelationAttributeEval")) {
//				evaluator = c;
//			}
//		}
//
//		for (Component c : cl_c.getComponents()) {
//			if (c.getName().equals("weka.classifiers.functions.Logistic")) {
//				classifier = c;
//			}
//		}
//
//		GGAOptimizer o = new GGAOptimizer(searcher, evaluator, classifier, "breast-cancer",
//				new File("F:\\Data\\Uni\\PG\\DefaultEvalEnvironment"),
//				new File("F:\\Data\\Uni\\PG\\DefaultEvalEnvironment\\datasets"), 0, 900, 1200);
//		o.optimize();
//	}


}
