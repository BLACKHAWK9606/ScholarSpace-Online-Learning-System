
-- PostgreSQL database dump
--

-- Dumped from database version 15.3
-- Dumped by pg_dump version 15.3

-- Started on 2025-10-13 10:58:04

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

--
-- TOC entry 858 (class 1247 OID 66594)
-- Name: attendance_status_enum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.attendance_status_enum AS ENUM (
    'PRESENT',
    'ABSENT',
    'EXCUSED'
);


ALTER TYPE public.attendance_status_enum OWNER TO postgres;

--
-- TOC entry 849 (class 1247 OID 66571)
-- Name: content_type_enum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.content_type_enum AS ENUM (
    'LECTURE',
    'ASSIGNMENT',
    'RESOURCE'
);


ALTER TYPE public.content_type_enum OWNER TO postgres;

--
-- TOC entry 852 (class 1247 OID 66578)
-- Name: enrollment_status_enum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.enrollment_status_enum AS ENUM (
    'PENDING',
    'ACTIVE',
    'DROPPED',
    'COMPLETED'
);


ALTER TYPE public.enrollment_status_enum OWNER TO postgres;

--
-- TOC entry 855 (class 1247 OID 66588)
-- Name: instructor_role_enum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.instructor_role_enum AS ENUM (
    'PRIMARY',
    'ASSISTANT'
);


ALTER TYPE public.instructor_role_enum OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 226 (class 1259 OID 66694)
-- Name: attendance_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.attendance_records (
    attendance_id bigint NOT NULL,
    course_id bigint NOT NULL,
    student_id bigint NOT NULL,
    session_date date NOT NULL,
    status character varying(255) NOT NULL,
    recorded_by bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.attendance_records OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 66693)
-- Name: attendance_records_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.attendance_records_attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.attendance_records_attendance_id_seq OWNER TO postgres;

--
-- TOC entry 3436 (class 0 OID 0)
-- Dependencies: 225
-- Name: attendance_records_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.attendance_records_attendance_id_seq OWNED BY public.attendance_records.attendance_id;


--
-- TOC entry 218 (class 1259 OID 66630)
-- Name: course_contents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_contents (
    content_id bigint NOT NULL,
    course_id bigint NOT NULL,
    title character varying(255) NOT NULL,
    description character varying(255),
    content_type character varying(255) NOT NULL,
    file_path character varying(255),
    file_type character varying(255),
    published_at timestamp without time zone,
    due_date timestamp without time zone,
    created_by bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.course_contents OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 66629)
-- Name: course_contents_content_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.course_contents_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.course_contents_content_id_seq OWNER TO postgres;

--
-- TOC entry 3437 (class 0 OID 0)
-- Dependencies: 217
-- Name: course_contents_content_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.course_contents_content_id_seq OWNED BY public.course_contents.content_id;


--
-- TOC entry 220 (class 1259 OID 66645)
-- Name: course_instructors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_instructors (
    id bigint NOT NULL,
    course_id bigint NOT NULL,
    instructor_id bigint NOT NULL,
    role character varying(255) DEFAULT 'PRIMARY'::public.instructor_role_enum,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.course_instructors OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 66644)
-- Name: course_instructors_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.course_instructors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.course_instructors_id_seq OWNER TO postgres;

--
-- TOC entry 3438 (class 0 OID 0)
-- Dependencies: 219
-- Name: course_instructors_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.course_instructors_id_seq OWNED BY public.course_instructors.id;


--
-- TOC entry 216 (class 1259 OID 66614)
-- Name: course_prerequisites; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_prerequisites (
    course_id bigint NOT NULL,
    prerequisite_id bigint NOT NULL
);


ALTER TABLE public.course_prerequisites OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 66602)
-- Name: courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.courses (
    course_id bigint NOT NULL,
    course_code character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    description character varying(255),
    credit_hours integer,
    semester character varying(255),
    academic_year character varying(255),
    department_id bigint,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.courses OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 66601)
-- Name: courses_course_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.courses_course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.courses_course_id_seq OWNER TO postgres;

--
-- TOC entry 3439 (class 0 OID 0)
-- Dependencies: 214
-- Name: courses_course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.courses_course_id_seq OWNED BY public.courses.course_id;


