package com.github.regyl.urlshortener.withduplicates.service;

import com.github.regyl.annotation.DefaultUnitTest;
import com.github.regyl.urlshortener.withduplicates.exception.InvalidShortenUrl;
import com.github.regyl.urlshortener.withduplicates.exception.InvalidSourceUrl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

@DefaultUnitTest
class UrlShortenerServiceImplTest {

    private static final String BASE_URL = "https://revoluttiny.com/";

    @InjectMocks
    private UrlShortenerServiceImpl urlShortener;

    @Mock
    private RandomPostfixGeneratorService randomPostfixGeneratorService;

    @Test
    void shorten_should_return_short_url() {
        //given
        String originalUrl = "https://www.google.com";
        when(randomPostfixGeneratorService.generatePostfix()).thenReturn("abc");

        //when
        String shortenUrl = urlShortener.shorten(originalUrl);

        //then
        Assertions.assertThat(shortenUrl).isNotEmpty();
    }

    @Test
    void shorten_should_throw_exception_on_wrong_url() {
        //given
        String originalUrl = "abc";

        Assertions.assertThatThrownBy(() -> urlShortener.shorten(originalUrl))
                .isInstanceOf(InvalidSourceUrl.class)
                .hasMessage("Url abc is not valid!");
    }

    @Test
    void shorten_should_return_url_starting_with_our_domain() {
        //given
        String originalUrl = "https://www.google.com";
        when(randomPostfixGeneratorService.generatePostfix()).thenReturn("abc");

        //when
        String shortenUrl = urlShortener.shorten(originalUrl);

        //then
        Assertions.assertThat(shortenUrl).startsWith(BASE_URL);
    }

    @Test
    void shorten_should_return_mocked_result() {
        //given
        String originalUrl = "https://www.google.com";
        String expectedPostfix = "adsjhkjsalkd98";
        when(randomPostfixGeneratorService.generatePostfix()).thenReturn(expectedPostfix);

        //when
        String shortenUrl = urlShortener.shorten(originalUrl);

        //then
        Assertions.assertThat(shortenUrl).isEqualTo(BASE_URL + expectedPostfix);
    }

    @Test
    void getOriginal_should_throw_invalid_shorten_url_on_broken_argument() {
        //given
        String shortenUrl = "https://revoluttin.com/abcde";

        Assertions.assertThatThrownBy(() -> urlShortener.getOriginal(shortenUrl))
                .isInstanceOf(InvalidShortenUrl.class)
                .hasMessage("Source url should start with https://revoluttiny.com/");
    }

    @Test
    void shorten_will_regenerate_postfix_on_duplicate() {
        //given
        String postfixA = "abc";
        String postfixB = "def";
        when(randomPostfixGeneratorService.generatePostfix()).thenReturn(postfixA, postfixA, postfixB);
        String firstShortenUrl = urlShortener.shorten("https://www.google.com");

        //when
        String secondShortenUrl = urlShortener.shorten("https://www.google.com1");

        //then
        Assertions.assertThat(firstShortenUrl).isNotEqualTo(secondShortenUrl);
    }

}
