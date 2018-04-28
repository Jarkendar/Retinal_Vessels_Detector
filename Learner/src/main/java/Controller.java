import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {
    public Button imageButton;
    public Button expertButton;
    public Button areaButton;
    public Button arffButton;
    public Label statusLabel;
    public Button dataSetButton;
    public Button modelButton;

    private File imageFile;
    private File expertFile;
    private File areaFile;
    private File arffFile;


    public void chooseImage(ActionEvent actionEvent) {
        imageFile = getImageFile(imageButton);
        validateFileNotNull();
    }

    public void chooseExpert(ActionEvent actionEvent) {
        expertFile = getImageFile(expertButton);
        validateFileNotNull();
    }

    public void chooseArea(ActionEvent actionEvent) {
        areaFile = getImageFile(areaButton);
        validateFileNotNull();
    }

    public void chooseARFF(ActionEvent actionEvent) {
        arffFile = Main.openFileChooser();
        if (getFileExtension(arffFile).equals("arff")) {
            arffButton.setText(arffFile.getName());
            modelButton.setDisable(false);
        } else {
            arffButton.setText(arffFile.getName() + " is not arff file");
            modelButton.setDisable(true);
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

    private File getImageFile(Button button) {
        File file = Main.openFileChooser();
        if (file != null) {
            button.setText(file.getName());
        } else {
            button.setText("Choose file");
        }
        return file;
    }

    private void validateFileNotNull() {
        dataSetButton.setDisable(!(imageFile != null && expertFile != null && areaFile != null));
    }

    public void startPreparingOfDataSet(ActionEvent actionEvent) {
        DataSetCreator dataSetCreator = new DataSetCreator(imageFile, expertFile, areaFile);
        dataSetCreator.addObserver(this);
        Thread thread = new Thread(dataSetCreator);
        thread.start();
    }

    public void startLearnModel(ActionEvent actionEvent) {
    }

    @Override
    public void update(Observable observable, Object argument) {
        Platform.runLater(() -> {
            if (observable instanceof DataSetCreator) {
                String prefix = "Status: ";
                String message = (String) argument;
                switch (message) {
                    case DataSetCreator.CREATE_MEASURES: {
                        statusLabel.setText(prefix + DataSetCreator.CREATE_MEASURES);
                        break;
                    }
                    case DataSetCreator.CREATE_ARFF_SET: {
                        statusLabel.setText(prefix + DataSetCreator.CREATE_ARFF_SET);
                        break;
                    }
                    case DataSetCreator.DONE: {
                        statusLabel.setText(prefix + DataSetCreator.DONE);
                        break;
                    }
                    case DataSetCreator.ERROR_MATRIX_SIZE_IS_INCORRECT: {
                        statusLabel.setText(prefix + DataSetCreator.ERROR_MATRIX_SIZE_IS_INCORRECT);
                        break;
                    }
                    default:
                        statusLabel.setText(prefix + " Empty");
                }
            }
        });

    }
}
