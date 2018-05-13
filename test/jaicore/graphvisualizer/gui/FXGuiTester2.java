package jaicore.graphvisualizer.gui;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestNode;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class FXGuiTester2 extends FXGui{
    @Override
    public void init() throws Exception {
        super.init();
        GraphGenerator generator = new TestGraphGenerator();
        BestFirst<TestNode, String> bf = new BestFirst<>(generator, n->(double)Math.round(Math.random()*100));
        this.rootRecorder = new Recorder<>(bf);
        rootRecorder.setTooltipGenerator(n->{
            Node node = (Node) n;
            return String.valueOf(node.getInternalLabel());
        });
        bf.nextSolution();
    }

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);

        NQueenGenerator queenGenerator = new NQueenGenerator(4);
        BestFirst<QueenNode, String> qs = new BestFirst<>(queenGenerator,  n->(double)n.getPoint().getNumberOfAttackedCells());
        Recorder qrec = new Recorder(qs);

        qs.nextSolution();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent r = loader.load();

        FXController crtl = loader.getController();
        crtl.setRecorder(qrec);
        Stage stage2 = new Stage();

        Scene scene = new Scene(r, 800, 600);

        stage2.setTitle("Queen");
        stage2.setScene(scene);
        stage2.show();


    }

    public static void main(String [] args){
        launch(args);
    }

}
