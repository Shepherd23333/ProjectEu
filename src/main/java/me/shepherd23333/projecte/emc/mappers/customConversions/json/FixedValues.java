package me.shepherd23333.projecte.emc.mappers.customConversions.json;

import com.google.gson.annotations.SerializedName;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedValues {
    @SerializedName("before")
    public Map<NormalizedSimpleStack, BigInteger> setValueBefore = new HashMap<>();
    @SerializedName("after")
    public Map<NormalizedSimpleStack, BigInteger> setValueAfter = new HashMap<>();
    public List<CustomConversion> conversion = new ArrayList<>();
}
