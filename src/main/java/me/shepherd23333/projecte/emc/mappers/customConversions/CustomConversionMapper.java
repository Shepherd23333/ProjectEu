package me.shepherd23333.projecte.emc.mappers.customConversions;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shepherd23333.projecte.PECore;
import me.shepherd23333.projecte.emc.collector.IMappingCollector;
import me.shepherd23333.projecte.emc.json.NSSFake;
import me.shepherd23333.projecte.emc.json.NSSItem;
import me.shepherd23333.projecte.emc.json.NSSOreDictionary;
import me.shepherd23333.projecte.emc.json.NormalizedSimpleStack;
import me.shepherd23333.projecte.emc.mappers.IEMCMapper;
import me.shepherd23333.projecte.emc.mappers.customConversions.json.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomConversionMapper implements IEMCMapper<NormalizedSimpleStack, BigInteger> {
    private static final String EXAMPLE_FILENAME = "example";
    private static final ImmutableList<String> defaultFilenames = ImmutableList.of("defaults", "ODdefaults", "metals");
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CustomConversion.class, new CustomConversionDeserializer())
            .registerTypeAdapter(FixedValues.class, new FixedValuesDeserializer())
            .registerTypeAdapter(NormalizedSimpleStack.class, NormalizedSimpleStack.Serializer.INSTANCE)
            .setPrettyPrinting()
            .create();

    @Override
    public String getName() {
        return "CustomConversionMapper";
    }

    @Override
    public String getDescription() {
        return "Uses json files within config/ProjectE/customConversions/ to add values and conversions";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, Configuration config) {
        File customConversionFolder = getCustomConversionFolder();
        if (customConversionFolder.isDirectory() || customConversionFolder.mkdir()) {
            tryToWriteDefaultFiles();

            for (String defaultFile : defaultFilenames) {
                readFile(new File(customConversionFolder, defaultFile + ".json"), config, mapper, true);
            }

            List<File> sortedFiles = Arrays.asList(customConversionFolder.listFiles());
            Collections.sort(sortedFiles);

            for (File f : sortedFiles) {
                readFile(f, config, mapper, false);
            }

            NSSFake.resetNamespace();
        } else {
            PECore.LOGGER.fatal("COULD NOT CREATE customConversions FOLDER IN config/ProjectE");
        }
    }

    private static void readFile(File f, Configuration config, IMappingCollector<NormalizedSimpleStack, BigInteger> mapper, boolean allowDefaults) {
        if (f.isFile() && f.canRead() && f.getName().toLowerCase().endsWith(".json")) {
            String name = f.getName().substring(0, f.getName().length() - ".json".length());

            if (!EXAMPLE_FILENAME.equals(name)
                    && (allowDefaults || !defaultFilenames.contains(name))
                    && config.getBoolean(name, "", true, String.format("Read file: %s?", f.getName()))) {
                try {
                    NSSFake.setCurrentNamespace(name);
                    addMappingsFromFile(new FileReader(f), mapper);
                    PECore.debugLog("Collected Mappings from {}", f.getName());
                } catch (Exception e) {
                    PECore.LOGGER.fatal("Exception when reading file: {}", f);
                    e.printStackTrace();
                }
            }
        }

    }

    private static File getCustomConversionFolder() {
        return new File(PECore.CONFIG_DIR, "customConversions");
    }

    private static void addMappingsFromFile(Reader json, IMappingCollector<NormalizedSimpleStack, BigInteger> mapper) {
        addMappingsFromFile(parseJson(json), mapper);
    }

    private static void addMappingsFromFile(CustomConversionFile file, IMappingCollector<NormalizedSimpleStack, BigInteger> mapper) {
        //TODO implement buffered IMappingCollector to recover from failures
        for (Map.Entry<String, ConversionGroup> entry : file.groups.entrySet()) {
            PECore.debugLog("Adding conversions from group '{}' with comment '{}'", entry.getKey(), entry.getValue().comment);
            try {
                for (CustomConversion conversion : entry.getValue().conversions) {
                    mapper.addConversion(conversion.count, conversion.output, conversion.ingredients);
                }
            } catch (Exception e) {
                PECore.LOGGER.fatal("ERROR reading custom conversion from group {}!", entry.getKey());
                e.printStackTrace();
            }
        }

        try {
            if (file.values.setValueBefore != null) {
                for (Map.Entry<NormalizedSimpleStack, BigInteger> entry : file.values.setValueBefore.entrySet()) {
                    NormalizedSimpleStack something = entry.getKey();
                    mapper.setValueBefore(something, entry.getValue());
                    if (something instanceof NSSOreDictionary) {
                        String odName = ((NSSOreDictionary) something).od;
                        for (ItemStack itemStack : OreDictionary.getOres(odName)) {
                            mapper.setValueBefore(NSSItem.create(itemStack), entry.getValue());
                        }
                    }
                }
            }
            if (file.values.setValueAfter != null) {
                for (Map.Entry<NormalizedSimpleStack, BigInteger> entry : file.values.setValueAfter.entrySet()) {
                    NormalizedSimpleStack something = entry.getKey();
                    mapper.setValueAfter(something, entry.getValue());
                    if (something instanceof NSSOreDictionary) {
                        String odName = ((NSSOreDictionary) something).od;
                        for (ItemStack itemStack : OreDictionary.getOres(odName)) {
                            mapper.setValueAfter(NSSItem.create(itemStack), entry.getValue());
                        }
                    }
                }
            }
            if (file.values.conversion != null) {
                for (CustomConversion conversion : file.values.conversion) {
                    NormalizedSimpleStack out = conversion.output;
                    if (conversion.evalOD && out instanceof NSSOreDictionary) {
                        String odName = ((NSSOreDictionary) out).od;
                        for (ItemStack itemStack : OreDictionary.getOres(odName)) {
                            mapper.setValueFromConversion(conversion.count, NSSItem.create(itemStack), conversion.ingredients);
                        }
                    }
                    mapper.setValueFromConversion(conversion.count, out, conversion.ingredients);
                }
            }
        } catch (Exception e) {
            PECore.LOGGER.fatal("ERROR reading custom conversion values!");
            e.printStackTrace();
        }
    }

    public static CustomConversionFile parseJson(Reader json) {
        return GSON.fromJson(new BufferedReader(json), CustomConversionFile.class);
    }


    private static void tryToWriteDefaultFiles() {
        writeDefaultFile(EXAMPLE_FILENAME);

        for (String filename : defaultFilenames) {
            writeDefaultFile(filename);
        }
    }

    private static void writeDefaultFile(String filename) {
        File f = new File(getCustomConversionFolder(), filename + ".json");

        if (f.exists()) {
            f.delete();
        }

        try {
            if (f.createNewFile() && f.canWrite()) {
                String path = "defaultCustomConversions/" + filename + ".json";
                try (InputStream stream = CustomConversionMapper.class.getClassLoader().getResourceAsStream(path);
                     OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f))) {
                    IOUtils.copy(stream, outputStream);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
