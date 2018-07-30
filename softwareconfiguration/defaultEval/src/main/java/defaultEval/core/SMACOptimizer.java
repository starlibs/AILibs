package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.Pair;

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

public class SMACOptimizer extends Optimizer {

	public SMACOptimizer(Component searcher, Component evaluator, Component classifier, String dataSet,
			File environment, File dataSetFolder, int seed, int maxRuntimeParam, int maxRuntime) {
		super(searcher, evaluator, classifier, dataSet, environment, dataSetFolder, seed, maxRuntimeParam, maxRuntime);
	}

	@Override
	public void optimize() {

		generatePCSFile();
		generatePyWrapper();

		// start SMAC
		try {

			ArrayList<String> cmd = new ArrayList<>();
			cmd.add(environment.getAbsolutePath() + "\\optimizer\\smac\\smac ");
			cmd.add("--run-obj");
			cmd.add("QUALITY");
			cmd.add("--use-instances");
			cmd.add("false");
			cmd.add("--wallclock_limit");
			cmd.add("" + maxRuntimeParam);
			cmd.add("--numberOfRunsLimit");
			cmd.add("100");
			cmd.add("--seed");
			cmd.add("" + seed);
			cmd.add("--pcs-file");
			cmd.add(environment.getAbsolutePath() + "/pcs/" + buildFileName() + ".pcs ");
			cmd.add("--output-dir");
			cmd.add(environment.getAbsolutePath() + "/smac-output/" + buildFileName() + " ");
			cmd.add("--algo");
			cmd.add("\"python " + environment.getAbsolutePath() + "/py_wrapper/" + buildFileName() + ".py\"");

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(environment.getAbsoluteFile());
			// pb.redirectErrorStream(true);
			// pb.redirectOutput(Redirect.PIPE);
			Process proc = pb.start();

			new Thread(new Runnable() {
				@Override
				public void run() {
					long start_time = System.currentTimeMillis();

					while (System.currentTimeMillis() - start_time < maxRuntime * 1000 && proc.isAlive()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO
							e.printStackTrace();
						}
					}
					int id = ProcessUtil.getPID(proc);
					try {
						if (proc.isAlive()) {
							System.err.println("Kill process...");
							ProcessUtil.killProcess(id);
						}
					} catch (IOException e) {
						// TODO
						e.printStackTrace();
					}
				}
			}).start();

			new Thread(new Runnable() {
				@Override
				public void run() {
					int r = 0;
					try {
						while ((r = proc.getErrorStream().read()) != -1) {
							System.out.write(r);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			new Thread(new Runnable() {
				@Override
				public void run() {
					int r = 0;
					try {
						while ((r = proc.getInputStream().read()) != -1) {
							System.out.write(r);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			proc.waitFor();
			int exitValue = proc.exitValue();

			createFinalInstances();

		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO
			e.printStackTrace();
		}

		System.out.println("final-searcher: " + finalSearcher);
		System.out.println("final-evaluator: " + finalEvaluator);
		System.out.println("final-classifier: " + finalClassifier);
	}

	private void createFinalInstances() throws IOException, FileNotFoundException {
		// read outputs
		File dir = new File(
				environment.getAbsolutePath() + "/smac-output/" + buildFileName() + "/NoScenarioFile/state-run" + seed);
		File[] outputRunsAndResultFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("runs_and_results");
			}
		});

		File[] outputUniqConfigurationFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("paramstrings");
			}
		});

		Arrays.sort(outputRunsAndResultFiles, new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return -arg0.getName().compareToIgnoreCase(arg1.getName());
			}
		});
		Arrays.sort(outputUniqConfigurationFiles, new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return -arg0.getName().compareToIgnoreCase(arg1.getName());
			}
		});

		File outputRunsAndResults = outputRunsAndResultFiles[0];
		File outputUniqConfigurations = outputUniqConfigurationFiles[0];

		CSVParser parserRunsAndResults = new CSVParser(new FileReader(outputRunsAndResults),
				CSVFormat.DEFAULT.withHeader());
		// CSVParser parserUniqConfigurations = new CSVParser(new
		// FileReader(outputUniqConfigurations), CSVFormat.DEFAULT);

