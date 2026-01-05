CREATE TABLE diary_entries (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    date DATE NOT NULL,
    summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, date)
);

CREATE TABLE workforce_entries (
    id UUID PRIMARY KEY,
    diary_entry_id UUID NOT NULL REFERENCES diary_entries(id) ON DELETE CASCADE,
    role VARCHAR(255) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    working_hours DECIMAL(5, 2)
);

CREATE TABLE material_usages (
    id UUID PRIMARY KEY,
    diary_entry_id UUID NOT NULL REFERENCES diary_entries(id) ON DELETE CASCADE,
    material_name VARCHAR(255) NOT NULL,
    quantity DECIMAL(12, 3) NOT NULL,
    unit VARCHAR(50) NOT NULL
);

CREATE INDEX idx_diary_entries_project_id ON diary_entries(project_id);
CREATE INDEX idx_diary_entries_date ON diary_entries(date);
CREATE INDEX idx_workforce_entries_diary_entry_id ON workforce_entries(diary_entry_id);
CREATE INDEX idx_material_usages_diary_entry_id ON material_usages(diary_entry_id);