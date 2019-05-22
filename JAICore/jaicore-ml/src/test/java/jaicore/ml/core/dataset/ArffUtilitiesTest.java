package jaicore.ml.core.dataset;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class ArffUtilitiesTest {

	@Test
	public void testArffHeaderExtraction() throws IOException {
		String extractedHeader = ArffUtilities.extractArffHeader(new File("testsrc/ml/orig/letter.arff"));
		String trueHeader = "@relation letter\n" + "@attribute 'x-box' integer\n" + "@attribute 'y-box' integer\n"
				+ "@attribute 'width' integer\n" + "@attribute 'high' integer\n" + "@attribute 'onpix' integer\n"
				+ "@attribute 'x-bar' integer\n" + "@attribute 'y-bar' integer\n" + "@attribute 'x2bar' integer\n"
				+ "@attribute 'y2bar' integer\n" + "@attribute 'xybar' integer\n" + "@attribute 'x2ybr' integer\n"
				+ "@attribute 'xy2br' integer\n" + "@attribute 'x-ege' integer\n" + "@attribute 'xegvy' integer\n"
				+ "@attribute 'y-ege' integer\n" + "@attribute 'yegvx' integer\n"
				+ "@attribute 'class' { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z}\n"
				+ "@data\n";
		assertEquals(trueHeader, extractedHeader);
	}

	@Test
	public void testArffDataentryCount() throws IOException {
		int countedEntries = ArffUtilities.countDatasetEntries(new File("testsrc/ml/orig/letter.arff"), true);
		int trueEntries = 20000;
		assertEquals(trueEntries, countedEntries);
	}

	@Test
	public void testSkippingToDataWithReader() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("testsrc/ml/orig/letter.arff")));
		ArffUtilities.skipWithReaderToDatapoints(reader);
		assertEquals("2,4,4,3,2,7,8,2,9,11,7,7,1,8,5,6,Z", reader.readLine());
		reader.close();
	}

}
