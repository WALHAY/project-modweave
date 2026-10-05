create or replace function modweave.check_mod_version_files()
returns trigger as '
begin
    if new.status = ''APPROVED'' and not exists (
        select 1 from modweave.mod_files where mod_version_id = new.id
    ) then
        raise exception ''version must contain at least one file'';
    end if;
    return new;
end;
' language plpgsql;

drop trigger if exists mod_version_validation_trigger on modweave.mod_versions;
create trigger mod_version_validation_trigger
before insert or update of status on modweave.mod_versions
for each row execute function modweave.check_mod_version_files();
