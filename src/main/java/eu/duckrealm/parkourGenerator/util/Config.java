package eu.duckrealm.parkourGenerator.util;

import eu.duckrealm.parkourGenerator.ParkourGenerator;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Config {
    YamlConfiguration config;
    File file = new File(ParkourGenerator.getPath().resolve("config.yml").toString());

    public void set(String key, Object value) {
        config.set(key, value);
    }

    public void setDefault(String key, Object value) {
        if (config.get(key) == null) {
            config.set(key, value);
        }
    }

    public HashMap<?, ?> getAsHashmap(String key) {
        MemorySection memorySection = (MemorySection) config.getConfigurationSection(key);
        if (memorySection == null) return null;
        return new HashMap<>(memorySection.getValues(false));
    }

    public Object get(String key) {
        return config.get(key);
    }

    public int getInt(String key) {
        return config.getInt(key);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public double getDouble(String s) {
        return config.getDouble(s);
    }
}
