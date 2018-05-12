package sample;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class ClassifierImage extends Observable implements Runnable {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final int ODD_SQUARE_SIZE = 5;
    private LinkedList<Observer> observers = new LinkedList<>();
    private Mat inputImage;
    private Mat maskImage;
    private Mat outputImage;
    private Classifier classifier;
    private Instances instances;


    public ClassifierImage(Mat image, File modelFile, File instancesFile, Mat mask) {
        inputImage = preprocesingFiltering(addEmptyRowsAboveAndBelowMat(image));
        maskImage = addEmptyRowsAboveAndBelowMat(mask);
        outputImage = new Mat(inputImage.rows() - 2 * ODD_SQUARE_SIZE, inputImage.cols(), inputImage.type());
        try {
            classifier = (Classifier) SerializationHelper.read(modelFile.getPath());
            instances = readInstanceFromFile(instancesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instances readInstanceFromFile(File file) {
        try {
            ArffLoader arffLoader = new ArffLoader();
            arffLoader.setFile(file);
            Instances instances = arffLoader.getDataSet();
            instances.setClassIndex(instances.numAttributes() - 1);
            return instances;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Mat addEmptyRowsAboveAndBelowMat(Mat mat) {
        int width = mat.width();
        Mat tmp = new Mat(ODD_SQUARE_SIZE, width, mat.type());
        Mat biggerMat = new Mat();
        biggerMat.push_back(tmp);
        biggerMat.push_back(mat);
        biggerMat.push_back(tmp);
        return biggerMat;
    }

    private Mat preprocesingFiltering(Mat mat) {
        Mat gray = new Mat();
        Imgproc.cvtColor(cutGreenToGrayscale(mat), gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    private Mat cutGreenToGrayscale(Mat bitmap) {
        Mat mat = new Mat(bitmap.rows(), bitmap.cols(), CvType.CV_8UC3);
        for (int i = 0; i < bitmap.rows(); i++) {
            for (int j = 0; j < bitmap.cols(); j++) {
                mat.put(i, j, 0.0, bitmap.get(i, j)[1], 0.0);
            }
        }
        return mat;
    }

    @Override
    public void run() {
        for (int i = ODD_SQUARE_SIZE; i < inputImage.rows() - ODD_SQUARE_SIZE; i++) {
            for (int j = ODD_SQUARE_SIZE; j < inputImage.cols() - ODD_SQUARE_SIZE; j++) {
                if (maskImage.get(i, j)[0] != 0.0) {
                    Mat surroundingSquare = cutSquareFromImage(j, i, inputImage);
                    Instance instance = createInstanceFromMat(surroundingSquare);
                    try {
                        double[] clsLabel = classifier.distributionForInstance(instance);
                        if (clsLabel[0] > clsLabel[1]) {
                            outputImage.put(i - ODD_SQUARE_SIZE, j, 255.0);
                        } else {
                            outputImage.put(i - ODD_SQUARE_SIZE, j, 0.0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    outputImage.put(i, j, 0.0);
                }
            }
        }
        saveBitmap(outputImage, "classified_without_medianBlur");
        Mat median = new Mat();
        Imgproc.medianBlur(outputImage, median, 9);
        outputImage = median;
        saveBitmap(outputImage, "classified");
        notifyObservers();
    }

    private Mat cutSquareFromImage(int widthPosition, int heightPosition, Mat srcMat) {
        int startWidthPosition = widthPosition - (ODD_SQUARE_SIZE / 2);
        int startHeightPosition = heightPosition - (ODD_SQUARE_SIZE / 2);
        Rect square = new Rect(startWidthPosition, startHeightPosition, ODD_SQUARE_SIZE, ODD_SQUARE_SIZE);
        return srcMat.submat(square);
    }

    private Instance createInstanceFromMat(Mat square) {
        Instance instance = new Instance((int) Math.pow(ODD_SQUARE_SIZE, 2) + 1);
        instance.setDataset(instances);
        for (int i = 0; i < square.width(); i++) {
            for (int j = 0; j < square.height(); j++) {
                instance.setValue(ODD_SQUARE_SIZE * i + j, square.get(j, i)[0]);
            }
        }
        return instance;
    }

    private void saveBitmap(Mat b, String name) {
        Imgcodecs.imwrite(name + ".jpg", b);
    }

    public Mat getOutputImage() {
        return outputImage;
    }

    @Override
    public synchronized void addObserver(Observer observer) {
        super.addObserver(observer);
        observers.addLast(observer);
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        observers.remove(observer);
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
