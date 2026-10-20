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
    mood_coefficient NUMERIC,
    FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.IdealDay (
    user_id INT PRIMARY KEY,
    max_working_hours NUMERIC CHECK (max_working_hours >= 0 AND max_working_hours <= 24),
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
    progress NUMERIC NOT NULL DEFAULT 0 CHECK (progress >= 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE
);

CREATE TABLE mochi_project_test4.Friendship (
    sender_id INT,
    receiver_id INT,
    accepted BOOLEAN,
    PRIMARY KEY (sender_id, receiver_id),
    FOREIGN KEY (sender_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_different_users CHECK (sender_id != receiver_id)
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
    user_id INT,
    person_goal_id INT,
    quantity NUMERIC NOT NULL CHECK (quantity > 0),
    title TEXT NOT NULL,
    description TEXT,
    progress NUMERIC NOT NULL DEFAULT 0 CHECK (progress >= 0),
    completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES mochi_project_test4.Person (user_id) ON DELETE CASCADE,
    FOREIGN KEY (person_goal_id) REFERENCES mochi_project_test4.PersonalGoal (id) ON DELETE CASCADE,
    goalTemplateId INT NOT NULL REFERENCES mochi_project_test4.GoalTemplate(id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION mochi_project_test4.initialize_user_achievements()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO mochi_project_test4.AchievementAssociation (user_id, achievement_id, completed)
    SELECT NEW.user_id, id, FALSE FROM mochi_project_test4.achievement;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.handle_challenge_progress_update()
RETURNS TRIGGER AS $$
DECLARE
    target_quantity NUMERIC;
    challenge_is_finished BOOLEAN;
BEGIN
    SELECT completed, quantity INTO challenge_is_finished, target_quantity
    FROM mochi_project_test4.Challenge WHERE id = NEW.challenge_id;

    IF challenge_is_finished THEN
        RETURN OLD;
    END IF;

    IF NEW.progress >= target_quantity THEN
        UPDATE mochi_project_test4.Challenge SET completed = TRUE WHERE id = NEW.challenge_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.handle_activity_completion_by_type()
RETURNS TRIGGER AS $$
DECLARE
    activity_hours      NUMERIC;
    matched_template_id INT;
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN

        SELECT id INTO matched_template_id
        FROM mochi_project_test4.GoalTemplate
        WHERE LOWER(type) = LOWER(NEW.type)
        LIMIT 1;

        IF matched_template_id IS NOT NULL THEN
            activity_hours := EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time)) / 3600.0;

            IF activity_hours > 0 THEN

               FOR r_challenge IN
                   SELECT id FROM mochi_project_test4.Challenge
                   WHERE goalTemplateId = matched_template_id AND completed = FALSE
               LOOP
                    INSERT INTO mochi_project_test4.ChallengeParticipation (user_id, challenge_id, progress)
                    VALUES (NEW.creator_id, r_challenge.id, activity_hours)
                    ON CONFLICT (user_id, challenge_id)
                    DO UPDATE SET progress = mochi_project_test4.ChallengeParticipation.progress + activity_hours;
                END LOOP;

                UPDATE mochi_project_test4.DailyGoal
                SET progress = progress + activity_hours,
                    completed = CASE
                        WHEN (progress + activity_hours) >= quantity THEN TRUE
                        ELSE FALSE
                    END
                WHERE user_id = NEW.creator_id
                  AND date = NEW.activity_date
                  AND goalTemplateId = matched_template_id
                  AND completed = FALSE;

            END IF;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.handle_daily_goal_completion()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN
        IF NEW.person_goal_id IS NOT NULL THEN
            UPDATE mochi_project_test4.PersonalGoal
            SET
                progress = progress + 1,
                completed = CASE
                    WHEN (progress + 1) >= quantity THEN TRUE
                    ELSE FALSE
                END
            WHERE id = NEW.person_goal_id;
                AND completed = FALSE;
        END IF;

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.handle_streak_increment()
RETURNS TRIGGER AS $$
DECLARE
    completed_count INT;
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN

        SELECT COUNT(*) INTO completed_count
        FROM mochi_project_test4.DailyGoal
        WHERE user_id = NEW.user_id
          AND date = NEW.date
          AND completed = TRUE;

        IF completed_count = 3 THEN
            UPDATE mochi_project_test4.Person
            SET streak = streak + 1
            WHERE user_id = NEW.user_id;
        END IF;

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.add_user_xp(target_user_id INT, xp_amount INT)
RETURNS VOID AS $$
BEGIN
    UPDATE mochi_project_test4.Person
    SET xp = xp + xp_amount
    WHERE user_id = target_user_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.trg_fn_activity_xp()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN
        PERFORM mochi_project_test4.add_user_xp(NEW.creator_id, 10);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.trg_fn_daily_goal_xp()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN
        PERFORM mochi_project_test4.add_user_xp(NEW.user_id, 25);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.trg_fn_personal_goal_xp()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN
        PERFORM mochi_project_test4.add_user_xp(NEW.user_id, 50);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.trg_fn_challenge_winner_xp()
RETURNS TRIGGER AS $$
DECLARE
    winner_id INT;
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN

        SELECT user_id INTO winner_id
        FROM mochi_project_test4.ChallengeParticipation
        WHERE challenge_id = NEW.id
        ORDER BY progress DESC
        LIMIT 1;

        IF winner_id IS NOT NULL THEN
            PERFORM mochi_project_test4.add_user_xp(winner_id, 50);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.initialize_default_ideal_day()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO mochi_project_test4.IdealDay (user_id, max_working_hours, wake_up_time, bedtime)
    VALUES (NEW.user_id, 8, '07:00:00'::time, '23:00:00'::time);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.initialize_default_mochi_entity()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO mochi_project_test4.MochiEntity (user_id, name, color, evolution_stage, costume, mood_coefficient)
    VALUES (NEW.user_id, 'Small Mochi', 1, 1, 0, 1.0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.trg_fn_achievement_xp()
RETURNS TRIGGER AS $$
DECLARE
    xp_reward INT;
BEGIN
    IF NEW.completed = TRUE AND (OLD.completed = FALSE OR OLD.completed IS NULL) THEN

        SELECT xp INTO xp_reward
        FROM mochi_project_test4.Achievement
        WHERE id = NEW.achievement_id;

        IF xp_reward IS NOT NULL AND xp_reward > 0 THEN
                PERFORM mochi_project_test4.add_user_xp(NEW.user_id, xp_reward);
        END IF;

    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mochi_project_test4.handle_user_level_calculation()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') OR (NEW.xp IS DISTINCT FROM OLD.xp) THEN
        NEW.level := (NEW.xp / 100) + 1;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION mochi_project_test4.handle_mochi_mood_recount()
RETURNS TRIGGER AS $$
DECLARE
    work_study_count INT;
    rest_gym_count   INT;
    new_coefficient  NUMERIC;
BEGIN
    -- Count the specific activity types for the user who created the activity
    SELECT 
        COUNT(*) FILTER (WHERE LOWER(type) IN ('work', 'study')),
        COUNT(*) FILTER (WHERE LOWER(type) IN ('rest', 'gym'))
    INTO work_study_count, rest_gym_count
    FROM mochi_project_test4.Activity
    WHERE creator_id = NEW.creator_id;

    -- Calculate the coefficient and safeguard against division by zero
    IF rest_gym_count = 0 THEN
        -- If no rest/gym activities exist, we can default to the work/study count 
        -- or 1.0 if work/study is also 0.
        new_coefficient := work_study_count::NUMERIC;
    ELSE
        new_coefficient := work_study_count::NUMERIC / rest_gym_count::NUMERIC;
    END IF;

    -- Update the user's MochiEntity
    UPDATE mochi_project_test4.MochiEntity
    SET mood_coefficient = new_coefficient
    WHERE user_id = NEW.creator_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;




CREATE TRIGGER trg_calculate_user_level
    BEFORE INSERT OR UPDATE OF xp ON mochi_project_test4.Person
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.handle_user_level_calculation();

CREATE TRIGGER trg_auto_create_mochi_entity
    AFTER INSERT ON mochi_project_test4.Person
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.initialize_default_mochi_entity();

CREATE TRIGGER trg_achievement_xp_reward
    AFTER UPDATE OF completed ON mochi_project_test4.AchievementAssociation
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.trg_fn_achievement_xp();

CREATE TRIGGER trg_auto_create_ideal_day
    AFTER INSERT ON mochi_project_test4.Person
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.initialize_default_ideal_day();

CREATE TRIGGER trg_daily_goal_xp_reward
    AFTER UPDATE OF completed ON mochi_project_test4.DailyGoal
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.trg_fn_daily_goal_xp();

CREATE TRIGGER trg_activity_xp_reward
    AFTER UPDATE OF completed ON mochi_project_test4.Activity
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.trg_fn_activity_xp();

CREATE TRIGGER trg_challenge_completed_xp_reward
    AFTER UPDATE OF completed ON mochi_project_test4.Challenge
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.trg_fn_challenge_winner_xp();

CREATE TRIGGER trg_personal_goal_xp_reward
    AFTER UPDATE OF completed ON mochi_project_test4.PersonalGoal
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.trg_fn_personal_goal_xp();

CREATE TRIGGER after_person_created
    AFTER INSERT ON mochi_project_test4.Person
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.initialize_user_achievements();

CREATE TRIGGER trg_challenge_progress_check
    BEFORE UPDATE OF progress ON mochi_project_test4.ChallengeParticipation
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.handle_challenge_progress_update();

CREATE TRIGGER trg_activity_completion_check
    BEFORE UPDATE OF completed ON mochi_project_test4.Activity
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.handle_activity_completion_by_type();

CREATE TRIGGER trg_daily_goal_completion_check
    AFTER UPDATE OF completed ON mochi_project_test4.DailyGoal
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.handle_daily_goal_completion();

CREATE TRIGGER trg_streak_increment_check
    AFTER UPDATE OF completed ON mochi_project_test4.DailyGoal
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.handle_streak_increment();

CREATE TRIGGER trg_activity_added_mood_recount
    AFTER INSERT ON mochi_project_test4.Activity
    FOR EACH ROW
    EXECUTE PROCEDURE mochi_project_test4.handle_mochi_mood_recount();


INSERT INTO mochi_project_test4.Timeslot (start_time, end_time)
SELECT
    slot::time AS start_time,
    CASE
        WHEN slot::time = '23:30:00'::time THEN '23:59:59'::time
        ELSE (slot + INTERVAL '30 minutes')::time
    END AS end_time
FROM generate_series(
    '2026-01-01 00:00:00'::timestamp,
    '2026-01-01 23:30:00'::timestamp,
    '30 minutes'
) AS slot;

