package biomeConfigurationGenerator;

import biomeConfigurationGenerator.BiomeMain;
import biomeConfigurationGenerator.YMLGenerator;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class BiomeMain extends Application
{
	//Linking properties to text fields in the gui
	@FXML
	private TextField chunkChance;
	@FXML
	private Button create;

	public static void main(String[] args) 
	{
		//Launches the gui
		launch(args);
	}
	//The on click event that takes all the attributes and generates the config file
	@FXML
	public void OnClick(ActionEvent evt)
	{
		//Grabbing the attribute values from the text fields of the gui
		String chanceForChunk = chunkChance.getText();
	
		//Instantiating the YMLgenerator
		YMLGenerator yml = new YMLGenerator(chanceForChunk);
		
		System.out.println("Inside the OnClick event");
		
		//Generating the config file using the valuse grabbed from the gui
		yml.generateFile();
	}
	
	//Building the gui
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		//Instantiating all the interactive pieces of the gui
		chunkChance = new TextField();
		
		//Trying to build the gui
		try {
			//Getting the pane used to hold all the parts of the gui
			BorderPane root;
			
			//Instantiating a loader to load my FXML file
			FXMLLoader loader = new FXMLLoader();
			
			//Telling the loader which file to load and where
			loader.setLocation(BiomeMain.class.getResource("/biomeConfigurationGenerator/BiomeConfigurationGeneratorScene.fxml"));
			
			//I have an event so I need to tell the FXML file this is the controller
			loader.setController(this);
			
			//Loading the pane from the FXML file
			root = (BorderPane) loader.load();
			
			//Creating the scene to hold the pane and everything else
			Scene scene = new Scene(root, 600, 360);
			
			//Setting the stage to not be resizable
			primaryStage.setResizable(false);
			
			//Setting the scene to be in the stage
			primaryStage.setScene(scene);
			
			//Show the stage
			primaryStage.show();
		} 
		catch(Exception e) 
		{
			System.err.println(e.getLocalizedMessage());
			System.err.println("GUI COULDN'T LOAD");
			e.printStackTrace();
		}
	}
}
