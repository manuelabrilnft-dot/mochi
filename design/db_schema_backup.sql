CREATE TABLE mochi_project_test4.Person (
                                            user_id INT PRIMARY KEY,
                                            username TEXT NOT NULL UNIQUE,
                                            password TEXT NOT NULL,
                                            level INT DEFAULT 1 CHECK (level >= 1),
                                            streak INT DEFAULT 0 CHECK (streak >= 0),
                                            xp INT DEFAULT 0 CHECK (xp >= 0),
                                            layout INT,
                                            background INT
);

CREATE TABLE mochi_project_test4.Timeslot (
                                              start_time TIME WITHOUT TIME ZONE,
                                              end_time TIME WITHOUT TIME ZONE,
                                              PRIMARY KEY (start_time, end_time),
                                              CONSTRAINT check_dates CHECK (end_time >= start_time)
);

CREATE TABLE mochi_project_test4.Achievement (
                                                 id INT PRIMARY KEY,
                                                 title TEXT NOT NULL,
                                                 description TEXT,
                                                 type TEXT,
                                                 threshold INT CHECK (threshold > 0),
                                                 xp INT CHECK (xp >= 0)
);

CREATE TABLE mochi_project_test4.GoalTemplate (
                                                  id INT PRIMARY KEY,
                                                  type TEXT
);

CREATE TABLE mochi_project_test4.Challenge (
                                               id INT PRIMARY KEY,
                                               title TEXT NOT NULL,
                                               description TEXT,
                                               quantity NUMERIC NOT NULL CHECK (quantity > 0),
                                               completed BOOLEAN DEFAULT FALSE,
                                               goalTemplateId INT NOT NULL REFERENCES mochi_project_test4.GoalTemplate(id) ON DELETE CASCADE,
                                               creator_id INT NOT NULL REFERENCES mochi_project_test4.Person(user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.MochiEntity (
                                                 user_id INT PRIMARY KEY,
                                                 name TEXT,
                                                 color INT,
                                                 evolution_stage INT,
                                                 costume INT,
                                                 FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.IdealDay (
                                              user_id INT PRIMARY KEY,
                                              max_working_hours INT CHECK (max_working_hours >= 0 AND max_working_hours <= 24),
                                              wake_up_time TIME WITHOUT TIME ZONE,
                                              bedtime TIME WITHOUT TIME ZONE,
                                              FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.Activity (
                                              id INT PRIMARY KEY,
                                              title TEXT NOT NULL,
                                              description TEXT,
                                              completed BOOLEAN DEFAULT FALSE,
                                              type TEXT,
                                              creator_id INT NOT NULL,
                                              activity_date DATE NOT NULL,
                                              start_time TIME WITHOUT TIME ZONE NOT NULL,
                                              end_time TIME WITHOUT TIME ZONE NOT NULL,
                                              FOREIGN KEY (creator_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
                                              FOREIGN KEY (start_time, end_time) REFERENCES mochi_project_test4.Timeslot (start_time, end_time) ON DELETE RESTRICT,
                                              CONSTRAINT unique_user_activity_timeslot UNIQUE (creator_id, activity_date, start_time, end_time)
);

CREATE TABLE mochi_project_test4.PersonalGoal (
                                                  id INT PRIMARY KEY,
                                                  user_id INT,
                                                  title TEXT NOT NULL,
                                                  description TEXT,
                                                  completed BOOLEAN DEFAULT FALSE,
                                                  quantity NUMERIC NOT NULL CHECK (quantity > 0),
                                                  FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.Friendship (
                                                friend1_id INT,
                                                friend2_id INT,
                                                accepted BOOLEAN,
                                                PRIMARY KEY (friend1_id, friend2_id),
                                                FOREIGN KEY (friend1_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
                                                FOREIGN KEY (friend2_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.AchievementAssociation (
                                                            user_id INT,
                                                            achievement_id INT,
                                                            completed BOOLEAN DEFAULT FALSE,
                                                            PRIMARY KEY (user_id, achievement_id),
                                                            FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
                                                            FOREIGN KEY (achievement_id) REFERENCES mochi_project_test4.Achievement (id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.ChallengeParticipation (
                                                            user_id INT,
                                                            challenge_id INT,
                                                            progress NUMERIC DEFAULT 0 CHECK (progress >= 0),
                                                            PRIMARY KEY (user_id, challenge_id),
                                                            FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
                                                            FOREIGN KEY (challenge_id) REFERENCES mochi_project_test4.Challenge (id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.DailyGoal (
                                               id INT PRIMARY KEY,
                                               date DATE,
                                               user_id