--
-- TOC entry 222 (class 1259 OID 66661)
-- Name: enrollments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.enrollments (
    enrollment_id bigint NOT NULL,
    course_id bigint NOT NULL,
    student_id bigint NOT NULL,
    enrollment_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status character varying(255) DEFAULT 'PENDING'::public.enrollment_status_enum NOT NULL,
    grade character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.enrollments OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 66660)
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.enrollments_enrollment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.enrollments_enrollment_id_seq OWNER TO postgres;

--
-- TOC entry 3440 (class 0 OID 0)
-- Dependencies: 221
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.enrollments_enrollment_id_seq OWNED BY public.enrollments.enrollment_id;


--
-- TOC entry 224 (class 1259 OID 66678)
-- Name: submissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.submissions (
    submission_id bigint NOT NULL,
    content_id bigint NOT NULL,
    student_id bigint NOT NULL,
    submission_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    file_path character varying(255),
    grade double precision,
    feedback character varying(255),
    graded_by bigint,
    graded_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.submissions OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 66677)
-- Name: submissions_submission_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.submissions_submission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.submissions_submission_id_seq OWNER TO postgres;

--
-- TOC entry 3441 (class 0 OID 0)
-- Dependencies: 223
-- Name: submissions_submission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.submissions_submission_id_seq OWNED BY public.submissions.submission_id;


--
-- TOC entry 3229 (class 2604 OID 66697)
-- Name: attendance_records attendance_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attendance_records ALTER COLUMN attendance_id SET DEFAULT nextval('public.attendance_records_attendance_id_seq'::regclass);


--
-- TOC entry 3217 (class 2604 OID 66633)
-- Name: course_contents content_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_contents ALTER COLUMN content_id SET DEFAULT nextval('public.course_contents_content_id_seq'::regclass);


--
-- TOC entry 3219 (class 2604 OID 66648)
-- Name: course_instructors id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_instructors ALTER COLUMN id SET DEFAULT nextval('public.course_instructors_id_seq'::regclass);


--
-- TOC entry 3214 (class 2604 OID 66605)
-- Name: courses course_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses ALTER COLUMN course_id SET DEFAULT nextval('public.courses_course_id_seq'::regclass);


--
-- TOC entry 3222 (class 2604 OID 66664)
-- Name: enrollments enrollment_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments ALTER COLUMN enrollment_id SET DEFAULT nextval('public.enrollments_enrollment_id_seq'::regclass);


--
-- TOC entry 3226 (class 2604 OID 66681)
-- Name: submissions submission_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submissions ALTER COLUMN submission_id SET DEFAULT nextval('public.submissions_submission_id_seq'::regclass);


--
-- TOC entry 3430 (class 0 OID 66694)
-- Dependencies: 226
-- Data for Name: attendance_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.attendance_records (attendance_id, course_id, student_id, session_date, status, recorded_by, created_at) FROM stdin;
\.


--
-- TOC entry 3422 (class 0 OID 66630)
-- Dependencies: 218
-- Data for Name: course_contents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_contents (content_id, course_id, title, description, content_type, file_path, file_type, published_at, due_date, created_by, created_at) FROM stdin;
\.


--
-- TOC entry 3424 (class 0 OID 66645)
-- Dependencies: 220
-- Data for Name: course_instructors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_instructors (id, course_id, instructor_id, role, created_at) FROM stdin;
1	1	5	PRIMARY	2025-09-29 14:06:20.918551
2	4	9	PRIMARY	2025-09-29 14:09:53.754217
3	3	8	PRIMARY	2025-09-29 14:10:05.358316
4	2	3	PRIMARY	2025-09-29 14:13:33.608914
5	5	10	PRIMARY	2025-09-29 14:30:27.151083
6	2	11	PRIMARY	2025-09-29 14:39:08.166835
7	3	13	PRIMARY	2025-09-29 17:05:20.497636
\.


--
-- TOC entry 3420 (class 0 OID 66614)
-- Dependencies: 216
-- Data for Name: course_prerequisites; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_prerequisites (course_id, prerequisite_id) FROM stdin;
\.


