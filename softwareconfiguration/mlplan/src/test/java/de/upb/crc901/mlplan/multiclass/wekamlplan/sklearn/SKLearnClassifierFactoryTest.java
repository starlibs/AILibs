package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import hasco.model.ComponentInstance;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import weka.core.Instances;

public class SKLearnClassifierFactoryTest {

	private static final File TEST_PIPE = new File("testrsc/sklearn/sklearn_classifier_factory.pipe_description.json");
	private static ComponentInstance componentInstance;
	private static SKLearnClassifierFactory factory;

	@BeforeClass
	public static void setup() throws IOException {
		// ObjectMapper om = new ObjectMapper();
		// componentInstance = ComponentInstance.fromJson(new ObjectMapper().readTree(FileUtil.readFileAsString(TEST_PIPE)));
		// System.out.println(componentInstance);
		// factory = new SKLearnClassifierFactory();
	}

	@Test
	public void testExtractClassifier() throws Exception {
		// Set<String> imports = new HashSet<>();
		// String constructionDescription = factory.extractSKLearnConstructInstruction(componentInstance, imports);
		//
		// System.out.println(imports);
		// System.out.println(constructionDescription);

		String imports = "from sklearn.neighbors import KNeighborsClassifier\nfrom sklearn.pipeline import make_pipeline\nfrom sklearn.preprocessing import MaxAbsScaler";
		System.out.println("Imports: " + imports);
		String composition = "make_pipeline(MaxAbsScaler(),KNeighborsClassifier(p=1,weights=\"distance\",n_neighbors=46))";
		System.out.println("Composition: " + composition);

		System.out.println("Load dataset");
		Instances data = new Instances(new FileReader(new File("testrsc/car.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> stratSplit = WekaUtil.getStratifiedSplit(data, 0, .7);

		System.out.println("Create ScikitLearnWrapper");
		ScikitLearnWrapper slw = new ScikitLearnWrapper(composition, imports);
		slw.buildClassifier(stratSplit.get(0));
		double[] prediction = slw.classifyInstances(stratSplit.get(1));

		System.out.println("Prediction result: " + Arrays.toString(prediction));

		List<Double> expected = stratSplit.get(1).stream().map(x -> x.classValue()).collect(Collectors.toList());

		System.out.println("Error Rate: " + new ZeroOneLoss().calculateAvgMeasure(Arrays.stream(prediction).mapToObj(x -> Double.valueOf(x)).collect(Collectors.toList()), expected));

	}

}
