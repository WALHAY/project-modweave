drop schema if exists modweave cascade;
create schema modweave;

create table modweave.users (
    id uuid default gen_random_uuid() primary key,
    login varchar unique not null,
    username varchar unique not null,
    email varchar unique not null,
    password varchar not null,
    register_date timestamp default current_date not null,
    is_admin boolean
);

create table modweave.games (
    id varchar primary key,
    name varchar not null,
    description text,
    image_path varchar not null
);

create table modweave.mods (
    id varchar primary key,
    name varchar not null,
    description text,
    image_path varchar not null,
    creation_date timestamp default current_date not null,
    game_id varchar not null references modweave.games (id) on delete cascade,
    publisher_id uuid not null references modweave.users (id) on delete cascade
);

drop type if exists version_status;
create type version_status as enum ('pending', 'approved', 'rejected');

create table modweave.mod_versions (
    id bigserial primary key,
    name varchar not null,
    changes text,
    upload_date timestamp default current_date not null,
    approved version_status,
    mod_id varchar not null references modweave.mods (id) on delete cascade
);

create table modweave.mod_files (
    id bigserial primary key,
    filename varchar not null,
    file_path varchar not null,
    downloads int,
    mod_version_id bigserial not null references modweave.mod_versions (id) on delete cascade
    -- возможно стоит задуматься о on delete set null и проверять файлы без связи раз в какое-то время
);

create table modweave.categories (
    name varchar primary key,
    description text
);

create table modweave.mods_categories (
    id serial primary key,
    mod_id varchar not null references modweave.mods (id) on delete cascade,
    category_name varchar references modweave.categories (name) on delete set null
);

create table modweave.comments (
    id serial primary key,
    content text not null,
    publish_date timestamp default current_date not null,
    user_id uuid not null references modweave.users (id) on delete cascade,
    mod_id varchar not null references modweave.mods (id) on delete cascade
);
