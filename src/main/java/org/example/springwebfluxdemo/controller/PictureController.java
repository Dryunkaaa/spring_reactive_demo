package org.example.springwebfluxdemo.controller;

import org.example.springwebfluxdemo.service.NasaPictureService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class PictureController {

    private final NasaPictureService pictureService;

    public PictureController(NasaPictureService pictureService) {
        this.pictureService = pictureService;
    }

    @GetMapping(value = "/pictures/largest", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getLargestPicture() {
        return pictureService.getLargestPictureUrl()
                .flatMap(url -> WebClient.create(url)
                        .mutate()
                        .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build()
                        .get()
                        .exchangeToMono(resp -> resp.bodyToMono(byte[].class)));
    }
}
