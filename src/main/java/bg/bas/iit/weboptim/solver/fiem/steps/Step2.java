package bg.bas.iit.weboptim.solver.fiem.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.vatov.ampl.model.OptimModel;
import net.vatov.ampl.solver.OptimModelInterpreter;

import org.apache.log4j.Logger;

import bg.bas.iit.weboptim.solver.fiem.BaseStep;
import bg.bas.iit.weboptim.solver.fiem.FiemException;
import bg.bas.iit.weboptim.solver.fiem.FiemSolver.Config;
import bg.bas.iit.weboptim.solver.fiem.util.ConstraintGoldenSplit;
import bg.bas.iit.weboptim.solver.fiem.util.Line;
import bg.bas.iit.weboptim.solver.fiem.util.Util;

public class Step2 extends BaseStep {

    private Logger logger;
    
    private class DominationsComparator implements Comparator<PointDominations> {

        public int compare(PointDominations p1, PointDominations p2) {
            return p1.dominations - p2.dominations;
        }

    }

    private class PointDominations {
        public double[] p = null;
        public int dominations = 0;

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            ret.append("[").append(p[0]);
            for (int i = 1; i < p.length; ++i) {
                ret.append(",").append(p[i]);
            }
            ret.append("]").append(" ").append(dominations);
            return ret.toString();
        }
    }

    private final Config cfg;

    private List<double[]> p;
    private List<double[]> pe;
    private double[] ci;
    private double[] ce;
    private double[] t;

    public Step2(OptimModel model, OptimModelInterpreter interpreter, Config cfg) {
        super(model, interpreter);
        this.cfg = cfg;
        logger = getLogger(Step2.class, cfg.threadedLogging);
    }

    public List<double[]> execute(List<double[]> ptot) {
        logger.debug("executing step 2");
        Util.dumpPopulation(logger, ptot, "starting population");
        findUndominated(ptot);
        Util.dumpPopulation(logger, p, "internal population");
        Util.dumpPopulation(logger, pe, "external population");
        computeWeightCenters();
        Util.dumpPoint(logger, ci, "computed Ci");
        Util.dumpPoint(logger, ce, "computed Ce");
        computeMoveVector();
        Util.dumpPoint(logger, t, "computed translation vector");
        movePoints();
        Util.dumpPopulation(logger, p, "internal population after translation");
        return p;
    }

    private void computeMoveVector() {
        t = new double[ci.length];
        for (int i = 0; i < ci.length; ++i) {
            t[i] = ce[i] - ci[i];
        }
        double maxT = Double.MIN_VALUE;
        for (int i = 0; i < t.length; ++i) {
            if (maxT < t[i]) {
                maxT = t[i];
            }
        }
        for (int i = 0; i < t.length; ++i) {
            t[i] /= maxT;
        }
    }

    private void findUndominated(List<double[]> ptot) {
        int peSize = Long.valueOf(Math.round(cfg.popSize * cfg.popSizeExtCoef)).intValue();
        p = new ArrayList<double[]>(cfg.popSize);
        pe = new ArrayList<double[]>(peSize);
        List<PointDominations> pds = initPointDominationsArray(ptot);
        countDominations(pds);
        Collections.sort(pds, new DominationsComparator());
        for (int i = 0; i < pds.size(); ++i) {
            if (i < cfg.popSize) {
                p.add(pds.get(i).p);
            } else {
                pe.add(pds.get(i).p);
            }
        }
    }

    private void countDominations(List<PointDominations> pds) {
        for (PointDominations pd : pds) {
            for (PointDominations e : pds) {
                if (pd == e) {
                    continue;
                }
                if (dominates(pd.p, e.p)) {
                    pd.dominations++;
                }
            }
        }
    }

    private List<PointDominations> initPointDominationsArray(List<double[]> ptot) {
        List<PointDominations> ret = new ArrayList<PointDominations>(ptot.size());
        for (double[] p : ptot) {
            PointDominations pd = new PointDominations();
            pd.p = p;
            ret.add(pd);
        }
        return ret;
    }

    private boolean dominates(double[] p1, double[] p2) {
        if (p1.length != p2.length) {
            throw new FiemException("dimension mismatch");
        }
        boolean atLeastOne = false;
        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] < p2[i]) {
                return false;
            }
            atLeastOne |= p1[i] > p2[i];
        }
        return atLeastOne;
    }

    // TODO ако две точки лежат на отсечка успоредна на вектора на транслация ще
    // бъдат транслирани в една и съща точка
    private void movePoints() {
        for (double[] point : p) {
            movePoint(point);
        }
    }

    private void movePoint(double[] point) {
        long a = 1;
        double[] left = Arrays.copyOf(point, point.length);
        double[] right;
        do {
            right = Util.translate(left, a, t);
            a *= 10;
        } while (Util.constraintsSatisfied(model, interpreter, right));
        copyPointTo(point, new ConstraintGoldenSplit(model, interpreter).spit(new Line(left, right)));
    }

    private void copyPointTo(double[] target, double[] source) {
        for (int i=0; i< source.length; ++i) {
            target[i] = source[i];
        }
    }

    private void computeWeightCenters() {
        ci = Util.computeWeightCenter(p);
        ce = Util.computeWeightCenter(pe);
    }
}
