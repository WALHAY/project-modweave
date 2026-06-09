drop role if exists mw_guest;
drop role if exists mw_user;
drop role if exists mw_admin;

create role mw_guest;
create role mw_user;
create role mw_admin;

grant usage on schema modweave to mw_guest, mw_user, mw_admin;

grant select on table modweave.users to mw_guest;
grant select, update on table modweave.users to mw_user;
grant all privileges on table modweave.users to mw_admin;

grant select on table modweave.games to mw_guest;
grant select on table modweave.games to mw_user;
grant all privileges on table modweave.games to mw_admin;

grant select on table modweave.mods to mw_guest;
grant all privileges on table modweave.mods to mw_user;
grant all privileges on table modweave.mods to mw_admin;

grant select on table modweave.mod_versions to mw_guest;
grant all privileges on table modweave.mod_versions to mw_user;
grant all privileges on table modweave.mod_versions to mw_admin;

grant select on table modweave.mod_files to mw_guest;
grant select, insert, delete on table modweave.mod_files to mw_user;
grant all privileges on table modweave.mod_files to mw_admin;

grant select on table modweave.categories to mw_guest;
grant select on table modweave.categories to mw_user;
grant all privileges on table modweave.categories to mw_admin;

grant select on table modweave.mods_categories to mw_guest;
grant select, insert, delete on table modweave.mods_categories to mw_user;
grant all privileges on table modweave.mods_categories to mw_admin;

grant select on table modweave.comments to mw_guest;
grant select, insert, delete on table modweave.comments to mw_user;
grant all privileges on table modweave.comments to mw_admin;

grant select on table modweave.collections to mw_guest;
grant all privileges on table modweave.collections to mw_user;
grant all privileges on table modweave.collections to mw_admin;

grant select on table modweave.collections_mods to mw_guest;
grant all privileges on table modweave.collections_mods to mw_user;
grant all privileges on table modweave.collections_mods to mw_admin;

grant usage, select on sequence modweave.mod_files_id_seq to mw_user, mw_admin;
grant usage, select on sequence modweave.comments_id_seq to mw_user, mw_admin;
grant usage,
select on sequence modweave.collections_id_seq to mw_user,
mw_admin;
