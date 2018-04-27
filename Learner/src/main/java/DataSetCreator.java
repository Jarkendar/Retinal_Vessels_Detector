import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import weka.core.*;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataSetCreator extends Observable implements Runnable {

    public static final String ERROR_MATRIX_SIZE_IS_INCORRECT = "MATRIX_SIZE_IS_INCORRECT";
    public static final String CREATE_MEASURES = "CREATE_MEASURES";
    public static final String CREATE_ARFF_SET = "CREATE_ARFF_SET";
    public static final String DONE = "DONE";
    private static final int ODD_SQUARE_SIZE = 5;
    private static final int NUMBER_OF_MEASURES = 10 * 1000;


    private LinkedList<Observer> observers = new LinkedList<>();

    private Mat imageBitmap;
    private Mat expertMaskBitmap;
    private Mat areaImageBitmap;

    public DataSetCreator(File image, File expert, File area) {
        imageBitmap = addEmptyRowsAboveAndBelowMat(readMatFromFile(image));
        expertMaskBitmap = addEmptyRowsAboveAndBelowMat(readMatFromFile(expert));
        areaImageBitmap = readMatFromFile(area);
    }

    private Mat readMatFromFile(File file) {
        return Imgcodecs.imread(file.getPath());
    }

    private Mat addEmptyRowsAboveAndBelowMat(Mat mat) {
        System.out.println("Matrix size before" + mat.width() + " " + mat.height());
        int width = mat.width();
        Mat tmp = new Mat(ODD_SQUARE_SIZE, width, mat.type());
        Mat biggerMat = new Mat();
        biggerMat.push_back(tmp);
        biggerMat.push_back(mat);
        biggerMat.push_back(tmp);
        System.out.println("Matrix size after" + biggerMat.width() + " " + biggerMat.height());
        return biggerMat;
    }

    @Override
    public void run() {
        System.out.println("Start thread DataSetCreator");
        imageBitmap = preprocesingFiltering(imageBitmap);
        System.out.println("Preprocessing is end");
        if (bitmapsSizeAreCorrect(imageBitmap, expertMaskBitmap, areaImageBitmap)) {
            System.out.println("Is correct");
            notifyObservers(CREATE_MEASURES);
            ArrayList<Measure> measures = createMeasurements(NUMBER_OF_MEASURES);
            notifyObservers(CREATE_ARFF_SET);
            saveDataSetAsARFF(measures);
            notifyObservers(DONE);
        } else {
            System.out.println("Error incorrect sizes");
            notifyObservers(ERROR_MATRIX_SIZE_IS_INCORRECT);
        }
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

    private boolean bitmapsSizeAreCorrect(Mat im, Mat ex, Mat ar) {
        boolean correctWidth = im.width() == ex.width() && ex.width() == ar.width();
        boolean correctHeight = im.height() == ex.height() && ex.height() == ar.height() + 2 * ODD_SQUARE_SIZE;//area is not expand
        return correctWidth && correctHeight;
    }

    private ArrayList<Measure> createMeasurements(int numberOfMeasures) {
        Random random = new Random(System.currentTimeMillis());
        int vesselsMeasure = 0;
        int backgroundMeasure = 0;
        ArrayList<Measure> measures = new ArrayList<>(numberOfMeasures);
        for (int i = 0; i < numberOfMeasures; i++) {
            int pointWidth = 0;
            int pointHeight = 0;
            while (areaImageBitmap.get(pointHeight, pointWidth)[0] == 0.0) {
                pointHeight = random.nextInt(areaImageBitmap.height());
                pointWidth = random.nextInt(areaImageBitmap.width());
            }
            pointHeight += ODD_SQUARE_SIZE;

            Mat square = cutSquareFromImage(pointWidth, pointHeight, imageBitmap);
            boolean expertResult = expertMaskBitmap.get(pointHeight, pointWidth)[0] > 0.0;
            if (expertResult) {
                vesselsMeasure++;
            } else {
                backgroundMeasure++;
            }
            Measure measure = new Measure(pointWidth, pointHeight, getValuesFromSquare(square), expertResult);
            measures.add(i, measure);
        }
        ArrayList<Measure> balancedMeasures = balanceDataSet(vesselsMeasure, backgroundMeasure, measures);

        return balancedMeasures;
    }

    private Mat cutSquareFromImage(int widthPosition, int heightPosition, Mat srcMat) {
        int startWidthPosition = widthPosition - (ODD_SQUARE_SIZE / 2);
        int endWidthPosition = widthPosition + (ODD_SQUARE_SIZE / 2);
        int startHeightPosition = heightPosition - (ODD_SQUARE_SIZE / 2);
        int endHeightPosition = heightPosition + (ODD_SQUARE_SIZE / 2);
        Rect square = new Rect(startHeightPosition, startWidthPosition, endHeightPosition, endWidthPosition);
        return srcMat.submat(square);
    }

    private ArrayList<Double> getValuesFromSquare(Mat square) {
        ArrayList<Double> values = new ArrayList<>((int) Math.pow(ODD_SQUARE_SIZE, 2));
        for (int i = 0; i < square.width(); i++) {
            for (int j = 0; j < square.height(); j++) {
                values.add(i * square.width() + j, square.get(j, i)[0]);
            }
        }
        return values;
    }

    private ArrayList<Measure> balanceDataSet(int good, int bad, ArrayList<Measure> dataArray) {
        ArrayList<Measure> balance = new ArrayList<>(2 * good);
        int badCounter = 0;
        for (int i = 0, j = 0; i < 2 * good; i++, j++) {
            if (dataArray.get(j).isVesselByExpert()) {
                balance.add(i, dataArray.get(j));
            } else {
                if (badCounter < good) {
                    balance.add(i, dataArray.get(j));
                    badCounter++;
                } else {
                    j++;
                }
            }
        }
        return balance;
    }

    private void saveDataSetAsARFF(ArrayList<Measure> measures) {
        FastVector atts = new FastVector();
        List<Instance> instances = new ArrayList<Instance>();
        for (int i = 0; i < Math.pow(ODD_SQUARE_SIZE, 2) + 1; i++) {
            Attribute current = new Attribute("Attribute" + i);
            if (i == 0) {
                for (int j = 0; j < measures.size(); j++) {
                    instances.add(new SparseInstance((int) Math.pow(ODD_SQUARE_SIZE, 2) + 1));
                }
            }
            if (i != Math.pow(ODD_SQUARE_SIZE, 2)) {
                for (int j = 0; j < measures.size(); j++) {
                    instances.get(j).setValue(current, measures.get(j).getSurroundingValues().get(i));
                }
            } else {
                Properties properties = new Properties();
                properties.setProperty("true", "true");
                properties.setProperty("false", "false");
                current = new Attribute("Attribute" + i, new ProtectedProperties(properties));
                for (int j = 0; j < measures.size(); j++) {
                    instances.get(j).setValue(current, Boolean.toString(measures.get(j).isVesselByExpert()));
                }
            }
            atts.addElement(current);
        }
        Instances newDataset = new Instances("Dataset", atts, measures.size());
        for (Instance instance : instances) {
            newDataset.add(instance);
        }
        ArffSaver arffSaver = new ArffSaver();
        arffSaver.setInstances(newDataset);
        try {
            arffSaver.setFile(new File("test.arff"));
            arffSaver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    public void notifyObservers(Object message) {
        super.notifyObservers(message);
        for (Observer observer : observers) {
            observer.update(this, message);
        }
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        observers.clear();
    }
}
