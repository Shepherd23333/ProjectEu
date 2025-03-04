package moze_intel.projecte.integration.mekanism;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import moze_intel.projecte.emc.json.NormalizedSimpleStack;

public class NSSGas implements NormalizedSimpleStack {
    public final String name;

    public NSSGas(Gas gas) {
        name = gas.getName();
    }

    public static NormalizedSimpleStack create(GasStack stack) {
        return create(stack.getGas());
    }

    public static NormalizedSimpleStack create(Gas gas) {
        return new NSSGas(gas);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NSSGas && name.equals(((NSSGas) o).name);
    }

    @Override
    public String json() {
        return "GAS|" + name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return "Gas: " + name;
    }
}
