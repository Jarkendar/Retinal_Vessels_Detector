package sample;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

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

    private File fileImage;
    private File fileExpertMask;

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

    private boolean checkFileExist() {
        return fileImage != null && fileExpertMask != null;
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
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof FilterImage) {
            filteringOutput.setImage(new FileManager().convertMatToImage(((FilterImage) observable).getOutputBitmap()));
        }
    }
}
