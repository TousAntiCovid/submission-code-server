--
-- Add new table to store jti already use from JWT
--

CREATE TABLE jwt
(
    id  bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL PRIMARY KEY,
    jti character varying(255)                                 NOT NULL UNIQUE
);

CREATE INDEX jti_idx ON jwt USING btree (jti);
