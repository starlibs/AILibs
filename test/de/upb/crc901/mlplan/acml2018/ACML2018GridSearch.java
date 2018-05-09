package de.upb.crc901.mlplan.acml2018;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ACML2018GridSearch {

	public static void main(String[] args) {
		try {
			List<String> datasets = Arrays.asList(new File(args[0]).listFiles()).stream().filter(f -> f.isFile()).map(f -> f.getAbsolutePath()).collect(Collectors.toList());
			int seed = (int) Math.ceil(Math.random() * 25);
			ACML2018 o = new ACML2018();
			Collections.shuffle(datasets);
			o.conductGridSearch(datasets.get(0), seed, Integer.valueOf(args[1]), 4 * 1024);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
