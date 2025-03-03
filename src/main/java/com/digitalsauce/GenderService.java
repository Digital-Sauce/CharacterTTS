package com.digitalsauce;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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

    public String determineGender(int npcId, String npcName) {
        GenderCache.GenderCacheEntry entry = genderCache.getEntry(npcName);
        if (entry == null) {
            String computed = determineGenderFromWiki(npcName);
            genderCache.setDefaultGender(npcName, computed);
            if (npcId != -1) {
                genderCache.setGenderForId(npcName, npcId, computed);
            }
            return computed;
        } else {
            if (npcId != -1) {
                String idGender = genderCache.getGenderForId(npcName, npcId);
                if (idGender != null) {
                    return idGender;
                }
            }
            String defaultGender = entry.getDefaultGender();
            String computed = determineGenderFromWiki(npcName);
            if (!defaultGender.equals(computed)) {
                genderCache.setDefaultGender(npcName, computed);
            }
            if (npcId != -1) {
                genderCache.setGenderForId(npcName, npcId, computed);
            }
            return computed;
        }
    }

    public void overrideGender(String npcName, int npcId, String gender) {
        if (npcId != -1) {
            genderCache.setGenderForId(npcName, npcId, gender);
        } else {
            genderCache.setDefaultGender(npcName, gender);
        }
    }

    private String determineGenderFromWiki(String npcName) {
        try {
            String encodedName = URLEncoder.encode(npcName, StandardCharsets.UTF_8.toString());
            String url = OSRS_WIKI_API_URL + encodedName;
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Unexpected response code {} for NPC: {}", response.code(), npcName);
                return "unknown";
            }
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonObject query = json.getAsJsonObject("query");
            if (query == null) return "unknown";
            JsonObject pages = query.getAsJsonObject("pages");
            if (pages == null || pages.entrySet().isEmpty()) return "unknown";
            JsonObject page = pages.entrySet().iterator().next().getValue().getAsJsonObject();
            if (page.has("missing")) {
                return "unknown";
            }
            JsonArray revisions = page.getAsJsonArray("revisions");
            if (revisions == null || revisions.size() == 0) return "unknown";
            JsonObject revision = revisions.get(0).getAsJsonObject();
            String content = revision.has("*") ? revision.get("*").getAsString() : "";
            if (content.isEmpty()) return "unknown";

            String variantResult = determineGenderFromVariants(content);
            if (!"unknown".equals(variantResult)) return variantResult;

            String cleanedText = content.replaceAll("\\{\\{.*?\\}\\}", " ");
            String lowerText = cleanedText.toLowerCase();
            int maleScore = countOccurrences(lowerText, "\\bhe\\b") +
                    countOccurrences(lowerText, "\\bhim\\b") +
                    countOccurrences(lowerText, "\\bhis\\b");
            int femaleScore = countOccurrences(lowerText, "\\bshe\\b") +
                    countOccurrences(lowerText, "\\bher\\b") +
                    countOccurrences(lowerText, "\\bhers\\b");
            if (maleScore > 0 && femaleScore == 0) return "male";
            else if (femaleScore > 0 && maleScore == 0) return "female";
            else if (maleScore == 0 && femaleScore == 0) return "unknown";
            else {
                int diff = Math.abs(maleScore - femaleScore);
                double ratio = Math.max(maleScore, femaleScore) / (double) Math.min(maleScore, femaleScore);
                if (diff >= 2 && ratio >= 1.5) {
                    return maleScore > femaleScore ? "male" : "female";
                } else {
                    return "unknown";
                }
            }
        } catch (IOException e) {
            log.error("Error querying OSRS Wiki for NPC: {}", npcName, e);
            return "unknown";
        }
    }

    private String determineGenderFromVariants(String content) {
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
        return genders.size() == 1 ? genders.iterator().next() : "unknown";
    }

    private int countOccurrences(String text, String regex) {
        int count = 0;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) count++;
        return count;
    }
}