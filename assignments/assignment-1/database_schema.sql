CREATE TABLE Person (
    user_id INT PRIMARY KEY,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    level INT DEFAULT 1 CHECK (level >= 1),
    streak INT DEFAULT 0 CHECK (streak >= 0),
    xp INT DEFAULT 0 CHECK (xp >= 0),
    layout INT,
    background INT
);

CREATE TABLE MochiEntity (
    user_id INT PRIMARY KEY,
    name TEXT,
    color INT,
    evolution_stage INT,
    costume INT,
    FOREIGN KEY (user_id) REFERENCES Person (user_id)
);

CREATE TABLE IdealDay (
    user_id INT PRIMARY KEY,
    max_working_hours INT CHECK (max_working_hours >= 0 AND max_working_hours <= 24),
    wake_up_time DATE,
    bedtime DATE,
    FOREIGN KEY (user_id) REFERENCES Person (user_id)
);

CREATE TABLE Friendship (
    friend1_id INT,
    friend2_id INT,
    accepted BOOLEAN,
    PRIMARY KEY (friend1_id, friend2_id),
    FOREIGN KEY (friend1_id) REFERENCES Person (user_id),
    FOREIGN KEY (friend2_id) REFERENCES Person (user_id)
);

CREATE TABLE Timeslot (
    start_date DATE,
    end_date DATE,
    PRIMARY KEY (start_date, end_date),
    CONSTRAINT check_dates CHECK (end_date >= start_date)
);

CREATE TABLE Activity (
    id INT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    completed BOOLEAN DEFAULT FALSE,
    type INT,
    creator_id INT,
    start_date DATE,
    end_date DATE,
    FOREIGN KEY (creator_id) REFERENCES Person (user_id),
    FOREIGN KEY (start_date, end_date) REFERENCES Timeslot (start_date, end_date)
);

CREATE TABLE Achievement (
    id INT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    type INT,
    threshold INT CHECK (threshold > 0),
    xp INT CHECK (xp >= 0)
);

CREATE TABLE AchievementAssociation (
    user_id INT,
    achievement_id INT,
    PRIMARY KEY (user_id, achievement_id),
    FOREIGN KEY (user_id) REFERENCES Person (user_id),
    FOREIGN KEY (achievement_id) REFERENCES Achievement (id)
);

CREATE TABLE Challenge (
    id INT PRIMARY KEY,
    description TEXT
);

CREATE TABLE ChallengeParticipation (
    user_id INT,
    challenge_id INT,
    PRIMARY KEY (user_id, challenge_id),
    FOREIGN KEY (user_id) REFERENCES Person (user_id),
    FOREIGN KEY (challenge_id) REFERENCES Challenge (id)
);

CREATE TABLE Goal (
    id INT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    completed BOOLEAN DEFAULT FALSE,
    completion_date DATE
);

CREATE TABLE PersonalGoal (
    id INT PRIMARY KEY,
    user_id INT NOT NULL,
    FOREIGN KEY (id) REFERENCES Goal (id),
    FOREIGN KEY (user_id) REFERENCES Person (user_id)
);

CREATE TABLE ChallengeGoal (
    id INT PRIMARY KEY,
    challenge_id INT NOT NULL,
    FOREIGN KEY (id) REFERENCES Goal (id),
    FOREIGN KEY (challenge_id) REFERENCES Challenge (id)
);

CREATE TABLE Subgoal (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES Goal (id)
);

CREATE TABLE DailyGoals (
    date DATE,
    user_id INT,
    subgoal_id INT,
    index INT CHECK (index <= 3),
    PRIMARY KEY (date, user_id, subgoal_id),
    FOREIGN KEY (user_id) REFERENCES Person (user_id),
    FOREIGN KEY (subgoal_id) REFERENCES Subgoal (id)
);
