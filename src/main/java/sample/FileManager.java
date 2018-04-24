package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FileManager {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Mat readImage(File file) {
        return Imgcodecs.imread(file.getPath());
    }

    public Image readImageFromFile(File file) {
        Mat bitmap = readImage(file);
        return convertMatToImage(bitmap);
    }

    public Image convertMatToImage(Mat mat) {
        BufferedImage bufferedImage = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < mat.width(); i++) {
            for (int j = 0; j < mat.height(); j++) {
                Color color;
                if (mat.get(j, i).length == 1) {
//                    System.out.println(i+" "+mat.width()+" "+(i<mat.width())+" "+j+" "+mat.height()+" "+(j<mat.height()));
                    color = new Color((int) mat.get(j, i)[0], (int) mat.get(j, i)[0], (int) mat.get(j, i)[0], 255);
                } else {
                    color = new Color((int) mat.get(j, i)[2], (int) mat.get(j, i)[1], (int) mat.get(j, i)[0], 255);
                }
                bufferedImage.setRGB(i, j, color.getRGB());
            }
        }
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
