import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;

public class MetaMLDataset {

	public static void main(final String[] args) throws FileNotFoundException, IOException {

		Instances data = new Instances(new FileReader(new File("testrsc/metaml.arff")));
		System.out.println(data.size());
		System.out.println(data.numAttributes());

	}

}
