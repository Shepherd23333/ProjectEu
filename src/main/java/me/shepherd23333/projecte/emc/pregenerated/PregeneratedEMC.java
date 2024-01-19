package me.shepherd23333.projecte.emc.pregenerated;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Map;

public class PregeneratedEMC {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(NormalizedSimpleStack.class, NormalizedSimpleStack.Serializer.INSTANCE)
            .enableComplexMapKeySerialization().setPrettyPrinting().create();

    public static boolean tryRead(File f, Map<NormalizedSimpleStack, BigInteger> map) {
        try {
            Map<NormalizedSimpleStack, BigInteger> m = read(f);
            map.clear();
            map.putAll(m);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<NormalizedSimpleStack, BigInteger> read(File file) throws IOException {
        Type type = new TypeToken<Map<NormalizedSimpleStack, BigInteger>>() {
        }.getType();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Map<NormalizedSimpleStack, BigInteger> map = gson.fromJson(reader, type);
            map.remove(null);
            return map;
        }
    }

    public static void write(File file, Map<NormalizedSimpleStack, BigInteger> map) throws IOException {
        Type type = new TypeToken<Map<NormalizedSimpleStack, Integer>>() {
        }.getType();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(map, type, writer);
        }
    }
}
