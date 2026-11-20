package com.wallet.auth.infrastructure.adapters.in.web.dto;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void registrationCreatesAPasswordCustomerAndKeepsIdentification() {
        User user = UserMapper.toDomain(new RegisterUserDto(" ID-9 ", " Jane Doe ", "jane@example.com", "pw-12345"));

        assertThat(user.identification()).isEqualTo("ID-9");
        assertThat(user.name()).isEqualTo("Jane Doe");
        assertThat(user.userType()).isEqualTo(UserType.CUSTOMER);
        assertThat(user.authType()).isEqualTo(UserAuthType.PASSWORD);
        assertThat(user.registeredAt()).isNotNull();
        assertThat(user.userId()).isZero();
    }

    @Test
    void publicViewNeverExposesThePassword() {
        User user = User.builder().userId(3L).identification("ID").name("Jane").email("jane@example.com")
                .password("hash").build();

        assertThat(UserMapper.toDto(user)).isEqualTo(new UserDto(3L, "ID", "Jane", "jane@example.com"));
        assertThat(UserDto.class.getRecordComponents()).extracting("name").doesNotContain("password");
    }
}
