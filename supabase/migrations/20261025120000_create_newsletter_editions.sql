-- Newsletter editions are unpublished until explicitly marked published.
create table if not exists public.newsletter_editions (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique check (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  edition_label text not null check (char_length(edition_label) between 2 and 60),
  title text not null check (char_length(title) between 8 and 180),
  excerpt text not null check (char_length(excerpt) between 20 and 320),
  content text not null check (char_length(content) between 100 and 20000),
  reading_minutes integer not null default 1 check (reading_minutes between 1 and 60),
  status text not null default 'draft' check (status in ('draft', 'published')),
  published_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (status <> 'published' or published_at is not null)
);

create index if not exists newsletter_editions_published_at_idx
  on public.newsletter_editions (published_at desc) where status = 'published';

alter table public.newsletter_editions enable row level security;
revoke all on public.newsletter_editions from anon, authenticated;
grant select on public.newsletter_editions to service_role;
