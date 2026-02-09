--liquibase formatted sql

--changeset Tatarinov A:042
--comment Update multiple descriptions and fix duplicate names

-- 1. Обновляем описания
UPDATE operation_statuses
SET description = CASE
                      WHEN name = 'Fail_test' THEN 'Тест не пройден'
                      WHEN name = 'Installation' THEN 'Выводной монтаж №1'
                      WHEN name = 'Fail_test2' THEN 'Тест №2 не пройден'
                      WHEN name = 'Test' THEN 'Тест пройден'
                      WHEN name = 'Test2' THEN 'Тест №2 пройден'
                      WHEN name = 'Fail_quality_check_№5' THEN 'Контроль качества №5 не пройден отправлен в ремонт'
                      ELSE description
    END
WHERE name IN ('Fail_test', 'Installation', 'Fail_test2', 'Test', 'Test2');
--rollback UPDATE operation_statuses
--rollback SET description = CASE
--rollback     WHEN name = 'Fail_test' THEN 'Тестировка'
--rollback     WHEN name = 'Installation' THEN 'Выводной монтаж'
--rollback     WHEN name = 'Fail_test2' THEN 'Тестировка №2 не пройдена'
--rollback     WHEN name = 'Test' THEN 'Тест пройден'
--rollback     WHEN name = 'Test2' THEN 'Тест №2 пройден'
--rollback     WHEN name = 'Fail_quality_check_№5' THEN 'Контроль качества №5 не пройден'
--rollback     ELSE description
--rollback END
--rollback WHERE name IN ('Fail_test', 'Installation', 'Fail_test2', 'Test', 'Test2');

-- 2. Исправляем дублирующиеся имена (Repair и Installation)
UPDATE operation_statuses
SET name = CASE
               WHEN description = 'Ремонт №1' THEN 'Repair1'
               WHEN description = 'Ремонт №2' THEN 'Repair2'
               WHEN description = 'Ремонт №3' THEN 'Repair3'
               WHEN description = 'Выводной монтаж №1' THEN 'Installation1'
               ELSE name
    END
WHERE description IN ('Ремонт №1', 'Ремонт №2', 'Ремонт №3', 'Выводной монтаж №1');
--rollback UPDATE operation_statuses
--rollback SET name = CASE
--rollback     WHEN description = 'Ремонт №1' THEN 'Repair'
--rollback     WHEN description = 'Ремонт №2' THEN 'Repair'
--rollback     WHEN description = 'Ремонт №3' THEN 'Repair'
--rollback     WHEN description = 'Выводной монтаж №1' THEN 'Installation'
--rollback     ELSE name
--rollback END
--rollback WHERE description IN ('Ремонт №1', 'Ремонт №2', 'Ремонт №3', 'Выводной монтаж №1');
