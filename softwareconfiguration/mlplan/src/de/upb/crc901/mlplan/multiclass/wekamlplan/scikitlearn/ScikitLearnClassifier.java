package de.upb.crc901.mlplan.multiclass.wekamlplan.scikitlearn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jaicore.basic.FileUtil;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class ScikitLearnClassifier implements Classifier {

	private static final String CLASSIFIER_TEMPLATE = "testrsc/scikit-learn/template.py";
	private static final String EXECUTION_FOLDER = "testrsc/sl-temp/";

	private static final String[] LIB_FILES = { "arff.py", "arffcontainer.py" };

	private static final AtomicInteger GROUND_TEMPLATE_ID = new AtomicInteger(0);

	private final int groundTemplateID;
	private final long currentTimestamp;

	public ScikitLearnClassifier(final ScikitLearnClassifier composition) {
		this.currentTimestamp = System.currentTimeMillis();
		this.groundTemplateID = GROUND_TEMPLATE_ID.incrementAndGet();
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(data.toString().getBytes());
		String hashOfDataset = new String(messageDigest.digest());
		File datasetFile = new File("data_" + hashOfDataset + ".arff");

		if (!datasetFile.exists()) {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(datasetFile))) {
				bw.write(data.toString());
			}
		}

		String templateCode = FileUtil.readFileAsString(new File(CLASSIFIER_TEMPLATE));

		File codeOutputFile = new File(EXECUTION_FOLDER + this.currentTimestamp + "_" + this.groundTemplateID + ".py");
		codeOutputFile.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(codeOutputFile))) {
			bw.write(templateCode);
		}

		String[] commandArray = new String[] { "python", "candidate_" + this.groundTemplateID + ".py", "train", datasetFile.getName() };
		ProcessBuilder pb = new ProcessBuilder(commandArray).directory(new File(EXECUTION_FOLDER)).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT);
		Process p = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		List<String> processOutput = new LinkedList<>();
		while ((line = br.readLine()) != null) {
			processOutput.add(line);
		}

		try {
			p.waitFor();
			System.out.println(processOutput);
		} catch (InterruptedException e) {
			System.out.println("Kill the process as this thread has been interrupted.");
			p.destroy();
		}
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return 0;
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return null;
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

}
