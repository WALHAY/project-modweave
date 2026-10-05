create schema if not exists modweave;

create table modweave.users (
    username varchar(50) primary key,
    name varchar(100) not null,
    email varchar(320) unique not null,
    password varchar(255) not null,
    register_date timestamp default current_timestamp not null,
    is_admin boolean default false not null
);
create unique index users_username_ignore_case on modweave.users (lower(username));
create unique index users_email_ignore_case on modweave.users (lower(email));

create table modweave.games (
    id varchar primary key,
    name varchar not null,
    description text,
    image_path varchar not null
);

create table modweave.mods (
    id varchar(50) primary key,
    name varchar(255) not null,
    description text,
    image_path varchar(500) not null,
    creation_date timestamp default current_timestamp not null,
    game_id varchar not null references modweave.games (id) on delete cascade,
    publisher_id varchar(50) not null references modweave.users (username) on delete cascade
);

create type modweave.version_status as enum ('PENDING', 'APPROVED', 'REJECTED');
create table modweave.mod_versions (
    id uuid primary key,
    name varchar not null,
    changes text,
    upload_date timestamp default current_timestamp not null,
    status modweave.version_status default 'PENDING' not null,
    mod_id varchar(50) not null references modweave.mods (id) on delete cascade,
    unique (name, mod_id)
);

create table modweave.mod_files (
    id uuid primary key,
    filename varchar not null,
    file_path varchar unique not null,
    downloads int default 0 not null check (downloads >= 0),
    mod_version_id uuid not null references modweave.mod_versions (id) on delete cascade
);

create table modweave.categories (
    name varchar primary key,
    description text
);
create unique index categories_name_ignore_case on modweave.categories (lower(name));

create table modweave.mods_categories (
    mod_id varchar(50) not null references modweave.mods (id) on delete cascade,
    category_name varchar not null references modweave.categories (name) on delete cascade,
    primary key (mod_id, category_name)
);

create table modweave.comments (
    id uuid primary key,
    content text not null,
    publish_date timestamp default current_timestamp not null,
    user_id varchar(50) not null references modweave.users (username) on delete cascade,
    mod_id varchar(50) not null references modweave.mods (id) on delete cascade
);

create table modweave.collections (
    id uuid primary key,
    name varchar not null,
    description text,
    owner varchar(50) not null references modweave.users (username) on delete cascade,
    unique (name, owner)
);

create table modweave.collections_mods (
    collection_id uuid not null references modweave.collections (id) on delete cascade,
    order_index int not null check (order_index >= 0),
    mod_id varchar(50) not null references modweave.mods (id) on delete cascade,
    primary key (collection_id, order_index),
    unique (collection_id, mod_id)
);

create index mods_game_idx on modweave.mods (game_id);
create index mods_publisher_idx on modweave.mods (publisher_id);
create index versions_mod_status_idx on modweave.mod_versions (mod_id, status);
create index files_version_idx on modweave.mod_files (mod_version_id);
create index mods_categories_category_idx on modweave.mods_categories (category_name);
create index comments_mod_idx on modweave.comments (mod_id);
create index comments_user_idx on modweave.comments (user_id);
create index collections_owner_idx on modweave.collections (owner);
create index collections_mods_mod_idx on modweave.collections_mods (mod_id);
