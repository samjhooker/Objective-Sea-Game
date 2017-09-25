package seng302.controllers;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import seng302.data.BoatAction;
import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.DisplayUtils;
import seng302.views.DisplayTouchController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;

/**
 * handles user key presses.
 */
public class KeyInputController extends Observable {

    private Scene scene;
    private int commandInt;
    private int clientID;
    private Race race;
    private Controller controller;
    private final Set<KeyCode> cosumedKeyCodes = new HashSet<>(Arrays.asList(KeyCode.SPACE, KeyCode.UP, KeyCode.DOWN));

    /**
     * Sets up user key press handler.
     * @param scene The scene of the client
     */
    public KeyInputController(Scene scene, Race race) {
        this.scene = scene;
        this.race = race;
        keyEventListener();
    }

    private void keyEventListener() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
            checkKeyPressed(key.getCode());
            if ( cosumedKeyCodes.contains(key.getCode()) ){
                key.consume();
            }
        });
    }

    /**
     * recorded the key that has been pressed and notifies the observes of this key
     * @param key the key that has been pressed
     */
    private void checkKeyPressed(KeyCode key){
        commandInt = BoatAction.getTypeFromKeyCode(key);
        if (commandInt != -1) {
            if(key.equals(KeyCode.ENTER)){
                controller.setUserHelpLabel("Tacking", Color.web("#4DC58B"));
            }
            setChanged();
            notifyObservers();
        }
        if (key.equals(SHIFT)){
            Boat boat = race.getBoatById(clientID);
            boat.changeSails();
        }
    }

    public int getCommandInt() {
        return commandInt;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setController(Controller controller){ this.controller = controller;}
}
