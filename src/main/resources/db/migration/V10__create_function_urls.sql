CREATE TABLE function_urls (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    function_id BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    can_create BOOLEAN NOT NULL DEFAULT FALSE,
    can_read BOOLEAN NOT NULL DEFAULT FALSE,
    can_update BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_function_urls_function
        FOREIGN KEY (function_id)
        REFERENCES functions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_function_urls
        UNIQUE (function_id, url, http_method)
);

CREATE INDEX idx_function_urls_function_id
    ON function_urls(function_id);

CREATE INDEX idx_function_urls_url
    ON function_urls(url);
