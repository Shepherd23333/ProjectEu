package moze_intel.projecte.emc.collector;

import com.google.common.collect.Maps;
import moze_intel.projecte.PECore;
import moze_intel.projecte.emc.arithmetics.IValueArithmetic;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MapCollector<T, V extends Comparable<V>, A extends IValueArithmetic<V>> extends AbstractMappingCollector<T, V, A> {
    private static final boolean DEBUG_GRAPHMAPPER = true;
    protected final A arithmetic;
    protected final Map<T, Conversion> overwriteConversion = new HashMap<>();
    protected final Map<T, V> fixValueBeforeInherit = new HashMap<>();
    protected final Map<T, V> fixValueAfterInherit = new HashMap<>();
    private final Map<T, Set<Conversion>> usedIn = new HashMap<>();

    protected MapCollector(A arithmetic) {
        super(arithmetic);
        this.arithmetic = arithmetic;
    }

    protected static void debugFormat(String format, Object... args) {
        if (DEBUG_GRAPHMAPPER)
            PECore.debugLog(format, args);
    }

    protected static void debugPrintln(String s) {
        debugFormat(s);
    }

    protected Set<Conversion> getUsesFor(T something) {
        return usedIn.computeIfAbsent(something, t -> new LinkedHashSet<>());
    }

    private void addConversionToIngredientUsages(Conversion conversion) {
        for (Map.Entry<T, Integer> ingredient : conversion.ingredientsWithAmount.entrySet()) {
            if (ingredient.getValue() == null)
                throw new IllegalArgumentException("ingredient amount value has to be not null");
            getUsesFor(ingredient.getKey()).add(conversion);
        }
    }

    public void addConversion(int outnumber, T output, Map<T, Integer> ingredientsWithAmount, A arithmeticForConversion) {
        Map<T, Integer> outs = new HashMap<>();
        outs.put(output, outnumber);
        addConversion(outs, ingredientsWithAmount, arithmeticForConversion);
    }

    public void addConversion(Map<T, Integer> outputsWithAmount, Map<T, Integer> ingredientsWithAmount, A arithmeticForConversion) {
        ingredientsWithAmount = Maps.newHashMap(ingredientsWithAmount);
        if (outputsWithAmount.containsKey(null) || ingredientsWithAmount.containsKey(null)
                || outputsWithAmount.values().stream().anyMatch(value -> value <= 0)) {
            PECore.debugLog("Ignoring Recipe because of invalid ingredient or output: {} -> {}", ingredientsWithAmount, outputsWithAmount);
            return;
        }

        //Add the Conversions to the conversionsFor and usedIn Maps:
        Conversion conversion = new Conversion(outputsWithAmount, ingredientsWithAmount, arithmeticForConversion, arithmetic.getZero());

    }

    @Override
    public void setValueBefore(T something, V value) {
        if (something == null)
            return;
        if (fixValueBeforeInherit.containsKey(something))
            PECore.debugLog("Overwriting fixValueBeforeInherit for {}:{} to {}", something, fixValueBeforeInherit.get(something), value);
        fixValueBeforeInherit.put(something, value);
        fixValueAfterInherit.remove(something);
    }

    @Override
    public void setValueAfter(T something, V value) {
        if (something == null)
            return;
        if (fixValueAfterInherit.containsKey(something))
            PECore.debugLog("Overwriting fixValueAfterInherit for {}:{} to {}", something, fixValueAfterInherit.get(something), value);
        fixValueAfterInherit.put(something, value);
    }

    @Override
    public void setValueFromConversion(int outnumber, T something, Map<T, Integer> ingredientsWithAmount) {
        if (something == null || ingredientsWithAmount.containsKey(null)) {
            PECore.debugLog("Ignoring setValueFromConversion because of invalid ingredient or output: {} -> {}x{}", ingredientsWithAmount, outnumber, something);
            return;
        }
        if (outnumber <= 0)
            throw new IllegalArgumentException("outnumber has to be positive!");
        Conversion conversion = new Conversion(something, outnumber, ingredientsWithAmount, this.arithmetic);
        if (overwriteConversion.containsKey(something)) {
            Conversion oldConversion = overwriteConversion.get(something);
            PECore.debugLog("Overwriting setValueFromConversion {} with {}", overwriteConversion.get(something), conversion);
            for (T ingredient : ingredientsWithAmount.keySet())
                getUsesFor(ingredient).remove(oldConversion);
        }
        addConversionToIngredientUsages(conversion);
        overwriteConversion.put(something, conversion);
    }

    protected class Conversion {
        public final Map<T, Integer> outputsWithAmount;
        public final V value;
        public final Map<T, Integer> ingredientsWithAmount;
        public final A arithmeticForConversion;

        Conversion(T output, int outnumber, Map<T, Integer> ingredientsWithAmount, A arithmeticForConversion) {
            this(output, outnumber, ingredientsWithAmount, arithmeticForConversion, arithmetic.getZero());
        }

        Conversion(T output, int outnumber, Map<T, Integer> ingredientsWithAmount, A arithmeticForConversion, V value) {
            this.outputsWithAmount = new HashMap<>();
            this.outputsWithAmount.put(output, outnumber);
            this.ingredientsWithAmount = ingredientsWithAmount == null ? Collections.emptyMap() : ingredientsWithAmount;
            this.arithmeticForConversion = arithmeticForConversion;
            this.value = value;
        }

        Conversion(Map<T, Integer> outputsWithAmount, Map<T, Integer> ingredientsWithAmount, A arithmeticForConversion, V value) {
            this.outputsWithAmount = outputsWithAmount;
            this.ingredientsWithAmount = ingredientsWithAmount == null ? Collections.emptyMap() : ingredientsWithAmount;
            this.arithmeticForConversion = arithmeticForConversion;
            this.value = value;
        }

        @Override
        public String toString() {
            return value + " + " + ingredientsToString() + " => " + outputsToString();
        }

        private String outputsToString() {
            if (outputsWithAmount == null || outputsWithAmount.isEmpty())
                return "nothing";
            return outputsWithAmount.entrySet().stream()
                    .map(e -> e.getValue() + "*" + e.getKey())
                    .collect(Collectors.joining(" + "));
        }

        private String ingredientsToString() {
            if (ingredientsWithAmount == null || ingredientsWithAmount.isEmpty())
                return "nothing";
            return ingredientsWithAmount.entrySet().stream()
                    .map(e -> e.getValue() + "*" + e.getKey())
                    .collect(Collectors.joining(" + "));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MapCollector.Conversion))
                return false;
            Conversion other = (Conversion) o;

            return outputsWithAmount.equals(other.outputsWithAmount) && value.equals(other.value)
                    && ingredientsWithAmount.equals(other.ingredientsWithAmount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(outputsWithAmount, value, ingredientsWithAmount);
        }
    }

}
