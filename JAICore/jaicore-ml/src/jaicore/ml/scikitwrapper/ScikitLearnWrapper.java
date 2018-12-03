package jaicore.ml.scikitwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import jaicore.ml.evaluation.IInstancesClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/*	Wraps a Skikit-Learn Python process by utilizing a template to
 *	start a classifier in Skikit with the given classifier. It is
 *	possible to train, predict or train and predict. When the classifier
 *	is only trained, the model is being saved with an unique ID.
 */
public class ScikitLearnWrapper implements IInstancesClassifier, Classifier {
	private boolean verbose = false;
	private static final File TMP_FOLDER = new File("tmp");
	private static File SCIKIT_TEMPLATE = new File("resources/scikit_template.twig.py");
	private String modelPath = "";
	private File script;
	private boolean isRegression = false;
	private String outputFolder = "";
	private int[] targetColumns = new int[0];
	private List<List<Double>> rawLastClassificationResults = null;

	public ScikitLearnWrapper(String constructInstruction, String imports) throws IOException {
		Map<String, Object> templateValues = initialize(constructInstruction, imports);
		createTmpFolder();
		String scriptName = getScriptName(constructInstruction, imports);
		script = generateSkikitScript(scriptName, templateValues);
	}

	public ScikitLearnWrapper(String constructInstruction, String imports, File importsFolder) throws IOException {
		if (importsFolder != null && importsFolder.list().length > 0) {
			String importStatementFolder = createImportStatementFromImportFolder(importsFolder);
			imports = imports + "\n" + importStatementFolder;
		}
		Map<String, Object> templateValues = initialize(constructInstruction, imports);
		createTmpFolder();
		String scriptName = getScriptName(constructInstruction, imports);
		script = generateSkikitScript(scriptName, templateValues);
	}

	private String createImportStatementFromImportFolder(File importsFolder) throws IOException {
		// Make the folder a module.
		if (!Arrays.asList(importsFolder.list()).contains("__init__.py")) {
			File initFile = new File(importsFolder, "__init__.py");
			initFile.createNewFile();
		}
		StringBuilder result = new StringBuilder();
		String absolute_folderPath = importsFolder.getAbsolutePath();
		result.append("sys.path.append('" + absolute_folderPath + "')\n");
		result.append("import " + importsFolder.getName() + "\n");
		return result.toString();
	}

	private Map<String, Object> initialize(String constructInstruction, String imports) {
		if (constructInstruction == null || constructInstruction.isEmpty()) {
			throw new AssertionError("Construction command for classifier must be stated.");
		}
		Map<String, Object> templateValues = new HashMap<>();
		templateValues.put("imports", imports != null ? imports : "");
		templateValues.put("classifier_construct", constructInstruction);
		return templateValues;
	}

	private void createTmpFolder() {
		if (!TMP_FOLDER.exists())
			TMP_FOLDER.mkdirs();
	}

	/**
	 * The parameters for the template are used to infer an script name from them (hopefully) unique for the parameterisation.
	 * @param parameters Parameters that the template is filled with.
	 * @return The proposed name for the script with this parameterisation.
	 */
	private String getScriptName(String... parameters) {
		String hash = "" + StringUtils.join(parameters).hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		hash = hash + ".py";
		return hash;
	}

	/**
	 * Generates the Python script that is wrapped.
	 * 
	 * @param scriptName     Name of script.
	 * @param templateValues Values to insert into the template to make an actual
	 *                       script from it.
	 * @return Path to the generated script.
	 * @throws IOException During serialization of the script something went wrong.
	 */
	private File generateSkikitScript(String scriptName, Map<String, Object> templateValues) throws IOException {
		File scriptFile = new File(TMP_FOLDER, scriptName);
		scriptFile.createNewFile();
		JtwigTemplate template = JtwigTemplate.fileTemplate(SCIKIT_TEMPLATE);
		JtwigModel model = JtwigModel.newModel(templateValues);
		template.render(model, new FileOutputStream(scriptFile));
		return scriptFile;
	}

	public static String getImportString(Collection<String> imports) {
		return (imports == null || imports.isEmpty()) ? "" : "import " + StringUtils.join(imports, "\nimport ");
	}

