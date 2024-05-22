package roomescape.controller.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.controller.member.dto.CookieMemberResponse;
import roomescape.controller.member.dto.LoginMember;
import roomescape.domain.Role;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    AuthController authController;

    @Test
    @DisplayName("로그인된 유저의 /check")
    void loginMemberCheck() {
        //given
        final LoginMember loginMember = new LoginMember(1L, "레디", Role.ADMIN);
        final CookieMemberResponse expected = new CookieMemberResponse("레디");

        //when
        final ResponseEntity<CookieMemberResponse> check = authController.check(loginMember);

        //then
        assertThat(check.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(check.getBody()).isEqualTo(expected);
    }

    @Test
    @DisplayName("로그인되지 않은 유저의 /check")
    void unLoginMemberCheck() {
        //when
        final ResponseEntity<CookieMemberResponse> check = authController.check(null);

        //then
        assertThat(check.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}