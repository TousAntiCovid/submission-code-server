--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.22
-- Dumped by pg_dump version 9.6.22

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

--
-- Name: lot_keys; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE lot_keys
(
    id              bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
    date_execution  timestamp without time zone NOT NULL,
    number_of_codes bigint DEFAULT 0                                       NOT NULL
);

--
-- Name: seq_fichier; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE seq_fichier
(
    id       bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
    annee    integer                                                NOT NULL,
    jour     integer                                                NOT NULL,
    mois     integer                                                NOT NULL,
    sequence integer                                                NOT NULL
);

--
-- Name: submission_code; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE submission_code
(
    id                bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
    code              character varying(255)                                 NOT NULL,
    date_available    timestamp without time zone NOT NULL,
    date_end_validity timestamp without time zone NOT NULL,
    date_generation   timestamp without time zone NOT NULL,
    date_use          timestamp without time zone,
    type_code         character varying(255)                                 NOT NULL,
    used              boolean                                                NOT NULL,
    lotkey_id         bigint
);

CREATE TABLE jwt
(
    id  bigint DEFAULT nextval('hibernate_sequence'::regclass) NOT NULL,
    jti character varying(255)                                 NOT NULL
);

--
-- Name: lot_keys lot_keys_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY lot_keys
    ADD CONSTRAINT lot_keys_pkey PRIMARY KEY (id);


--
-- Name: seq_fichier seq_fichier_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY seq_fichier
    ADD CONSTRAINT seq_fichier_pkey PRIMARY KEY (id);


--
-- Name: submission_code submission_code_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY submission_code
    ADD CONSTRAINT submission_code_pkey PRIMARY KEY (id);


ALTER TABLE ONLY jwt
    ADD CONSTRAINT jwt_pkey PRIMARY KEY (id);

--
-- Name: submission_code uk_neb13efe0uryoy16dkr526b6c; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY submission_code
    ADD CONSTRAINT uk_neb13efe0uryoy16dkr526b6c UNIQUE (code);


--
-- Name: type_code_date_available_idx; Type: INDEX; Schema: public; Owner: test
--

CREATE INDEX type_code_date_available_idx ON submission_code USING btree (type_code, date_available);


--
-- Name: type_code_idx; Type: INDEX; Schema: public; Owner: test
--

CREATE INDEX type_code_idx ON submission_code USING btree (type_code);


--
-- Name: submission_code fkd56txfmmdc7cvuq1mtlltdm50; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY submission_code
    ADD CONSTRAINT fkd56txfmmdc7cvuq1mtlltdm50 FOREIGN KEY (lotkey_id) REFERENCES lot_keys (id) ON
DELETE
CASCADE;


--
-- PostgreSQL database dump complete
--

