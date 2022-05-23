--
-- Add new table to store jti already use from JWT
--

CREATE TABLE jwt
(
    id  bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
    jti character varying(255)                                 NOT NULL
);


ALTER TABLE ONLY jwt
    ADD CONSTRAINT jwt_pkey PRIMARY KEY (id);

