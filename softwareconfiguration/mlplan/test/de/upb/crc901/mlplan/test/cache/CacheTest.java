package de.upb.crc901.mlplan.test.cache;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import weka.core.Instances;

/**
 * Tests the functionality of {@link ReproducibleInstances}, by creating one an
 * using its history to recreate it. Fails if these two are not the same.
 * 
 * @author jnowack
 */
public class CacheTest {

	@Test
	public void testGetStratifiedSplit() {
		try {

			OpenmlConnector connector = new OpenmlConnector();
			DataSetDescription ds = connector.dataGet(40983);
			File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
			Instances data = new Instances(new BufferedReader(new FileReader(file)));
			data.setClassIndex(data.numAttributes() - 1);

			// perform a split with normal instances
			Instances i = WekaUtil.getStratifiedSplit(data, 45, 0.7).get(0);

			//
			ReproducibleInstances rData = ReproducibleInstances.fromOpenML("40983", "4350e421cdc16404033ef1812ea38c01");

			// perform a split
			ReproducibleInstances ri0 = WekaUtil.getStratifiedSplit(rData, new Random(45), 0.1).get(0);

			// perform a split
			Instances ri1 = WekaUtil.getStratifiedSplit((Instances) rData, 45, 0.1).get(0);

			if (ri0.getInstructions().size() != 2) {
				fail("wrong number of instructions");
			}
			if (((ReproducibleInstances) ri1).getInstructions().size() != 2) {
				fail("wrong number of instructions");
			}

			// test reproduction
			ReproducibleInstances r = ReproducibleInstances.fromHistory(ri0.getInstructions(),
					"4350e421cdc16404033ef1812ea38c01");

			if (ri0.size() != r.size()) {
				fail("wrong number of instructions");
			}

			for (int j = 0; j < r.size(); j++) {
				if (!ri0.get(j).toString().equals(r.get(j).toString())) { // Instances in Weka do not implement equals!
					fail("Reproduction failed");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

}
