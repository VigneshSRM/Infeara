-- Published field notes are read by the Java website at request time.
create table if not exists public.insights (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique check (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  title text not null check (char_length(title) between 8 and 180),
  category text not null check (char_length(category) between 2 and 80),
  excerpt text not null check (char_length(excerpt) between 20 and 320),
  content text not null check (char_length(content) between 100 and 12000),
  author text not null default 'Infeara Editorial',
  reading_minutes integer not null default 1 check (reading_minutes between 1 and 60),
  status text not null default 'draft' check (status in ('draft', 'published')),
  published_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (status <> 'published' or published_at is not null)
);

create index if not exists insights_published_at_idx
  on public.insights (published_at desc) where status = 'published';

alter table public.insights enable row level security;
revoke all on public.insights from anon, authenticated;
grant select on public.insights to service_role;

insert into public.insights (slug, title, category, excerpt, content, author, reading_minutes, status, published_at)
values
('server-backup-strategy', 'How to build a reliable server backup strategy', 'INFRASTRUCTURE', 'A practical way to think about recovery points, restore testing, and ownership.', $article$A backup plan is useful only when it matches what the business needs to recover. Begin by listing critical systems, the data they hold, and the people who depend on them.

Agree recovery point and recovery time expectations with the business owners. These targets help determine how often data should be protected and how quickly services need to return.

Keep backup copies isolated from the systems they protect, limit administrative access, and monitor backup jobs for failures. A successful job report is not the same as a verified restore.

Schedule restore tests for representative systems and document the steps, dependencies, and responsibilities. Update the plan when applications, infrastructure, or business requirements change.$article$, 'Infeara Editorial', 4, 'published', '2026-10-25T00:00:00Z'),
('patch-management-checklist', 'Windows Server patch management: a planning checklist', 'INFRASTRUCTURE', 'Coordinate updates with system owners, maintenance windows, and rollback plans.', $article$Start with an accurate inventory of servers, applications, owners, and support dependencies. Classify systems by business criticality and understand which services depend on each one.

Review vendor advisories and test updates in a representative non-production environment where possible. Identify application compatibility risks, disk-space requirements, and any prerequisite changes.

Agree a maintenance window with affected stakeholders. Prepare a backup or recovery point, a validation checklist, communication steps, and a rollback approach before deployment.

After patching, verify the operating system and the business application—not only that a server restarted. Record exceptions, outcomes, and follow-up work so the next maintenance cycle starts with better information.$article$, 'Infeara Editorial', 4, 'published', '2026-10-25T00:00:00Z'),
('cloud-vs-on-premises', 'Cloud vs. on-premises: a business decision framework', 'CLOUD & DATACENTER', 'Compare workload needs, operating responsibilities, resilience, and total cost.', $article$Cloud and on-premises infrastructure are operating choices, not a simple old-versus-new comparison. The right fit depends on the workload, its users, its data, and the team's ability to operate it.

Consider demand patterns, performance requirements, data location, connectivity, recovery expectations, and integration with existing systems. Include the skills and operational processes needed after implementation.

Compare total cost over a realistic planning period. Include migration, licensing, network connectivity, backup, monitoring, support, and future capacity—not only the initial server or monthly cloud bill.

Many environments use a mix of platforms. Start with clear workload requirements, document the trade-offs, and review the decision as business priorities change.$article$, 'Infeara Editorial', 4, 'published', '2026-10-25T00:00:00Z')
on conflict (slug) do nothing;
