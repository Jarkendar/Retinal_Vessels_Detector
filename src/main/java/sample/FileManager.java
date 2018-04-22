package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FileManager {
    public Mat readImage(File file) {
        return Imgcodecs.imread(file.getPath());
    }

    public Image readImageFromFile(File file) {
        Mat bitmap = readImage(file);
        BufferedImage bufferedImage = new BufferedImage(bitmap.width(), bitmap.height(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < bitmap.width(); i++) {
            for (int j = 0; j < bitmap.height(); j++) {
                if (bitmap.get(j,i) == null){
                    System.out.println(i+" "+bitmap.width()+" "+(i<bitmap.width())+" "+j+" "+bitmap.height()+" "+(j<bitmap.height()));
                }
                Color color = new Color((int) bitmap.get(j,i)[2], (int) bitmap.get(j,i)[1], (int) bitmap.get(j,i)[0], 255);
                bufferedImage.setRGB(i,j, color.getRGB());
            }
        }
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
