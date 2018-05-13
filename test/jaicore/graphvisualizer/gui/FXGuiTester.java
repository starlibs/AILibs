package jaicore.graphvisualizer.gui;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestNode;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.GraphGenerator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.IOException;

public class FXGuiTester extends FXGui{


	@Test
	public void test() {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		GraphGenerator generator = new TestGraphGenerator();
		BestFirst<TestNode, String> bf = new BestFirst<>(generator, n->(double)Math.round(Math.random()*100));
		open(bf,"BestFirst");
//		open(new Recorder(bf), "Recorder");
		bf.nextSolution();


	}

}
