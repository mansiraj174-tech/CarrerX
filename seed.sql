-- ==============================================================================
-- JobPlatform Seed Data - Realistic Production Sample Data
-- Includes:
--   1. 5 Companies (Tech Stacks, Employee Ratings, Funding Stages)
--   2. 5 Job Postings (Linked to Companies, Salaries, Requirements)
--   3. 5 Candidates (Skills, Years Experience, Bio, Expected Compensation)
--   4. 5 Applications (Linked Candidates -> Jobs across distinct live status stages)
--   5. 5 Intelligent Match Scores (Calculated percentage and breakdown subscores)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. COMPANIES (5 Real-World Tech Profiles)
-- ------------------------------------------------------------------------------
INSERT INTO companies (
    id, name, industry, tech_stack, employee_rating, funding_stage, company_size, headquarters, website_url
) VALUES 
(
    'c0000000-0000-0000-0000-000000000001',
    'Stripe',
    'Fintech & Payments',
    ARRAY['Java', 'Spring Boot', 'Kafka', 'PostgreSQL', 'Docker', 'Kubernetes'],
    4.6,
    'Public (IPO)',
    '8,000+ employees',
    'South San Francisco, CA',
    'https://stripe.com'
),
(
    'c0000000-0000-0000-0000-000000000002',
    'Datadog',
    'Cloud Observability & Security',
    ARRAY['Java', 'Go', 'React', 'TypeScript', 'Kubernetes', 'Redis', 'AWS'],
    4.5,
    'Public (IPO)',
    '5,000+ employees',
    'New York, NY',
    'https://datadoghq.com'
),
(
    'c0000000-0000-0000-0000-000000000003',
    'Anthropic',
    'Artificial Intelligence & Safety',
    ARRAY['Python', 'PyTorch', 'FastAPI', 'Rust', 'Kubernetes', 'AWS', 'PostgreSQL'],
    4.8,
    'Series C+',
    '500-1000 employees',
    'San Francisco, CA',
    'https://anthropic.com'
),
(
    'c0000000-0000-0000-0000-000000000004',
    'Snowflake',
    'Data Cloud & Analytics',
    ARRAY['Java', 'C++', 'Python', 'Apache Spark', 'SQL', 'AWS', 'Azure'],
    4.4,
    'Public (IPO)',
    '7,000+ employees',
    'Bozeman, MT',
    'https://snowflake.com'
),
(
    'c0000000-0000-0000-0000-000000000005',
    'Figma',
    'Product Design & Collaboration',
    ARRAY['TypeScript', 'React', 'Rust', 'WebAssembly', 'Ruby', 'PostgreSQL'],
    4.7,
    'Series C+',
    '1,500+ employees',
    'San Francisco, CA',
    'https://figma.com'
)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 2. CANDIDATES (5 Varied Experience Profiles)
-- ------------------------------------------------------------------------------
INSERT INTO candidates (
    id, full_name, email, phone, headline, years_experience, skills, bio, expected_salary, preferred_work_mode, portfolio_url
) VALUES
(
    'a0000000-0000-0000-0000-000000000001',
    'Rohan Patel',
    'rohan.patel@graduate.edu',
    '+1 (408) 555-0192',
    'Associate Software Engineer',
    1.0,
    ARRAY['Java', 'SQL', 'JavaScript', 'HTML', 'CSS', 'Git', 'OOP'],
    'Recent Computer Science graduate with strong foundations in Java OOP, RESTful API design, and SQL relational databases.',
    85000.00,
    'Online/Remote',
    'https://github.com/rohan-grad'
),
(
    'a0000000-0000-0000-0000-000000000002',
    'Elena Rostova',
    'elena.rostova@techhub.io',
    '+1 (415) 555-0841',
    'Senior Java Full Stack Engineer',
    6.5,
    ARRAY['Java', 'Spring Boot', 'React', 'TypeScript', 'Microservices', 'Kafka', 'PostgreSQL', 'Docker'],
    'Full-stack engineering leader specializing in high-throughput transactional backends and responsive React/TypeScript frontends.',
    165000.00,
    'Hybrid',
    'https://linkedin.com/in/elena-rostova-dev'
),
(
    'a0000000-0000-0000-0000-000000000003',
    'Marcus Vance',
    'marcus.vance@aimind.org',
    '+1 (206) 555-0477',
    'Staff AI & LLM Systems Engineer',
    8.0,
    ARRAY['Python', 'PyTorch', 'FastAPI', 'Rust', 'Transformers', 'Kubernetes', 'AWS', 'Docker'],
    'AI systems architect focused on large language model fine-tuning, RAG pipelines, and distributed GPU inference clusters.',
    215000.00,
    'Online/Remote',
    'https://github.com/marcusvance-ai'
),
(
    'a0000000-0000-0000-0000-000000000004',
    'Priya Sharma',
    'priya.sharma@datatech.com',
    '+1 (512) 555-0329',
    'Lead Data Platform Engineer',
    7.0,
    ARRAY['Java', 'Python', 'Apache Spark', 'SQL', 'Kafka', 'AWS', 'Snowflake', 'Airflow'],
    'Data infrastructure specialist with extensive experience scaling multi-terabyte data warehouses and real-time streaming architectures.',
    180000.00,
    'Hybrid',
    'https://priyasharma-data.dev'
),
(
    'a0000000-0000-0000-0000-000000000005',
    'Liam O''Connor',
    'liam.oconnor@infraops.net',
    '+1 (617) 555-0963',
    'Senior DevOps & Site Reliability Engineer',
    5.5,
    ARRAY['Kubernetes', 'Docker', 'Terraform', 'AWS', 'CI/CD', 'Go', 'Linux', 'Prometheus'],
    'SRE dedicated to zero-downtime infrastructure automation, GitOps pipelines, and scalable Kubernetes cluster management.',
    155000.00,
    'Online/Remote',
    'https://github.com/liamoconnor-sre'
)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 3. JOB POSTINGS (5 Jobs Linked to Respective Companies)
-- ------------------------------------------------------------------------------
INSERT INTO jobs (
    id, company_id, title, department, description, required_skills, bonus_skills, min_years_experience, min_salary, max_salary, work_mode, location, urgency
) VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'c0000000-0000-0000-0000-000000000001', -- Stripe
    'Associate Java Software Engineer',
    'Merchant Processing',
    'Join our Merchant Payments core group to build resilient payment processing APIs and transaction reconciliation services.',
    ARRAY['Java', 'SQL', 'Git', 'OOP'],
    ARRAY['Spring Boot', 'Docker', 'PostgreSQL'],
    0,
    80000.00,
    115000.00,
    'Online/Remote',
    'Remote / US Tech Hubs',
    'Fresher Friendly'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'c0000000-0000-0000-0000-000000000002', -- Datadog
    'Senior Full Stack Engineer (Java & React)',
    'Platform Metrics',
    'Architect real-time telemetry dashboards and high-throughput ingestion microservices using Java Spring Boot and React/TypeScript.',
    ARRAY['Java', 'Spring Boot', 'React', 'TypeScript', 'PostgreSQL'],
    ARRAY['Kafka', 'Docker', 'Kubernetes'],
    5,
    155000.00,
    185000.00,
    'Hybrid',
    'New York, NY',
    'Immediate Hiring'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'c0000000-0000-0000-0000-000000000003', -- Anthropic
    'Staff AI / LLM Systems Engineer',
    'Alignment Research Infrastructure',
    'Build and maintain highly distributed training clusters, inference optimization services, and evaluation pipelines for next-generation frontier models.',
    ARRAY['Python', 'PyTorch', 'FastAPI', 'Kubernetes', 'AWS'],
    ARRAY['Rust', 'Docker', 'Distributed Systems'],
    6,
    195000.00,
    250000.00,
    'Online/Remote',
    'San Francisco, CA / Remote',
    'High Priority'
),
(
    'b0000000-0000-0000-0000-000000000004',
    'c0000000-0000-0000-0000-000000000004', -- Snowflake
    'Senior Data Platform Engineer',
    'Core Database Infrastructure',
    'Design large-scale distributed query engines, streaming ingestion pipelines, and storage optimization subsystems.',
    ARRAY['Java', 'Python', 'Apache Spark', 'SQL', 'AWS'],
    ARRAY['Kafka', 'Snowflake', 'Distributed Storage'],
    5,
    165000.00,
    205000.00,
    'Hybrid',
    'San Mateo, CA',
    'Standard'
),
(
    'b0000000-0000-0000-0000-000000000005',
    'c0000000-0000-0000-0000-000000000005', -- Figma
    'Senior Site Reliability & Cloud Infrastructure Engineer',
    'Platform Operations',
    'Ensure 99.99% availability for Figma multi-tenant collaborative canvas through Kubernetes orchestration, Terraform automation, and observability.',
    ARRAY['Kubernetes', 'Docker', 'Terraform', 'AWS', 'CI/CD'],
    ARRAY['Go', 'Prometheus', 'Linux'],
    4,
    150000.00,
    190000.00,
    'Online/Remote',
    'San Francisco, CA / Remote',
    'Immediate Hiring'
)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 4. INTELLIGENT MATCH SCORES (5 Calculated Pairings)
-- ------------------------------------------------------------------------------
INSERT INTO candidate_job_matches (
    id, candidate_id, job_id, match_percentage, skills_score, experience_score, salary_score, matched_skills, missing_skills
) VALUES
(
    'd0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001', -- Rohan Patel
    'b0000000-0000-0000-0000-000000000001', -- Stripe Associate Java
    93.50,
    95.00, 92.00, 95.00,
    ARRAY['Java', 'SQL', 'Git', 'OOP'],
    ARRAY[]::TEXT[]
),
(
    'd0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000002', -- Elena Rostova
    'b0000000-0000-0000-0000-000000000002', -- Datadog Senior Full Stack
    88.00,
    92.00, 85.00, 88.00,
    ARRAY['Java', 'Spring Boot', 'React', 'TypeScript', 'PostgreSQL'],
    ARRAY[]::TEXT[]
),
(
    'd0000000-0000-0000-0000-000000000003',
    'a0000000-0000-0000-0000-000000000003', -- Marcus Vance
    'b0000000-0000-0000-0000-000000000003', -- Anthropic Staff AI
    95.50,
    98.00, 94.00, 93.00,
    ARRAY['Python', 'PyTorch', 'FastAPI', 'Kubernetes', 'AWS'],
    ARRAY[]::TEXT[]
),
(
    'd0000000-0000-0000-0000-000000000004',
    'a0000000-0000-0000-0000-000000000004', -- Priya Sharma
    'b0000000-0000-0000-0000-000000000004', -- Snowflake Senior Data
    86.50,
    90.00, 85.00, 84.00,
    ARRAY['Java', 'Python', 'Apache Spark', 'SQL', 'AWS'],
    ARRAY[]::TEXT[]
),
(
    'd0000000-0000-0000-0000-000000000005',
    'a0000000-0000-0000-0000-000000000005', -- Liam O'Connor
    'b0000000-0000-0000-0000-000000000005', -- Figma Senior SRE
    91.00,
    94.00, 88.00, 92.00,
    ARRAY['Kubernetes', 'Docker', 'Terraform', 'AWS', 'CI/CD'],
    ARRAY[]::TEXT[]
)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 5. LIVE APPLICATIONS (5 Linking Candidates -> Jobs with Distinct Stages)
-- ------------------------------------------------------------------------------
INSERT INTO applications (
    id, reference_id, candidate_id, job_id, status, status_note, expected_salary, joining_timeline, cover_letter
) VALUES
(
    'e0000000-0000-0000-0000-000000000001',
    'APP-2026-1001',
    'a0000000-0000-0000-0000-000000000001', -- Rohan Patel
    'b0000000-0000-0000-0000-000000000001', -- Stripe Associate Java
    'Selected',                              -- 🟢 Completely Selected
    'Congratulations! Your 93% technical evaluation exceeded benchmarks. The committee has completely selected you for this role.',
    85000.00,
    'Immediate (Within 2 weeks)',
    'Dear Stripe Hiring Team, I am excited to apply for the Associate Java Engineer position. My foundational Java OOP and relational database skills directly align with your requirements.'
),
(
    'e0000000-0000-0000-0000-000000000002',
    'APP-2026-1002',
    'a0000000-0000-0000-0000-000000000002', -- Elena Rostova
    'b0000000-0000-0000-0000-000000000002', -- Datadog Senior Full Stack
    'Interview',                             -- 🟣 Interview Round
    'Application shortlisted for System Architecture & Live Coding round with the Platform Engineering panel.',
    165000.00,
    '1 Month Notice',
    'To the Datadog Team, With 6.5 years of experience architecting Java Spring Boot microservices and React dashboards, I welcome the opportunity to discuss my qualifications.'
),
(
    'e0000000-0000-0000-0000-000000000003',
    'APP-2026-1003',
    'a0000000-0000-0000-0000-000000000003', -- Marcus Vance
    'b0000000-0000-0000-0000-000000000003', -- Anthropic Staff AI
    'Interview',                             -- 🟣 Interview Round
    'Shortlisted for Technical Deep Dive on Distributed Training Infrastructure with Lead Alignment Engineers.',
    215000.00,
    'Flexible (Within 3 weeks)',
    'Dear Anthropic Research Team, Having spent 8 years scaling distributed GPU training clusters and PyTorch pipelines, I am eager to contribute to your frontier safety missions.'
),
(
    'e0000000-0000-0000-0000-000000000004',
    'APP-2026-1004',
    'a0000000-0000-0000-0000-000000000004', -- Priya Sharma
    'b0000000-0000-0000-0000-000000000004', -- Snowflake Senior Data
    'Assessment',                            -- 🔵 Test/Assessment
    'Automated Screening Assessment dispatched. Please complete the Spark & SQL optimization benchmark challenge.',
    180000.00,
    'Immediate (Within 2 weeks)',
    'To the Snowflake Hiring Committee, My extensive background building high-throughput Apache Spark and SQL streaming architectures aligns with your core database group.'
),
(
    'e0000000-0000-0000-0000-000000000005',
    'APP-2026-1005',
    'a0000000-0000-0000-0000-000000000005', -- Liam O'Connor
    'b0000000-0000-0000-0000-000000000005', -- Figma Senior SRE
    'Waiting List',                          -- 🟡 Waiting List
    'Application placed in Active Candidate Talent Pool. Awaiting opening of additional cluster operations capacity.',
    155000.00,
    '30 Days Notice',
    'Dear Figma Infrastructure Team, I have followed Figma web performance engineering with admiration. My expertise in Kubernetes GitOps and AWS resilience will bring immediate reliability.'
)
ON CONFLICT (id) DO NOTHING;