--
-- TOC entry 3419 (class 0 OID 66602)
-- Dependencies: 215
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.courses (course_id, course_code, title, description, credit_hours, semester, academic_year, department_id, is_active, created_at) FROM stdin;
1	CS101	Introduction to Microservices Architecture	A deep dive into the monolithic vs microservice based architecture, pros and cons of both architectures.	4	Summer	2026	3	t	2025-09-26 16:32:48.028321
3	ENG101	Introduction to Civil Engineering	A brief view into civil engineering principles	30	Spring	2025	5	t	2025-09-27 09:47:18.372324
4	AGR101	Introduction to Agriculture	Introduction to Agriculture	30	Fall	2025	6	t	2025-09-29 13:08:08.020521
2	MA101	Introduction to Discrete Structures	A basic study into discrete structures	27	Summer	2025	4	t	2025-09-27 09:12:33.483732
5	CS102	Advanced Microservice Architectures	Advanced Microservice Architectures	30	Fall	2025	3	t	2025-09-29 14:27:47.16487
\.


--
-- TOC entry 3426 (class 0 OID 66661)
-- Dependencies: 222
-- Data for Name: enrollments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.enrollments (enrollment_id, course_id, student_id, enrollment_date, status, grade, created_at) FROM stdin;
\.


--
-- TOC entry 3428 (class 0 OID 66678)
-- Dependencies: 224
-- Data for Name: submissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.submissions (submission_id, content_id, student_id, submission_date, file_path, grade, feedback, graded_by, graded_at, created_at) FROM stdin;
\.


--
-- TOC entry 3442 (class 0 OID 0)
-- Dependencies: 225
-- Name: attendance_records_attendance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.attendance_records_attendance_id_seq', 1, false);


--
-- TOC entry 3443 (class 0 OID 0)
-- Dependencies: 217
-- Name: course_contents_content_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.course_contents_content_id_seq', 1, false);


--
-- TOC entry 3444 (class 0 OID 0)
-- Dependencies: 219
-- Name: course_instructors_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.course_instructors_id_seq', 7, true);


--
-- TOC entry 3445 (class 0 OID 0)
-- Dependencies: 214
-- Name: courses_course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.courses_course_id_seq', 5, true);


--
-- TOC entry 3446 (class 0 OID 0)
-- Dependencies: 221
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.enrollments_enrollment_id_seq', 1, false);


--
-- TOC entry 3447 (class 0 OID 0)
-- Dependencies: 223
-- Name: submissions_submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.submissions_submission_id_seq', 1, false);


--
-- TOC entry 3263 (class 2606 OID 66702)
-- Name: attendance_records attendance_records_course_id_student_id_session_date_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT attendance_records_course_id_student_id_session_date_key UNIQUE (course_id, student_id, session_date);


--
-- TOC entry 3265 (class 2606 OID 66700)
-- Name: attendance_records attendance_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT attendance_records_pkey PRIMARY KEY (attendance_id);


--
-- TOC entry 3241 (class 2606 OID 66638)
-- Name: course_contents course_contents_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_contents
    ADD CONSTRAINT course_contents_pkey PRIMARY KEY (content_id);


--
-- TOC entry 3246 (class 2606 OID 66654)
-- Name: course_instructors course_instructors_course_id_instructor_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_instructors
    ADD CONSTRAINT course_instructors_course_id_instructor_id_key UNIQUE (course_id, instructor_id);


--
-- TOC entry 3248 (class 2606 OID 66652)
-- Name: course_instructors course_instructors_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_instructors
    ADD CONSTRAINT course_instructors_pkey PRIMARY KEY (id);


--
-- TOC entry 3239 (class 2606 OID 66618)
-- Name: course_prerequisites course_prerequisites_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_prerequisites
    ADD CONSTRAINT course_prerequisites_pkey PRIMARY KEY (course_id, prerequisite_id);


--
-- TOC entry 3232 (class 2606 OID 66613)
-- Name: courses courses_course_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_course_code_key UNIQUE (course_code);


--
-- TOC entry 3234 (class 2606 OID 66611)
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (course_id);


--
-- TOC entry 3252 (class 2606 OID 66671)
-- Name: enrollments enrollments_course_id_student_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_course_id_student_id_key UNIQUE (course_id, student_id);


--
-- TOC entry 3254 (class 2606 OID 66669)
-- Name: enrollments enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_pkey PRIMARY KEY (enrollment_id);


