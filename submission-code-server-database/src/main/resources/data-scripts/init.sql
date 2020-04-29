drop table if exists codepositive;

create table codepositive(
id bigint not null , lot float  null,
code varchar(128) not null, type_code char(1) not null,
used boolean not null, date_end_validity timestamp not null,
date_available timestamp  not null,
date_use timestamp,
date_generation timestamp,
CONSTRAINT codepositive_pk PRIMARY KEY(id)
);