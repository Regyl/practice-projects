package com.github.regyl.urlshortener.withoutduplicates.service;

import com.github.regyl.annotation.DefaultUnitTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

@DefaultUnitTest
class UrlShortenerServiceTest {

    private static final String BASE_URL = "http://tiny.revolut.com/";

    private UrlShortenerService service;
    @Mock
    private PostfixGeneratorService postfixGeneratorService;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerService(postfixGeneratorService, BASE_URL);
    }

    @Test
    void shorten() {
        //given
        String originalUrl = "https://www.google.com";
        String exceptedPostfix = "abc";
        when(postfixGeneratorService.generate()).thenReturn(exceptedPostfix);

        //when
        String shortenUrl = service.shorten(originalUrl);

        //then
        Assertions.assertThat(shortenUrl).isEqualTo(BASE_URL + exceptedPostfix);
    }

    @Test
    void shorten_with_null_should_produce_invalid_argument() {
        Assertions.assertThatThrownBy(() -> service.shorten(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void duplicate_shorten_will_return_same_postfix() {
        //given
        String originalUrl = "https://www.google.com";
        String exceptedPostfix1 = "abc";
        String exceptedPostfix2 = "def";
        when(postfixGeneratorService.generate()).thenReturn(exceptedPostfix1, exceptedPostfix2);
        String shortenUrl = service.shorten(originalUrl);

        //when
        String shortenUrlSecond = service.shorten(originalUrl);

        Assertions.assertThat(shortenUrl).isEqualTo(shortenUrlSecond);
    }

    @Test
    void getOriginal() {
        //given
        String originalUrl = "https://www.google.com";
        when(postfixGeneratorService.generate()).thenReturn("abc");
        String shortenUrl = service.shorten(originalUrl);

        //when
        String actualOriginal = service.getOriginal(shortenUrl);

        //then
        Assertions.assertThat(actualOriginal).isEqualTo(originalUrl);
    }

    @Test
    void getOriginal_with_null_should_produce_invalid_argument() {
        Assertions.assertThatThrownBy(() -> service.getOriginal(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getOriginal_with_broken_shorten_input_produce_invalid_argument() {
        Assertions.assertThatThrownBy(() -> service.getOriginal(BASE_URL)).isInstanceOf(IllegalArgumentException.class);
    }
}
