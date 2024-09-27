INSERT INTO emails (id, receiver)
VALUES (1, 'nik.malykh.2024@mail.ru')
on conflict (receiver) do nothing;