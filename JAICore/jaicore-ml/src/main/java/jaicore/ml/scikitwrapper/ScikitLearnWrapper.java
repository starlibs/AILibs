package jaicore.ml.scikitwrapper;

import jaicore.ml.evaluation.IInstancesClassifier;
import org.apache.commons.lang3.StringUtils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*	Wraps a Scikit-Learn Python process by utilizing a template to
 *	start a classifier in Scikit with the given classifier.
 *
 *Usage:
 *Set the constructInstruction to exactly the command how the classifier should be instantiated.
 *E.g. "LinearRegression()" or "MLPRegressor(solver = 'lbfgs’)".
 *
 *Set the imports to exactly what the additional imports lines that are necessary to run
 *the construction command must look like. It is up to the user to decide whether fully
 *qualified names or only the class name themself are used as long as the import is
 *on par with the construct call.
 *E.g (without namespace in construct call) "from sklearn.linear_model import LinearRegression"
 * or (without namespace) "import sklearn.linear_model"
 * 
 *createImportStatementFromImportFolder might help to import an own folder of modules. It initializes the folder
 *to be utilizable as a source of modules. Depending on the shape of the construct call the keepNamespace flag must be set
 *(as described above).
 *
 *Before starting the classification it must be set whether the given dataset is a categorical or a regression task (setIsRegression).
 *
 *If the task is a multi target prediction, setTargets must be used to define which columns of the dataset are the targets.
 *If no targets are defined it is assumed that only the last column is the target vector.
 *
 *Moreover the outputFolder might be set to something else but the default (setOutputFolder).
 *
 *Now buildClassifier can be run.
 *
 *If classifyInstances is run with the same ScikitLearnWrapper instance after training,
 *the previously trained model is used for testing.
 *If another model shall be used or there was no training prior to classifyInstances,
 *the model must be set with setModelPath.
 *
 *After a multi target prediction the results might be more accessible with the unflattened representation
 *that can be obtained with getRawLastClassificationResults.
 *
 *For debug purposes the wrapper might be set to be verbose with setIsVerbose.
 */
public class ScikitLearnWrapper implements IInstancesClassifier, Classifier {
	// Folder to put the serialized arff files and the scripts in.
	private static final File TMP_FOLDER = new File("tmp");
	// Path to the used python template.
	private static File SCIKIT_TEMPLATE = new File("resources/scikit_template.twig.py");
	// If true the output stream of the python process is printed.
	private boolean verbose = false;
	// Path to the model to be used for testing. Each buildClassifier call will set
	// this variable to the last created model.
	private String modelPath = "";
	// Script of this wrapper to be executed.
	private File script;
	// Set to true if the dataset is a regression problem. Else its assumed to be a
	// categorical.
	private boolean isRegression = false;
	// Path to but the prediction results and serialized models to.
	private File outputFolder = null;
	// Defines which of the columns in the arff file represent the target vectors.
	// If not set, the last column is assumed to be the target vector.
	private int[] targetColumns = new int[0];
	// Since the ScikitLearn is able to do multi-target prediction but Weka is
	// unable to depict it as a result of classifyInstances correctly, this List of
	// Lists will keep the unflattened results until classifyInstances is called
	// again. classifyInstances will only return a flattened representation of a
	// multi-target prediction. The outer list represents the rows whilst the inner
	// list represents the x target values in this row.
	private List<List<Double>> rawLastClassificationResults = null;

	/**
	 * Starts a new wrapper and creates its underlying script with the given
	 * parameters.
	 * 
	 * @param constructInstruction String that defines what constructor to call for
	 *                             the classifier and with which parameters to call
	 *                             it.
	 * @param imports              Imports that are appended to the beginning of the
	 *                             script. Normally only the necessary imports for
	 *                             the constructor instruction must be added here.
	 * @throws IOException The script could not be created.
	 */
	public ScikitLearnWrapper(String constructInstruction, String imports) throws IOException {
		Map<String, Object> templateValues = getTemplateValueMap(constructInstruction, imports);
		String scriptName = getScriptName(constructInstruction, imports);
		script = generateSkikitScript(scriptName, templateValues);
	}

