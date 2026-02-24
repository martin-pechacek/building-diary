package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.entity.VerificationToken;
import cz.mp.construction_site_diary.enums.TokenType;
import cz.mp.construction_site_diary.exception.VerificationTokenException;
import cz.mp.construction_site_diary.repository.VerificationTokenRepository;
import cz.mp.construction_site_diary.service.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private KeycloakService keycloakService;

    private EmailVerificationServiceImpl emailVerificationService;

    private User user;

    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationServiceImpl(verificationTokenRepository, keycloakService);
        user = new User("kc-123", "user@test.com", "Test", "User");
    }

    @Nested
    class CreateToken {

        @Test
        void shouldCreateAndSaveToken() {
            String token = emailVerificationService.createToken(user, TokenType.EMAIL_VERIFICATION);

            assertThat(token).isNotBlank();

            ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenRepository).save(captor.capture());

            VerificationToken saved = captor.getValue();
            assertThat(saved.getUser()).isEqualTo(user);
            assertThat(saved.getToken()).isEqualTo(token);
            assertThat(saved.getTokenType()).isEqualTo(TokenType.EMAIL_VERIFICATION);
            assertThat(saved.isExpired()).isFalse();
        }

        @Test
        void shouldGenerateUniqueTokens() {
            String token1 = emailVerificationService.createToken(user, TokenType.EMAIL_VERIFICATION);
            String token2 = emailVerificationService.createToken(user, TokenType.EMAIL_VERIFICATION);

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    class VerifyEmail {

        @Test
        void shouldMarkTokenAsUsedAndUpdateKeycloak() {
            String tokenValue = "valid-token";
            VerificationToken token = new VerificationToken(
                    user, tokenValue, TokenType.EMAIL_VERIFICATION,
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );

            when(verificationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            emailVerificationService.verifyEmail(tokenValue);

            assertThat(token.isUsed()).isTrue();
            verify(verificationTokenRepository).save(token);
            verify(keycloakService).updateEmailVerified("kc-123", true);
        }

        @Test
        void shouldThrowWhenTokenNotFound() {
            when(verificationTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> emailVerificationService.verifyEmail("nonexistent"))
                    .isInstanceOf(VerificationTokenException.class)
                    .hasMessageContaining("Invalid");
        }

        @Test
        void shouldThrowWhenTokenAlreadyUsed() {
            String tokenValue = "used-token";
            VerificationToken token = new VerificationToken(
                    user, tokenValue, TokenType.EMAIL_VERIFICATION,
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );
            token.setUsedAt(Instant.now().minus(1, ChronoUnit.HOURS));

            when(verificationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> emailVerificationService.verifyEmail(tokenValue))
                    .isInstanceOf(VerificationTokenException.class)
                    .hasMessageContaining("already used");
        }

        @Test
        void shouldThrowWhenTokenExpired() {
            String tokenValue = "expired-token";
            VerificationToken token = new VerificationToken(
                    user, tokenValue, TokenType.EMAIL_VERIFICATION,
                    Instant.now().minus(1, ChronoUnit.HOURS)
            );

            when(verificationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> emailVerificationService.verifyEmail(tokenValue))
                    .isInstanceOf(VerificationTokenException.class)
                    .hasMessageContaining("expired");
        }
    }
}
