# PostGIS + pgvector in one image. Used by docker-compose and by the Testcontainers integration tests.
# Built on the official postgres image (its PGDG apt repo is already configured) because the
# postgis/postgis:17 image is based on EOL Debian bullseye, whose PGDG repo no longer exists.
FROM postgres:17-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-17-postgis-3 postgresql-17-pgvector \
    && rm -rf /var/lib/apt/lists/*
