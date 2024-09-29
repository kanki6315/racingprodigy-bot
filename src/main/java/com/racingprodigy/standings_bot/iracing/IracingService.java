package com.racingprodigy.standings_bot.iracing;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.racingprodigy.standings_bot.iracing.model.*;
import com.racingprodigy.standings_bot.util.LocalDateAdapter;
import com.racingprodigy.standings_bot.util.OffsetDateTimeAdapter;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import okhttp3.java.net.cookiejar.JavaNetCookieJar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.net.CookieManager;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class IracingService {

    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json");
    private Gson gson;

    @Value("${iracing.email}")
    private String iracingEmail;
    @Value("${iracing.password}")
    private String iracingPassword;

    @PostConstruct
    public void init() {
        this.client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .build();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        try {
            var string = login();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IracingSeasonDetailsResponse getiRacingSeries(String seasonId) throws Exception {
        List<IracingSeasonDetailsResponse> responses = getiRacingResponseFromPathAndGetLinkResponse(
                "series/seasons",
                new TypeToken<List<IracingSeasonDetailsResponse>>() {
                }.getType());
        return responses.stream().filter((s) -> s.seasonId() == Integer.parseInt(seasonId)).findFirst().get();

    }

    public IracingDriverResponse getiRacingDriver(String iRacingID) throws Exception {
        List<IracingDriverResponse> responses = getiRacingResponseFromPathAndGetLinkResponse(
                String.format("lookup/drivers?search_term=%s", iRacingID),
                new TypeToken<List<IracingDriverResponse>>() {
                }.getType());
        return responses.get(0);
    }

    public IracingSeasonStandingsResponse getSeriesResults(String seasonId, String carClassId) throws Exception {
        return getiRacingResponseFromPathAndGetLinkResponse(
                String.format("stats/season_driver_standings?season_id=%s&car_class_id=%s", seasonId, carClassId),
                new TypeToken<IracingSeasonStandingsResponse>() {
                }.getType());
    }

    private <T> T getiRacingResponseFromPathAndGetLinkResponse(String path, Type type) throws Exception {
        var request = new Request.Builder()
                .url(String.format("https://members-ng.iracing.com/data/%s", path))
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            var responseString = response.body().string();
            var linkModel = gson.fromJson(responseString, IracingLinkResponse.class);
            return getModelFromIracingLink(linkModel.link(), type);
        }
    }

    private <T> T getModelFromIracingLink(String link, Type type) throws Exception {
        var request = new Request.Builder()
                .url(link)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            var responseString = response.body().string();
            return gson.fromJson(responseString, type);
        }
    }

    public List<IracingSeasonStandingChunkResponse> getChunkSeriesResult(String url) throws Exception {
        return getModelFromIracingLink(url, new TypeToken<List<IracingSeasonStandingChunkResponse>>() {
        }.getType());
    }

    private String login() throws Exception {
        var hashedPassword = getSHA256AndBase64(iracingPassword + iracingEmail.toLowerCase());

        var body = Map.of("email", iracingEmail, "password", hashedPassword);
        var requestBody = RequestBody.create(gson.toJson(body), JSON);
        var request = new Request.Builder()
                .url("https://members-ng.iracing.com/auth")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String getSHA256AndBase64(String input) {
        try {
            // Get the SHA-256 MessageDigest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Hash the input
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Encode the hash in Base64
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing SHA-256 hash", e);
        }
    }

}
