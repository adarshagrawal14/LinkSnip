package com.linksnip.urlshortener.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Base62EncoderTest {

    @Test
    void encodesZero() {
        assertThat(Base62Encoder.encode(0)).isEqualTo("0");
    }

    @Test
    void roundTripsVariousValues() {
        long[] values = {0, 1, 61, 62, 1000, 123456789L, Long.MAX_VALUE};
        for (long value : values) {
            String encoded = Base62Encoder.encode(value);
            assertThat(Base62Encoder.decode(encoded)).isEqualTo(value);
        }
    }

    @Test
    void producesUrlSafeCharactersOnly() {
        String encoded = Base62Encoder.encode(999999999L);
        assertThat(encoded).matches("[0-9a-zA-Z]+");
    }
}
