package ai.libs.mlplan.multilabel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.AtomicDouble;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.test.LongTest;
import ai.libs.mlplan.meka.EMLPlanMekaProblemType;
import ai.libs.mlplan.meka.MekaPipelineFactory;
import meka.core.MLUtils;
import weka.core.Instances;

public class MekaPipelineFactoryTest extends ATest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL.getSearchSpaceConfigFileFromResource(),
			EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL.getSearchSpaceConfigFromFileSystem());

	private static IComponentRepository repository;
	private static MekaPipelineFactory mpf;
	private static IMekaInstances dTrain;

	@BeforeAll
	public static void setup() throws Exception {
		repository = new ComponentSerialization().deserializeRepository(SSC);
		mpf = new MekaPipelineFactory();
		Instances data = new Instances(new FileReader(new File("testrsc/flags.arff")));
		MLUtils.prepareData(data);
		dTrain = new MekaInstances(data);
	}

	@Disabled
	@Test
	public void testValidDefaultConfigInstantiation() throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		Collection<ComponentInstance> algorithmSelections = ComponentUtil.getAllAlgorithmSelectionInstances("MLClassifier", repository);
		List<ComponentInstance> list = new ArrayList<>(algorithmSelections);

		double currentPercentage = 0.0;
		double step = 0.05;
		for (int i = 0; i < list.size(); i++) {
			IMekaClassifier c = mpf.getComponentInstantiation(list.get(i));

			if ((double) i / list.size() >= currentPercentage) {
				System.out.println("Current state: " + (currentPercentage * 100) + "%");
				currentPercentage += step;
			}
		}
	}

	@Test
	@LongTest
	public void testValidRandomConfigInstantiation() throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		List<ComponentInstance> list = new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances("MLClassifier", repository));
		this.logger.info("Testing {} configurations.", list.size());

		AtomicInteger count = new AtomicInteger(0);
		AtomicDouble currentPercentage = new AtomicDouble(0.0);
		double step = 0.01;
		IntStream.range(0, list.size()).parallel().forEach(i -> {
			this.logger.trace("Checking {}", ComponentInstanceUtil.getComponentInstanceAsComponentNames(list.get(i)));
			int currentI = i;
			boolean success = false;
			for (int j = 0; j < 5 && !success; j++) {
				try {
					mpf.getComponentInstantiation(ComponentUtil.getRandomParametrization(list.get(currentI), new Random(j)));
					success = true;
				} catch (ComponentInstantiationFailedException e) {
					this.logger.warn("Failed to instantiate. Error was: {}", e.getMessage());
				}
			};
			assertTrue(success, "Could not find a realization of component " + list.get(currentI));

			if ((double) count.incrementAndGet() / list.size() >= currentPercentage.get()) {
				this.logger.debug("Current state: {}%", currentPercentage.get() * 100);
				currentPercentage.addAndGet(step);
			}
		});
	}

}
