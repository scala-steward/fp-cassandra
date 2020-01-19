CREATE TABLE IF NOT EXISTS notebook_keyspace.notes (
  id varchar,
  title varchar,
  content text,
  author_id varchar,
  is_active boolean,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS notebook_keyspace.requests (
  id varchar,
  action varchar,
  request_time timestamp,
  response_time timestamp,
  status int,
  PRIMARY KEY (id)
);
