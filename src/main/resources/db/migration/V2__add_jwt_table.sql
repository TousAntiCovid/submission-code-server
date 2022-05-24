--
-- Add new table to store jti already use from JWT
--

CREATE TABLE jwt_used
(
    id  bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL PRIMARY KEY,
    jti character varying(255)                                 NOT NULL UNIQUE
);
