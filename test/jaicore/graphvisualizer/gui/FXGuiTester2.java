package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestNode;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

import java.util.List;

public class FXGuiTester2 extends FXGui{
    @Override
    public void init() throws Exception {
        super.init();
        GraphGenerator generator = new TestGraphGenerator();
        BestFirst<TestNode, String> bf = new BestFirst<>(generator, n->(double)Math.round(Math.random()*100));
        this.rootRecorder = new Recorder<>(bf);

//        SimpleGraphVisualizationWindow<Node<TestNode,String>> win = new SimpleGraphVisualizationWindow(bf);
//        win.getPanel().setTooltipGenerator(n->String.valueOf(n.getInternalLabel()));

//        SimpleGraphVisualizationWindow<Node<TestNode,String>> win2 = new SimpleGraphVisualizationWindow(this.rootRecorder);


        rootRecorder.setToolTipGenerator(n->{
            Node node = (Node) n;
            return String.valueOf(node.getInternalLabel());
        });

        bf.nextSolution();

    }


    public static void main(String [] args){
        launch(args);
    }

    //    public void test(){
//        System.out.println("Test");
//        GraphGenerator gen = new TestGraphGenerator();
//        gen.setNodeNumbering(true);
//
//        BestFirst<TestNode,String> bf = new BestFirst<>(gen, n->(double)Math.round(Math.random()*100));
//
//        Recorder<Node<TestNode, String>> rec = new Recorder();
//        bf.registerListener(rec);
//
//
////        SimpleGraphVisualizationWindow<Node<TestNode,String>> win = new SimpleGraphVisualizationWindow(bf);
////        win.getPanel().setTooltipGenerator(n->String.valueOf(n.getInternalLabel()));
//
//        rec.setTooltipGenerator(n->String.valueOf(n.getInternalLabel()));
////        rec.setTooltipGenerator(new TooltipTest());
//
//        List<TestNode> solution = bf.nextSolution();
//
//        String args [] = new String[0];
//        FXController.setRec(rec);
//        javafx.application.Application.launch(FXGui.class, args);
//    }
}
