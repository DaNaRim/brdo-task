-- password is '12345678'
INSERT INTO users (email, is_email_verified, password)
VALUES ('test@test.test', TRUE, '$2a$11$I7Ig6LYL4eKb7rS0g0MgJOlsCmjqWcr0lLlQ2/bij6cCrdyhekNZS')
    ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
VALUES (1, 1)
    ON CONFLICT DO NOTHING;
