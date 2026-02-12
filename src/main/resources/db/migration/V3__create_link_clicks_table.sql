CREATE TABLE link_clicks (
                             id BIGSERIAL PRIMARY KEY,
                             link_id BIGINT NOT NULL,
                             clicked_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             ip_address VARCHAR(45),
                             CONSTRAINT fk_link FOREIGN KEY (link_id) REFERENCES short_links(id) ON DELETE CASCADE
);