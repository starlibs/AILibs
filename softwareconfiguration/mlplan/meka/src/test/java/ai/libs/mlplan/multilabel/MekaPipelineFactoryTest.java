package ai.libs.mlplan.multilabel;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentLoader;
import ai.libs.mlplan.multilabel.mekamlplan.EMLPlanMekaProblemType;
import ai.libs.mlplan.multilabel.mekamlplan.MekaPipelineFactory;

public class MekaPipelineFactoryTest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL.getSearchSpaceConfigFileFromResource(),
			EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL.getSearchSpaceConfigFromFileSystem());
	private static final Logger LOGGER = LoggerFactory.getLogger(MekaPipelineFactoryTest.class);

	private static ComponentLoader cl;
	private static MekaPipelineFactory mpf;

	@BeforeClass
	public static void setup() throws Exception {
		cl = new ComponentLoader(SSC);
		mpf = new MekaPipelineFactory();
	}

	@Test
	public void testValidDefaultConfigInstantiation() throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		Collection<ComponentInstance> algorithmSelections = ComponentUtil.getAllAlgorithmSelectionInstances("MLClassifier", cl.getComponents());
		List<ComponentInstance> list = new ArrayList<>(algorithmSelections);

		AtomicInteger count = new AtomicInteger(0);
		AtomicDouble currentPercentage = new AtomicDouble(0.0);
		double step = 0.05;
		List<String> listOfFails = Collections.synchronizedList(new ArrayList<>());
		IntStream.range(0, list.size()).parallel().forEach(i -> {
			try {
				mpf.getComponentInstantiation(list.get(i));
			} catch (ComponentInstantiationFailedException e) {
				String name = ComponentUtil.getComponentInstanceAsComponentNames(list.get(i));
				LOGGER.debug("Exception occurred instantiating {}", name, e);
				listOfFails.add(name);
			}

			if ((double) count.incrementAndGet() / list.size() >= currentPercentage.get()) {
				System.out.println("Current state: " + (currentPercentage.get() * 100) + "%");
				currentPercentage.addAndGet(step);
			}
		});

		assertTrue("List of fails is not empty: " + listOfFails, listOfFails.isEmpty());
	}

	@Test
	public void testValidRandomConfigInstantiation() throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		Collection<ComponentInstance> algorithmSelections = ComponentUtil.getAllAlgorithmSelectionInstances("MLClassifier", cl.getComponents());
		List<ComponentInstance> list = new ArrayList<>(algorithmSelections);

		AtomicInteger count = new AtomicInteger(0);
		AtomicDouble currentPercentage = new AtomicDouble(0.0);
		double step = 0.05;
		List<String> listOfFails = Collections.synchronizedList(new ArrayList<>());
		IntStream.range(0, list.size()).parallel().forEach(i -> {
			int currentI = i;
			IntStream.range(0, 5).forEach(s -> {
				try {
					mpf.getComponentInstantiation(ComponentUtil.getRandomParametrization(list.get(currentI), new Random(s)));
				} catch (ComponentInstantiationFailedException e) {
					String name = ComponentUtil.getComponentInstanceAsComponentNames(list.get(i));
					LOGGER.debug("Exception occurred instantiating {}", name, e);
					listOfFails.add(name);
				}
			});

			if ((double) count.incrementAndGet() / list.size() >= currentPercentage.get()) {
				System.out.println("Current state: " + (currentPercentage.get() * 100) + "%");
				currentPercentage.addAndGet(step);
			}
		});

		assertTrue("List of fails is not empty: " + listOfFails, listOfFails.isEmpty());
	}

}
