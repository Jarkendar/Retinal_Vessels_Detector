package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
        int type;

        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage bufferedImage;
        byte[] data = new byte[mat.width() * mat.height() * (int) mat.elemSize()];
        mat.get(0, 0, data);
        bufferedImage = new BufferedImage(mat.width(), mat.height(), type);
        bufferedImage.getRaster().setDataElements(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), data);

        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
