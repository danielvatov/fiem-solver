package bg.bas.iit.weboptim.solver.fiem.util;

import java.util.ArrayList;
import java.util.List;


public class Cluster {

    private List<double[]> members;
    
    public Cluster(double[] point) {
        addPoint(point);
    }
    
    public void addPoint(double[] point) {
        createIfNull().add(point);
    }

    private List<double[]> createIfNull() {
        if (null == members) {
            members = new ArrayList<double[]>();
        }
        return members;
    }
    
    public void addAllPoints(Cluster otherCluster) {
        createIfNull().addAll(otherCluster.members);
    }
    
    public int size() {
        return createIfNull().size();
    }
    
    public double[] getPoint(int index) {
        return createIfNull().get(index);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (double[] p : members) {
            sb.append(Util.toString(p)).append(" ");
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("}");
        return sb.toString();
    }
}