		// find opt
		int bestIndex = 0;
		double bestScore = 100;
		for (CSVRecord result : parserRunsAndResults) {
			try {
				double newScore = Double.valueOf(result.get(3));
				if (newScore < bestScore) {
					bestScore = newScore;
					bestIndex = Integer.valueOf(result.get(0));
				}
			} catch (NumberFormatException e) {
				// skip
			}
		}

		Scanner scanner = new Scanner(outputUniqConfigurations);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] l = line.split(":");
			if (l[0].equals(Integer.toString(bestIndex))) {
				HashMap<String, String> searcherParameter = new HashMap<>();
				HashMap<String, String> evaluatorParameter = new HashMap<>();
				HashMap<String, String> classifierParameter = new HashMap<>();

				String[] params = l[1].trim().split(",");
				for (String param : params) {
					String[] nameValue = param.split("=");

					switch (Util.getTypeFromUniqueParamName(nameValue[0].trim())) {
					case searcher:
						searcherParameter.put(Util.revertFromUniqueParamName(nameValue[0].trim()), nameValue[1].trim().substring(1, nameValue[1].trim().length()-1));
						break;
					case evaluator:
						evaluatorParameter.put(Util.revertFromUniqueParamName(nameValue[0].trim()), nameValue[1].trim().substring(1, nameValue[1].trim().length()-1));
						break;
					case classifier:
						classifierParameter.put(Util.revertFromUniqueParamName(nameValue[0].trim()), nameValue[1].trim().substring(1, nameValue[1].trim().length()-1));
						break;

					default:
						break;
					}

				}

				finalSearcher = new ComponentInstance(searcher, searcherParameter, new HashMap<>());
				finalEvaluator = new ComponentInstance(evaluator, evaluatorParameter, new HashMap<>());
				finalClassifier = new ComponentInstance(classifier, classifierParameter, new HashMap<>());
			}

		}

		parserRunsAndResults.close();
		scanner.close();
	}

	private void generatePyWrapper() {
		// generate py-wrapper file
		PrintStream pyWrapperStream = null;
		// TODO do not generate if allready there

		try {
			pyWrapperStream = new PrintStream(new FileOutputStream(
					new File(environment.getAbsolutePath() + "/py_wrapper/" + buildFileName() + ".py")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		pyWrapperStream.println("#!/usr/bin/python");
		pyWrapperStream.println("import sys, math");
		pyWrapperStream.println("from subprocess import call");

		for (Pair<Parameter, ParamType> parameterPair : parameterList) {
			pyWrapperStream.println(getUniqueParamName(parameterPair) + " = "
					+ getInitialValue(parameterPair.getFirst().getDefaultDomain()));
		}

		pyWrapperStream.println("for i in range(len(sys.argv)-1): ");
		int i = 0;
		for (Pair<Parameter, ParamType> parameterPair : parameterList) {
			if (i == 0) {
				pyWrapperStream.println("\t if (sys.argv[i] == '-" + getUniqueParamName(parameterPair) + "'):");

			} else {
				pyWrapperStream.println("\t elif(sys.argv[i] == '-" + getUniqueParamName(parameterPair) + "'):");
			}

			pyWrapperStream.println("\t \t " + getUniqueParamName(parameterPair) + " = "
					+ getConverter(parameterPair.getFirst().getDefaultDomain()) + "(sys.argv[i+1])");
			i++;
		}

		// run java programm
		pyWrapperStream.print("call(\"java -jar " + environment.getAbsolutePath() + "/PipelineEvaluator.jar");

		pyWrapperStream.print(" " + environment.getAbsolutePath() + "/results/" + buildFileName() + ".txt");

		pyWrapperStream.print(" " + dataSetFolder.getAbsolutePath() + "/" + dataSet + ".arff");

		pyWrapperStream.print(" " + seed);

		if (searcher != null) {
			pyWrapperStream.print(" " + searcher.getName());

			for (Parameter parameter : searcher.getParameters()) {
				pyWrapperStream.print(" " + parameter.getName());
				pyWrapperStream.print(" \"+" + createDomainWrapper(getUniqueParamName(parameter, ParamType.searcher),
						parameter.getDefaultDomain()) + " + \"");
			}

			pyWrapperStream.print(" " + evaluator.getName());

			for (Parameter parameter : evaluator.getParameters()) {
				pyWrapperStream.print(" " + parameter.getName());
				pyWrapperStream.print(" \"+ " + createDomainWrapper(getUniqueParamName(parameter, ParamType.evaluator),
						parameter.getDefaultDomain()) + " + \"");
			}

		} else {
			pyWrapperStream.print(" null");
		}

		pyWrapperStream.print(" " + classifier.getName());

		for (Parameter parameter : classifier.getParameters()) {
			pyWrapperStream.print(" " + parameter.getName());
			pyWrapperStream.print(" \"+" + createDomainWrapper(getUniqueParamName(parameter, ParamType.classifier),
					parameter.getDefaultDomain()) + " + \"");
		}

		pyWrapperStream.println("\")");

		// read result file

		pyWrapperStream.println("file = open(\"" + environment.getAbsolutePath() + "\\\\results\\\\" + buildFileName()
				+ ".txt\", \"r\")");
		pyWrapperStream.println("yValue = float(file.read())");

		pyWrapperStream.println("print \"Result for SMAC: SUCCESS, 0, 0, %f, 0\" % yValue");

		pyWrapperStream.close();
	}

	private void generatePCSFile() {
		// generate params file
		PrintStream pcsStream = null;
		try {
			pcsStream = new PrintStream(
					new FileOutputStream(new File(environment.getAbsolutePath() + "/pcs/" + buildFileName() + ".pcs")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Pair<Parameter, ParamType> parameterPair : parameterList) {
			ParameterDomain pd = parameterPair.getFirst().getDefaultDomain();

			// Numeric (integer or real/double)
			if (pd instanceof NumericParameterDomain) {
				NumericParameterDomain n_pd = (NumericParameterDomain) pd;
				pcsStream.println(getUniqueParamName(parameterPair) + " " + (n_pd.isInteger() ? "integer" : "real")
						+ " [" + n_pd.getMin() + ", " + n_pd.getMax() + "] ["
						+ parameterPair.getFirst().getDefaultValue().toString() + "]");
			}

			// Boolean (categorical)
			else if (pd instanceof BooleanParameterDomain) {
				BooleanParameterDomain b_pd = (BooleanParameterDomain) pd;
				pcsStream.println(getUniqueParamName(parameterPair) + " categorical {true, false} ["
						+ parameterPair.getFirst().getDefaultValue().toString() + "]");
			}

			// categorical
			else if (pd instanceof CategoricalParameterDomain) {
				CategoricalParameterDomain c_pd = (CategoricalParameterDomain) pd;
				pcsStream.print(getUniqueParamName(parameterPair) + " categorical {");
				for (int i = 0; i < c_pd.getValues().length; i++) {
					pcsStream.print(c_pd.getValues()[i]);
					if (i != c_pd.getValues().length - 1) {
						pcsStream.print(",");
					}
				}
				pcsStream.println("} [" + parameterPair.getFirst().getDefaultValue().toString() + "]");
			}
		}

		pcsStream.close();
	}

	@Override
	protected String buildFileName() {
		return super.buildFileName() + "SMAC";
	}

	private String getConverter(ParameterDomain pd) {
		if (pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;
			return n_pd.isInteger() ? "int" : "float";
		}
		return "";
	}

	private String createDomainWrapper(String input, ParameterDomain pd) {
		if (pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;
			return n_pd.isInteger() ? "str(" + input + ")" : "\"{:.9f}\".format(" + input + ")";
		}

		return "str(" + input + ")";
	}


	private String getInitialValue(ParameterDomain pd) {
		if (pd instanceof NumericParameterDomain) {
			return "0";
		}

		// Boolean (categorical)
		else if (pd instanceof BooleanParameterDomain) {
			return "'true'";
		}

		// categorical
		else if (pd instanceof CategoricalParameterDomain) {
			return "''";
		}

		return "0";
	}

}
