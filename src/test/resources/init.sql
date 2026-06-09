drop schema if exists modweave cascade;
create schema modweave;

create table modweave.users (
    username varchar primary key,
    name varchar unique not null,
    email varchar unique not null,
    password varchar not null,
    register_date timestamp default current_timestamp not null,
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
    creation_date timestamp default current_timestamp not null,
    game_id varchar not null references modweave.games (id) on delete cascade,
    publisher_id varchar not null references modweave.users (
        username
    ) on delete cascade
);

create type modweave.version_status as enum ('PENDING', 'APPROVED', 'REJECTED');

create table modweave.mod_versions (
id uuid primary key,
name varchar not null,
changes text,
upload_date timestamp default current_timestamp not null,
status modweave.version_status default 'PENDING',
mod_id varchar not null references modweave.mods (id) on delete cascade,
unique (name, mod_id)
) ;

create table modweave.mod_files (
id uuid primary key,
filename varchar not null,
file_path varchar not null,
downloads int check (downloads >= 0),
mod_version_id uuid not null references modweave.mod_versions (id) on delete cascade
) ;

create table modweave.categories (
name varchar primary key,
description text
) ;

create table modweave.mods_categories (
mod_id varchar not null references modweave.mods (id) on delete cascade,
category_name varchar references modweave.categories (name) on delete cascade,
primary key (mod_id, category_name)
) ;

create table modweave.comments (
id uuid primary key,
content text not null,
publish_date timestamp default current_timestamp not null,
user_id varchar not null references modweave.users (username) on delete cascade,
mod_id varchar not null references modweave.mods (id) on delete cascade
) ;

create table modweave.collections (
id uuid primary key,
name varchar not null,
description text,
owner varchar not null references modweave.users (username) on delete cascade,
unique (name, owner)
) ;

create table modweave.collections_mods (
collection_id uuid not null references modweave.collections (id) on delete cascade,
order_index int not null,
mod_id varchar not null references modweave.mods (id) on delete cascade,
primary key (collection_id, order_index),
unique (collection_id, mod_id)
) ;
