-- TRAINING TYPES
INSERT INTO training_types (name) VALUES ('CARDIO');
INSERT INTO training_types (name) VALUES ('STRENGTH');

-- USERS
INSERT INTO users (active, created_at, deleted_at, updated_at, first_name, last_name, password, username)
VALUES
    (true, TIMESTAMP '2026-05-21 22:25:43.674585', null, TIMESTAMP '2026-05-22 12:06:39.660994', 'Trainee', 'Admin', '$2a$10$LMbuazOB/M5A1.sbg49fieak.l9UhhBiVNa0E4hh2uG.NeAnHW2va', 'trainee.admin'),
    (true, TIMESTAMP '2026-05-21 22:25:43.972696', null, TIMESTAMP '2026-05-21 22:25:43.972696', 'Trainer-2', 'Trainerrrrr-2', '$2a$10$LMbuazOB/M5A1.sbg49fieak.l9UhhBiVNa0E4hh2uG.NeAnHW2va', 'trainer-2.admin'),
    (true, TIMESTAMP '2026-05-22 12:00:03.127727', null, TIMESTAMP '2026-05-22 12:00:03.127727', 'Brian', 'Mills', '$2a$10$qMqCA5nNIMqLUgYRhMCbnekaYs1w/jYwp/R9HhLbRWsbnzJ/NPM0e', 'brian.mills1'),
    (true, TIMESTAMP '2026-05-21 22:25:43.762584', null, TIMESTAMP '2026-05-21 23:05:28.347365', 'Hero', 'Heros', '$2a$10$Yq6rSP3SovpmHV/lNLu3I.HNdPhnsQePD4qiHjS/jeBF/eBOGb9pO', 'hero.heros'),
    (true, TIMESTAMP '2026-05-21 22:25:43.926793', null, TIMESTAMP '2026-05-22 12:06:40.293999', 'Trainer-1', 'Trainerrrrr-1', '$2a$10$QrcjOEbAUJFAMZ40ErjthOyP1ufvmfwstZOoCAqT80DBehq/ptbQ.', 'trainer-1.trainerrrrr-1');

-- TRAINEES
INSERT INTO trainees (active, date_of_birth, created_at, deleted_at, updated_at, user_id, address)
VALUES
    (true, DATE '2004-05-11', TIMESTAMP '2026-05-21 22:25:43.822485', null, TIMESTAMP '2026-05-21 22:25:43.822485',
     (SELECT id FROM users WHERE username = 'trainee.admin'), 'Baku'),
    (true, DATE '1200-05-11', TIMESTAMP '2026-05-21 22:25:43.762584', null, TIMESTAMP '2026-05-21 23:05:28.347365',
     (SELECT id FROM users WHERE username = 'hero.heros'), 'Baku'),
    (true, DATE '1997-06-15', TIMESTAMP '2026-05-22 12:00:03.130581', null, TIMESTAMP '2026-05-22 12:00:03.130581',
     (SELECT id FROM users WHERE username = 'brian.mills1'), 'Baku');

-- TRAINERS
INSERT INTO trainers (active, specialization, created_at, deleted_at, updated_at, user_id)
VALUES
    (true,  (SELECT id FROM training_types WHERE name = 'STRENGTH'),
     TIMESTAMP '2026-05-21 22:25:43.976697', null, TIMESTAMP '2026-05-21 22:25:43.976697',
     (SELECT id FROM users WHERE username = 'trainer-2.admin')),
    (false, (SELECT id FROM training_types WHERE name = 'CARDIO'),
     TIMESTAMP '2026-05-21 22:25:43.926793', null, TIMESTAMP '2026-05-22 12:06:40.293999',
     (SELECT id FROM users WHERE username = 'trainer-1.trainerrrrr-1'));

-- TRAINER_TRAINEE RELATIONSHIPS
INSERT INTO trainer_trainee (trainee_id, trainer_id) VALUES
                                                         (
                                                             (SELECT trn.id FROM trainees trn JOIN users u ON u.id = trn.user_id WHERE u.username = 'trainee.admin'),
                                                             (SELECT t.id FROM trainers t JOIN users u ON u.id = t.user_id WHERE u.username = 'trainer-1.trainerrrrr-1')
                                                         ),
                                                         (
                                                             (SELECT trn.id FROM trainees trn JOIN users u ON u.id = trn.user_id WHERE u.username = 'hero.heros'),
                                                             (SELECT t.id FROM trainers t JOIN users u ON u.id = t.user_id WHERE u.username = 'trainer-2.admin')
                                                         ),
                                                         (
                                                             (SELECT trn.id FROM trainees trn JOIN users u ON u.id = trn.user_id WHERE u.username = 'trainee.admin'),
                                                             (SELECT t.id FROM trainers t JOIN users u ON u.id = t.user_id WHERE u.username = 'trainer-2.admin')
                                                         );

-- TRAININGS
INSERT INTO trainings (active, date, duration, type_id, created_at, deleted_at, trainee_id, trainer_id, updated_at, name)
VALUES
    (
        true, DATE '2026-05-23', 60,
        (SELECT id FROM training_types WHERE name = 'CARDIO'),
        TIMESTAMP '2026-05-22 12:06:40.757164', null,
        (SELECT trn.id FROM trainees trn JOIN users u ON u.id = trn.user_id WHERE u.username = 'trainee.admin'),
        (SELECT t.id FROM trainers t JOIN users u ON u.id = t.user_id WHERE u.username = 'trainer-1.trainerrrrr-1'),
        TIMESTAMP '2026-05-22 12:06:40.757164', 'Morning Session'
    ),
    (
        false, DATE '2026-05-24', 75,
        (SELECT id FROM training_types WHERE name = 'CARDIO'),
        TIMESTAMP '2026-05-21 22:30:29.493191', null,
        (SELECT trn.id FROM trainees trn JOIN users u ON u.id = trn.user_id WHERE u.username = 'hero.heros'),
        (SELECT t.id FROM trainers t JOIN users u ON u.id = t.user_id WHERE u.username = 'trainer-2.admin'),
        TIMESTAMP '2026-05-22 02:11:29.781712', 'Evening Session'
    );