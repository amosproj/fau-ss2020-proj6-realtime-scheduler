-- Table: public."group"

-- DROP TABLE public."group";

CREATE TABLE public."group"
(
    id character varying(128) COLLATE pg_catalog."default" NOT NULL,
    parent_id character varying(128) COLLATE pg_catalog."default",
    priority integer NOT NULL,
    active_times jsonb,
    working_days integer[],
    type_flag character varying COLLATE pg_catalog."default" NOT NULL,
    mode character varying COLLATE pg_catalog."default" NOT NULL,
    paused boolean NOT NULL,
    last_index_number bigint,
    parallelism_degree integer,
    CONSTRAINT group_pkey PRIMARY KEY (id),
    CONSTRAINT parent_fk FOREIGN KEY (parent_id)
        REFERENCES public."group" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public."group"
    OWNER to realtimescheduler;



-- Table: public.task

-- DROP TABLE public.task;

CREATE TABLE public.task
(
    id character varying(36) COLLATE pg_catalog."default" NOT NULL,
    group_id character varying(128) COLLATE pg_catalog."default" NOT NULL,
    priority integer NOT NULL,
    active_times jsonb,
    working_days integer[],
    type_flag character varying COLLATE pg_catalog."default" NOT NULL,
    mode character varying COLLATE pg_catalog."default" NOT NULL,
    retries integer,
    paused boolean NOT NULL,
    force boolean NOT NULL,
    index_number bigint,
    parallelism_degree integer,
    meta_data jsonb,
    CONSTRAINT task_pkey PRIMARY KEY (id),
    CONSTRAINT group_fk FOREIGN KEY (group_id)
        REFERENCES public."group" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.task
    OWNER to realtimescheduler;