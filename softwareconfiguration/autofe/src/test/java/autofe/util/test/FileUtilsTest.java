package autofe.util.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import autofe.util.DataSetUtils;
import autofe.util.FileUtils;
import weka.core.Instances;

public class FileUtilsTest {
	@Test
	public void saveInstancesTest() throws Exception {
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);

		File file2 = ds.getDataset(DataSetUtils.API_KEY);
		Instances data2 = new Instances(new BufferedReader(new FileReader(file2)));
		data2.setClassIndex(data.numAttributes() - 1);

		FileUtils.saveInstances(Arrays.asList(data, data2), "D:\\Data\\Ranking", "test");
	}
}
