package sample;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class StatisticCounter extends Observable implements Runnable {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final double WHITE = 255.0;
    private static final double BLACK = 0.0;

    private LinkedList<Observer> observers = new LinkedList<>();
    private Mat filteredImage;
    private Mat classifiedImage;
    private Mat maskImage;
    private Mat expertImage;

    private int tpFiltered = 0;
    private int tnFiltered = 0;
    private int fpFiltered = 0;
    private int fnFiltered = 0;

    private int tpClassified = 0;
    private int tnClassified = 0;
    private int fpClassified = 0;
    private int fnClassified = 0;

    public StatisticCounter(Mat filteredImage, Mat classifiedImage, Mat maskImage, Mat expertImage) {
        this.filteredImage = filteredImage;
        this.classifiedImage = classifiedImage;
        this.maskImage = maskImage;
        this.expertImage = expertImage;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < maskImage.height(); i++) {
            for (int j = 0; j < maskImage.width(); j++) {
                if (maskImage.get(i, j)[0] != 0) {
                    double expertColor = expertImage.get(i, j)[0];
                    checkFilteredImage(expertColor, i, j);
                    checkClassifiedImage(expertColor, i, j);
                }
            }
        }
        System.out.println("Stat time = "+(System.currentTimeMillis()-start)+" ms");
        notifyObservers();
    }

    private void checkFilteredImage(double expertColor, int i, int j) {
        if (expertColor == filteredImage.get(i, j)[0]) {
            if (expertColor == WHITE) {
                tpFiltered++;
            } else if (expertColor == BLACK) {
                fnFiltered++;
            }
        } else {
            if (expertColor == WHITE) {
                fpFiltered++;
            } else if (expertColor == BLACK) {
                tnFiltered++;
            }
        }
    }

    private void checkClassifiedImage(double expertColor, int i, int j) {
        if (expertColor == classifiedImage.get(i, j)[0]) {
            if (expertColor == WHITE) {
                tpClassified++;
            } else if (expertColor == BLACK) {
                fnClassified++;
            }
        } else {
            if (expertColor == WHITE) {
                fpClassified++;
            } else if (expertColor == BLACK) {
                tnClassified++;
            }
        }
    }

    public int getTpFiltered() {
        return tpFiltered;
    }

    public int getTnFiltered() {
        return tnFiltered;
    }

    public int getFpFiltered() {
        return fpFiltered;
    }

    public int getFnFiltered() {
        return fnFiltered;
    }

    public int getTpClassified() {
        return tpClassified;
    }

    public int getTnClassified() {
        return tnClassified;
    }

    public int getFpClassified() {
        return fpClassified;
    }

    public int getFnClassified() {
        return fnClassified;
    }

    public String getFilteredPrecission(){
        double result = (double) tpFiltered / (double)(tpFiltered+fpFiltered);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getFilteredRecall(){
        double result = (double) tpFiltered / (double)(tpFiltered+fnFiltered);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getFilteredSpecificity(){
        double result = (double) tnFiltered/ (double)(tnFiltered+fpFiltered);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getFilteredAccuracy(){
        double result = (double) (tpFiltered+tnFiltered) / (double)(tpFiltered+tnFiltered+fpFiltered+fnFiltered);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getClassifiedPrecission(){
        double result = (double) tpClassified / (double)(tpClassified+fpClassified);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getClassifiedRecall(){
        double result = (double) tpClassified / (double)(tpClassified+fnClassified);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getClassifiedSpecificity(){
        double result = (double) tnClassified/ (double)(tnClassified+fpClassified);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
    }

    public String getClassifiedAccuracy(){
        double result = (double) (tpClassified+tnClassified) / (double)(tpClassified+tnClassified+fpClassified+fnClassified);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(result);
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
