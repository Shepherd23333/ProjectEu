package moze_intel.projecte.emc;

import moze_intel.projecte.PECore;
import moze_intel.projecte.emc.arithmetics.IValueArithmetic;
import moze_intel.projecte.emc.collector.MapCollector;
import moze_intel.projecte.emc.generators.IValueGenerator;
import moze_intel.projecte.utils.Constants;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;

public class LinearConstraintMapper<T, V extends Number & Comparable<V>, A extends IValueArithmetic<V>> extends MapCollector<T, V, A> implements IValueGenerator<T, V> {
    private static final boolean OVERWRITE_FIXED_VALUES = false;
    private static boolean logFoundExploits = true;
    private final V ZERO;

    public LinearConstraintMapper(A arithmetic) {
        super(arithmetic);
        ZERO = arithmetic.getZero();
    }

    private static <K, V extends Number & Comparable<V>> boolean hasSmallerOrEqual(Map<K, V> m, K key, V value) {
        if (m.containsKey(key)) {
            BigFraction v = (BigFraction) m.get(key), t = (BigFraction) value;
            return v.subtract(Constants.eps).compareTo(t) <= 0;
        }
        return false;
    }

    private static <K, V extends Number & Comparable<V>> boolean hasNoSmaller(Map<K, V> m, K key, V value) {
        if (m.containsKey(key)) {
            BigFraction v = (BigFraction) m.get(key), t = (BigFraction) value;
            return v.subtract(Constants.eps).compareTo(t) >= 0;
        }
        return true;
    }

    static void setLogFoundExploits(boolean log) {
        logFoundExploits = log;
    }

    private static <K, V extends Number & Comparable<V>> boolean updateMapWithMinimum(Map<K, V> m, K key, V value) {
        if (hasNoSmaller(m, key, value)) {
            //No Value or a value that is smaller than this
            m.put(key, value);
            return true;
        }
        return false;
    }

    private boolean canOverride(T something, V value) {
        if (OVERWRITE_FIXED_VALUES)
            return true;
        if (fixValueBeforeInherit.containsKey(something))
            return fixValueBeforeInherit.get(something).compareTo(value) == 0;
        return true;
    }

    @Override
    public Map<T, V> generateValues() {
        Map<T, V> values = new HashMap<>();


        debugPrintln("");
        values.putAll(fixValueAfterInherit);
        //Remove all 'free' items from the output-values
        values.entrySet().removeIf(something -> arithmetic.isFree(something.getValue()));
        return values;
    }

    /**
     * Calculate the combined Cost for the ingredients in the Conversion.
     *
     * @param values     The values for the ingredients to use in the calculation
     * @param conversion The Conversion for which to calculate the combined ingredient cost.
     * @return The combined ingredient value, ZERO or arithmetic.getFree()
     */
    private V valueForConversion(Map<T, V> values, Conversion conversion) {
        try {
            return valueForConversionUnsafe(values, conversion);
        } catch (Exception e) {
            PECore.LOGGER.warn("Could not calculate value for {}: {}", conversion.toString(), e.toString());
            if (!(e instanceof ArithmeticException))
                e.printStackTrace();
            return ZERO;
        }
    }

    private V valueForConversionUnsafe(Map<T, V> values, Conversion conversion) {
        V value = conversion.value;
        boolean allIngredientsAreFree = true;
        boolean hasPositiveIngredientValues = false;
        for (Map.Entry<T, Integer> entry : conversion.ingredientsWithAmount.entrySet()) {
            if (values.containsKey(entry.getKey())) {
                //The ingredient has a value
                if (entry.getValue() == 0) {
                    //Ingredients with an amount of 'zero' do not need to be handled.
                    continue;
                }
                //value = value + amount * ingredientCost
                V ingredientValue = conversion.arithmeticForConversion.mul(entry.getValue(), values.get(entry.getKey()));
                if (ingredientValue.compareTo(ZERO) != 0) {
                    if (!conversion.arithmeticForConversion.isFree(ingredientValue)) {
                        value = conversion.arithmeticForConversion.add(value, ingredientValue);
                        if (ingredientValue.compareTo(ZERO) > 0 && entry.getValue() > 0)
                            hasPositiveIngredientValues = true;
                        allIngredientsAreFree = false;
                    }
                } else {
                    //There is an ingredient with value = 0 => we cannot calculate the combined ingredient cost.
                    return ZERO;
                }
            } else {
                //There is an ingredient that does not have a value => we cannot calculate the combined ingredient cost.
                return ZERO;
            }
        }
        //When all the ingredients for are 'free' or ingredients with negative amount made the Conversion have a value <= 0 this item should be free
        if (allIngredientsAreFree || (hasPositiveIngredientValues && value.compareTo(ZERO) <= 0))
            return conversion.arithmeticForConversion.getFree();
        return value;
    }
}
