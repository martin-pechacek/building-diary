package cz.mp.notification_service.service.impl;

import cz.mp.notification_service.dto.EmailVerificationEvent;
import cz.mp.notification_service.dto.EmailVerifiedEvent;
import cz.mp.notification_service.dto.ProjectStatusChangedEvent;
import cz.mp.notification_service.entity.NotificationLog;
import cz.mp.notification_service.properties.MailProperties;
import cz.mp.notification_service.repository.NotificationLogRepository;
import cz.mp.notification_service.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationLogRepository notificationLogRepository;
    private final MailProperties mailProperties;

    @Override
    public void sendEmailVerification(EmailVerificationEvent event) {
        Context ctx = new Context();
        ctx.setVariable("verificationUrl", event.verificationUrl());

        sendHtmlEmail(event.email(), "Verify your Construction Site Diary account", "email-verification", ctx);
        logNotification("EMAIL_VERIFICATION", event.email());
    }

    @Override
    public void sendEmailVerified(EmailVerifiedEvent event) {
        Context ctx = new Context();
        sendHtmlEmail(event.email(), "Your Construction Site Diary email has been verified", "email-verified", ctx);
        logNotification("EMAIL_VERIFIED", event.email());
    }

    @Override
    public void sendProjectStatusChanged(ProjectStatusChangedEvent event) {
        Context ctx = new Context();
        ctx.setVariable("projectName", event.projectName());
        ctx.setVariable("status", event.status());
        ctx.setVariable("date", event.date());

        sendHtmlEmail(event.ownerEmail(), "Project status update: " + event.projectName(), "project-status-changed", ctx);
        logNotification("PROJECT_STATUS_CHANGED", event.ownerEmail());

        if (StringUtils.isNotBlank(event.managerEmail()) && !event.managerEmail().equals(event.ownerEmail())) {
            sendHtmlEmail(event.managerEmail(), "Project status update: " + event.projectName(), "project-status-changed", ctx);
            logNotification("PROJECT_STATUS_CHANGED", event.managerEmail());
        }
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Context ctx) {
        try {
            String html = templateEngine.process(templateName, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            LOG.info("Email sent to: {} subject: {}", to, subject);
        } catch (Exception e) {
            LOG.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    private void logNotification(String eventType, String recipientEmail) {
        notificationLogRepository.save(new NotificationLog(eventType, recipientEmail, Instant.now(), "SENT"));
    }
}