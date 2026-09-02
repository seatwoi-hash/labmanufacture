import os
from urllib.parse import quote_plus

SECRET_KEY = os.environ["SUPERSET_SECRET_KEY"]

db_user = quote_plus(os.getenv("SUPERSET_DB_USER", "superset"))
db_password = quote_plus(os.getenv("SUPERSET_DB_PASSWORD", "superset123"))
db_host = os.getenv("SUPERSET_DB_HOST", "superset-db")
db_name = os.getenv("SUPERSET_DB_NAME", "superset")

SQLALCHEMY_DATABASE_URI = (
    f"postgresql+psycopg2://"
    f"{db_user}:{db_password}@"
    f"{db_host}:5432/"
    f"{db_name}"
)

WTF_CSRF_ENABLED = True
SESSION_COOKIE_HTTPONLY = True
SESSION_COOKIE_SAMESITE = "Lax"

FEATURE_FLAGS = {
    "ENABLE_TEMPLATE_PROCESSING": True,
}
