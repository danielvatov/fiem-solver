package bg.bas.iit.weboptim.solver.fiem.util;

import java.util.List;

import net.vatov.ampl.model.ObjectiveDeclaration;

public class ObjectivesValuesForPoint {
    private double[] point;
    private List<Tuple<ObjectiveDeclaration, Double>> goals;

    public ObjectivesValuesForPoint(double[] point, List<Tuple<ObjectiveDeclaration, Double>> goals) {
        this.goals = goals;
        this.point = point;
    }

    public double[] getPoint() {
        return point;
    }

    public List<Tuple<ObjectiveDeclaration, Double>> getGoals() {
        return goals;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Point: ").append(Util.toString(point)).append(" ");
        if (null != goals) {
            for (Tuple<ObjectiveDeclaration, Double> g : goals) {
                sb.append(g.getA().getName()).append(" : ").append(g.getB()).append("; ");
            }
        }
        return sb.toString();
    }
}