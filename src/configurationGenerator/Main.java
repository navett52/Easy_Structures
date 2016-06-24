package configurationGenerator;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application
{
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
		launch(args);
		//YMLgenerator yml = new YMLgenerator();
		//yml.generateFile("d", "d", "d", "d", "d", "d", "d", "d", "d");
	}
	
	@FXML
	public void OnClick(ActionEvent evt) {
		YMLgenerator yml = new YMLgenerator();
		String nameOfFile = fileName.getText();
		String placeToSpawn = place.getText();
		String maxNumOfSpawns = maxSpawns.getText();
		String spawnChance = chanceToSpawn.getText();
		String depthOfbasement = basementDepth.getText();
		String minimumHeight = minHeight.getText();
		String maximumHeight = maxHeight.getText();
		String randomRotate = randomRotation.getText();
		String pasteSchematicAir = pasteAir.getText();
		yml.generateFile(nameOfFile, placeToSpawn, maxNumOfSpawns, spawnChance, depthOfbasement, minimumHeight, maximumHeight, randomRotate, pasteSchematicAir);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
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
		try {
			Pane root;
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("/configurationGenerator/ConfigurationGeneratorScene.fxml"));
			loader.setController(this);
			root = (Pane) loader.load();
			Scene scene = new Scene(root, 600, 360);
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}