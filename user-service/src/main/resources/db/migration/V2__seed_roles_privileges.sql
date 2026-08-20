INSERT INTO privileges (id, name, description) VALUES
    ('11111111-1111-1111-1111-111111111001', 'USER_READ', 'List and view users'),
    ('11111111-1111-1111-1111-111111111002', 'USER_CREATE', 'Create users'),
    ('11111111-1111-1111-1111-111111111003', 'USER_UPDATE', 'Update users'),
    ('11111111-1111-1111-1111-111111111004', 'USER_DELETE', 'Soft-delete users'),
    ('11111111-1111-1111-1111-111111111005', 'ROLE_READ', 'List and view roles'),
    ('11111111-1111-1111-1111-111111111006', 'ROLE_MANAGE', 'Create, update, and delete roles and role privileges'),
    ('11111111-1111-1111-1111-111111111007', 'PRIVILEGE_READ', 'List privileges'),
    ('11111111-1111-1111-1111-111111111008', 'AUDIT_READ', 'Read the audit log');

INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
    ('22222222-2222-2222-2222-222222222001', 'ADMIN', 'Full user-service administration', NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222002', 'CUSTOMER', 'Self-service profile only', NOW(), NOW());

INSERT INTO role_privileges (role_id, privilege_id)
SELECT '22222222-2222-2222-2222-222222222001', id FROM privileges;
