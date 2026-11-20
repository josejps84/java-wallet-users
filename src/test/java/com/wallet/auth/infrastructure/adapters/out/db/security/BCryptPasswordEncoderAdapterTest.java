package com.wallet.auth.infrastructure.adapters.out.db.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter encoder = new BCryptPasswordEncoderAdapter();

    @Test
    void hashesAreSaltedAndVerifiable() {
        String first = encoder.encode("long-enough-pw");
        String second = encoder.encode("long-enough-pw");

        assertThat(first).isNotEqualTo("long-enough-pw").startsWith("$2").isNotEqualTo(second);
        assertThat(encoder.matches("long-enough-pw", first)).isTrue();
        assertThat(encoder.matches("wrong-password", first)).isFalse();
    }
}
