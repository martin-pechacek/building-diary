package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.entity.VerificationToken;
import cz.mp.construction_site_diary.repository.VerificationTokenRepository;
import cz.mp.construction_site_diary.service.EmailVerificationService;
import cz.mp.construction_site_diary.service.KeycloakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationServiceImpl extends VerificationTokenServiceImpl implements EmailVerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final KeycloakService keycloakService;

    public EmailVerificationServiceImpl(VerificationTokenRepository verificationTokenRepository,
                                        KeycloakService keycloakService) {
        super(verificationTokenRepository);
        this.keycloakService = keycloakService;
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = validateAndUseToken(token);
        keycloakService.updateEmailVerified(verificationToken.getUser().getKeycloakId(), true);
        LOG.info("Email verified for user: {}", verificationToken.getUser().getEmail());
    }
}