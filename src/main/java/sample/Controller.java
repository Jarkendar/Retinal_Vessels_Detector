package sample;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {


    public Button chooseFileImageButton;
    public Button chooseFileExpertButton;
    public Button startButton;
    public ImageView inputImage;
    public ImageView expertMaskImage;
    public ImageView filteringOutput;
    public ImageView machineLearningOutput;
    public Button chooseFileModelButton;
    public Button chooseFileDataSetButton;
    public Button chooseFileMaskButton;
    public CheckBox switchCheckBox;

    public Label tp;
    public Label tn;
    public Label fp;
    public Label fn;

    public Label filtrPrecision;
    public Label filtrRecall;
    public Label filtrSpecificity;
    public Label filtrAccuracy;
    public Label filtrMatthews;

    public Label classPrecision;
    public Label classRecall;
    public Label classSpecificity;
    public Label classAccuracy;
    public Label classMathhews;

    public GridPane trueFalseGrid;
    public GridPane statisticGrid;

    private File fileImage;
    private File fileExpertMask;
    private File fileMask;
    private File fileModel;
    private File fileDataSet;

    private boolean isFilteredEnd = false;
    private boolean isClassifiedEnd = false;
    private FilterImage filterImage;
    private ClassifierImage classifierImage;


    public void initialize() {
        switchCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            trueFalseGrid.setVisible(!trueFalseGrid.isVisible());
            statisticGrid.setVisible(!statisticGrid.isVisible());
        });
    }

    public void clickChooseFileImege(ActionEvent actionEvent) {
        fileImage = Main.openFileChooser();
        if (fileImage != null) {
            Platform.runLater(() -> {
                inputImage.setImage(new FileManager().readImageFromFile(fileImage));
            });
        }
        checkFile(fileImage);
    }

    public void clickChooseFileExpert(ActionEvent actionEvent) {
        fileExpertMask = Main.openFileChooser();
        if (fileExpertMask != null) {
            Platform.runLater(() -> {
                expertMaskImage.setImage(new FileManager().readImageFromFile(fileExpertMask));
            });
        }
        checkFile(fileExpertMask);
    }

    public void clickChooseFileMask(ActionEvent actionEvent) {
        fileMask = Main.openFileChooser();
        checkFile(fileMask);
    }

    private boolean checkFileExist() {
        return fileImage != null && fileExpertMask != null && fileMask != null && fileModel != null && fileDataSet != null;
    }

    private void checkFile(File file) {
        if (file != null) {
            startButton.setDisable(!checkFileExist());
        } else {
            startButton.setDisable(checkFileExist());
        }
    }

    public void clickStartButton(ActionEvent actionEvent) {
        filterImage = new FilterImage(new FileManager().readImage(fileImage));
        filterImage.addObserver(this);
        new Thread(filterImage).start();
        classifierImage = new ClassifierImage(new FileManager().readImage(fileImage), fileModel, fileDataSet, new FileManager().readImage(fileMask));
        classifierImage.addObserver(this);
        new Thread(classifierImage).start();
        startButton.setDisable(true);
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof FilterImage) {
            isFilteredEnd = true;
            filteringOutput.setImage(new FileManager().convertMatToImage(((FilterImage) observable).getOutputBitmap()));
            tryToStartCountStatistics();
        } else if (observable instanceof ClassifierImage) {
            isClassifiedEnd = true;
            machineLearningOutput.setImage(new FileManager().convertMatToImage(((ClassifierImage) observable).getOutputImage()));
            tryToStartCountStatistics();
        } else if (observable instanceof StatisticCounter) {
            setTrueFalseGrid((StatisticCounter) observable);
            setStatisticGrid((StatisticCounter) observable);
            isFilteredEnd = false;
            isClassifiedEnd = false;
            filterImage = null;
            classifierImage = null;
            startButton.setDisable(false);

        }
    }

    private void setTrueFalseGrid(StatisticCounter statisticCounter) {
        Platform.runLater(()->{
            tp.setText(statisticCounter.getTpFiltered() + " / " + statisticCounter.getTpClassified());
            tn.setText(statisticCounter.getTnFiltered() + " / " + statisticCounter.getTnClassified());
            fp.setText(statisticCounter.getFpFiltered() + " / " + statisticCounter.getFpClassified());
            fn.setText(statisticCounter.getFnFiltered() + " / " + statisticCounter.getFnClassified());
        });
    }

    private void setStatisticGrid(StatisticCounter statisticCounter) {
        Platform.runLater(()->{
            filtrPrecision.setText(statisticCounter.getFilteredPrecission());
            filtrRecall.setText(statisticCounter.getFilteredRecall());
            filtrSpecificity.setText(statisticCounter.getFilteredSpecificity());
            filtrAccuracy.setText(statisticCounter.getFilteredAccuracy());
            filtrMatthews.setText(statisticCounter.getFilteredMatthews());
            classPrecision.setText(statisticCounter.getClassifiedPrecission());
            classRecall.setText(statisticCounter.getClassifiedRecall());
            classSpecificity.setText(statisticCounter.getClassifiedSpecificity());
            classAccuracy.setText(statisticCounter.getClassifiedAccuracy());
            classMathhews.setText(statisticCounter.getClassifiedMatthews());
        });
    }

    private void tryToStartCountStatistics() {
        if (isFilteredEnd && isClassifiedEnd) {
            StatisticCounter statisticCounter = new StatisticCounter(filterImage.getOutputBitmap(), classifierImage.getOutputImage(), new FileManager().readImage(fileMask), new FileManager().readImage(fileExpertMask));
            statisticCounter.addObserver(this);
            new Thread(statisticCounter).start();
        }
    }

    public void clickChooseFileModel(ActionEvent actionEvent) {
        fileModel = Main.openFileChooser();
        if (fileModel != null && getFileExtension(fileModel).equals("model")) {
            chooseFileModelButton.setText(fileModel.getName());
            startButton.setDisable(!checkFileExist());
        } else {
            chooseFileModelButton.setText("Choose model file");
            startButton.setDisable(checkFileExist());
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void clickChooseFileDataSet(ActionEvent actionEvent) {
        fileDataSet = Main.openFileChooser();
        if (fileDataSet != null && getFileExtension(fileDataSet).equals("arff")) {
            chooseFileDataSetButton.setText(fileDataSet.getName());
            startButton.setDisable(!checkFileExist());
        } else {
            chooseFileDataSetButton.setText("Choose model file");
            startButton.setDisable(checkFileExist());
        }
    }


}
