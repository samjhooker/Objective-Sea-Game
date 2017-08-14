package seng302.utilities;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import seng302.controllers.*;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class to manage the handling of switching FXML displays
 */
public class DisplaySwitcher {

    private static Scene scene;
    private Stage stage;
    private Main main;

    public DisplaySwitcher(Main main, Stage stage){
        this.stage = stage;
        this.main = main;
    }

    /**
     * loads the main menu into the stage
     */
    public void loadMainMenu() {
        try {
            MainMenuController mainMenu = (MainMenuController) replaceSceneContent("main_menu.fxml");
            mainMenu.setApp(main);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * loads the race view into the stage
     * @param isHost a boolean showing if the user is a host or not
     */
    public void loadRaceView(boolean isHost) {
        try {
            Controller race = (Controller) replaceSceneContent("race_view.fxml");
            race.setApp(isHost, this, scene);
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
    public Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL fxmlLocation = getClass().getClassLoader().getResource(fxml);
        loader.setLocation(fxmlLocation);
        Parent root = loader.load();
        scene = new Scene(root);
        setScene(scene);
        AnimationUtils.transitionFXML(root);
        stage.setScene(scene);
        return (Initializable) loader.getController();
    }

    private void setScene(Scene newScene){
        scene = newScene;
    }

    public static Scene getScene(){return scene;}

}