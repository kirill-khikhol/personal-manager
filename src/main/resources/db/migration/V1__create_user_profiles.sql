
CREATE TABLE user_profiles (
                               id UUID PRIMARY KEY,
                               time_zone VARCHAR(64) NOT NULL,
                               preferred_language VARCHAR(10) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);