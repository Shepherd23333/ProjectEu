package me.shepherd23333.projecte.emc.collector;

import me.shepherd23333.projecte.emc.arithmetics.IValueArithmetic;
import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.Map;

public class BigIntegerToBigFractionCollector<T, A extends IValueArithmetic> extends AbstractMappingCollector<T, BigInteger, A> {
    private final IExtendedMappingCollector<T, BigFraction, A> inner;

    public BigIntegerToBigFractionCollector(IExtendedMappingCollector<T, BigFraction, A> inner) {
        super(inner.getArithmetic());
        this.inner = inner;
    }

    @Override
    public void setValueFromConversion(int outnumber, T something, Map<T, Integer> ingredientsWithAmount) {
        inner.setValueFromConversion(outnumber, something, ingredientsWithAmount);
    }

    @Override
    public void addConversion(int outnumber, T output, Map<T, Integer> ingredientsWithAmount, A arithmeticForConversion) {
        inner.addConversion(outnumber, output, ingredientsWithAmount, arithmeticForConversion);
    }

    @Override
    public void setValueBefore(T something, BigInteger value) {
        inner.setValueBefore(something, new BigFraction(value));
    }

    @Override
    public void setValueAfter(T something, BigInteger value) {
        inner.setValueAfter(something, new BigFraction(value));
    }
}