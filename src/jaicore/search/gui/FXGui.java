package jaicore.search.gui;

import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.search.structure.core.GraphEventBus;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class FXGui<T> extends Application {

	private Recorder<T> rec;
	private SearchVisualizationPanel<T> searchPanel;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public FXGui(GraphEventBus<T> eventBus) {
		this.rec = rec;
		searchPanel = new SearchVisualizationPanel(rec.getEventBus());
	}
	
	
	@Override
    public void init()
    {
         //By default this does nothing, but it
         //can carry out code to set up your app.
         //It runs once before the start method,
         //and after the constructor.
		if(rec == null) {
			System.out.println("No Recorder was set, therefore the buttons will not work.");
		}
		
    }
    
    @Override
    public void start(Stage primaryStage) {
        //creating the play button
    	final Button play = new Button();
    	play.setText(">");
        // Registering a handler for play
        play.setOnAction((ActionEvent event) -> {
            // Printing Hello World! to the console
            System.out.println("Start to play the record.");
            rec.play();
        });
        
        //Creating a grid pane
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        //Creating a swing node, to add a graph visualization pane
        SwingNode graphNode = new SwingNode();
        graphNode.setContent(searchPanel);
        
        // Adding all the nodes to the grid
        grid.getChildren().add(play);
        // Creating a scene object
        final Scene scene = new Scene(grid);
        // Adding the title to the window (primaryStage)
        primaryStage.setTitle("Search");
        primaryStage.setScene(scene);
        // Show the window(primaryStage)
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

}
