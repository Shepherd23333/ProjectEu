package moze_intel.projecte.emc.arithmetics;

import moze_intel.projecte.utils.Constants;
import org.apache.commons.math3.fraction.BigFraction;

public class FullBigFracArithmetic implements IValueArithmetic<BigFraction> {

    @Override
    public boolean isZero(BigFraction value) {
        return BigFraction.ZERO.equals(value);
    }

    @Override
    public BigFraction getZero() {
        return BigFraction.ZERO;
    }

    @Override
    public BigFraction add(BigFraction a, BigFraction b) {
        if (isFree(a))
            return b;
        if (isFree(b))
            return a;
        return a.add(b);
    }

    @Override
    public BigFraction mul(long a, BigFraction b) {
        if (this.isFree(b))
            return getFree();
        return b.multiply(a);
    }

    @Override
    public BigFraction div(BigFraction a, long b) {
        if (this.isFree(a))
            return getFree();
        if (b == 0)
            return BigFraction.ZERO;
        return a.divide(b);
    }

    @Override
    public BigFraction getFree() {
        return new BigFraction(Long.MIN_VALUE);
    }

    @Override
    public boolean isFree(BigFraction value) {
        return value.getNumerator().equals(Constants.FREE);
    }
}