package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.entity.VerificationToken;
import cz.mp.building_diary.exception.VerificationTokenException;
import cz.mp.building_diary.repository.VerificationTokenRepository;
import cz.mp.building_diary.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);
    private static final long TOKEN_EXPIRY_HOURS = 24;

    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    @Transactional
    public String createToken(User user, String tokenType) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                user,
                token,
                tokenType,
                Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS)
        );
        verificationTokenRepository.save(verificationToken);
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