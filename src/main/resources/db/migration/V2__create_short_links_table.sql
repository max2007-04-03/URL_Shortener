CREATE TABLE short_links (
                             id BIGSERIAL PRIMARY KEY,
                             short_url VARCHAR(8) NOT NULL UNIQUE,
                             original_url VARCHAR(2048) NOT NULL,
                             created_at TIMESTAMP WITHOUT TIME ZONE,
                             expiry_date TIMESTAMP WITHOUT TIME ZONE,
                             visit_count BIGINT DEFAULT 0,
                             user_id BIGINT,
                             CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);