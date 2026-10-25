-- Persist a timestamp for the required privacy acknowledgement on new enquiries.
alter table public.consultation_requests
  add column if not exists consent_confirmed_at timestamptz not null default now();
