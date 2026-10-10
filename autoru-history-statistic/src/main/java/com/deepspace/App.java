package com.deepspace;

import com.deepspace.dto.request.RootRequest;
import com.deepspace.dto.response.RootResponse;
import com.deepspace.dto.response.offer.Offer;
import com.deepspace.enums.RootRequestEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App {
    private static final String url = "https://auto.ru/-/ajax/desktop-search/listing/";
    public static void main( String[] args ) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        
        RootRequest rootRequest = RootRequestEnum.WITH_CREDIT.getRequest();
        String requestStr = mapper.writeValueAsString(rootRequest);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("cookie", cookie)
                .setHeader("user-agent", userAgent)
                .setHeader("x-csrf-token", csrfToken)
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestStr))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseStr = response.body();
        
        RootResponse root = mapper.readValue(responseStr, RootResponse.class);
        if (!(root.getStatus().equals("SUCCESS"))) {
            System.out.println(responseStr);
            throw new IllegalArgumentException();
        }
        
        List<String> models = root.getOffers().stream().map(Offer::getVehicleInfo).map(item -> item.getMarkInfo().getName() + " " + item.getModelInfo().getName()).toList();
        Set<String> uniqueModels = new HashSet<>(models);
        
        List<Pair<String, Integer>> pairs = new ArrayList<>();
        for (String model : uniqueModels) {
            pairs.add(Pair.of(model, Collections.frequency(models, model)));
        }
        
        pairs.stream()
                .sorted(Comparator.comparingInt(Pair::getValue))
                .forEachOrdered(item ->
                        System.out.printf("Model %s quantity %s%n", item.getKey(), item.getValue())
                );
    }
    
    private static final String cookie = "_csrf_token=5dbee01e4b89c03578ded2c8a69172c1385371e1be7bd9c7; yandex_login=corgidile01; yandexuid=6554140321687880871; my=YwA=; gdpr=0; _ym_uid=1695305909898447097; panorama_press_and_spin_closed=true; gids=; gradius=200; index-selector-tab=wizard; vertical_filters_onboarding_shown=true; modal_pdd_exam_visited=true; mindboxDeviceUUID=1a660955-1ff5-42b9-8ade-da5cb754f26d; directCrm-session=%7B%22deviceGuid%22%3A%221a660955-1ff5-42b9-8ade-da5cb754f26d%22%7D; count_show_onboarding_offer_in_mag_article=1; autoru_gdpr=1; suid=36d7b9974c4ba4397de0b8aa0940ae14.ecc1c6ec8c684bb5352bf1dbfa5f027c; autoruuid=g650c50b02ks9dqg6i0vvbskgc4m0lbl.853d40afae6db6d12776006afea4f51a; twins_popup=1; i=PUhv0YzWi33XE4PlTdBjEXWNmOCi1c2tMuA3iaQ0MHy6Zr5hj9y84lkaZOLvw4uk/wYTHusWivlSkI+tQDC6ndXXgrg=; L=dVZzSWVdV3NaTFJ3a3tND3leXWl7AkhyJS0/DhtWWVQSeX4=.1723211905.15838.356052.d13910c82301c473f1856096a3bf84fb; autoru_sid=72816725%7C1723191496514.7776000.3uuU4GfUxNLPCzJrmkEz3Q.73rJmPIafyoWVXEKvwf4i1tpXCJdlaTXdLMMlcM0xBM; los=1; coockoos=1; from=direct; crookie=2GhEEI0aNBrKS4LhFRnLxHrpCuSzgB7pnlU7GmFa1iX/pkjtCoYKfMVH+XZQj5uEM90ipaqmSQF7ioXO6s6bZdVCRwY=; cmtchd=MTcyNDkzMDE4MzQyOA==; autoru-visits-count=3; Session_id=3:1725027597.5.0.1688197317279:Sq4YilSHq_SsTHREoB8AKg:cb.1.2:1|1430257434.0.2.3:1688197317|125802120.34926491.2.2:34926491.3:1723123808|61:10025544.719660.tkIIAMtKmSi8kte8nQKmunYsW_M; sessar=1.1193.CiDVH25sabCrtGtKg4NnfpbhbUpU2tOxeppP6nxBPWS_iA.gMCShVtIsljkSaMF8BnzdS5cn7rlm4o8AQvkQHFvd2A; ys=udn.cDpjb3JnaWRpbGUwMQ%3D%3D#c_chck.2915334413; mda2_beacon=1725027597308; _ym_isad=1; yaPassportTryAutologin=1; fp=fbb1d8ed1bb74ffcc85e39202b2f0b04%7C1725027583857; spravka=dD0xNzI1MDI5NjQxO2k9NjIuMjE3LjE4NS4xNzA7RD1GM0I2QTI5NEI0RDlGNzgwQTQ3NTNFMTQ0MUY1QjJCMTQ4NkFFQTk0NDZFNjJERDQ2MDZEN0I0MkE1NkY1NjMwOTVFREQ4QUZFQkQ3OTYxNUU1NTNDNEM0ODVGNDJGNkNEQzc4RDI4QzJFRUI4MDc1OTM5Q0RBNERCMzU1QTgyMDE0QTQ3QzdCMEYyODMwRjAwNDUyNjA7dT0xNzI1MDI5NjQxMzQzNzM0Njg5O2g9ZjhhODY0MzkxNTA4NWE1YzgwZmEzZDlkMmNhYjgwNmM=; _ym_d=1725029641; count-visits=3; _yasc=Wz08RGFJcr+wdN6Z3Z2WtCUXjw/vDIvWlT+z0E06rSN2P7oa5mwfjcH9Bx+0ovVxHaYEl+c0; layout-config={\"screen_height\":800,\"screen_width\":1280,\"win_width\":859.3333129882812,\"win_height\":614.6666870117188}; from_lifetime=1725042220985";
    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
    private static final String csrfToken = "5dbee01e4b89c03578ded2c8a69172c1385371e1be7bd9c7";
}
