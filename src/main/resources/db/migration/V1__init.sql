CREATE TABLE measure (
  time           TIMESTAMPTZ       NOT NULL,
  container      TEXT              NOT NULL,
  content_info   TEXT              NOT NULL,
  content        DOUBLE PRECISION  NOT NULL
);

SELECT create_hypertable('measure', 'time');