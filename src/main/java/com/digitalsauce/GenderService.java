package com.digitalsauce;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GenderService {
    private static final String OSRS_WIKI_API_URL =
            "https://oldschool.runescape.wiki/api.php?action=query&format=json&prop=revisions&rvprop=content&titles=";
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final GenderCache genderCache;

    public GenderService() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
        this.genderCache = new GenderCache();
    }

    /**
     * Determines the gender of an NPC.
     * Primary key: npcName.
     * If a unique NPC ID is available (npcId != -1), then we use that as additional context.
     * The logic is as follows:
     * - Look up the cache entry for the NPC name.
     * - If none exists, compute the gender from the wiki and store it as the default.
     * - If an entry exists:
     *    - If npcId is not available, return the default.
     *    - If npcId is available and a mapping exists, return that.
     *    - Otherwise, compute the gender from the wiki.
     *      In either case, store the computed value as the default (if not already) and,
     *      if npcId is available, also store it in the secondary mapping.
     *
     * Variant extraction is attempted first (checking for explicit "Gender" fields).
     * If that fails (returns "unknown"), pronoun-based detection is used.
     *
     * @param npcId   The unique identifier of the NPC, or -1 if not available.
     * @param npcName The name of the NPC.
     * @return "male", "female", or "unknown".
     */
    public String determineGender(int npcId, String npcName) {
        // Use npcName as the primary key.
        GenderCache.GenderCacheEntry entry = genderCache.getEntry(npcName);
        if (entry == null) {
            // No cache entry exists for this name—compute and store it as the default.
            String computed = determineGenderFromWiki(npcName);
            genderCache.setDefaultGender(npcName, computed);
            if (npcId != -1) {
                genderCache.setGenderForId(npcName, npcId, computed);
            }
            return computed;
        } else {
            // Entry exists.
            if (npcId != -1) {
                // If we have a unique ID and a value is cached for that, use it.
                String idGender = genderCache.getGenderForId(npcName, npcId);
                if (idGender != null) {
                    return idGender;
                }
            }
            // Otherwise, use the default value.
            String defaultGender = entry.getDefaultGender();
            // Now, compute the gender from the wiki.
            String computed = determineGenderFromWiki(npcName);
            // Always update the default.
            if (!defaultGender.equals(computed)) {
                genderCache.setDefaultGender(npcName, computed);
            }
            // If npcId is available, store/update the secondary mapping.
            if (npcId != -1) {
                genderCache.setGenderForId(npcName, npcId, computed);
            }
            return computed;
        }
    }

    private String determineGenderFromWiki(String npcName) {
        try {
            String encodedName = URLEncoder.encode(npcName, StandardCharsets.UTF_8.toString());
            String url = OSRS_WIKI_API_URL + encodedName;
            Request request = new Request.Builder().url(url).build();

            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Unexpected response code: " + response.code());
                return "unknown";
            }
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonObject query = json.getAsJsonObject("query");
            if (query == null) {
                return "unknown";
            }
            JsonObject pages = query.getAsJsonObject("pages");
            if (pages == null || pages.entrySet().isEmpty()) {
                return "unknown";
            }
            // Retrieve the first page.
            JsonObject page = null;
            for (Map.Entry<String, JsonElement> entry : pages.entrySet()) {
                page = entry.getValue().getAsJsonObject();
                break;
            }
            if (page.has("missing")) {
                log.info("NPC \"" + npcName + "\" not found in OSRS Wiki.");
                return "unknown";
            }
            JsonArray revisions = page.getAsJsonArray("revisions");
            if (revisions == null || revisions.size() == 0) {
                return "unknown";
            }
            JsonObject revision = revisions.get(0).getAsJsonObject();
            String content = revision.has("*") ? revision.get("*").getAsString() : "";
            if (content.isEmpty()) {
                return "unknown";
            }

            // First, attempt variant-based extraction (looking for explicit "Gender" fields).
            String variantResult = determineGenderFromVariants(content);
            if (!"unknown".equals(variantResult)) {
                return variantResult;
            }

            // If no explicit gender was found, fall back to pronoun-based detection.
            // Remove infobox templates to focus on descriptive text.
            String cleanedText = content.replaceAll("\\{\\{.*?\\}\\}", " ");
            String lowerText = cleanedText.toLowerCase();
            int maleScore = countOccurrences(lowerText, "\\bhe\\b")
                    + countOccurrences(lowerText, "\\bhim\\b")
                    + countOccurrences(lowerText, "\\bhis\\b");
            int femaleScore = countOccurrences(lowerText, "\\bshe\\b")
                    + countOccurrences(lowerText, "\\bher\\b")
                    + countOccurrences(lowerText, "\\bhers\\b");
            if (maleScore > 0 && femaleScore == 0) {
                return "male";
            } else if (femaleScore > 0 && maleScore == 0) {
                return "female";
            } else if (maleScore == 0 && femaleScore == 0) {
                return "unknown";
            } else {
                int diff = Math.abs(maleScore - femaleScore);
                double ratio = Math.max(maleScore, femaleScore) / (double) Math.min(maleScore, femaleScore);
                if (diff >= 2 && ratio >= 1.5) {
                    return (maleScore > femaleScore) ? "male" : "female";
                } else {
                    return "unknown";
                }
            }
        } catch (IOException e) {
            log.error("Error querying OSRS Wiki for NPC: " + npcName, e);
        }
        return "unknown";
    }

    private String determineGenderFromVariants(String content) {
        // Split content into sections assuming sections are separated by two or more newlines.
        String[] sections = content.split("\\n\\s*\\n");
        HashSet<String> genders = new HashSet<>();
        Pattern pattern = Pattern.compile("(?i)gender\\s*[:=\\t-]*\\s*(\\w+)");
        for (String section : sections) {
            Matcher matcher = pattern.matcher(section);
            if (matcher.find()) {
                String gender = matcher.group(1).trim().toLowerCase();
                if (gender.equals("male") || gender.equals("female")) {
                    genders.add(gender);
                }
            }
        }
        if (genders.size() == 1) {
            return genders.iterator().next();
        }
        return "unknown";
    }

    private int countOccurrences(String text, String regex) {
        int count = 0;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
