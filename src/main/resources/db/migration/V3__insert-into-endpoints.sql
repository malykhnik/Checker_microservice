INSERT INTO endpoints (username, password, role_id, url, period)
VALUES ('endpoint1', '$2a$12$cgyAVISqKmCZp.CxB5mSVOdGIHYEPlMChL3OVclSpntS5.9fJeR4C', 1, 'http://endpoint-without-db:3050/', 30),
       ('endpoint2', '$2a$12$ONebYb21q/BIEbQfZNT5ieIBEEhoZWZTnp6Jvwk.fpf4J/NgPUHDm', 1, 'http://endpoint-with-sql:3080/', 60),
       ('endpoint3', '$2a$12$VF25ECS9yzo16aU1RkiCueaCPlVV77DrAMWY46QPrQO62nNtJ1qLa', 2, 'http://endpoint-with-nosql:3090/', 90)
    on conflict (username) do nothing;