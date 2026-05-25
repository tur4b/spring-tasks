-- TRAINING TYPES
insert into training_types (name) values ('CARDIO');
insert into training_types (name) values ('STRENGTH');

-- USERS (only rows referenced by trainee/trainer seeds below)
insert into users (active, createdat, deletedat, updatedat, firstname, lastname, password, username)
values
    (true, timestamp '2026-05-21 22:25:43.674585', null, timestamp '2026-05-22 12:06:39.660994', 'Trainee', 'Admin', '$2a$10$LMbuazOB/M5A1.sbg49fieak.l9UhhBiVNa0E4hh2uG.NeAnHW2va', 'trainee.admin'),
    (true, timestamp '2026-05-21 22:25:43.972696', null, timestamp '2026-05-21 22:25:43.972696', 'Trainer-2', 'Trainerrrrr-2', '$2a$10$LMbuazOB/M5A1.sbg49fieak.l9UhhBiVNa0E4hh2uG.NeAnHW2va', 'trainer-2.admin'),
    (true, timestamp '2026-05-22 12:00:03.127727', null, timestamp '2026-05-22 12:00:03.127727', 'Brian', 'Mills', '$2a$10$qMqCA5nNIMqLUgYRhMCbnekaYs1w/jYwp/R9HhLbRWsbnzJ/NPM0e', 'brian.mills1'),
    (true, timestamp '2026-05-21 22:25:43.762584', null, timestamp '2026-05-21 23:05:28.347365', 'Hero', 'Heros', '$2a$10$Yq6rSP3SovpmHV/lNLu3I.HNdPhnsQePD4qiHjS/jeBF/eBOGb9pO', 'hero.heros'),
    (true, timestamp '2026-05-21 22:25:43.926793', null, timestamp '2026-05-22 12:06:40.293999', 'Trainer-1', 'Trainerrrrr-1', '$2a$10$QrcjOEbAUJFAMZ40ErjthOyP1ufvmfwstZOoCAqT80DBehq/ptbQ.', 'trainer-1.trainerrrrr-1');

-- TRAINEES (FK by username)
insert into trainees (active, dateofbirth, createdat, deletedat, updatedat, user_id, address)
values
    (true, date '2004-05-11', timestamp '2026-05-21 22:25:43.822485', null, timestamp '2026-05-21 22:25:43.822485',
     (select id from users where username = 'trainee.admin'), 'Baku'),
    (true, date '1200-05-11', timestamp '2026-05-21 22:25:43.762584', null, timestamp '2026-05-21 23:05:28.347365',
     (select id from users where username = 'hero.heros'), 'Baku'),
    (true, date '1997-06-15', timestamp '2026-05-22 12:00:03.130581', null, timestamp '2026-05-22 12:00:03.130581',
     (select id from users where username = 'brian.mills1'), 'Baku');

-- TRAINERS (FK by username + type name)
insert into trainers (active, specialization, createdat, deletedat, updatedat, user_id)
values
    (true,  (select id from training_types where name = 'STRENGTH'),
     timestamp '2026-05-21 22:25:43.976697', null, timestamp '2026-05-21 22:25:43.976697',
     (select id from users where username = 'trainer-2.admin')),
    (false, (select id from training_types where name = 'CARDIO'),
     timestamp '2026-05-21 22:25:43.926793', null, timestamp '2026-05-22 12:06:40.293999',
     (select id from users where username = 'trainer-1.trainerrrrr-1'));

-- TRAINER_TRAINEE RELATIONSHIP (lookup by usernames)
insert into trainer_trainee (trainee_id, trainer_id) values
             (
                 (select trn.id from trainees trn join users u on u.id = trn.user_id where u.username = 'trainee.admin'),
                 (select t.id from trainers t join users u on u.id = t.user_id where u.username = 'trainer-1.trainerrrrr-1')
             ),
             (
                 (select trn.id from trainees trn join users u on u.id = trn.user_id where u.username = 'hero.heros'),
                 (select t.id from trainers t join users u on u.id = t.user_id where u.username = 'trainer-2.admin')
             ),
             (
                 (select trn.id from trainees trn join users u on u.id = trn.user_id where u.username = 'trainee.admin'),
                 (select t.id from trainers t join users u on u.id = t.user_id where u.username = 'trainer-2.admin')
             );

-- TRAININGS (FK by usernames + training type name)
insert into trainings (active, date, duration, type_id, createdat, deletedat, trainee_id, trainer_id, updatedat, name)
values
    (
        true, date '2026-05-23', 60,
        (select id from training_types where name = 'CARDIO'),
        timestamp '2026-05-22 12:06:40.757164', null,
        (select trn.id from trainees trn join users u on u.id = trn.user_id where u.username = 'trainee.admin'),
        (select t.id from trainers t join users u on u.id = t.user_id where u.username = 'trainer-1.trainerrrrr-1'),
        timestamp '2026-05-22 12:06:40.757164', 'Morning Session'
    ),
    (
        false, date '2026-05-24', 75,
        (select id from training_types where name = 'CARDIO'),
        timestamp '2026-05-21 22:30:29.493191', null,
        (select trn.id from trainees trn join users u on u.id = trn.user_id where u.username = 'hero.heros'),
        (select t.id from trainers t join users u on u.id = t.user_id where u.username = 'trainer-2.admin'),
        timestamp '2026-05-22 02:11:29.781712', 'Evening Session'
    );