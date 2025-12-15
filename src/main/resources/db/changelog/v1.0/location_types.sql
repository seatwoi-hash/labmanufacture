--liquibase formatted sql

--changeset Tatarinov A:030
--comment Create location_types table
CREATE TABLE location_types
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type_name        VARCHAR(50)                   NOT NULL UNIQUE,
    type_description TEXT,
    is_active        BOOLEAN          DEFAULT TRUE NOT NULL
);
--rollback DROP TABLE location_types CASCADE;

--changeset Tatarinov A:031
--comment Insert default location types
INSERT INTO location_types (type_name, type_description)
VALUES ('склад', 'Складские помещения'),
       ('лаборатория', 'Производственно-испытательная лаборатория');
--rollback DELETE FROM location_types;
