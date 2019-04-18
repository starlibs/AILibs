package jaicore.ml.core.dataset.sampling.infile;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import jaicore.ml.core.dataset.sampling.infiles.DatasetFileSorter;

public class DataFileSorterTest {

	@Test
	public void testDataFileSorting() throws IOException {
		DatasetFileSorter sorter = new DatasetFileSorter(new File("testsrc/ml/orig/letter_small.arff"));
		File sortedFile = sorter.sort("testsrc/ml/orig/letter_small_sorted.arff");
		BufferedReader sortedFileReader = new BufferedReader(new FileReader(sortedFile));
		String sortedFileContent = "";
		String line;
		while ((line = sortedFileReader.readLine()) != null) {
			sortedFileContent += line + "\n";
		}
		String sortedData = "@relation letter\n" + "@attribute 'x-box' integer\n" + "@attribute 'y-box' integer\n"
				+ "@attribute 'width' integer\n" + "@attribute 'high' integer\n" + "@attribute 'onpix' integer\n"
				+ "@attribute 'x-bar' integer\n" + "@attribute 'y-bar' integer\n" + "@attribute 'x2bar' integer\n"
				+ "@attribute 'y2bar' integer\n" + "@attribute 'xybar' integer\n" + "@attribute 'x2ybr' integer\n"
				+ "@attribute 'xy2br' integer\n" + "@attribute 'x-ege' integer\n" + "@attribute 'xegvy' integer\n"
				+ "@attribute 'y-ege' integer\n" + "@attribute 'yegvx' integer\n"
				+ "@attribute 'class' { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z}\n"
				+ "@data\n" + "1,0,2,0,1,6,10,7,2,7,5,8,2,7,4,9,R\n" + "1,0,2,1,1,5,7,8,6,7,6,6,2,8,3,8,D\n"
				+ "2,4,4,3,2,7,8,2,9,11,7,7,1,8,5,6,Z\n" + "4,7,5,5,3,4,12,2,5,13,7,5,1,10,1,7,F\n"
				+ "4,7,5,5,5,5,9,6,4,8,7,9,2,9,7,10,P\n" + "4,9,5,7,4,7,7,13,1,7,6,8,3,8,0,8,H\n"
				+ "5,9,7,6,7,7,7,2,4,9,8,9,7,6,2,8,M\n" + "6,10,8,8,4,7,8,2,5,10,7,8,5,8,1,8,N\n"
				+ "6,7,8,5,4,7,6,3,7,10,7,9,3,8,3,7,H\n" + "7,10,8,7,4,8,8,5,10,11,2,8,2,5,5,10,S\n";
		sortedFile.delete();
		sortedFileReader.close();
		assertEquals(sortedData, sortedFileContent);
	}

}
