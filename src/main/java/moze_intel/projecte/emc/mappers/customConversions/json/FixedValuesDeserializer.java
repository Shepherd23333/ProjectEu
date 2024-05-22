package moze_intel.projecte.emc.mappers.customConversions.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import moze_intel.projecte.emc.json.NormalizedSimpleStack;
import moze_intel.projecte.utils.Constants;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedValuesDeserializer implements JsonDeserializer<FixedValues> {
    @Override
    public FixedValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        FixedValues fixed = new FixedValues();
        JsonObject o = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            switch (entry.getKey()) {
                case "before":
                    fixed.setValueBefore = parseSetValueMap(entry.getValue().getAsJsonObject(), context);
                    break;
                case "after":
                    fixed.setValueAfter = parseSetValueMap(entry.getValue().getAsJsonObject(), context);
                    break;
                case "conversion":
                    fixed.conversion = context.deserialize(entry.getValue().getAsJsonArray(), new TypeToken<List<CustomConversion>>() {
                    }.getType());
                    break;
                default:
                    throw new JsonParseException(String.format("Can not parse \"%s\":%s in fixedValues", entry.getKey(), entry.getValue()));
            }
        }
        return fixed;
    }

    private Map<NormalizedSimpleStack, BigInteger> parseSetValueMap(JsonObject o, JsonDeserializationContext context) {
        Map<NormalizedSimpleStack, BigInteger> out = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
            if (primitive.isNumber()) {
                out.put(context.deserialize(new JsonPrimitive(entry.getKey()), NormalizedSimpleStack.class), primitive.getAsBigInteger());
                continue;
            } else if (primitive.isString()) {
                if (primitive.getAsString().equalsIgnoreCase("free")) {
                    out.put(context.deserialize(new JsonPrimitive(entry.getKey()), NormalizedSimpleStack.class), Constants.FREE); //TODO Get Value for 'free' from arithmetic?
                    continue;
                }
            }
            throw new JsonParseException("Could not parse " + o + " into 'free' or integer.");
        }
        return out;
    }
}
