-- ================================================
-- Sunrise Dental Clinic Management System
-- Views for Reports and Common Queries
-- ================================================

USE sunrise_dental_db;

-- ================================================
-- View 1: Appointment Details View
-- ================================================
CREATE OR REPLACE VIEW v_appointment_details AS
SELECT
    a.appointment_id,
    a.appointment_number,
    a.appointment_date,
    a.appointment_time,
    a.status,
    a.notes as appointment_notes,
    p.patient_id,
    p.patient_number,
    p.full_name as patient_name,
    p.contact_number as patient_contact,
    d.dentist_id,
    d.dentist_number,
    d.full_name as dentist_name,
    d.specialization,
    t.treatment_id,
    t.treatment_code,
    t.treatment_name,
    t.treatment_cost,
    t.consultation_fee as treatment_consultation_fee
FROM appointments a
         JOIN patients p ON a.patient_id = p.patient_id
         JOIN dentists d ON a.dentist_id = d.dentist_id
         JOIN treatments t ON a.treatment_id = t.treatment_id
WHERE a.status != 'CANCELLED';

-- ================================================
-- View 2: Today's Appointments
-- ================================================
CREATE OR REPLACE VIEW v_today_appointments AS
SELECT
    a.appointment_number,
    a.appointment_time,
    p.full_name as patient_name,
    d.full_name as dentist_name,
    t.treatment_name,
    a.status
FROM appointments a
         JOIN patients p ON a.patient_id = p.patient_id
         JOIN dentists d ON a.dentist_id = d.dentist_id
         JOIN treatments t ON a.treatment_id = t.treatment_id
WHERE a.appointment_date = CURDATE()
ORDER BY a.appointment_time;

-- ================================================
-- View 3: Bill Details View
-- ================================================
CREATE OR REPLACE VIEW v_bill_details AS
SELECT
    b.bill_id,
    b.bill_number,
    b.bill_date,
    b.subtotal,
    b.discount,
    b.tax,
    b.total_amount,
    b.payment_status,
    b.paid_amount,
    p.patient_id,
    p.patient_number,
    p.full_name as patient_name,
    a.appointment_number,
    d.full_name as dentist_name,
    t.treatment_name,
    t.treatment_cost,
    t.consultation_fee
FROM bills b
         JOIN patients p ON b.patient_id = p.patient_id
         JOIN appointments a ON b.appointment_id = a.appointment_id
         JOIN dentists d ON a.dentist_id = d.dentist_id
         JOIN treatments t ON a.treatment_id = t.treatment_id;

-- ================================================
-- View 4: Revenue by Treatment
-- ================================================
CREATE OR REPLACE VIEW v_revenue_by_treatment AS
SELECT
    t.treatment_id,
    t.treatment_name,
    COUNT(a.appointment_id) as appointment_count,
    SUM(b.total_amount) as total_revenue,
    AVG(b.total_amount) as average_revenue
FROM treatments t
         JOIN appointments a ON t.treatment_id = a.treatment_id
         JOIN bills b ON a.appointment_id = b.appointment_id
WHERE b.payment_status = 'PAID'
GROUP BY t.treatment_id, t.treatment_name
ORDER BY total_revenue DESC;

-- ================================================
-- View 5: Dentist Performance
-- ================================================
CREATE OR REPLACE VIEW v_dentist_performance AS
SELECT
    d.dentist_id,
    d.full_name as dentist_name,
    d.specialization,
    COUNT(a.appointment_id) as total_appointments,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_appointments,
    SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_appointments,
    COUNT(DISTINCT a.patient_id) as unique_patients
FROM dentists d
         LEFT JOIN appointments a ON d.dentist_id = a.dentist_id
GROUP BY d.dentist_id, d.full_name, d.specialization;

-- ================================================
-- View 6: Payment Summary
-- ================================================
CREATE OR REPLACE VIEW v_payment_summary AS
SELECT
    DATE(p.payment_date) as payment_date,
    p.payment_method,
    COUNT(p.payment_id) as payment_count,
    SUM(p.amount) as total_amount
FROM payments p
GROUP BY DATE(p.payment_date), p.payment_method
ORDER BY payment_date DESC;

-- Show confirmation
SELECT 'Views created successfully!' AS message;