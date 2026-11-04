package com.github.regyl.urlshortener.withduplicates.integration;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.urlshortener.withduplicates.service.RandomPostfixGeneratorService;
import com.github.regyl.urlshortener.withduplicates.service.UrlShortenerServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

@DefaultUnitTest
class UrlShortenerIntegrationTest {

    @InjectMocks
    private UrlShortenerServiceImpl urlShortener;

    @Spy
    private RandomPostfixGeneratorService randomPostfixGeneratorService = new RandomPostfixGeneratorService();

    @Test
    void shorten_should_return_original() {
        //given
        String originalUrl = "https://www.google.com";
        String shortenUrl = urlShortener.shorten(originalUrl);

        String actualOriginalUrl = urlShortener.getOriginal(shortenUrl);

        //then
        Assertions.assertThat(actualOriginalUrl).isEqualTo(originalUrl);
    }
}
