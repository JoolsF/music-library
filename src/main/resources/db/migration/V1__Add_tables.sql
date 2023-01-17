create table foo
(
    id varchar not null,
    bar varchar not null
);

create unique index foo_id_uindex
    on foo (id);

