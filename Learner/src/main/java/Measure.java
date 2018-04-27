import java.util.ArrayList;

public class Measure {
    private int widthPosition;
    private int heightPosition;
    private ArrayList<Double> surroundingValues;
    private boolean vesselByExpert;


    public Measure(int widthPosition, int heightPosition, ArrayList<Double> surroundingValues, boolean vesselByExpert) {
        this.widthPosition = widthPosition;
        this.heightPosition = heightPosition;
        this.surroundingValues = surroundingValues;
        this.vesselByExpert = vesselByExpert;
    }

    public int getWidthPosition() {
        return widthPosition;
    }

    public int getHeightPosition() {
        return heightPosition;
    }

    public ArrayList<Double> getSurroundingValues() {
        return surroundingValues;
    }

    public boolean isVesselByExpert() {
        return vesselByExpert;
    }
}
