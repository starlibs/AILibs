package jaicore.search.gui;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.swing.*;

public class FXGui<T> extends Application {



    private RecordPlayer<T> rec;
    private String t;
    
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
        Button play = new Button("play");
        play.setOnAction((ActionEvent) ->{
            System.out.println("play")

;        });

        Button step = new Button("step");
        step.setOnAction((ActionEvent)->{
            System.out.println("step");
            System.out.println(t);
        });


        //toolbar for the different nodes
        ToolBar toolbar = new ToolBar(
                play, step
        );
        toolbar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        root.setTop(toolbar);

        SwingNode swing = new SwingNode();
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
       // SearchVisualizationPanel<T> panel = new SearchVisualizationPanel<>(null);
        JPanel panel = new JPanel();
        panel.add(new JButton("Test"));
	    SwingUtilities.invokeLater(()-> {
            swingnode.setContent(panel);
        });
    }

    public void setRecorder(RecordPlayer<T> rec){
        this.rec = rec;
        t= "aiudvöiu#";
    }


}