	/**
	 * Makes the given folder a module to be usable as an import for python and
	 * creates a string that adds the folder to the python environment and then
	 * imports the folder itself as a module.
	 * 
	 * @param importsFolder Folder to be added as a module.
	 * @param keepNamespace If true, a class must be called by the modules' name
	 *                      plus the class name. This is only important if multiple
	 *                      modules are imported and the classes' names are
	 *                      ambiguous. Keep in mind that the constructor call for
	 *                      the classifier must be created accordingly.
	 * @return String which can be appended to other imports to care for the folder
	 *         to be added as a module.
	 * @throws IOException The __init__.py couldn't be created in the given folder
	 *                     (which is necessary to declare it as a module).
	 */
	public static String createImportStatementFromImportFolder(File importsFolder, boolean keepNamespace)
			throws IOException {
		if (importsFolder == null || !importsFolder.exists() || importsFolder.list().length == 0) {
			return "";
		}
		// Make the folder a module.
		if (!Arrays.asList(importsFolder.list()).contains("__init__.py")) {
			File initFile = new File(importsFolder, "__init__.py");
			initFile.createNewFile();
		}
		StringBuilder result = new StringBuilder();
		String absolute_folderPath = importsFolder.getAbsolutePath();
		// Add the folder to the environment of the python script
		result.append("\n");
		result.append("sys.path.append('" + absolute_folderPath + "')\n");
		for (File module : importsFolder.listFiles()) {
			if (!module.getName().startsWith("__")) {
				// Either import the module by its name. Then the classes of it have to be
				// referenced by the fully qualified name.
				if (keepNamespace) {
					result.append("import " + module.getName().substring(0, module.getName().length() - 3) + "\n");
				}
				// ... else all the content of the module is imported. Than they can be called
				// by only their name but therefore there should not be multiple modules
				// imported that overlap in class names.
				else {
					result.append(
							"from " + module.getName().substring(0, module.getName().length() - 3) + " import *\n");
				}
			}
		}
		return result.toString();
	}

	/**
	 * Returns a map with the values for the script template.
	 * 
	 * @param constructInstruction String that defines what constructor to call for
	 *                             the classifier and with which parameters to call
	 *                             it.
	 * @param imports              Imports that are appended to the beginning of the
	 *                             script. Normally only the necessary imports for
	 *                             the constructor instruction must be added here.
	 * @return A map to call the template engine with.
	 */
	private Map<String, Object> getTemplateValueMap(String constructInstruction, String imports) {
		if (constructInstruction == null || constructInstruction.isEmpty()) {
			throw new AssertionError("Construction command for classifier must be stated.");
		}
		Map<String, Object> templateValues = new HashMap<>();
		templateValues.put("imports", imports != null ? imports : "");
		templateValues.put("classifier_construct", constructInstruction);
		return templateValues;
	}

	/**
	 * The parameters for the template are used to infer an script name from them
	 * (hopefully) unique for the parameterization.
	 * 
	 * @param parameters Parameters that the template is filled with.
	 * @return The proposed name for the script with this parameterization.
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
		if (!TMP_FOLDER.exists()) {
			TMP_FOLDER.mkdirs();
		}
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

	public void setOutputFolder(File outputFolder) {
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
		trainOptions.addAll(parseOptions(data));
		String[] processParameterArray = createProcessParameterArray(trainOptions);
		TrainProcessListener processListener = new TrainProcessListener(verbose);
		runProcess(processParameterArray, processListener);
		modelPath = processListener.getModelPath();
	}

	@Override
	public double[] classifyInstances(Instances data) throws Exception {
		List<String> testOptions = new ArrayList<>(Arrays.asList("--mode", "test", "--model", modelPath));
		testOptions.addAll(parseOptions(data));
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
			resultsArray[i] =  flatresults.get(i);
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
	private List<String> parseOptions(Instances data) throws IOException {
		List<String> parameters = new ArrayList<>();
		File arff = instancesToArffFile(data, getArffName(data));
		// If these attributes are renamed for some reason take care to rename them in
		// the script template as well.
		parameters.add("--arff");
		parameters.add(arff.getAbsolutePath());
		if (isRegression) {
			parameters.add("--regression");
		}
		if (outputFolder != null) {
			parameters.add("--output");
			parameters.add(outputFolder.getAbsolutePath());
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
