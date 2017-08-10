package seng302.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import seng302.models.Boat;
import seng302.utilities.AnimationUtils;
import seng302.utilities.DisplaySwitcher;
import seng302.utilities.DisplayUtils;
import seng302.views.BoatDisplay;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import seng302.models.Race;

import java.io.IOException;

/**
 * Created by Louis on 20-Apr-17.
 *
 */
public class ScoreBoardController {

    // Controllers
    private Controller parent;
    private RaceViewController raceViewController;
    private Race race;
    private SelectionController selectionController;

    //FXML fields
    @FXML private CheckBox fpsToggle;
    @FXML private Slider annotationsSlider;
    @FXML private Label raceTimerLabel;
    @FXML private Label lblAnnotation;
    @FXML private CheckBox chkName;
    @FXML private CheckBox chkSpeed;
    @FXML private CheckBox chkPassMarkTime;
    @FXML private CheckBox chkEst;
    @FXML private CheckBox chkStart;
    @FXML private CheckBox zoomToggle;
    @FXML public Button btnTrack;
    @FXML public Button btnExit;
    @FXML private CheckBox chkLaylines;
    @FXML private CheckBox chkVectors;
    @FXML private LineChart chtSparkLine;
    @FXML private NumberAxis xAxis ;
    @FXML private NumberAxis yAxis ;
    @FXML private CheckBox DistanceLinesToggle;
    @FXML private TableView<Boat> tblPlacings;
    @FXML private TableColumn<Boat, Integer> columnPosition;
    @FXML private TableColumn<Boat, String> columnName;
    @FXML private TableColumn<Boat, String> columnSpeed;
    @FXML private TableColumn<Boat, String> columnStatus;


    private final Color UNSELECTED_BOAT_COLOR = Color.WHITE;
    private final Color SELECTED_BOAT_COLOR = Color.rgb(77, 197, 138);
    private Scene scene;



    public void setControllers(Controller parent, RaceViewController raceViewController, Race race, SelectionController selectionController, Scene scene){
        this.parent = parent;
        this.raceViewController = raceViewController;
        this.selectionController = selectionController;
        this.race = race;
        this.scene = scene;
    }

    public class ColoredTextListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item);
            setTextFill(UNSELECTED_BOAT_COLOR);

            BoatDisplay userBoat = raceViewController.getCurrentUserBoatDisplay();
            if(userBoat != null && item != null){
                if(item.contains(userBoat.getBoat().getName())){
                    setTextFill(SELECTED_BOAT_COLOR);
                }
            }

        }
    }

    public void setUp(){
        race = Client.getRace();
        setUpTable();
        raceTimerLabel.textProperty().bind(parent.raceTimerString);
        setupAnnotationControl();
        setupSparkLine();
        annotationsSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if(n < 1) return "None";
                if(n < 2) return "Important";
                if(n < 3) return "All";

                return "All";
            }

            @Override
            public Double fromString(String s) {
                switch(s) {
                    case "None":
                        return 0d;
                    case "Important":
                        return 1d;
                    case "All":
                        return 2d;
                    default:
                        return 2d;
                }
            }
        });
        annotationsSlider.setValue(1);

        addButtonListeners(btnTrack);
        addButtonListeners(btnExit);
    }

    private void setUpTable(){
        columnName.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        columnPosition.setCellValueFactory(cellData -> cellData.getValue().getCurrPlacingProperty().asObject());
        columnSpeed.setCellValueFactory(cellData -> Bindings.format("%.2f kn", cellData.getValue().getSpeedProperty()));
        columnStatus.setCellValueFactory(cellData -> cellData.getValue().getStatusProperty());

        SortedList<Boat> sortedCompetitors = new SortedList<>(race.getObservableCompetitors());
        sortedCompetitors.comparatorProperty().bind(tblPlacings.comparatorProperty());

        tblPlacings.setItems(sortedCompetitors);
        columnPosition.setStyle( "-fx-alignment: CENTER;");
        columnName.setStyle( "-fx-alignment: CENTER;");
        columnSpeed.setStyle( "-fx-alignment: CENTER;");
        columnStatus.setStyle( "-fx-alignment: CENTER;");
        addTableListeners();
    }

    private void addTableListeners(){
        tblPlacings.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                raceViewController.boatSelectedInTable(newSelection);
                btnTrack.setVisible(true);
            }
        });
    }

    @FXML
    /**
     * Called from the GUI when the fpsToggle checkbox is clicked. Updates visibility of fpsLabel.
     */
    private void fpsToggle(){
        parent.fpsLabel(fpsToggle.isSelected());
    }

    @FXML
    private void btnTrackPressed(){
        selectionController.trackBoat();
    }

    /**
     * Set up a listener for the annotation slider so that we can keep the annotations on the boats up to date with
     * the user's selection
     */
    private void setupAnnotationControl() {
        annotationsSlider.valueProperty().addListener((observable, oldValue, newValue) -> raceViewController.changeAnnotations(newValue.intValue(), false));
        zoomToggle.selectedProperty().addListener((observable, oldValue, newValue) -> selectionController.zoomToggle(newValue));
        annotationsSlider.adjustValue(annotationsSlider.getMax());
    }

    private void setupSparkLine(){
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(race.getCourse().getCourseOrder().size());
        xAxis.setTickUnit(1);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(race.getCompetitors().size() + 1);
        yAxis.setUpperBound(0);
        yAxis.setTickUnit(1);
        chtSparkLine.setCreateSymbols(false);
        chtSparkLine.setLegendVisible(false);
        chtSparkLine.getYAxis().setTickLabelsVisible(false);
        chtSparkLine.getXAxis().setTickLabelsVisible(false);
        chtSparkLine.getXAxis().setTickLength(0);
        chtSparkLine.getYAxis().setTickLength(0);
    }

    /**
     * attaches click and hover listeners to buttons
     * @param button the button to attach the listener
     */
    private void addButtonListeners(Button button){
        button.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> AnimationUtils.scaleButtonHover(button));

        button.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> AnimationUtils.scaleButtonHoverExit(button));
    }

    @FXML private void btnExitRacePressed() throws IOException {
        parent.exitRunningRace();
    }

    @FXML
    private void toggleDistanceLines() {
        raceViewController.updateDistanceLine(DistanceLinesToggle.isSelected());
    }

    public boolean isSpeedSelected(){return chkSpeed.isSelected();}

    public boolean isNameSelected(){return chkName.isSelected();}

    public boolean isStartTimeSelected(){return chkStart.isSelected();}

    public boolean isEstSelected(){return chkEst.isSelected();}

    public boolean isTimePassedSelected(){return chkPassMarkTime.isSelected();}

    public boolean areVectorsSelected() {
        return chkVectors.isSelected();
    }

    public boolean isLayLinesSelected(){
        return chkLaylines.isSelected();
    }

    public boolean isDistanceLineSelected(){return DistanceLinesToggle.isSelected();}

    public void addBoatToSparkLine(Series boatSeries){
        if(!chtSparkLine.getData().contains(boatSeries)){
            chtSparkLine.getData().add(boatSeries);
        }
    }

    public void highlightUserBoat(){
        BoatDisplay userBoat = raceViewController.getCurrentUserBoatDisplay();
        if(userBoat != null){
            tblPlacings.getSelectionModel().select(userBoat.getBoat());
        }
    }
}
