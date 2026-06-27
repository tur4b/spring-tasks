-- SEQUENCES
CREATE SEQUENCE IF NOT EXISTS training_types_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS users_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS trainees_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS trainers_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS trainings_id_seq START 1 INCREMENT 1;

-- 1) Lookup tables
CREATE TABLE IF NOT EXISTS training_types (
                                              id INTEGER DEFAULT nextval('training_types_id_seq') PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT chk_training_types_name
    CHECK (name IN ('CARDIO', 'STRENGTH'))
    );

-- 2) Users
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT DEFAULT nextval('users_id_seq') PRIMARY KEY,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    deleted_at TIMESTAMP(6),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
    );

-- 3) Trainees
CREATE TABLE IF NOT EXISTS trainees (
                                        id BIGINT DEFAULT nextval('trainees_id_seq') PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    active BOOLEAN NOT NULL,
    address VARCHAR(255),
    date_of_birth DATE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    deleted_at TIMESTAMP(6),
    CONSTRAINT fk_trainees_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- 4) Trainers
CREATE TABLE IF NOT EXISTS trainers (
                                        id BIGINT DEFAULT nextval('trainers_id_seq') PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialization INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    deleted_at TIMESTAMP(6),
    CONSTRAINT fk_trainers_user
    FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_trainers_training_type
    FOREIGN KEY (specialization) REFERENCES training_types(id)
    );

-- 5) Many-to-many: trainer <-> trainee
CREATE TABLE IF NOT EXISTS trainer_trainee (
                                               trainer_id BIGINT NOT NULL,
                                               trainee_id BIGINT NOT NULL,
                                               CONSTRAINT pk_trainer_trainee PRIMARY KEY (trainer_id, trainee_id),
    CONSTRAINT fk_trainer_trainee_trainer
    FOREIGN KEY (trainer_id) REFERENCES trainers(id),
    CONSTRAINT fk_trainer_trainee_trainee
    FOREIGN KEY (trainee_id) REFERENCES trainees(id)
    );

-- 6) Trainings
CREATE TABLE IF NOT EXISTS trainings (
                                         id BIGINT DEFAULT nextval('trainings_id_seq') PRIMARY KEY,
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    type_id INTEGER NOT NULL,
    name VARCHAR(150) NOT NULL,
    date DATE NOT NULL,
    duration INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    deleted_at TIMESTAMP(6),
    CONSTRAINT fk_trainings_trainee
    FOREIGN KEY (trainee_id) REFERENCES trainees(id),
    CONSTRAINT fk_trainings_trainer
    FOREIGN KEY (trainer_id) REFERENCES trainers(id),
    CONSTRAINT fk_trainings_type
    FOREIGN KEY (type_id) REFERENCES training_types(id)
    );
