INSERT INTO mochi_project_test4.GoalTemplate (id, type)
VALUES 
    (1, 'work'),
    (2, 'study'),
    (3, 'gym'),
    (4, 'rest')
ON CONFLICT (id) DO NOTHING;

INSERT INTO mochi_project_test4.PersonalGoal (id, user_id, title, description, completed, progress, quantity)
VALUES 
    (101, 1, 'Academic Excellence', 'Master advanced database systems and algorithms.', FALSE, 0, 2),
    (102, 1, 'Career Advancement', 'Deliver high-value milestones and improve software architecture workflows.', FALSE, 0, 3),
    (103, 1, 'Physical Fitness', 'Build a stronger, healthier body through regular physical training.', FALSE, 0, 4)
ON CONFLICT (id) DO NOTHING;


INSERT INTO mochi_project_test4.DailyGoal (id, date, user_id, person_goal_id, quantity, title, description, completed, goalTemplateId, progress)
VALUES 
    (201, '2026-06-16', 1, 101, 4.0, 'Study Database Internals', 'Dedicate 4 hours to indexing and query parsing deep dives.', FALSE, 2, 0),
    
    (202, '2026-06-16', 1, 102, 5.0, 'Feature Engineering Dev Work', 'Focus 5 hours on backend optimization tasks.', FALSE, 1, 0),
    
    (203, '2026-06-16', 1, 103, 1.0, 'Weightlifting Training Session', 'Spend 1 hour completing the compound heavy lifting routine.', FALSE, 3, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO mochi_project_test4.Achievement (id, title, description, type, threshold, xp) VALUES
(1, 'First Step Forward', 'Mark your very first scheduled activity as completed.', 'activity', 1, 50),
(2, 'Deep Focus', 'Complete 10 study or work activities.', 'activity', 10, 250),
(3, 'Hyper Focus', 'Complete 50 study or work activities.', 'activity', 50, 1000),
(4, 'Hat Trick', 'Complete all 3 of your subgoals in a single day.', 'daily_goal', 1, 150),
(5, 'Goal Getter', 'Complete a total of 30 daily subgoals.', 'daily_goal', 30, 400),
(6, 'Consistency is Key', 'Reach a 7-day daily goal streak.', 'daily_goal', 7, 500);