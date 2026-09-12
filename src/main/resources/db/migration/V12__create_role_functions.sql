CREATE TABLE role_functions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id BIGINT NOT NULL,
    function_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_role_functions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_functions_function
        FOREIGN KEY (function_id)
        REFERENCES functions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_role_functions
        UNIQUE (role_id, function_id)
);

CREATE INDEX idx_role_functions_role_id
    ON role_functions(role_id);

CREATE INDEX idx_role_functions_function_id
    ON role_functions(function_id);
