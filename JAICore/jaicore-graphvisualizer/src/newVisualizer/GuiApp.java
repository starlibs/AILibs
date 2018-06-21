package newVisualizer;

import java.io.IOException;

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.gui.Recorder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiApp extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("App");
		
	}
	
	public void open(String i, Recorder recorder) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui2.fxml"));
		try {
			System.out.println(getClass().getResourceAsStream("/gui2.fxml").read());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Parent root = null;
        try {
            root = loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("test");
            System.exit(0);
        }


        
        Scene scene = new Scene(root, 800, 600);
        Stage stage = new Stage();
        stage.setTitle(i);
        stage.setScene(scene);

		Controller controller = loader.getController();

//		controller.register(algorithm);
        controller.registerRecorder(recorder);
        stage.show();
		
	}


}
