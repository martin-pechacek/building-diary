ALTER TABLE diary_entries
    ADD COLUMN weather_condition VARCHAR(100) NOT NULL,
    ADD COLUMN temperature DECIMAL(5,2) NOT NULL;