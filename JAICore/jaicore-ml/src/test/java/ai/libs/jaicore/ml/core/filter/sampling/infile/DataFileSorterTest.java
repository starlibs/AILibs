package ai.libs.jaicore.ml.core.filter.sampling.infile;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.DatasetFileSorter;

public class DataFileSorterTest {
	private static final File ARFF_DATASET = new File("testrsc" + File.separator + "ml" + File.separator + "orig" + File.separator + "letter_small.arff");

	@Test
	public void testDataFileSorting() throws IOException, InterruptedException, AlgorithmExecutionCanceledException {
		if (!ARFF_DATASET.exists()) {
			throw new FileNotFoundException();
		}

		/* create an ordered file in the same folder as the dataset */
		File tmpOutFile = new File(ARFF_DATASET.getParentFile() + File.separator + "letter_small_sorted.arff");
		DatasetFileSorter sorter = new DatasetFileSorter(ARFF_DATASET);
		File sortedFile = sorter.sort(tmpOutFile.getPath());

		/* compare the file content to the ground truth */
		String sortedFileContent = FileUtil.readFileAsString(sortedFile);
		String sortedData = "@relation letter_small\n" + "@attribute 'x-box' integer\n" + "@attribute 'y-box' integer\n" + "@attribute 'width' integer\n" + "@attribute 'high' integer\n" + "@attribute 'onpix' integer\n"
				+ "@attribute 'x-bar' integer\n" + "@attribute 'y-bar' integer\n" + "@attribute 'x2bar' integer\n" + "@attribute 'y2bar' integer\n" + "@attribute 'xybar' integer\n" + "@attribute 'x2ybr' integer\n"
				+ "@attribute 'xy2br' integer\n" + "@attribute 'x-ege' integer\n" + "@attribute 'xegvy' integer\n" + "@attribute 'y-ege' integer\n" + "@attribute 'yegvx' integer\n"
				+ "@attribute 'class' { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z}\n" + "@data\n" + "1,0,2,0,1,6,10,7,2,7,5,8,2,7,4,9,R\n" + "1,0,2,1,1,5,7,8,6,7,6,6,2,8,3,8,D\n"
				+ "2,4,4,3,2,7,8,2,9,11,7,7,1,8,5,6,Z\n" + "4,7,5,5,3,4,12,2,5,13,7,5,1,10,1,7,F\n" + "4,7,5,5,5,5,9,6,4,8,7,9,2,9,7,10,P\n" + "4,9,5,7,4,7,7,13,1,7,6,8,3,8,0,8,H\n" + "5,9,7,6,7,7,7,2,4,9,8,9,7,6,2,8,M\n"
				+ "6,10,8,8,4,7,8,2,5,10,7,8,5,8,1,8,N\n" + "6,7,8,5,4,7,6,3,7,10,7,9,3,8,3,7,H\n" + "7,10,8,7,4,8,8,5,10,11,2,8,2,5,5,10,S\n";
		Files.delete(tmpOutFile.toPath());
		assertEquals(sortedData, sortedFileContent);
	}

}
