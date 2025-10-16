--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3
-- Dumped by pg_dump version 15.3

-- Started on 2025-10-13 10:51:54

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
-- TOC entry 217 (class 1259 OID 66463)
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_tokens (
    id bigint NOT NULL,
    token character varying(255) NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    used boolean DEFAULT false
);


ALTER TABLE public.password_reset_tokens OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 66462)
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.password_reset_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.password_reset_tokens_id_seq OWNER TO postgres;

--
-- TOC entry 3342 (class 0 OID 0)
-- Dependencies: 216
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.password_reset_tokens_id_seq OWNED BY public.password_reset_tokens.id;


--
-- TOC entry 215 (class 1259 OID 66451)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    name character varying(255),
    email character varying(255) NOT NULL,
    password character varying(255),
    role character varying(255),
    is_active boolean DEFAULT true,
    created_at timestamp without time zone,
    last_login timestamp without time zone,
    address character varying(255),
    date_of_birth date,
    emergency_contact character varying(255),
    phone character varying(255),
    is_first_login boolean,
    department_id bigint
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 66450)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_user_id_seq OWNER TO postgres;

--
-- TOC entry 3343 (class 0 OID 0)
-- Dependencies: 214
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 3180 (class 2604 OID 66466)
-- Name: password_reset_tokens id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens ALTER COLUMN id SET DEFAULT nextval('public.password_reset_tokens_id_seq'::regclass);


--
-- TOC entry 3178 (class 2604 OID 66454)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 3336 (class 0 OID 66463)
-- Dependencies: 217
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.password_reset_tokens (id, token, user_id, created_at, expires_at, used) FROM stdin;
\.


--
-- TOC entry 3334 (class 0 OID 66451)
-- Dependencies: 215
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, name, email, password, role, is_active, created_at, last_login, address, date_of_birth, emergency_contact, phone, is_first_login, department_id) FROM stdin;
12	Andridge Munene	munene@scholarspace.com	$2a$10$UKlwxSSu/zKhtKkthPkgYueBWjDkzqy1AX4Zvd.Cgw.tF.rVyxOJK	INSTRUCTOR	t	2025-09-29 15:49:06.707514	2025-09-29 17:04:25.15096	\N	\N	\N	\N	f	3
4	Test Student	test.student@example.com	$2a$10$/.ogK.Gqh20aHuy1jKX1SOSOgguDz1Yey678oa2uVdiOrqHZQlET6	STUDENT	t	2025-09-25 08:42:18.203037	\N	\N	\N	\N	\N	\N	\N
10	John Kimani	kimani@scholarspace.com	$2a$10$5heVQb0sCdx4aLpaPPevsOIC80vCPT6W9uHyldTUoxEWrhr6tykf.	INSTRUCTOR	t	2025-09-29 14:26:54.441694	2025-09-29 14:31:51.616425	\N	\N	\N	\N	f	3
1	John Doe Updated	john.doe.updated@example.com	$2a$10$4nei29Rlffyz3uAkI4K81.6uxJrdiwjQoeNFBgTp8LV00vgaQU4x2	STUDENT	t	2025-09-25 00:17:48.830197	2025-09-25 09:15:18.361919	\N	\N	\N	\N	\N	\N
13	Agnes Kairetu	kairetu@scholarspace.com	$2a$10$NJXxBBVw/BI2aTGoBP2O/eE6rewARXFEN/Eirlk5zyaRn4tzlSMw2	INSTRUCTOR	t	2025-09-29 17:05:11.997802	2025-09-29 17:06:38.747439	\N	\N	\N	\N	f	5
5	Elian Njori	elian.njori@example.com	$2a$10$tZRawhegHeHTZMNtQmcBIOzWMXzWWeGQew5.H3OQDTysN9ZiLlh9C	INSTRUCTOR	t	2025-09-25 09:30:10.728546	2025-09-29 14:39:25.391254	\N	\N	\N	\N	f	3
2	Admin	admin@scholarspace.com	$2a$10$BWgj7HF/85PxZx9QJ744CuMnQF6/56fBYUD0KbYd6WboKfN7r4XAK	ADMIN	t	2025-09-25 00:18:25.580028	2025-09-30 09:27:35.82444	409-00217	2006-02-21	87654321	12345678	\N	\N
6	Abel Korir	abel.korir@example.com	$2a$10$zb5aIwJkL5bN97GZJXwHluswpAUx4y8EUr6rxIEY79ATSTJCFQUhi	STUDENT	t	2025-09-25 09:39:57.183603	2025-09-25 09:54:05.846977	\N	\N	\N	\N	\N	\N
11	Brian Kamunya	kamunya@scholarspace.com	$2a$10$Y6.QtwwaQg3E0benDbqQ9.jodpdL5ZmQhmcppqEIkMDHbzG45PY.i	INSTRUCTOR	t	2025-09-29 14:38:38.545036	2025-09-29 15:41:18.23606	\N	\N	\N	\N	f	4
3	Jane Smith	jane.smith@example.com	$2a$10$8A3H0KCsV1VMegTi9qMmJO1Sg/10afgeGCuJA86Zq3kYrE.0xlDbW	INSTRUCTOR	t	2025-09-25 00:18:44.701048	2025-09-25 09:06:33.227528	\N	\N	\N	\N	\N	4
8	James Kamau	kamau@scholarspace.com	$2a$10$ZEj5a0.xY6dtqvCBJBbP8uR.BAQh729rb2dCvBmZ1LzVCQBg5sYoK	INSTRUCTOR	t	2025-09-27 10:15:09.129939	2025-09-27 10:19:35.476168	\N	\N	\N	\N	f	5
7	Liam Karinga	liam.karinga@example.com	$2a$10$9sPK33mACo1.LJnqaBLkduwROWLTVg9v5ID8yACae4exgj07oN5dW	STUDENT	t	2025-09-25 12:27:15.624527	2025-09-26 14:39:38.425557	\N	\N	\N	\N	\N	\N
9	Kaunda Kasongo	kasongo@scholarspace.com	$2a$10$RwAe/puqecwXdLjmwWtn4OqVEKPVv22rUyLMDRRyxOPEAWYs59Ura	INSTRUCTOR	t	2025-09-29 10:58:38.658268	2025-09-29 14:15:26.907926	\N	\N	\N	\N	f	6
\.


--
-- TOC entry 3344 (class 0 OID 0)
-- Dependencies: 216
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.password_reset_tokens_id_seq', 1, false);


--
-- TOC entry 3345 (class 0 OID 0)
-- Dependencies: 214
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 13, true);


--
-- TOC entry 3187 (class 2606 OID 66469)
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 3189 (class 2606 OID 66471)
-- Name: password_reset_tokens password_reset_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_key UNIQUE (token);


--
-- TOC entry 3183 (class 2606 OID 66461)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3185 (class 2606 OID 66459)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3190 (class 2606 OID 66472)
-- Name: password_reset_tokens password_reset_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


-- Completed on 2025-10-13 10:51:55

--
-- PostgreSQL database dump complete
--

