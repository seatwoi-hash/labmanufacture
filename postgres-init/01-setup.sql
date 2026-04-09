-- 01-setup.sql
-- Настройка прав для Nextcloud

-- Сначала убедимся, что база данных существует
-- Если нет - создаем ее
SELECT 'CREATE DATABASE nextcloud_db OWNER nextcloud'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'nextcloud_db')\gexec

-- Подключаемся к базе данных
\c nextcloud_db

-- Даем все права на схему public
GRANT ALL ON SCHEMA public TO nextcloud;
GRANT ALL PRIVILEGES ON SCHEMA public TO nextcloud;
GRANT CREATE ON SCHEMA public TO nextcloud;

-- Делаем пользователя владельцем схемы
ALTER SCHEMA public OWNER TO nextcloud;

-- Устанавливаем права по умолчанию для новых таблиц
ALTER DEFAULT PRIVILEGES FOR USER nextcloud IN SCHEMA public
    GRANT ALL ON TABLES TO nextcloud;
ALTER DEFAULT PRIVILEGES FOR USER nextcloud IN SCHEMA public
    GRANT ALL ON SEQUENCES TO nextcloud;
ALTER DEFAULT PRIVILEGES FOR USER nextcloud IN SCHEMA public
    GRANT ALL ON FUNCTIONS TO nextcloud;

-- Создаем необходимые расширения
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Выводим информацию для отладки
\du
\dn+
