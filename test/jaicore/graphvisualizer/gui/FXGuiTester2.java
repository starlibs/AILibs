package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.algorithms.standard.bestfirst.abstractVersioning.TestNode;
import jaicore.search.structure.core.GraphGenerator;
import org.junit.Test;

import java.util.List;

public class FXGuiTester2 {

    @Test
    public void test(){

        GraphGenerator gen = new TestGraphGenerator();
        gen.setNodeNumbering(true);

        BestFirst<TestNode,String> bf = new BestFirst<>(gen, n->(double)Math.round(Math.random()*100));

        Recorder rec = new Recorder();
        bf.registerListener(rec);


        SimpleGraphVisualizationWindow win = new SimpleGraphVisualizationWindow(bf);
        win.getPanel().setTooltipGenerator(n->{

        });

        List<TestNode> solution = bf.nextSolution();

        String args [] = new String[0];
        FXController.setRec(rec);
        javafx.application.Application.launch(FXGui.class, args);





    }
}
