package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.entity.VerificationToken;
import cz.mp.construction_site_diary.enums.TokenType;
import cz.mp.construction_site_diary.exception.VerificationTokenException;
import cz.mp.construction_site_diary.repository.VerificationTokenRepository;
import cz.mp.construction_site_diary.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);
    private static final long TOKEN_EXPIRY_HOURS = 24;

    protected final VerificationTokenRepository verificationTokenRepository;

    @Override
    @Transactional
    public String createToken(User user, TokenType tokenType) {
        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(new VerificationToken(
                user,
                token,
                tokenType,
                Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS)
        ));
        LOG.info("Created {} token for user: {}", tokenType, user.getEmail());
        return token;
    }

    @Override
    @Transactional
    public VerificationToken validateAndUseToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new VerificationTokenException("Verification token already used");
        }

        if (verificationToken.isExpired()) {
            throw new VerificationTokenException("Verification token has expired");
        }

        verificationToken.setUsedAt(Instant.now());
        verificationTokenRepository.save(verificationToken);

        LOG.info("Token validated and used for user: {}", verificationToken.getUser().getEmail());
        return verificationToken;
    }
}