drop schema if exists modweave cascade;
create schema modweave;

create table modweave.users (
    login varchar(255) primary key,
    username varchar(255) unique not null,
    email varchar(255) unique not null,
    passhash varchar not null,
    register_date timestamp default current_date not null,
    is_admin boolean
);

create table modweave.games (
    id serial primary key,
    name varchar(255) not null,
    description text
);

create table modweave.mods (
    id serial primary key,
    name varchar(255) not null,
    description text,
    game_id int,
    publisher_login varchar,
    foreign key(publisher_login) references modweave.users (login),
    foreign key(game_id) references modweave.games (id)
);

create table modweave.mod_versions (
    version_name varchar primary key,
    changes text,
    upload_date date,
    bucket_key varchar,
    mod_id int,
    foreign key(mod_id) references modweave.mods (id)
);

create table modweave.mod_files (
    id serial primary key,
    file_key varchar,
    mod_version varchar,
    foreign key(mod_version) references modweave.mod_versions (version_name)
);

create table modweave.categories (
    name varchar primary key,
    description text
);

create table modweave.mods_categories (
    id serial primary key,
    mod_id int,
    category_name varchar,
    foreign key(mod_id) references modweave.mods (id),
    foreign key(category_name) references modweave.categories (name)
);

create table modweave.comments (
    id serial primary key,
    user_login varchar(255),
    mod_id int,
    foreign key(user_login) references modweave.users (login),
    foreign key(mod_id) references modweave.mods (id)
);
