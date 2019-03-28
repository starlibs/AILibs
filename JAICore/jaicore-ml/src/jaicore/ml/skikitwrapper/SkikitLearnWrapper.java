package jaicore.ml.skikitwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SkikitLearnWrapper implements IInstancesClassifier, Classifier {
	private static final File TMP_FOLDER = new File("tmp");
	private static final String SCIKIT_TEMPLATE = "skikit_template.twig.py";
	private Map<String, Object> templateValues = new HashMap<>();
	private String model_path = "";
	private File script;

	public SkikitLearnWrapper(String pythonClassifierFilePath, String imports, String constructorParameters)
			throws IOException {
		initialize(pythonClassifierFilePath, imports, constructorParameters);
		String scriptName = getScriptName(pythonClassifierFilePath, imports, constructorParameters);
		script = generateSkikitScript(scriptName);
	}

	private void initialize(String pythonClassifierFilePath, String imports, String constructorParameters)
			throws IOException {
		if (pythonClassifierFilePath == null || pythonClassifierFilePath.isEmpty()) {
			throw new AssertionError("A classifier must be stated.");
		}
		templateValues.put("imports", imports != null ? imports : "");
		templateValues.put("constructor_parameters", constructorParameters != null ? constructorParameters : "");
		// Get folder path and file name of the given classifier file.
		File classifierFile = new File(pythonClassifierFilePath);
		// Transform path into Python path.
		String classifierPath = classifierFile.getParentFile().getPath();
		classifierPath = classifierPath.replace("/", ".");
		templateValues.put("classifier_path", classifierPath);
		// Is some module named or a Python file?
		boolean isModule = !classifierFile.getName().endsWith(".py");
		String classifierName = isModule ? classifierFile.getName()
				: classifierFile.getName().substring(0, classifierFile.getName().length() - 3);
		templateValues.put("classifier_name", classifierName);
		/*
		 * Ensure folder that includes Python file being marked as module (Necessary for
		 * the Python import). Only needed when a file is referenced and not a module.
		 */
		if (!isModule) {
			new File(new File(classifierPath), "__init__.py").createNewFile();
		}
		// Ensure temporary folder exists.
		if (!TMP_FOLDER.exists()) {
			TMP_FOLDER.mkdirs();
		}
	}

	private String getScriptName(String pythonClassifierFilePath, String imports, String constructorParameters) {
		String hash = "" + (pythonClassifierFilePath + imports + constructorParameters).hashCode();
		hash = hash.startsWith("-") ? hash.replace("-", "1") : "0" + hash;
		hash = hash + ".py";
		return hash;
	}

	public String getModelPath() {
		return model_path;
	}

	public void setModelPath(String model_path) {
		this.model_path = model_path;
	}

	/**
	 * Generates the Python script that is wrapped.
	 */
	private File generateSkikitScript(String scriptName) throws IOException {
		File script_file = new File(TMP_FOLDER, scriptName);
		script_file.createNewFile();
		JtwigTemplate template = JtwigTemplate.classpathTemplate(SCIKIT_TEMPLATE);
		JtwigModel model = JtwigModel.newModel(templateValues);
		template.render(model, new FileOutputStream(script_file));
		return script_file;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		File arff = instancesToArffFile(data, getArffName(data));
		Collection<String> trainOptionsWithArff = new ArrayList<>();
		trainOptionsWithArff.add("train");
		trainOptionsWithArff.add(arff.getAbsolutePath());
		String[] processParameterArray = createProcessParameterArray(trainOptionsWithArff);
		TrainProcessListener processListener = new TrainProcessListener();
		runProcess(processParameterArray, processListener);
		model_path = processListener.getModelPath();
	}

	@Override
	public double[] classifyInstances(Instances data) throws Exception {
		File arff = instancesToArffFile(data, getArffName(data));
		Collection<String> testOptionsWithArff = new ArrayList<>();
		testOptionsWithArff.add("test");
		testOptionsWithArff.add(arff.getAbsolutePath());
		testOptionsWithArff.add(model_path);
		String[] processParameterArray = createProcessParameterArray(testOptionsWithArff);
		TestProcessListener processListener = new TestProcessListener();
		runProcess(processParameterArray, processListener);
		double[] results = processListener.getTestResults();
		return results;
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
	private String[] createProcessParameterArray(Collection<String> additionalParameter) {
		int numberFixedParameters = 3;
		String[] processParameterArray = new String[additionalParameter.size() + numberFixedParameters];
		processParameterArray[0] = "python";
		// Force python to run stdout and stderr unbuffered.
		processParameterArray[1] = "-u";
		processParameterArray[2] = script.getAbsolutePath();
		List<String> additionalOptionsList = new ArrayList<>(additionalParameter);
		for (int i = 0; i < additionalOptionsList.size(); i++) {
			processParameterArray[i + numberFixedParameters] = additionalOptionsList.get(i);
		}
		return processParameterArray;
	}

	/*
	 * Starts a process with the given attributes. The first String in the array is
	 * the executed program.
	 */
	private void runProcess(String[] parameters, ProcessListener listener) throws Exception {
		System.out.println("Starting subprocess...");
		String a = Arrays.toString(parameters).replace(",", "");
		a = a.substring(1, a.length() - 1);
		System.out.println(a);
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
