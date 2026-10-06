-- Schema is WRITTEN by the Python pipeline and only READ by this backend.
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Videos are only referenced via TikTok embed: we keep the URL and metadata, never video files.
CREATE TABLE video (
    tiktok_id      text PRIMARY KEY,
    web_video_url  text        NOT NULL,
    author_hash    text        NOT NULL,   -- hashed by the pipeline; raw usernames must never be stored
    created_at     timestamptz NOT NULL,
    play_count     bigint      NOT NULL DEFAULT 0 CHECK (play_count >= 0),
    like_count     bigint      NOT NULL DEFAULT 0 CHECK (like_count >= 0),
    save_count     bigint      NOT NULL DEFAULT 0 CHECK (save_count >= 0),
    comment_count  bigint      NOT NULL DEFAULT 0 CHECK (comment_count >= 0),
    share_count    bigint      NOT NULL DEFAULT 0 CHECK (share_count >= 0),
    language       text,
    embed_status   text        NOT NULL DEFAULT 'unknown'
);

-- Identity is goong_place_id + coordinates, never the (changing) address string.
CREATE TABLE place (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name           text        NOT NULL,
    category       text        NOT NULL CHECK (category IN ('eat', 'play', 'stay', 'transport')),
    city           text        NOT NULL,
    address        text,
    geom           geography(Point, 4326) NOT NULL,
    goong_place_id text,
    energy         real        NOT NULL DEFAULT 0.5 CHECK (energy BETWEEN 0 AND 1),
    tags           text[]      NOT NULL DEFAULT '{}',
    best_for       text[]      NOT NULL DEFAULT '{}',
    price_min      integer     CHECK (price_min >= 0),
    price_max      integer     CHECK (price_max >= 0),
    quality_score  real        NOT NULL DEFAULT 0 CHECK (quality_score BETWEEN 0 AND 1),
    mention_count  integer     NOT NULL DEFAULT 0 CHECK (mention_count >= 0),
    creator_count  integer     NOT NULL DEFAULT 0 CHECK (creator_count >= 0),
    resolve_status text        NOT NULL DEFAULT 'unverified'
                               CHECK (resolve_status IN ('matched', 'ambiguous', 'unverified')),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT place_price_range CHECK (price_min IS NULL OR price_max IS NULL OR price_max >= price_min),
    CONSTRAINT place_matched_has_goong_id CHECK (resolve_status <> 'matched' OR goong_place_id IS NOT NULL)
);

CREATE UNIQUE INDEX place_goong_place_id_uq ON place (goong_place_id) WHERE goong_place_id IS NOT NULL;
CREATE INDEX place_geom_gist ON place USING GIST (geom);
CREATE INDEX place_tags_gin ON place USING GIN (tags);
CREATE INDEX place_best_for_gin ON place USING GIN (best_for);
CREATE INDEX place_city_category_idx ON place (city, category) WHERE resolve_status = 'matched';

-- One row per place mention extracted from a video by an LLM run. place_id stays NULL until resolved.
CREATE TABLE mention (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id         text    NOT NULL REFERENCES video (tiktok_id) ON DELETE CASCADE,
    place_id         uuid    REFERENCES place (id) ON DELETE SET NULL,
    name_raw         text    NOT NULL,
    category         text    CHECK (category IN ('eat', 'play', 'stay', 'transport')),
    sentiment        text,
    sponsored_signal boolean NOT NULL DEFAULT false,
    evidence         jsonb   NOT NULL,
    prompt_version   text    NOT NULL,
    model            text    NOT NULL,
    source_version   text    NOT NULL
);

CREATE INDEX mention_video_idx ON mention (video_id);
CREATE INDEX mention_place_idx ON mention (place_id) WHERE place_id IS NOT NULL;

-- Every fact keeps its evidence: video + start timestamp + observed date.
CREATE TABLE place_fact (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    place_id          uuid        NOT NULL REFERENCES place (id) ON DELETE CASCADE,
    type              text        NOT NULL,
    value             jsonb       NOT NULL,
    evidence_video_id text        NOT NULL REFERENCES video (tiktok_id),
    t_start_sec       integer     NOT NULL CHECK (t_start_sec >= 0),
    observed_at       timestamptz NOT NULL
);

CREATE INDEX place_fact_place_idx ON place_fact (place_id, type);

CREATE TABLE place_video (
    place_id    uuid    NOT NULL REFERENCES place (id) ON DELETE CASCADE,
    video_id    text    NOT NULL REFERENCES video (tiktok_id) ON DELETE CASCADE,
    rank        integer NOT NULL CHECK (rank >= 1),
    t_start_sec integer NOT NULL DEFAULT 0 CHECK (t_start_sec >= 0),
    PRIMARY KEY (place_id, video_id)
);

CREATE INDEX place_video_rank_idx ON place_video (place_id, rank);

CREATE TABLE user_preference (
    user_id      uuid PRIMARY KEY,
    energy       real   NOT NULL CHECK (energy BETWEEN 0 AND 1),
    budget_level text   NOT NULL CHECK (budget_level IN ('low', 'medium', 'high')),
    tags         text[] NOT NULL DEFAULT '{}',
    party_type   text   CHECK (party_type IN ('solo', 'couple', 'friends', 'family'))
);
