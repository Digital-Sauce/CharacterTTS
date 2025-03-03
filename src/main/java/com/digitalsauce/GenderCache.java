package com.digitalsauce;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GenderCache {
    private static final String CACHE_DIR = System.getProperty("user.home") + File.separator + ".runelite" + File.separator + "charactertts";
    private static final String CACHE_FILE = CACHE_DIR + File.separator + "genderCache.json";

    private final Map<String, GenderCacheEntry> cache;
    private final Gson gson;

    public GenderCache() {
        gson = new Gson();
        cache = new HashMap<>();
        loadCache();
    }

    private void loadCache() {
        File file = new File(CACHE_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type type = new TypeToken<HashMap<String, GenderCacheEntry>>(){}.getType();
                Map<String, GenderCacheEntry> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    cache.putAll(loaded);
                }
                log.info("Loaded gender cache with {} entries", cache.size());
            } catch (IOException | JsonSyntaxException e) {
                log.error("Error loading gender cache, resetting: ", e);
                cache.clear();
                saveCache();
            }
        } else {
            new File(CACHE_DIR).mkdirs();
        }
    }

    public void saveCache() {
        try (Writer writer = new FileWriter(CACHE_FILE)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            log.error("Error saving gender cache: ", e);
        }
    }

    public GenderCacheEntry getEntry(String npcName) {
        return cache.get(npcName);
    }

    public void setDefaultGender(String npcName, String gender) {
        GenderCacheEntry entry = cache.computeIfAbsent(npcName, k -> new GenderCacheEntry());
        entry.setDefaultGender(gender);
        saveCache();
    }

    public void setGenderForId(String npcName, int npcId, String gender) {
        GenderCacheEntry entry = cache.computeIfAbsent(npcName, k -> new GenderCacheEntry());
        entry.getIdGenderMapping().put(npcId, gender);
        saveCache();
    }

    public String getDefaultGender(String npcName) {
        GenderCacheEntry entry = cache.get(npcName);
        return entry != null ? entry.getDefaultGender() : null;
    }

    public String getGenderForId(String npcName, int npcId) {
        GenderCacheEntry entry = cache.get(npcName);
        return entry != null ? entry.getIdGenderMapping().get(npcId) : null;
    }

    public static class GenderCacheEntry {
        private String defaultGender = "unknown";
        private Map<Integer, String> idGenderMapping = new HashMap<>();

        public String getDefaultGender() {
            return defaultGender;
        }

        public void setDefaultGender(String defaultGender) {
            this.defaultGender = defaultGender;
        }

        public Map<Integer, String> getIdGenderMapping() {
            return idGenderMapping;
        }

        public void setIdGenderMapping(Map<Integer, String> idGenderMapping) {
            this.idGenderMapping = idGenderMapping;
        }
    }
}