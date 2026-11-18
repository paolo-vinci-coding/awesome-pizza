package com.awesomepizza.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCodeGeneratorTest {

    private final OrderCodeGenerator generator = new OrderCodeGenerator();

    @Test
    void codeHasPrefixAndEightCharsWithoutAmbiguousOnes() {
        assertThat(generator.generate()).matches("AP-[A-HJKMNP-Z2-9]{8}");
    }
}
