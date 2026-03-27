#!/bin/bash
set -e

# Đợi DB sẵn sàng
sleep 5

# Chạy psql để tạo extension và cấp quyền
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Enable PostGIS extension
    CREATE EXTENSION IF NOT EXISTS postgis;

    -- Enable PostGIS Topology (optional)
    CREATE EXTENSION IF NOT EXISTS postgis_topology;

    -- Full-Text Search extensions
    CREATE EXTENSION IF NOT EXISTS pg_trgm;      -- Fuzzy search (similarity matching)
    CREATE EXTENSION IF NOT EXISTS unaccent;      -- Bỏ dấu tiếng Việt khi search

    -- LƯU Ý: $POSTGRES_USER là user container mặc định,
    -- cần đảm bảo user này có quyền. Ta dùng nó để tạo.

    -- Cấp quyền cho user này trên schema public
    GRANT ALL ON ALL TABLES IN SCHEMA public TO "$POSTGRES_USER";
    GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO "$POSTGRES_USER";
    GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO "$POSTGRES_USER";

    -- Cấp quyền cho user này trên schema public (cho các đối tượng mới)
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO "$POSTGRES_USER";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO "$POSTGRES_USER";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO "$POSTGRES_USER";

    RAISE NOTICE 'PostGIS extensions and permissions configured for user %', '$POSTGRES_USER';
EOSQL