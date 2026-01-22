DELETE FROM user_actions;
DELETE FROM events_similarity;

ALTER TABLE user_actions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE events_similarity ALTER COLUMN id RESTART WITH 1;