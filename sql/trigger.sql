create or replace function
check_mod_version_files ()
returns trigger as $$
	begin
		if new.status = 'APPROVED' then
			if not exists (
				select 1
				from mod_files
				where mod_version_id = new.id
				) then
				raise exception
				'version must contain at least one file';
			end if;
		end if;
	
		return new;
	end;
	$$ language plpgsql ;

create trigger
mod_version_validation_trigger
before update on mod_versions
for each row
execute function
check_mod_version_files () ;
