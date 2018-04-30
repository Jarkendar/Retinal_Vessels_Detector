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


    public Label tt;
    public Label tf;
    public Label ft;
    public Label ff;

    public Label filtrPrecision;
    public Label filtrRecall;
    public Label filtrSensitivity;
    public Label filtrAccuracy;

    public Label classPrecision;
    public Label classRecall;
    public Label classSensitivity;
    public Label classAccuracy;

    public GridPane trueFalseGrid;
    public GridPane statisticGrid;

    private File fileImage;
    private File fileExpertMask;
    private File fileMask;
    private File fileModel;
    private File fileDataSet;

    public void initialize(){
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
        FilterImage filterImage = new FilterImage(new FileManager().readImage(fileImage));
        filterImage.addObserver(this);
        new Thread(filterImage).start();
        ClassifierImage classifierImage = new ClassifierImage(new FileManager().readImage(fileImage), fileModel, fileDataSet, new FileManager().readImage(fileMask));
        classifierImage.addObserver(this);
        new Thread(classifierImage).start();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof FilterImage) {
            filteringOutput.setImage(new FileManager().convertMatToImage(((FilterImage) observable).getOutputBitmap()));
        } else if (observable instanceof ClassifierImage) {
            machineLearningOutput.setImage(new FileManager().convertMatToImage(((ClassifierImage) observable).getOutputImage()));
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
