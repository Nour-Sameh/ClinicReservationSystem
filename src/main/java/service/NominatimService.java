package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NominatimService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";

    public static List<String> getAddressSuggestions(String query) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = BASE_URL + "?format=json&limit=5&addressdetails=1&q=" + encodedQuery;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "ClinicSystem/1.0 (Nermen@example.com)")
                .header("Accept", "application/json")
                .header("Accept-Language", "ar")
                .header("Referer", "https://nominatim.openstreetmap.org/")
                .timeout(java.time.Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int code = response.statusCode();
        if (code == 403) {
            throw new RuntimeException("⚠️ Nominatim: ممنوع إرسال طلبات كثيرة. انتظر دقيقة وجرب تاني.");
        }
        if (code == 429) {
            throw new RuntimeException("⚠️ Nominatim: تجاوزت الحد المسموح. خذ استراحة قصيرة ");
        }
        if (code != 200) {
            String preview = response.body().length() > 100 ? response.body().substring(0, 100) + "..." : response.body();
            throw new RuntimeException("Nominatim error " + code + ": " + preview);
        }

        JsonArray results = JsonParser.parseString(response.body()).getAsJsonArray();
        List<String> suggestions = new ArrayList<>();

        for (JsonElement element : results) {
            JsonObject obj = element.getAsJsonObject();
            String displayName = obj.has("display_name") ?
                    obj.get("display_name").getAsString() :
                    obj.get("name").getAsString();

            if (displayName != null && !displayName.trim().isEmpty()) {
                suggestions.add(displayName);
            }
        }

        return suggestions;
    }
}