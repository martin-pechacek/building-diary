CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    parcel_number VARCHAR(100),
    street VARCHAR(255),
    street_number VARCHAR(50),
    city VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    building_permit_number VARCHAR(100),
    construction_site_address_id UUID REFERENCES addresses(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
    created_by_id UUID NOT NULL REFERENCES users(id),
    construction_manager_id UUID REFERENCES users(id),
    start_date DATE,
    end_date DATE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_projects_created_by ON projects(created_by_id);
CREATE INDEX idx_projects_construction_manager ON projects(construction_manager_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_archived ON projects(archived);