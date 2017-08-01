package seng302.utilities;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Created by Devin on 25/07/17.
 */
public class AnimationUtils {

    public static ScaleTransition scaleTransitionCollision(Node node, int duration, double amount){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(duration), node);
        scaleTransition.setByY(amount);
        scaleTransition.setByX(amount);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        return scaleTransition;
    }

    public static FadeTransition fadeOutTransition(Node node, int duration){
        FadeTransition fadeTransition = new FadeTransition(new Duration(duration), node);
        fadeTransition.setFromValue(node.getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        return fadeTransition;
    }

    public static ScaleTransition scaleButtonHover(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setByY(0.1);
        scaleTransition.setByX(0.1);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        return scaleTransition;
    }

    public static ScaleTransition scaleButtonHoverExit(Node node){
        ScaleTransition scaleTransition = new ScaleTransition(new Duration(200), node);
        scaleTransition.setFromX(node.getScaleX());
        scaleTransition.setFromY(node.getScaleY());
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        scaleTransition.play();
        return scaleTransition;
    }
}
