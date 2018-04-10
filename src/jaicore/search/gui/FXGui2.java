package jaicore.search.gui;

import com.google.common.eventbus.EventBus;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.search.structure.core.GraphEventBus;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class FXGui2<T> extends Application {



    //private RecordPlayer<T> rec;
    private static Object referenceobject;
    private static EventBus eventBus;
    private static Recorder rec;

    private SwingNode swing;

    
	/*@Override
    public void init() throws Exception
    {
         //By default this does nothing, but it
         //can carry out code to set up your app.
         //It runs once before the start method,
         //and after the constructor.
		//if(rec == null) {
		//	System.out.println("No RecordPlayer was set, therefore the buttons will not work.");
		//}

        if(rec == null){
            System.out.println("There is no recorder, which can be used");
            throw new Exception();
        }
		
    }
    */
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("/gui.fxml"));



        //creating the Borderpane for as main pane
        BorderPane root = new BorderPane();

        //Create Buttons for controlling

        //play button
        Button play = new Button("play");
        play.setOnAction((ActionEvent ActionEvent) ->{
            //System.out.println("play");
            //rec.play();
            int events = rec.getNumberOfEvents();
            for(int i = 0; i< events ; i++){
                rec.step();
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
;        });

        //step button
        Button step = new Button("step");
        step.setOnAction((ActionEvent)->{
            //System.out.println("step");
            rec.step();

        });

        //reset button
        Button reset = new Button("reset");
        reset.setOnAction((ActionEvent)->{
            //System.out.println("Reset");
            createSwingContent(swing);
            rec.reset();
        });

        //back step button
        Button back = new Button("back");
        back.setOnAction((ActionEvent)->{
            //System.out.println("back");
            createSwingContent(swing);
            rec.back();
        });




        //toolbar for the different nodes
        ToolBar toolbar = new ToolBar(
                play, step, back, reset
        );
        toolbar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.setTop(toolbar);

        //Create Swing node
        swing = new SwingNode();
        createSwingContent(swing);
        root.setCenter(swing);


        //slider for the speed of the replay
        Slider slider = new Slider();
        root.setBottom(slider);


	    primaryStage.setScene(new Scene(root, 400,300));
	    primaryStage.setTitle("RecordPlayer");
	    primaryStage.show();


    }
    @Override
    public void stop()
    {
        //By default this does nothing
        //It runs if the user clicks the go-away button
        //closing the window or if Platorm.exit() is called.
        //Use Platorm.exit() instead of System.exit(0).
        //is called. This is where you should offer to 
        //save unsaved stuff the user has generated.
    }

    private void createSwingContent(SwingNode swingnode){

        SearchVisualizationPanel<T> panel = new SearchVisualizationPanel<>(rec.getEventBus());
        //JPanel panel = new JPanel();
        panel.add(new JButton("Test"));
	    SwingUtilities.invokeLater(()-> {
            swingnode.setContent(panel);
        });
    }

    public static void setReferenceobject(Object object){
        referenceobject = object;
    }
    public static void setEventBus(GraphEventBus bus){
        eventBus = bus;
    }

    public static void setRec(Recorder recorder){
        rec = recorder;
    }


}
