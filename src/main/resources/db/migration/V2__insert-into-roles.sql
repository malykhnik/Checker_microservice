insert into roles  (id, name)
values (1, 'user'), (2,'admin')
    on conflict (name) do nothing;