package newVisualizer;

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
	
	public void open(Integer i) {
//		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui2.fxml"));
//		
//		Parent root = null;
//        try {
//            root = loader.load();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("test");
//            System.exit(0);
//        }
        
//        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle(i.toString());
//        stage.setScene(scene);
        stage.show();
        
		
	}


}
