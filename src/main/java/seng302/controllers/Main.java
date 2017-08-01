package seng302.controllers;

/**
 * Main class. Sets up server and client and starts GUI.
 */
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import seng302.data.ConnectionManager;
import seng302.data.DataStreamReader;
import seng302.utilities.Config;
import seng302.models.Race;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main extends Application {

    private static Scene scene;
    private static Client client;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Config.initializeConfig();
        setupServer();
        setupClient();

        //Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        this.primaryStage.setTitle("Race Vision");
        this.primaryStage.getIcons().add(new Image("graphics/icon.png"));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        this.primaryStage.setHeight(primaryScreenBounds.getHeight());
        this.primaryStage.setWidth(primaryScreenBounds.getWidth());
        loadMainMenu();
        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
        this.primaryStage.show();
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });

        //UserInputController userInputController = new UserInputController(scene, Client.getRace());
        //client.setUserInputController(userInputController);
        //userInputController.addObserver(client);
    }

    public static void main( String[] args ) {launch(args); }

    /**
     * Initializes the client on it's own thread.
     */
    private static void setupClient() {
        client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.setName("Client");
        clientThread.start();
    }

    /**
     * Creates a Server object, puts it in it's own thread and starts the thread
     */
    private static void setupServer() throws IOException {
        RaceUpdater runner = new RaceUpdater();
        runner.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread runnerThread = new Thread(runner);
        runnerThread.setName("Race Updater");
        runnerThread.start();
        Server server;
        server = new Server(2828, runner);
        server.setScaleFactor(Config.MOCK_SPEED_SCALE);
        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }

    public static Client getClient() {
        return client;
    }

    private void loadMainMenu() {
        try {
            MainMenuController mainMenu = (MainMenuController) replaceSceneContent("main_menu.fxml");
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * takes an fxml file and replaces the current screen with it
     * @param fxml an FXML file
     * @return a display
     * @throws Exception if can't find FXML
     */
    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL fxmlLocation = getClass().getClassLoader().getResource("main_menu.fxml");
        Node node;
        node = loader.load(fxmlLocation.openStream());
        AnchorPane pane = new AnchorPane();
        pane.getChildren().setAll(node);
        scene = new Scene(pane);
        setScene(scene);
        primaryStage.setScene(scene);

        double stageWidth = primaryStage.getWidth();
        if (!Double.isNaN(stageWidth)) {
            stageWidth -= (primaryStage.getWidth() - primaryStage.getScene().getWidth());
        }

        double stageHeight = primaryStage.getHeight();
        if (!Double.isNaN(stageHeight)) {
            stageHeight -= (primaryStage.getHeight() - primaryStage.getScene().getHeight());
        }
        if (!Double.isNaN(stageWidth)) {
            pane.setPrefWidth(stageWidth);
        }
        if (!Double.isNaN(stageHeight)) {
            pane.setPrefHeight(stageHeight);
        }

        return (Initializable) loader.getController();
    }

    private void setScene(Scene newScene){
        scene = newScene;
    }
}

