package bg.bas.iit.weboptim.solver.fiem.util;

import java.util.Comparator;


public class ObjectivesValuesComparator implements Comparator<ObjectivesValuesForPoint> {

    private double[] refPoint;

    public ObjectivesValuesComparator(double[] refPoint) {
        this.refPoint = refPoint;
    }

    public int compare(ObjectivesValuesForPoint arg0, ObjectivesValuesForPoint arg1) {
        double[] g1 = Util.getGoalVector(arg0);
        double[] g2 = Util.getGoalVector(arg1);
        double dif = Util.euclidDistance(refPoint, g1) - Util.euclidDistance(refPoint, g2);
        return dif == 0 ? 0 : (dif < 0 ? -1 : 1);
    }

}
