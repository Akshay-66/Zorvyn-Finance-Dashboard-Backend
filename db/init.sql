CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('VIEWER', 'ANALYST', 'ADMIN')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    auth_token VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS financial_records (
    id UUID PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category VARCHAR(80) NOT NULL,
    record_date DATE NOT NULL,
    notes VARCHAR(255),
    created_by UUID NOT NULL REFERENCES users(id),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_token ON users(auth_token);
CREATE INDEX IF NOT EXISTS idx_records_date ON financial_records(record_date);
CREATE INDEX IF NOT EXISTS idx_records_type ON financial_records(type);
CREATE INDEX IF NOT EXISTS idx_records_category ON financial_records(category);

INSERT INTO users (id, name, email, role, status, auth_token)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Admin User', 'admin@zorvyn.com', 'ADMIN', 'ACTIVE', 'admin-token'),
    ('00000000-0000-0000-0000-000000000002', 'Analyst User', 'analyst@zorvyn.com', 'ANALYST', 'ACTIVE', 'analyst-token'),
    ('00000000-0000-0000-0000-000000000003', 'Viewer User', 'viewer@zorvyn.com', 'VIEWER', 'ACTIVE', 'viewer-token')
ON CONFLICT (email) DO NOTHING;

INSERT INTO financial_records (id, amount, type, category, record_date, notes, created_by)
VALUES
    ('10000000-0000-0000-0000-000000000001', 120000.00, 'INCOME', 'Salary', '2026-03-01', 'Monthly salary', '00000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000002', 15000.00, 'EXPENSE', 'Rent', '2026-03-03', 'House rent', '00000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000003', 3500.00, 'EXPENSE', 'Groceries', '2026-03-06', 'Weekly groceries', '00000000-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000004', 8000.00, 'INCOME', 'Freelance', '2026-03-15', 'Website project payment', '00000000-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000005', 4200.00, 'EXPENSE', 'Transport', '2026-03-18', 'Travel and fuel', '00000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

