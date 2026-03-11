CREATE TABLE notification_log (
    id              UUID PRIMARY KEY,
    event_type      VARCHAR(50)  NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    sent_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    status          VARCHAR(20)  NOT NULL
);

CREATE INDEX idx_notification_log_event_type ON notification_log(event_type);
CREATE INDEX idx_notification_log_recipient  ON notification_log(recipient_email);