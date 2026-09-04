-- ================================================
-- Sunrise Dental Clinic Management System
-- Stored Procedures
-- ================================================

USE sunrise_dental_db;

DELIMITER //

-- ================================================
-- Procedure 1: Get Daily Appointments
-- ================================================
CREATE OR REPLACE PROCEDURE sp_get_daily_appointments(
    IN p_date DATE
)
BEGIN
SELECT
    a.appointment_number,
    a.appointment_time,
    p.full_name as patient_name,
    d.full_name as dentist_name,
    t.treatment_name,
    a.status,
    a.notes
FROM appointments a
         JOIN patients p ON a.patient_id = p.patient_id
         JOIN dentists d ON a.dentist_id = d.dentist_id
         JOIN treatments t ON a.treatment_id = t.treatment_id
WHERE a.appointment_date = p_date
ORDER BY a.appointment_time;
END //

-- ================================================
-- Procedure 2: Get Dentist Appointments
-- ================================================
CREATE OR REPLACE PROCEDURE sp_get_dentist_appointments(
    IN p_dentist_id INT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
SELECT
    a.appointment_number,
    a.appointment_date,
    a.appointment_time,
    p.full_name as patient_name,
    t.treatment_name,
    a.status
FROM appointments a
         JOIN patients p ON a.patient_id = p.patient_id
         JOIN treatments t ON a.treatment_id = t.treatment_id
WHERE a.dentist_id = p_dentist_id
  AND a.appointment_date BETWEEN p_start_date AND p_end_date
ORDER BY a.appointment_date, a.appointment_time;
END //

-- ================================================
-- Procedure 3: Generate Revenue Report
-- ================================================
CREATE OR REPLACE PROCEDURE sp_generate_revenue_report(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
SELECT
    t.treatment_name,
    COUNT(a.appointment_id) as treatment_count,
    SUM(b.total_amount) as total_revenue,
    AVG(b.total_amount) as average_amount
FROM treatments t
         JOIN appointments a ON t.treatment_id = a.treatment_id
         JOIN bills b ON a.appointment_id = b.appointment_id
WHERE b.bill_date BETWEEN p_start_date AND p_end_date
  AND b.payment_status = 'PAID'
GROUP BY t.treatment_id, t.treatment_name
ORDER BY total_revenue DESC;
END //

-- ================================================
-- Procedure 4: Check Dentist Availability
-- ================================================
CREATE OR REPLACE PROCEDURE sp_check_dentist_availability(
    IN p_dentist_id INT,
    IN p_date DATE,
    IN p_time TIME
)
BEGIN
SELECT COUNT(*) as appointment_count
FROM appointments a
WHERE a.dentist_id = p_dentist_id
  AND a.appointment_date = p_date
  AND a.appointment_time = p_time
  AND a.status NOT IN ('CANCELLED', 'NO_SHOW');
END //

-- ================================================
-- Procedure 5: Get Patient Appointment History
-- ================================================
CREATE OR REPLACE PROCEDURE sp_get_patient_history(
    IN p_patient_id INT
)
BEGIN
SELECT
    a.appointment_number,
    a.appointment_date,
    a.appointment_time,
    d.full_name as dentist_name,
    t.treatment_name,
    a.status,
    b.bill_number,
    b.total_amount,
    b.payment_status
FROM appointments a
         JOIN dentists d ON a.dentist_id = d.dentist_id
         JOIN treatments t ON a.treatment_id = t.treatment_id
         LEFT JOIN bills b ON a.appointment_id = b.appointment_id
WHERE a.patient_id = p_patient_id
ORDER BY a.appointment_date DESC;
END //

-- ================================================
-- Procedure 6: Generate Monthly Summary
-- ================================================
CREATE OR REPLACE PROCEDURE sp_monthly_summary(
    IN p_month INT,
    IN p_year INT
)
BEGIN
SELECT
    'Total Appointments' as metric,
    COUNT(*) as value
FROM appointments
WHERE MONTH(appointment_date) = p_month
  AND YEAR(appointment_date) = p_year
UNION ALL
SELECT
    'Completed Appointments',
    COUNT(*)
FROM appointments
WHERE MONTH(appointment_date) = p_month
  AND YEAR(appointment_date) = p_year
  AND status = 'COMPLETED'
UNION ALL
SELECT
    'Total Revenue',
    IFNULL(SUM(total_amount), 0)
FROM bills
WHERE MONTH(bill_date) = p_month
  AND YEAR(bill_date) = p_year
  AND payment_status = 'PAID'
UNION ALL
SELECT
    'New Patients',
    COUNT(*)
FROM patients
WHERE MONTH(registration_date) = p_month
  AND YEAR(registration_date) = p_year;
END //

-- ================================================
-- Procedure 7: Update Appointment Status with Audit
-- ================================================
CREATE OR REPLACE PROCEDURE sp_update_appointment_status(
    IN p_appointment_id INT,
    IN p_new_status VARCHAR(20),
    IN p_user_id INT
)
BEGIN
    DECLARE v_old_status VARCHAR(20);

    -- Get current status
SELECT status INTO v_old_status
FROM appointments
WHERE appointment_id = p_appointment_id;

-- Update appointment
UPDATE appointments
SET status = p_new_status,
    updated_at = CURRENT_TIMESTAMP
WHERE appointment_id = p_appointment_id;

-- Insert audit log
INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
VALUES (
           p_user_id,
           'UPDATE_STATUS',
           'APPOINTMENT',
           p_appointment_id,
           CONCAT('status=', v_old_status),
           CONCAT('status=', p_new_status)
       );
END //

DELIMITER ;

-- Show confirmation
SELECT 'Stored procedures created successfully!' AS message;