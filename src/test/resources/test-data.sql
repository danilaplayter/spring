INSERT
    INTO
        tasks(
            id,
            title,
            description,
            status,
            priority,
            assignee,
            created_at,
            updated_at
        )
    VALUES(
        '550e8400-e29b-41d4-a716-446655440000',
        'Preloaded Task 1',
        'Description 1',
        'TODO',
        'MEDIUM',
        'user1',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440001',
        'Preloaded Task 2',
        'Description 2',
        'IN_PROGRESS',
        'HIGH',
        'user2',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );