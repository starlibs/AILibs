import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.FileUtil;
import jaicore.ml.latex.LatexDatasetTableGenerator;

public class DatasetTableGenerator {
	public static void main(final String[] args) throws Exception {
		LatexDatasetTableGenerator gen = new LatexDatasetTableGenerator();
		File folder = new File("res");
		List<File> files = FileUtil.getFilesOfFolder(folder).stream().sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());
		gen.addLocalFiles(files);
		gen.setNumMajorColumns(2);
		System.out.println(gen.getLatexCode());

	}
}
