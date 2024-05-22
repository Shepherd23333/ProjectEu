package moze_intel.projecte.emc.generators;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Composes another IValueGenerator, and truncates all fractional values towards 0.
 *
 * @param <T> The type we are generating values for
 */
public class BigFracToBigIntGenerator<T> implements IValueGenerator<T, BigInteger> {
    private final IValueGenerator<T, BigFraction> inner;

    public BigFracToBigIntGenerator(IValueGenerator<T, BigFraction> inner) {
        this.inner = inner;
    }

    @Override
    public Map<T, BigInteger> generateValues() {
        Map<T, BigFraction> innerResult = inner.generateValues();
        Map<T, BigInteger> myResult = new HashMap<>();
        for (Map.Entry<T, BigFraction> entry : innerResult.entrySet()) {
            BigFraction value = entry.getValue();
            if (value.compareTo(BigFraction.ZERO) > 0) {
                myResult.put(entry.getKey(), value.bigDecimalValue(6, BigDecimal.ROUND_HALF_DOWN).toBigInteger());
            }
        }
        return myResult;
    }
}