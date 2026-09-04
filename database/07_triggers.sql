-- ================================================
-- Sunrise Dental Clinic Management System
-- Triggers
-- ================================================

USE sunrise_dental_db;

DELIMITER //

-- ================================================
-- Trigger 1: Auto-generate Appointment Number
-- ================================================
CREATE OR REPLACE TRIGGER before_insert_appointment
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    IF NEW.appointment_number IS NULL OR NEW.appointment_number = '' THEN
        SET NEW.appointment_number = CONCAT('A', LPAD((
            SELECT IFNULL(MAX(CAST(SUBSTRING(appointment_number, 2) AS UNSIGNED)), 0) + 1
            FROM appointments
        ), 4, '0'));
END IF;
END //

-- ================================================
-- Trigger 2: Auto-generate Bill Number
-- ================================================
CREATE OR REPLACE TRIGGER before_insert_bill
BEFORE INSERT ON bills
FOR EACH ROW
BEGIN
    IF NEW.bill_number IS NULL OR NEW.bill_number = '' THEN
        SET NEW.bill_number = CONCAT('B', LPAD((
            SELECT IFNULL(MAX(CAST(SUBSTRING(bill_number, 2) AS UNSIGNED)), 0) + 1
            FROM bills
        ), 4, '0'));
END IF;
END //

-- ================================================
-- Trigger 3: Auto-generate Patient Number
-- ================================================
CREATE OR REPLACE TRIGGER before_insert_patient
BEFORE INSERT ON patients
FOR EACH ROW
BEGIN
    IF NEW.patient_number IS NULL OR NEW.patient_number = '' THEN
        SET NEW.patient_number = CONCAT('P', LPAD((
            SELECT IFNULL(MAX(CAST(SUBSTRING(patient_number, 2) AS UNSIGNED)), 0) + 1
            FROM patients
        ), 4, '0'));
END IF;

    IF NEW.registration_date IS NULL THEN
        SET NEW.registration_date = CURDATE();
END IF;
END //

-- ================================================
-- Trigger 4: Auto-generate Payment Number
-- ================================================
CREATE OR REPLACE TRIGGER before_insert_payment
BEFORE INSERT ON payments
FOR EACH ROW
BEGIN
    IF NEW.payment_number IS NULL OR NEW.payment_number = '' THEN
        SET NEW.payment_number = CONCAT('PAY', LPAD((
            SELECT IFNULL(MAX(CAST(SUBSTRING(payment_number, 4) AS UNSIGNED)), 0) + 1
            FROM payments
        ), 4, '0'));
END IF;

    IF NEW.payment_date IS NULL THEN
        SET NEW.payment_date = NOW();
END IF;
END //

-- ================================================
-- Trigger 5: Update Bill Total on Payment
-- ================================================
CREATE OR REPLACE TRIGGER after_insert_payment
AFTER INSERT ON payments
FOR EACH ROW
BEGIN
    DECLARE v_total_paid DECIMAL(10,2);
    DECLARE v_bill_total DECIMAL(10,2);

    -- Calculate total paid for the bill
SELECT SUM(amount) INTO v_total_paid
FROM payments
WHERE bill_id = NEW.bill_id;

-- Get bill total
SELECT total_amount INTO v_bill_total
FROM bills
WHERE bill_id = NEW.bill_id;

-- Update bill payment status
IF v_total_paid >= v_bill_total THEN
UPDATE bills
SET payment_status = 'PAID',
    paid_amount = v_total_paid,
    updated_at = CURRENT_TIMESTAMP
WHERE bill_id = NEW.bill_id;
ELSEIF v_total_paid > 0 THEN
UPDATE bills
SET payment_status = 'PARTIAL',
    paid_amount = v_total_paid,
    updated_at = CURRENT_TIMESTAMP
WHERE bill_id = NEW.bill_id;
END IF;
END //

-- ================================================
-- Trigger 6: Audit Log for Patient Updates
-- ================================================
CREATE OR REPLACE TRIGGER after_update_patient
AFTER UPDATE ON patients
                     FOR EACH ROW
BEGIN
INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
VALUES (
           NULL,
           'UPDATE',
           'PATIENT',
           NEW.patient_id,
           CONCAT_WS(', ',
                     IF(OLD.full_name != NEW.full_name, CONCAT('full_name=', OLD.full_name), NULL),
                     IF(OLD.contact_number != NEW.contact_number, CONCAT('contact=', OLD.contact_number), NULL),
                     IF(OLD.address != NEW.address, CONCAT('address=', OLD.address), NULL)
           ),
           CONCAT_WS(', ',
                     IF(OLD.full_name != NEW.full_name, CONCAT('full_name=', NEW.full_name), NULL),
                     IF(OLD.contact_number != NEW.contact_number, CONCAT('contact=', NEW.contact_number), NULL),
                     IF(OLD.address != NEW.address, CONCAT('address=', NEW.address), NULL)
           )
       );
END //

DELIMITER ;

-- Show confirmation
SELECT 'Triggers created successfully!' AS message;