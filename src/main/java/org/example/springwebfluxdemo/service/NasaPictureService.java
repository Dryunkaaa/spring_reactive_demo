package org.example.springwebfluxdemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class NasaPictureService {

    private static final String URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=14&api_key=DEMO_KEY";

    public Mono<String> getLargestPictureUrl() {
        return WebClient.create(URL)
                .get()
                .exchangeToMono(resp -> resp.bodyToMono(JsonNode.class))
                .map(json -> json.get("photos"))
                .flatMapMany(Flux::fromIterable)
                .map(photo -> photo.get("img_src"))
                .map(JsonNode::asText)
                .flatMap(pictureUrl ->
                        WebClient.create(pictureUrl)
                                .head()
                                .exchangeToMono(ClientResponse::toBodilessEntity)
                                .map(HttpEntity::getHeaders)
                                .map(HttpHeaders::getLocation)
                                .map(URI::toString)
                                .flatMap(redirectedUrl -> WebClient.create(redirectedUrl)
                                        .head()
                                        .exchangeToMono(ClientResponse::toBodilessEntity)
                                        .map(HttpEntity::getHeaders)
                                        .map(HttpHeaders::getContentLength).map(length -> new Picture(redirectedUrl, length)))
                )
                .reduce((p1, p2) -> (p1.size > p2.size) ? p1 : p2)
                .map(Picture::url);
    }

    record Picture(String url, Long size) {

    }
}
