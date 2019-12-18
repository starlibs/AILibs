package ai.libs.mlplan.multilabel;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.ComponentUtil;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.multilabel.mekamlplan.ML2PlanMekaPathConfig;
import ai.libs.mlplan.multilabel.mekamlplan.MekaPipelineFactory;
import meka.core.MLUtils;
import weka.core.Instances;

public class MekaPipelineFactoryTest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(ML2PlanMekaPathConfig.RES_SSC, ML2PlanMekaPathConfig.FS_SSC);

	private static ComponentLoader cl;
	private static MekaPipelineFactory mpf;
	private static IWekaInstances dTrain;

	@BeforeClass
	public static void setup() throws Exception {
		System.out.println(SSC);
		cl = new ComponentLoader(SSC);
		mpf = new MekaPipelineFactory();
		Instances data = new Instances(new FileReader(new File("testrsc/flags.arff")));
		MLUtils.prepareData(data);
		dTrain = new WekaInstances(data);
	}

	@Before
	public void init() {
	}

	@Test
	public void testRandomComponentInstantiation() throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		Collection<ComponentInstance> algorithmSelections = ComponentUtil.getAllAlgorithmSelectionInstances("MLClassifier", cl.getComponents());
		List<ComponentInstance> list = new ArrayList<>(algorithmSelections);
		System.out.println(list.size());

		for (int i = 0; i < 10; i++) {
			ComponentInstance ci = list.get(new Random().nextInt(list.size()));
			System.out.println(ci);
			IMekaClassifier c = mpf.getComponentInstantiation(ci);
			c.fit(dTrain);
		}
	}

}
