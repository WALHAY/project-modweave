drop schema modweave cascade;
create schema modweave;

create table modweave.users (
    login varchar(255) primary key,
    nickname varchar(255) unique,
    email varchar(255) unique,
    passhash varchar,
    salt varchar,
    register_date date,
    is_admin boolean
);

insert into modweave.users
values ('asd', 'asd', 'asd@asd.asd', 'asd', current_date, true);

create table modweave.games (
    id int primary key,
    name varchar(255),
    description text
);

insert into modweave.games
values (0, 'membuddha', 'game about some religious shit'),
(1, 'zizka', 'game about kakayato zizka');

create table modweave.mods (
    id int primary key,
    name varchar(255),
    description text,
    game_id int,
    publisher_login varchar,
    foreign key(publisher_login) references modweave.users (login),
    foreign key(game_id) references modweave.games (id)
);

insert into modweave.mods
values (0, 'Govno', 'Mod about govno', 0, 'asd'),
(1, 'Gaziki', 'Mod about gaziki', 1, 'asd');
