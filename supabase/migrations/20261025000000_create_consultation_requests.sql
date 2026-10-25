-- Consultation form submissions. The site sends writes through its Java API using a server-only service role key.
create table if not exists public.consultation_requests (
  id uuid primary key default gen_random_uuid(),
  full_name text not null check (char_length(full_name) between 2 and 120),
  business_email text not null check (char_length(business_email) <= 254),
  company_name text not null check (char_length(company_name) between 1 and 160),
  role_department text check (role_department is null or char_length(role_department) <= 120),
  service_of_interest text not null check (char_length(service_of_interest) between 2 and 120),
  description text not null check (char_length(description) between 10 and 3000),
  preferred_contact text check (preferred_contact is null or char_length(preferred_contact) <= 40),
  landing_page text not null default '/',
  status text not null default 'new' check (status in ('new', 'contacted', 'qualified', 'closed', 'spam')),
  consent_confirmed_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

create index if not exists consultation_requests_created_at_idx
  on public.consultation_requests (created_at desc);

alter table public.consultation_requests enable row level security;
revoke all on public.consultation_requests from anon, authenticated;
grant all on public.consultation_requests to service_role;
