-- Durable recovery journal. Apply before starting an upgraded application.
create table if not exists modweave.storage_cleanup_tasks (
    id uuid primary key,
    bucket varchar(16) not null check (bucket in ('MODS', 'IMAGES')),
    object_key text not null,
    created_at timestamptz default current_timestamp not null,
    next_attempt_at timestamptz default current_timestamp not null,
    attempts integer default 0 not null check (attempts >= 0),
    last_error varchar(1000),
    unique (bucket, object_key)
);
create index if not exists storage_cleanup_due_idx
    on modweave.storage_cleanup_tasks (next_attempt_at, created_at, id);
