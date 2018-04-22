package sample;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;

import java.io.File;

public class Controller {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

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
        if(fileImage != null){
            Platform.runLater(()->{
                inputImage.setImage(new FileManager().readImageFromFile(fileImage));
            });
        }
        checkFile(fileImage);
    }

    public void clickChooseFileExpert(ActionEvent actionEvent) {
        fileExpertMask = Main.openFileChooser();
        if(fileExpertMask != null){
            Platform.runLater(()->{
                expertMaskImage.setImage(new FileManager().readImageFromFile(fileExpertMask));
            });
        }
        checkFile(fileExpertMask);
    }

    private boolean checkFileExist(){
        return fileImage != null && fileExpertMask != null;
    }

    private void checkFile(File file){
        if (file != null){
            startButton.setDisable(!checkFileExist());
        }else {
            startButton.setDisable(checkFileExist());
        }
    }

    public void clickStartButton(ActionEvent actionEvent) {
    }
}
