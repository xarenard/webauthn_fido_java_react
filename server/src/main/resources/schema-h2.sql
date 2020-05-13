CREATE SCHEMA APP;
CREATE TYPE key_type AS ENUM('RSA','ECDSA');
CREATE SEQUENCE app.user_seq START 1 INCREMENT 1;

CREATE TABLE app.user(id integer default nextval('app.user_seq'),
                        first_name VARCHAR(250),
                        last_name VARCHAR(250),
                        email VARCHAR(100),
                        PRIMARY KEY(id));

CREATE SEQUENCE app.wa_user_seq START 1 INCREMENT 1;
CREATE TABLE app.wa_user(id integer default nextval('app.wa_user_seq'),
                         user_id integer REFERENCES user(id),
                         wa_id VARCHAR(255),
                         PRIMARY KEY (id));

CREATE SEQUENCE app.wa_credential_seq START 1 INCREMENT 1;
CREATE TABLE app.wa_user_credential(id integer default nextval('app.wa_credential_seq'),
                                wa_id integer REFERENCES wa_user(id),
                                wa_credential_key oid,
                                wa_credential_id VARCHAR(255),
                                wa_credential_creation timestamp,
                                wa_cose_algorithm integer,
                                PRIMARY KEY (id));