--
-- TOC entry 3261 (class 2606 OID 66687)
-- Name: submissions submissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_pkey PRIMARY KEY (submission_id);


--
-- TOC entry 3266 (class 1259 OID 66721)
-- Name: idx_attendance_course_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_attendance_course_id ON public.attendance_records USING btree (course_id);


--
-- TOC entry 3267 (class 1259 OID 66723)
-- Name: idx_attendance_session_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_attendance_session_date ON public.attendance_records USING btree (session_date);


--
-- TOC entry 3268 (class 1259 OID 66722)
-- Name: idx_attendance_student_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_attendance_student_id ON public.attendance_records USING btree (student_id);


--
-- TOC entry 3242 (class 1259 OID 66711)
-- Name: idx_course_contents_course_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_course_contents_course_id ON public.course_contents USING btree (course_id);


--
-- TOC entry 3243 (class 1259 OID 66713)
-- Name: idx_course_contents_created_by; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_course_contents_created_by ON public.course_contents USING btree (created_by);


--
-- TOC entry 3244 (class 1259 OID 66732)
-- Name: idx_course_contents_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_course_contents_type ON public.course_contents USING btree (content_type);


--
-- TOC entry 3249 (class 1259 OID 66714)
-- Name: idx_course_instructors_course_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_course_instructors_course_id ON public.course_instructors USING btree (course_id);


--
-- TOC entry 3250 (class 1259 OID 66715)
-- Name: idx_course_instructors_instructor_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_course_instructors_instructor_id ON public.course_instructors USING btree (instructor_id);


--
-- TOC entry 3235 (class 1259 OID 66710)
-- Name: idx_courses_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_courses_active ON public.courses USING btree (is_active);


--
-- TOC entry 3236 (class 1259 OID 66709)
-- Name: idx_courses_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_courses_code ON public.courses USING btree (course_code);


--
-- TOC entry 3237 (class 1259 OID 66708)
-- Name: idx_courses_department_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_courses_department_id ON public.courses USING btree (department_id);


--
-- TOC entry 3255 (class 1259 OID 66716)
-- Name: idx_enrollments_course_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_enrollments_course_id ON public.enrollments USING btree (course_id);


--
-- TOC entry 3256 (class 1259 OID 66779)
-- Name: idx_enrollments_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_enrollments_status ON public.enrollments USING btree (status);


--
-- TOC entry 3257 (class 1259 OID 66717)
-- Name: idx_enrollments_student_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_enrollments_student_id ON public.enrollments USING btree (student_id);


--
-- TOC entry 3258 (class 1259 OID 66719)
-- Name: idx_submissions_content_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_submissions_content_id ON public.submissions USING btree (content_id);


--
-- TOC entry 3259 (class 1259 OID 66720)
-- Name: idx_submissions_student_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_submissions_student_id ON public.submissions USING btree (student_id);


--
-- TOC entry 3275 (class 2606 OID 66703)
-- Name: attendance_records attendance_records_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attendance_records
    ADD CONSTRAINT attendance_records_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 3271 (class 2606 OID 66639)
-- Name: course_contents course_contents_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_contents
    ADD CONSTRAINT course_contents_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 3272 (class 2606 OID 66655)
-- Name: course_instructors course_instructors_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_instructors
    ADD CONSTRAINT course_instructors_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 3269 (class 2606 OID 66619)
-- Name: course_prerequisites course_prerequisites_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_prerequisites
    ADD CONSTRAINT course_prerequisites_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 3270 (class 2606 OID 66624)
-- Name: course_prerequisites course_prerequisites_prerequisite_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_prerequisites
    ADD CONSTRAINT course_prerequisites_prerequisite_id_fkey FOREIGN KEY (prerequisite_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 3273 (class 2606 OID 66672)
-- Name: enrollments enrollments_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id) ON DELETE CASCADE;


--
-- TOC entry 3274 (class 2606 OID 66688)
-- Name: submissions submissions_content_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_content_id_fkey FOREIGN KEY (content_id) REFERENCES public.course_contents(content_id) ON DELETE CASCADE;


-- Completed on 2025-10-13 10:58:10

--
-- PostgreSQL database dump complete
--

