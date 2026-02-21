CREATE TABLE photos (
    id UUID PRIMARY KEY,
    diary_entry_id UUID NOT NULL,
    project_id UUID NOT NULL,
    owner_user_id VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_photos_diary_entry_id ON photos(diary_entry_id);
CREATE INDEX idx_photos_project_id ON photos(project_id);
CREATE INDEX idx_photos_owner_user_id ON photos(owner_user_id);
