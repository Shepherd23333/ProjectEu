package me.shepherd23333.projecte.emc.generators;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Composes another IValueGenerator, and truncates all fractional values towards 0.
 *
 * @param <T> The type we are generating values for
 */
public class BigFractionToBigIntegerGenerator<T> implements IValueGenerator<T, BigInteger> {
    private final IValueGenerator<T, BigFraction> inner;

    public BigFractionToBigIntegerGenerator(IValueGenerator<T, BigFraction> inner) {
        this.inner = inner;
    }

    @Override
    public Map<T, BigInteger> generateValues() {
        Map<T, BigFraction> innerResult = inner.generateValues();
        Map<T, BigInteger> myResult = new HashMap<>();
        for (Map.Entry<T, BigFraction> entry : innerResult.entrySet()) {
            BigFraction value = entry.getValue();
            if (value.compareTo(BigFraction.ZERO) > 0) {
                myResult.put(entry.getKey(), value.bigDecimalValue().toBigInteger());
            }
        }
        return myResult;
    }
}