	public String getModelPath() {
		return modelPath;
	}

	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}

	public List<List<Double>> getRawLastClassificationResults() {
		return rawLastClassificationResults;
	}

	public void setIsRegression(boolean isRegression) {
		this.isRegression = isRegression;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setTargets(int... targetColumns) {
		this.targetColumns = targetColumns;
	}

	public void setIsVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		List<String> trainOptions = new ArrayList<>(Arrays.asList("--mode", "train"));
		trainOptions.addAll(parseSetOptions(data));
		String[] processParameterArray = createProcessParameterArray(trainOptions);
		TrainProcessListener processListener = new TrainProcessListener(verbose);
		runProcess(processParameterArray, processListener);
		modelPath = processListener.getModelPath();
	}

	@Override
	public double[] classifyInstances(Instances data) throws Exception {
		List<String> testOptions = new ArrayList<>(Arrays.asList("--mode", "test", "--model", modelPath));
		testOptions.addAll(parseSetOptions(data));
		String[] processParameterArray = createProcessParameterArray(testOptions);
		TestProcessListener processListener = new TestProcessListener(verbose);
		runProcess(processParameterArray, processListener);
		// Since Scikit supports multiple target results but Weka does not, the results
		// have to be flattened.
		// The structured results of the last classifyInstances call is accessable over
		// getRawLastClassificationResults().
		rawLastClassificationResults = processListener.getTestResults();
		List<Double> flatresults = rawLastClassificationResults.stream().flatMap(List::stream)
				.collect(Collectors.toList());
		double[] resultsArray = new double[flatresults.size()];
		for (int i = 0; i < resultsArray.length; i++) {
			resultsArray[i] = flatresults.get(i);
		}
		return resultsArray;
	}

	/**
	 * Creates a list of parameters with the set flags and also the path to the
	 * data.
	 * 
	 * @param data Instances to be used for train/ test.
	 * @return list of parameters to call the python script with.
	 * @throws IOException During the serialization of the data as an arff file
	 *                     something went wrong.
	 */
	private List<String> parseSetOptions(Instances data) throws IOException {
		List<String> parameters = new ArrayList<>();
		File arff = instancesToArffFile(data, getArffName(data));
		// If these attributes are renamed for some reason take care to rename them in
		// the script template as well.
		parameters.add("--arff");
		parameters.add(arff.getAbsolutePath());
		if (isRegression) {
			parameters.add("--regression");
		}
		if (outputFolder != null && !outputFolder.equals("")) {
			parameters.add("--output");
			parameters.add(outputFolder);
		}
		if (targetColumns != null && targetColumns.length > 0) {
			parameters.add("--targets");
			for (int i : targetColumns) {
				parameters.add("" + i);
			}
		}
		return parameters;
	}

	/**
	 * Dumps given Instances in an arff file if this hash does not already exist.
	 * 
	 * @param data     Instances to be serialized.
	 * @param fileName Name of the created file.
	 * @return File object corresponding to the arff file.
	 * @throws IOException During the serialization of the data as an arff file
	 *                     something went wrong.
	 */
	private File instancesToArffFile(Instances data, String fileName) throws IOException {
		ArffSaver saver = new ArffSaver();
		File arffOutputFile = new File(TMP_FOLDER, fileName + ".arff");
		// If Instances with the same Instance (given the hash is collision
		// resistant)
		// is already serialized, there is no need for doing it once more.
		if (arffOutputFile.exists()) {
			if (verbose) {
				System.out.printf("Reusing %s.arff\n", fileName);
			}
			return arffOutputFile;
		}
		// ... else serialize it and the return the created file.
		saver.setInstances(data);
		saver.setFile(arffOutputFile);
		try {
			saver.writeBatch();
		} catch (IOException e) {
			throw new IOException("Could not write into temporary ARFF file", e);
		}
		return arffOutputFile;
	}

	/**
	 * Returns a hash for the given Instances based on the Weka implementation of
	 * hashCode(). Additionally the sign is replaces by an additional 0/1.
	 * 
	 * @param data Instances to get a hash code for.
	 * @return A hash for the given Instances.
	 */
	private String getArffName(Instances data) {
		String hash = "" + data.hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		return hash;
	}

	/**
	 * Return an array with all the parameters to start the process with.
	 * 
	 * @param additionalParameter Parameters that the script itself receives.
	 * @return An array of all parameters for the process to be started with.
	 */
	private String[] createProcessParameterArray(List<String> additionalParameter) {
		List<String> processParameters = new ArrayList<>();
		processParameters.add("python");
		// Force python to run stdout and stderr unbuffered.
		processParameters.add("-u");
		// Script to be executed.
		processParameters.add(script.getAbsolutePath());
		// All additional parameters that the script shall consider.
		processParameters.addAll(additionalParameter);
		String[] processParameterArray = new String[processParameters.size()];
		processParameterArray = processParameters.toArray(processParameterArray);
		return processParameterArray;
	}

	/*
	 * Starts a process with the given attributes. The first String in the array is
	 * the executed program.
	 */
	private void runProcess(String[] parameters, ProcessListener listener) throws Exception {
		if (verbose) {
			String call = Arrays.toString(parameters).replace(",", "");
			System.out.println("Starting process: " + call.substring(1, call.length() - 1));
		}
		ProcessBuilder processBuilder = new ProcessBuilder(parameters);
		listener.listenTo(processBuilder.start());
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		return 0;
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		return null;
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}
}
