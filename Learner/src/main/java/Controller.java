import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;

public class Controller {
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
        getImageFile(imageFile, imageButton);
    }

    public void chooseExpert(ActionEvent actionEvent) {
        getImageFile(expertFile, expertButton);
    }

    public void chooseArea(ActionEvent actionEvent) {
        getImageFile(areaFile, areaButton);
    }

    public void chooseARFF(ActionEvent actionEvent) {
        arffFile = Main.openFileChooser();
        if (getFileExtension(arffFile).equals("arff")) {
            arffButton.setText(arffFile.getName());
        } else {
            arffButton.setText(arffFile.getName() + " is not arff file");
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

    private void getImageFile(File file, Button button) {
        file = Main.openFileChooser();
        System.out.println(file.getPath());
        if (file != null) {
            button.setText(file.getName());
        } else {
            button.setText("Choose file");
        }
    }

    public void startCreationOfDataSet(ActionEvent actionEvent) {
    }

    public void startLearnModel(ActionEvent actionEvent) {
    }
}
