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

    // Cache directory and file (in the standard RuneLite directory)
    private static final String CACHE_DIR = System.getProperty("user.home")
            + File.separator + ".runelite" + File.separator + "charactertts";
    private static final String CACHE_FILE = CACHE_DIR + File.separator + "genderCache.json";

    // The cache is keyed by NPC name. Each entry stores a default gender
    // and a mapping from unique NPC IDs to their gender.
    private Map<String, GenderCacheEntry> cache;
    private Gson gson;

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
                cache = gson.fromJson(reader, type);
                if (cache == null) {
                    cache = new HashMap<>();
                }
                log.info("Loaded gender cache with {} entries", cache.size());
            } catch (IOException | JsonSyntaxException | IllegalStateException e) {
                log.error("Error loading gender cache, resetting cache. Error: ", e);
                cache = new HashMap<>();
                saveCache();
            }
        } else {
            new File(CACHE_DIR).mkdirs();
            log.info("No gender cache file found; starting with an empty cache.");
        }
    }

    public void saveCache() {
        try (Writer writer = new FileWriter(CACHE_FILE)) {
            gson.toJson(cache, writer);
            log.info("Saved gender cache with {} entries", cache.size());
        } catch (IOException e) {
            log.error("Error saving gender cache", e);
        }
    }

    /**
     * Returns the cache entry for the given NPC name.
     */
    public GenderCacheEntry getEntry(String npcName) {
        return cache.get(npcName);
    }

    /**
     * Creates or updates the default gender for the given NPC name.
     */
    public void setDefaultGender(String npcName, String gender) {
        GenderCacheEntry entry = cache.get(npcName);
        if (entry == null) {
            entry = new GenderCacheEntry();
            cache.put(npcName, entry);
        }
        entry.setDefaultGender(gender);
        saveCache();
    }

    /**
     * Sets the gender for a specific NPC (by unique ID) under the given NPC name.
     */
    public void setGenderForId(String npcName, int npcId, String gender) {
        GenderCacheEntry entry = cache.get(npcName);
        if (entry == null) {
            entry = new GenderCacheEntry();
            cache.put(npcName, entry);
        }
        entry.getIdGenderMapping().put(npcId, gender);
        saveCache();
    }

    /**
     * Returns the default gender stored for the given NPC name.
     */
    public String getDefaultGender(String npcName) {
        GenderCacheEntry entry = cache.get(npcName);
        return entry != null ? entry.getDefaultGender() : null;
    }

    /**
     * Returns the gender stored for the given NPC name and unique ID.
     */
    public String getGenderForId(String npcName, int npcId) {
        GenderCacheEntry entry = cache.get(npcName);
        if (entry != null && entry.getIdGenderMapping() != null) {
            return entry.getIdGenderMapping().get(npcId);
        }
        return null;
    }

    /**
     * Inner class representing a cache entry for an NPC name.
     */
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
