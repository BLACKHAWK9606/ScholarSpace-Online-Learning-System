
-- PostgreSQL database dump
--

-- Dumped from database version 15.3
-- Dumped by pg_dump version 15.3

-- Started on 2025-10-13 10:57:29

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 66544)
-- Name: departments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.departments (
    department_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(255),
    institution_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    department_email character varying(255),
    head_of_department character varying(255),
    office_location character varying(255)
);


ALTER TABLE public.departments OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 66543)
-- Name: departments_department_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.departments_department_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.departments_department_id_seq OWNER TO postgres;

--
-- TOC entry 3342 (class 0 OID 0)
-- Dependencies: 216
-- Name: departments_department_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.departments_department_id_seq OWNED BY public.departments.department_id;


--
-- TOC entry 215 (class 1259 OID 66534)
-- Name: institutions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institutions (
    institution_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    location character varying(255),
    contact character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    established_year integer,
    phone character varying(255),
    institution_type character varying(255),
    website character varying(255),
    CONSTRAINT institutions_institution_type_check CHECK (((institution_type)::text = ANY ((ARRAY['UNIVERSITY'::character varying, 'COLLEGE'::character varying, 'SCHOOL'::character varying, 'INSTITUTE'::character varying])::text[])))
);


ALTER TABLE public.institutions OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 66533)
-- Name: institutions_institution_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.institutions_institution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.institutions_institution_id_seq OWNER TO postgres;

--
-- TOC entry 3343 (class 0 OID 0)
-- Dependencies: 214
-- Name: institutions_institution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.institutions_institution_id_seq OWNED BY public.institutions.institution_id;


--
-- TOC entry 3180 (class 2604 OID 66547)
-- Name: departments department_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments ALTER COLUMN department_id SET DEFAULT nextval('public.departments_department_id_seq'::regclass);


--
-- TOC entry 3178 (class 2604 OID 66537)
-- Name: institutions institution_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institutions ALTER COLUMN institution_id SET DEFAULT nextval('public.institutions_institution_id_seq'::regclass);


--
-- TOC entry 3336 (class 0 OID 66544)
-- Dependencies: 217
-- Data for Name: departments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.departments (department_id, name, code, description, institution_id, created_at, department_email, head_of_department, office_location) FROM stdin;
4	Mathematics	MATH	Department of Pure and Applied Mathematics	3	2025-09-26 11:04:47.15553	\N	\N	\N
3	Computer Science and IT	CS	Department focusing on modern computing technology	3	2025-09-26 11:02:56.983201	\N	\N	\N
5	Engineering	ENG	Department of Engineering	3	2025-09-27 09:46:15.910348	\N	\N	\N
6	Agriculture	AGR	Agriculture department	3	2025-09-29 12:56:17.513625	\N	\N	\N
\.


--
-- TOC entry 3334 (class 0 OID 66534)
-- Dependencies: 215
-- Data for Name: institutions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institutions (institution_id, name, location, contact, created_at, established_year, phone, institution_type, website) FROM stdin;
3	Harvard University	Cambridge, Massachusetts	newadmin@harvard.edu	2025-09-26 11:00:59.559289	1800	617-495-1551	UNIVERSITY	https://www.harvard.edu/
4	E&M Technology House	Eneo Central, Tatu City, Kiambu, Kenya	info@emtechhouse.co.ke	2025-09-29 10:38:02.249105	2017	+254 722582328	INSTITUTE	https://www.emtechhouse.co.ke/
\.


--
-- TOC entry 3344 (class 0 OID 0)
-- Dependencies: 216
-- Name: departments_department_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.departments_department_id_seq', 6, true);


--
-- TOC entry 3345 (class 0 OID 0)
-- Dependencies: 214
-- Name: institutions_institution_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.institutions_institution_id_seq', 4, true);


--
-- TOC entry 3187 (class 2606 OID 66552)
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (department_id);


--
-- TOC entry 3185 (class 2606 OID 66542)
-- Name: institutions institutions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institutions
    ADD CONSTRAINT institutions_pkey PRIMARY KEY (institution_id);


--
-- TOC entry 3188 (class 1259 OID 66560)
-- Name: idx_departments_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_departments_code ON public.departments USING btree (code);


--
-- TOC entry 3189 (class 1259 OID 66558)
-- Name: idx_departments_institution_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_departments_institution_id ON public.departments USING btree (institution_id);


--
-- TOC entry 3183 (class 1259 OID 66559)
-- Name: idx_institutions_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_institutions_name ON public.institutions USING btree (name);


--
-- TOC entry 3190 (class 2606 OID 66553)
-- Name: departments fk_department_institution; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT fk_department_institution FOREIGN KEY (institution_id) REFERENCES public.institutions(institution_id) ON DELETE CASCADE;


-- Completed on 2025-10-13 10:57:30

--
-- PostgreSQL database dump complete
--

