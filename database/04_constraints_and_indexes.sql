-- ================================================
-- Sunrise Dental Clinic Management System
-- Constraints and Indexes
-- ================================================

USE sunrise_dental_db;

-- ================================================
-- Additional Indexes for Performance
-- ================================================

-- Patients table indexes
CREATE INDEX idx_patients_name_contact ON patients(full_name, contact_number);
CREATE INDEX idx_patients_registration_date ON patients(registration_date);

-- Appointments table indexes
CREATE INDEX idx_appointments_date_status ON appointments(appointment_date, status);
CREATE INDEX idx_appointments_dentist_date ON appointments(dentist_id, appointment_date);

-- Bills table indexes
CREATE INDEX idx_bills_patient_date ON bills(patient_id, bill_date);
CREATE INDEX idx_bills_status_date ON bills(payment_status, bill_date);

-- Payments table indexes
CREATE INDEX idx_payments_bill_date ON payments(bill_id, payment_date);

-- ================================================
-- Check Constraints (Additional)
-- ================================================

-- Ensure appointment date is not in the past (except for historical data)
-- ALTER TABLE appointments ADD CONSTRAINT chk_appointment_date
-- CHECK (appointment_date >= CURDATE());

-- Ensure bill amount is positive
ALTER TABLE bills ADD CONSTRAINT chk_bill_total CHECK (total_amount >= 0);

-- Ensure discount is not more than subtotal
ALTER TABLE bills ADD CONSTRAINT chk_discount CHECK (discount <= subtotal);

-- ================================================
-- Unique Constraints (Already in table creation)
-- ================================================

-- These are already defined in the table creation:
-- UNIQUE KEY unique_dentist_slot (dentist_id, appointment_date, appointment_time)
-- UNIQUE KEY on patient_number, dentist_number, treatment_code, etc.

-- Show confirmation
SELECT 'Constraints and indexes created successfully!' AS message;