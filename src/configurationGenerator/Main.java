package configurationGenerator;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application
{
	//Linking properties to text fields in the gui
	@FXML
	private TextField fileName;
	@FXML
	private TextField place;
	@FXML
	private TextField maxSpawns;
	@FXML
	private TextField chanceToSpawn;
	@FXML
	private TextField basementDepth;
	@FXML
	private TextField minHeight;
	@FXML
	private TextField maxHeight;
	@FXML
	private TextField randomRotation;
	@FXML
	private TextField pasteAir;
	@FXML
	private Button create;
	
	public static void main(String[] args) 
	{	
		//Launches the gui
		launch(args);
	}
	//The on click event that takes all the attributes and generates the config file
	@FXML
	public void OnClick(ActionEvent evt) {
		//Instantiating the YMLgenerator
		YMLgenerator yml = new YMLgenerator();
		//Grabbing the attribute values from the text fields of the gui
		String nameOfFile = fileName.getText();
		String placeToSpawn = place.getText();
		String maxNumOfSpawns = maxSpawns.getText();
		String spawnChance = chanceToSpawn.getText();
		String depthOfbasement = basementDepth.getText();
		String minimumHeight = minHeight.getText();
		String maximumHeight = maxHeight.getText();
		String randomRotate = randomRotation.getText();
		String pasteSchematicAir = pasteAir.getText();
		//Generating the config file using the valuse grabbed from the gui
		yml.generateFile(nameOfFile, placeToSpawn, maxNumOfSpawns, spawnChance, depthOfbasement, minimumHeight, maximumHeight, randomRotate, pasteSchematicAir);
	}
	//Building the gui
	@Override
	public void start(Stage primaryStage) throws Exception {
		//Instantiating all the interactive pieces of the gui
		fileName = new TextField();
		place = new TextField();
		maxSpawns = new TextField();
		chanceToSpawn = new TextField();
		basementDepth = new TextField();
		minHeight = new TextField();
		maxHeight = new TextField();
		randomRotation = new TextField();
		pasteAir = new TextField();
		create = new Button();
		//Trying to build the gui
		try {
			//Getting the pane used to hold all the parts of the gui
			BorderPane root;
			//Instantiating a loader to load my FXML file
			FXMLLoader loader = new FXMLLoader();
			//Telling the loader which file to load and where
			loader.setLocation(Main.class.getResource("/configurationGenerator/ConfigurationGeneratorScene.fxml"));
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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}