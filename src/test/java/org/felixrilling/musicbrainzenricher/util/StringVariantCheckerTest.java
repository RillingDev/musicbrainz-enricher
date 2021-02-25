package org.felixrilling.musicbrainzenricher.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StringVariantCheckerTest {

    @Test
    @DisplayName("Detects variants")
    void isVariantDetectsVariants() {
        StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "));

        assertThat(stringVariantChecker.isVariant("hip-hop", "hip hop")).isTrue();
        assertThat(stringVariantChecker.isVariant("hiphop", "hip hop")).isTrue();
        assertThat(stringVariantChecker.isVariant("hip-hop", "hiphop")).isTrue();

        assertThat(stringVariantChecker.isVariant("bip-hop", "hiphop")).isFalse();
    }

    @Test
    @DisplayName("Only uses known delimiters")
    void isVariantUsesKnownDelimiters() {
        StringVariantChecker stringVariantChecker = new StringVariantChecker(Set.of("-", " "));

        assertThat(stringVariantChecker.isVariant("hip-hop", "hip/hop")).isFalse();
        assertThat(stringVariantChecker.isVariant("hiphop", "hip/hop")).isFalse();
        assertThat(stringVariantChecker.isVariant("hip-hop", "hipXhop")).isFalse();
        assertThat(stringVariantChecker.isVariant("hiphop", "hipXhop")).isFalse();
    }
}