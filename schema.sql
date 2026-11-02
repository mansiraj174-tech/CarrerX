-- ==============================================================================
-- CarrerX - Production PostgreSQL Database Schema
-- Features:
--   1. Company Intelligence (Profiles, Tech Stacks, Ratings, Funding)
--   2. Intelligent Matching (Candidates, Jobs, Multi-Attribute Match Scores)
--   3. Live Application Tracking (5-Stage Pipeline, Timeline Audit)
-- ==============================================================================

-- Enable UUID extension (built-in for Postgres 13+)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==============================================================================
-- 1. COMPANY INTELLIGENCE
-- ==============================================================================

CREATE TABLE companies (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(150) NOT NULL UNIQUE,
    industry            VARCHAR(100) NOT NULL,
    tech_stack          TEXT[] NOT NULL DEFAULT '{}',
    employee_rating     NUMERIC(2, 1) CHECK (employee_rating >= 1.0 AND employee_rating <= 5.0),
    funding_stage       VARCHAR(50) NOT NULL CHECK (funding_stage IN (
                            'Bootstrapped', 'Seed', 'Series A', 'Series B', 
                            'Series C+', 'Public (IPO)', 'Acquired'
                        )),
    company_size        VARCHAR(50), -- e.g., '50-200 employees', '10,000+'
    headquarters        VARCHAR(150),
    website_url         VARCHAR(255),
    verified            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 2. INTELLIGENT MATCHING (CANDIDATES & JOBS)
-- ==============================================================================

-- Candidates table
CREATE TABLE candidates (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name           VARCHAR(120) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    phone               VARCHAR(30),
    headline            VARCHAR(150), -- e.g., 'Senior Java Full Stack Engineer'
    years_experience    NUMERIC(4, 1) NOT NULL DEFAULT 0.0 CHECK (years_experience >= 0),
    skills              TEXT[] NOT NULL DEFAULT '{}',
    bio                 TEXT,
    expected_salary     NUMERIC(12, 2) CHECK (expected_salary >= 0),
    preferred_work_mode VARCHAR(30) CHECK (preferred_work_mode IN ('Online/Remote', 'Offline/On-site', 'Hybrid')),
    portfolio_url       VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Jobs table
CREATE TABLE jobs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title               VARCHAR(150) NOT NULL,
    department          VARCHAR(100),
    description         TEXT NOT NULL,
    required_skills     TEXT[] NOT NULL DEFAULT '{}',
    bonus_skills        TEXT[] DEFAULT '{}',
    min_years_experience INT NOT NULL DEFAULT 0 CHECK (min_years_experience >= 0),
    min_salary          NUMERIC(12, 2) NOT NULL CHECK (min_salary >= 0),
    max_salary          NUMERIC(12, 2) NOT NULL CHECK (max_salary >= min_salary),
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    work_mode           VARCHAR(30) NOT NULL CHECK (work_mode IN ('Online/Remote', 'Offline/On-site', 'Hybrid')),
    location            VARCHAR(150) NOT NULL,
    urgency             VARCHAR(50) DEFAULT 'Standard', -- e.g., 'Fresher Friendly', 'Immediate Hiring'
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Candidate-Job Match Scores (Precomputed or Cached recommendation scores)
CREATE TABLE candidate_job_matches (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_id        UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    job_id              UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    match_percentage    NUMERIC(5, 2) NOT NULL CHECK (match_percentage >= 0 AND match_percentage <= 100),
    skills_score        NUMERIC(5, 2) CHECK (skills_score >= 0 AND skills_score <= 100),
    experience_score    NUMERIC(5, 2) CHECK (experience_score >= 0 AND experience_score <= 100),
    salary_score        NUMERIC(5, 2) CHECK (salary_score >= 0 AND salary_score <= 100),
    preferences_score   NUMERIC(5, 2) CHECK (preferences_score >= 0 AND preferences_score <= 100),
    matched_skills      TEXT[] DEFAULT '{}',
    missing_skills      TEXT[] DEFAULT '{}',
    calculated_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure 1 score record per candidate-job pair
    CONSTRAINT uq_candidate_job_match UNIQUE (candidate_id, job_id)
);

-- ==============================================================================
-- 3. LIVE APPLICATIONS & LIFECYCLE AUDIT
-- ==============================================================================

-- Applications Table
CREATE TABLE applications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_id        VARCHAR(30) NOT NULL UNIQUE, -- e.g., 'APP-2026-1001'
    candidate_id        UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    job_id              UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    
    -- 5 Defined Live Hiring Stages (Automatically assigned by backend screening)
    status              VARCHAR(30) NOT NULL DEFAULT 'Assessment' CHECK (status IN (
                            'Assessment',      -- 🔵 Technical test / assessment
                            'Interview',       -- 🟣 Shortlisted for technical/behavioral interview
                            'Waiting List',    -- 🟡 Competitive pool hold
                            'Selected',        -- 🟢 Offer extended / completely selected
                            'Rejected'         -- 🔴 Disqualified by experience/skill mismatch
                        )),
    status_note         TEXT, -- Real-time explanation from the backend matching engine
    
    expected_salary     NUMERIC(12, 2) CHECK (expected_salary > 0),
    joining_timeline    VARCHAR(50), -- e.g., 'Immediate (Within 2 weeks)'
    work_authorization  VARCHAR(100),
    cover_letter        TEXT,
    
    applied_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Prevent duplicate submissions for the same role
    CONSTRAINT uq_candidate_job_application UNIQUE (candidate_id, job_id)
);

-- Application Status Audit Timeline (Immutable historical tracking)
CREATE TABLE application_status_history (
    id                  BIGSERIAL PRIMARY KEY,
    application_id      UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    previous_status     VARCHAR(30),
    new_status          VARCHAR(30) NOT NULL,
    note                TEXT,
    changed_by          VARCHAR(100) DEFAULT 'BACKEND_SCREENING_ENGINE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- PERFORMANCE INDEXES
-- ==============================================================================

-- 1. Foreign Key Indexes (Eliminates full table scans on JOINs)
CREATE INDEX idx_jobs_company_id ON jobs(company_id);
CREATE INDEX idx_matches_candidate_id ON candidate_job_matches(candidate_id);
CREATE INDEX idx_matches_job_id ON candidate_job_matches(job_id);
CREATE INDEX idx_applications_candidate_id ON applications(candidate_id);
CREATE INDEX idx_applications_job_id ON applications(job_id);
CREATE INDEX idx_history_application_id ON application_status_history(application_id);

-- 2. Ranked Match Index (For rapid candidate feed & recommendation ranking)
CREATE INDEX idx_matches_ranking ON candidate_job_matches(candidate_id, match_percentage DESC);

-- 3. Live Application Status Filter Index (For fast dashboard queries)
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_candidate_applied ON applications(candidate_id, applied_at DESC);

-- 4. PostgreSQL GIN Indexes for Array Search (Ultra-fast skill overlap checks)
CREATE INDEX idx_candidates_skills ON candidates USING GIN (skills);
CREATE INDEX idx_jobs_required_skills ON jobs USING GIN (required_skills);
CREATE INDEX idx_companies_tech_stack ON companies USING GIN (tech_stack);

-- ==============================================================================
-- SAMPLE SEED DATA
-- ==============================================================================

INSERT INTO companies (name, industry, tech_stack, employee_rating, funding_stage, company_size, headquarters)
VALUES 
('Stripe', 'Fintech', ARRAY['Java', 'Spring Boot', 'Kafka', 'PostgreSQL', 'Docker'], 4.6, 'Public (IPO)', '8,000+', 'San Francisco, CA'),
('Anthropic', 'Artificial Intelligence', ARRAY['Python', 'PyTorch', 'FastAPI', 'Kubernetes', 'AWS'], 4.8, 'Series C+', '500-1000', 'San Francisco, CA');

INSERT INTO candidates (full_name, email, headline, years_experience, skills, expected_salary, preferred_work_mode)
VALUES 
('Rohan Patel', 'rohan.patel@graduate.edu', 'Associate Software Engineer', 1.0, ARRAY['Java', 'SQL', 'JavaScript', 'HTML', 'CSS', 'Git'], 85000.00, 'Online/Remote');

INSERT INTO jobs (company_id, title, department, description, required_skills, min_years_experience, min_salary, max_salary, work_mode, location)
VALUES 
((SELECT id FROM companies WHERE name = 'Stripe'), 'Associate Java Developer', 'Core Engineering', 'Develop and maintain mission-critical microservices.', ARRAY['Java', 'SQL', 'Git'], 0, 80000.00, 110000.00, 'Online/Remote', 'Remote / US');

INSERT INTO applications (reference_id, candidate_id, job_id, status, status_note, expected_salary, joining_timeline, cover_letter)
VALUES 
('APP-2026-1001', 
 (SELECT id FROM candidates WHERE email = 'rohan.patel@graduate.edu'), 
 (SELECT id FROM jobs WHERE title = 'Associate Java Developer'), 
 'Assessment', 
 'Application shortlisted for the technical Test/Assessment stage.', 
 85000.00, 
 'Immediate (Within 2 weeks)', 
 'Dear Stripe Hiring Team, I am enthusiastic to apply for the Associate Java Developer position...');
