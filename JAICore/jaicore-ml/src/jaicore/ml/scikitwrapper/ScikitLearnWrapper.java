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
	private static final File TMP_FOLDER = new File("tmp");
	private static File SCIKIT_TEMPLATE = new File("resources/scikit_template.twig.py");
	private String modelPath = "";
	private File script;
	private boolean isRegression = false;
	private String outputFolder = "";
	private int[] targetColumns = new int[0];

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

	private String getScriptName(String... parameters) {
		String hash = "" + StringUtils.join(parameters).hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		hash = hash + ".py";
		return hash;
	}

	/**
	 * Generates the Python script that is wrapped.
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

	public void setIsRegression(boolean isRegression) {
		this.isRegression = isRegression;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setTargets(int... targetColumns) {
		this.targetColumns = targetColumns;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		List<String> trainOptions = new ArrayList<>();
		trainOptions.add("--mode");
		trainOptions.add("train");
		parseSetOptions(trainOptions, data);
		String[] processParameterArray = createProcessParameterArray(trainOptions);
		TrainProcessListener processListener = new TrainProcessListener();
		runProcess(processParameterArray, processListener);
		modelPath = processListener.getModelPath();
	}

	@Override
	public double[] classifyInstances(Instances data) throws Exception {
		List<String> testOptions = new ArrayList<>();
		testOptions.add("--mode");
		testOptions.add("test");
		testOptions.add("--model");
		testOptions.add(modelPath);
		parseSetOptions(testOptions, data);
		String[] processParameterArray = createProcessParameterArray(testOptions);
		TestProcessListener processListener = new TestProcessListener();
		runProcess(processParameterArray, processListener);
		List<Double> results = processListener.getTestResults();
		double[] resultsArray = new double[results.size()];
		for (int i = 0; i < resultsArray.length; i++) {
			resultsArray[i] = results.get(i);
		}
		return resultsArray;
	}

	private void parseSetOptions(List<String> parameters, Instances data) throws IOException {
		File arff = instancesToArffFile(data, getArffName(data));
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
	}

	/*
	 * Dumps given Instances in an ARFF file if this hash does not already exist.
	 */
	private File instancesToArffFile(Instances data, String fileName) throws IOException {
		ArffSaver saver = new ArffSaver();
		File arffOutputFile = new File(TMP_FOLDER, fileName + ".arff");
		// If Instances with the same Instance (granted that the hash is collision
		// resistant)
		// is already serialized, there is no need for doing it once more.
		if (arffOutputFile.exists()) {
			System.out.printf("Reusing %s.arff\n", fileName);
			return arffOutputFile;
		}
		saver.setInstances(data);
		saver.setFile(arffOutputFile);
		try {
			saver.writeBatch();
		} catch (IOException e) {
			throw new IOException("Could not write into temporary ARFF file", e);
		}
		return arffOutputFile;
	}

	private String getArffName(Instances data) {
		String hash = "" + data.hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		return hash;
	}

	/*
	 * Return an array with the parameters for the process.
	 */
	private String[] createProcessParameterArray(List<String> additionalParameter) {
		List<String> processParameters = new ArrayList<>();
		processParameters.add("python");
		// Force python to run stdout and stderr unbuffered.
		processParameters.add("-u");
		processParameters.add(script.getAbsolutePath());
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
		String call = Arrays.toString(parameters).replace(",", "");
		System.out.println("Starting subprocess: " + call.substring(1, call.length() - 1));
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
