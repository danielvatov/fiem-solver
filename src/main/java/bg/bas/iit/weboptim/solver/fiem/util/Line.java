/*******************************************************************************
 * Copyright (c) 2008-2012 VMware, Inc. All rights reserved.
 ******************************************************************************/
package bg.bas.iit.weboptim.solver.fiem.util;

import bg.bas.iit.weboptim.solver.fiem.FiemException;

public class Line {
    private double[] left;
    private double[] right;

    public Line(double[] left, double[] right) {
        this.left = left;
        this.right = right;
    }

    public double[] getLeft() {
        return left;
    }

    public double[] getRight() {
        return right;
    }

    public int dim() {
        return left.length;
    }
    
    public double getVectorComponent(int i) {
        if (i >= left.length || i<0) {
            throw new FiemException("Invalid dimension " + i);
        }
        return  right[i] - left[i];
    }
    
    public double[] getVector() {
        double[] ret = new double[left.length];
        for (int i=0; i<left.length; ++i) {
            ret[i] = getVectorComponent(i);
        }
        return ret;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i;
        for (i = 0; i<left.length ; i++) {
            sb.append(left[i]).append(",");
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("] -> [");
        for (i = 0; i<right.length ; i++) {
            sb.append(right[i]).append(",");
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("]\n");
        return sb.toString();
    }
}
