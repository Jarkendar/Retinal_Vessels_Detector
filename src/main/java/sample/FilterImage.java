package sample;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class FilterImage extends Observable implements Runnable {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    private LinkedList<Observer> observers = new LinkedList<>();

    private Mat bitmap;
    private Mat outputBitmap;

    public FilterImage(Mat bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        Mat greenChannel = cutGreenToGrayscale(bitmap);
        Mat gray = new Mat();
        Imgproc.cvtColor(greenChannel, gray, Imgproc.COLOR_BGR2GRAY);
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, 150, 255, 5, true);
        Mat dilate = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.dilate(canny, dilate, kernel);
        Mat median = new Mat();
        Imgproc.medianBlur(dilate, median, 5);
        dilate = new Mat();
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9, 9));
        Imgproc.dilate(median, dilate, kernel);
        outputBitmap = dilate;
        saveBitmap(outputBitmap, "filtered");
        notifyObservers();
    }

    private void saveBitmap(Mat b, String name) {
        Imgcodecs.imwrite(name + ".jpg", b);
    }

    /**
     * Cut green channel from input image and paste this color to new matrix as grayscale.
     *
     * @param bitmap input bitmap in BGR color format
     * @return Mat in grayscale
     */
    private Mat cutGreenToGrayscale(Mat bitmap) {
        Mat mat = new Mat(bitmap.rows(), bitmap.cols(), CvType.CV_8UC3);
        for (int i = 0; i < bitmap.rows(); i++) {
            for (int j = 0; j < bitmap.cols(); j++) {
                mat.put(i, j, 0.0, bitmap.get(i, j)[1], 0.0);
            }
        }
        return mat;
    }


    public Mat getOutputBitmap() {
        return outputBitmap;
    }

    @Override
    public synchronized void addObserver(Observer observer) {
        super.addObserver(observer);
        observers.addLast(observer);
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        observers.remove(0);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
        for (Observer observer : observers) {
            observer.update(this, null);
        }
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        observers.clear();
    }
}
