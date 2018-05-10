import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class ClassifierCreator extends Observable implements Runnable {

    public static final String RANDOMIZE_DATA_SET = "RANDOMIZE_DATA_SET";
    public static final String BUILD_CLASSIFIER = "BUILD_CLASSIFIER";
    public static final String CROSS_VALIDATE = "CROSS_VALIDATE";
    public static final String SAVING_MODEL = "SAVING_MODEL";
    public static final String DONE = "DONE";

    private LinkedList<Observer> observers = new LinkedList<>();
    private Instances dataSet;

    public ClassifierCreator(File file) {
        dataSet = readInstanceFromFile(file);
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

    @Override
    public void run() {
        notifyObservers(RANDOMIZE_DATA_SET);
        dataSet.randomize(new Random());
        try {
            long start = System.currentTimeMillis();
            notifyObservers(BUILD_CLASSIFIER);
            MultilayerPerceptron classifier = new MultilayerPerceptron();
            classifier.setLearningRate(0.1);
            classifier.setMomentum(0.2);
            classifier.setTrainingTime(5000);
            classifier.setHiddenLayers("3");
            classifier.buildClassifier(dataSet);
            System.out.println("Build = "+(System.currentTimeMillis()-start)+" ms");
            start = System.currentTimeMillis();
            notifyObservers(CROSS_VALIDATE);
            Evaluation evaluation = new Evaluation(dataSet);
            evaluation.crossValidateModel(classifier, dataSet, 10, new Random());
            System.out.println("CrossValidate = "+(System.currentTimeMillis()-start)+" ms");
            System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
            start = System.currentTimeMillis();
            notifyObservers(SAVING_MODEL);
            SerializationHelper.write("MultilayerPerceptron_"+(dataSet.numAttributes()-1)+"_"+dataSet.numInstances()+".model", classifier);
            System.out.println("Saving = "+(System.currentTimeMillis()-start)+" ms");
            notifyObservers(DONE);
        } catch (Exception e) {
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
