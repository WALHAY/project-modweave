-- foreign keys
CREATE INDEX idx_mods_game_id
ON modweave.mods (game_id);

CREATE INDEX idx_mods_publisher_id
ON modweave.mods (publisher_id);

CREATE INDEX idx_mod_versions_mod_id
ON modweave.mod_versions (mod_id);

CREATE INDEX idx_mod_files_mod_version_id
ON modweave.mod_files (mod_version_id);

CREATE INDEX idx_mods_categories_category_name
ON modweave.mods_categories (category_name);

CREATE INDEX idx_comments_user_id
ON modweave.comments (user_id);

CREATE INDEX idx_comments_mod_id
ON modweave.comments (mod_id);

CREATE INDEX idx_comments_parent_comment_id
ON modweave.comments (parent_comment_id);

CREATE INDEX idx_collections_owner
ON modweave.collections (owner);

CREATE INDEX idx_collections_mods_mod_id
ON modweave.collections_mods (mod_id);

-- sort/filter
CREATE INDEX idx_mods_creation_date
ON modweave.mods (creation_date DESC);

CREATE INDEX idx_mod_versions_upload_date
ON modweave.mod_versions (upload_date DESC);

CREATE INDEX idx_mod_versions_status
ON modweave.mod_versions (status);

CREATE INDEX idx_comments_publish_date
ON modweave.comments (publish_date DESC);

CREATE INDEX idx_mod_files_downloads
ON modweave.mod_files (downloads DESC);

-- composite
CREATE INDEX idx_mod_versions_mod_status
ON modweave.mod_versions (mod_id, status);

CREATE INDEX idx_comments_mod_publish_date
ON modweave.comments (mod_id, publish_date DESC);

CREATE INDEX idx_collections_mods_collection_order
ON modweave.collections_mods (collection_id, order_index);
