package sample;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class FilterImage extends Observable implements Runnable {

    private LinkedList<Observer> observers = new LinkedList<>();

    private Mat bitmap;
    private Mat outputBitmap;

    public FilterImage(Mat bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        Mat greenChannel = cutGreenToGrayscale(bitmap);

        outputBitmap = greenChannel;
        notifyObservers();
    }

    /**
     * Cut green channel from input image and paste this color to new matrix as grayscale.
     *
     * @param bitmap input bitmap in BGR color format
     * @return Mat in grayscale
     */
    private Mat cutGreenToGrayscale(Mat bitmap) {
        Mat mat = new Mat(bitmap.rows(), bitmap.cols(), CvType.CV_32F);
        for (int i = 0; i < bitmap.rows(); i++) {
            for (int j = 0; j < bitmap.cols(); j++) {
                mat.put(i, j, bitmap.get(i, j)[1]);
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
