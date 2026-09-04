-- ================================================
-- Sunrise Dental Clinic Management System
-- Sample Data Insertion Script
-- ================================================

USE sunrise_dental_db;

-- ================================================
-- Insert Users (Passwords: admin123, rec123, dent123)
-- BCrypt hashed passwords
-- ================================================
INSERT INTO users (username, password_hash, full_name, email, role, active) VALUES
                                                                                ('admin', '$2a$10$NkM5kN2IxU.4bRqEKxK5Y.7c4XgZjW9xq3p8rY2tL6sJdF1hGvBnO', 'Admin User', 'admin@sunrisedental.lk', 'ADMIN', TRUE),
                                                                                ('receptionist', '$2a$10$XqW7L5jD2sL8yB3tNpRwV.6zPmQkXwR9fNcY3uGvHjK1LmNoPqR', 'Reception Staff', 'reception@sunrisedental.lk', 'RECEPTIONIST', TRUE),
                                                                                ('dentist', '$2a$10$YrA8L6kE3sL9yC4tOqRwV.7zQnRlYxS0gOcZ4vHwIjK2MnOpQrS', 'Dr. Dental Staff', 'dentist@sunrisedental.lk', 'DENTIST', TRUE);

-- ================================================
-- Insert Dentists
-- ================================================
INSERT INTO dentists (dentist_number, full_name, specialization, contact_number, email, consultation_fee, availability_status, active, joined_date) VALUES
                                                                                                                                                        ('D001', 'Dr. Kumara Perera', 'General Dentistry', '0771234567', 'kumara.p@sunrisedental.lk', 1500.00, 'AVAILABLE', TRUE, '2020-01-15'),
                                                                                                                                                        ('D002', 'Dr. Nimali Fernando', 'Orthodontist', '0772345678', 'nimali.f@sunrisedental.lk', 2000.00, 'AVAILABLE', TRUE, '2020-03-20'),
                                                                                                                                                        ('D003', 'Dr. Chamara Silva', 'Periodontist', '0773456789', 'chamara.s@sunrisedental.lk', 1800.00, 'AVAILABLE', TRUE, '2021-06-10'),
                                                                                                                                                        ('D004', 'Dr. Priyani Jayawardena', 'Endodontist', '0774567890', 'priyani.j@sunrisedental.lk', 2500.00, 'AVAILABLE', TRUE, '2021-09-05'),
                                                                                                                                                        ('D005', 'Dr. Ruwan Wickramasinghe', 'Oral Surgeon', '0775678901', 'ruwan.w@sunrisedental.lk', 3000.00, 'BUSY', TRUE, '2022-01-12');

-- ================================================
-- Insert Treatments
-- ================================================
INSERT INTO treatments (treatment_code, treatment_name, description, treatment_cost, consultation_fee, active) VALUES
                                                                                                                   ('T001', 'Dental Consultation', 'Initial dental examination and consultation', 0.00, 1000.00, TRUE),
                                                                                                                   ('T002', 'Dental Cleaning', 'Professional teeth cleaning and scaling', 5000.00, 1000.00, TRUE),
                                                                                                                   ('T003', 'Tooth Filling', 'Composite tooth filling for cavities', 8000.00, 1000.00, TRUE),
                                                                                                                   ('T004', 'Tooth Extraction', 'Simple tooth extraction procedure', 6000.00, 1000.00, TRUE),
                                                                                                                   ('T005', 'Root Canal Treatment', 'Root canal therapy for infected tooth', 25000.00, 1500.00, TRUE),
                                                                                                                   ('T006', 'Dental X-Ray', 'Digital dental X-ray imaging', 3000.00, 0.00, TRUE),
                                                                                                                   ('T007', 'Crown', 'Dental crown placement', 35000.00, 1500.00, TRUE),
                                                                                                                   ('T008', 'Teeth Whitening', 'Professional teeth whitening treatment', 20000.00, 1000.00, TRUE),
                                                                                                                   ('T009', 'Orthodontic Consultation', 'Braces and orthodontic assessment', 0.00, 1500.00, TRUE),
                                                                                                                   ('T010', 'Dental Implant', 'Dental implant placement', 75000.00, 2000.00, TRUE);

-- ================================================
-- Insert Patients
-- ================================================
INSERT INTO patients (patient_number, full_name, address, contact_number, email, date_of_birth, gender, registration_date, active) VALUES
                                                                                                                                       ('P0001', 'Saman Kumara', 'No. 15, Galle Road, Colombo 03', '0712345678', 'saman.k@email.com', '1985-03-15', 'MALE', CURDATE(), TRUE),
                                                                                                                                       ('P0002', 'Shanthi Perera', 'No. 22, Kandy Road, Colombo 05', '0723456789', 'shanthi.p@email.com', '1990-07-20', 'FEMALE', CURDATE(), TRUE),
                                                                                                                                       ('P0003', 'Priyantha Silva', 'No. 8, Negombo Road, Colombo 07', '0734567890', 'priyantha.s@email.com', '1978-11-05', 'MALE', CURDATE(), TRUE),
                                                                                                                                       ('P0004', 'Lakshmi Jayawardena', 'No. 45, Colombo Road, Colombo 10', '0745678901', 'lakshmi.j@email.com', '1995-01-30', 'FEMALE', CURDATE(), TRUE),
                                                                                                                                       ('P0005', 'Gamini Fernando', 'No. 12, Marine Drive, Colombo 03', '0756789012', 'gamini.f@email.com', '1982-09-12', 'MALE', CURDATE(), TRUE),
                                                                                                                                       ('P0006', 'Kumari Weerasinghe', 'No. 33, Galle Road, Colombo 04', '0767890123', 'kumari.w@email.com', '1988-05-25', 'FEMALE', CURDATE(), TRUE),
                                                                                                                                       ('P0007', 'Sumith Rathnayake', 'No. 56, Peradeniya Road, Colombo 06', '0778901234', 'sumith.r@email.com', '1975-12-18', 'MALE', CURDATE(), TRUE),
                                                                                                                                       ('P0008', 'Nilanthi Ranasinghe', 'No. 78, Kandy Road, Colombo 08', '0789012345', 'nilanthi.r@email.com', '1992-08-08', 'FEMALE', CURDATE(), TRUE),
                                                                                                                                       ('P0009', 'Chandana Ekanayake', 'No. 90, Negombo Road, Colombo 09', '0790123456', 'chandana.e@email.com', '1980-04-22', 'MALE', CURDATE(), TRUE),
                                                                                                                                       ('P0010', 'Sandya Gunasekara', 'No. 100, Colombo Road, Colombo 11', '0701234567', 'sandya.g@email.com', '1987-10-14', 'FEMALE', CURDATE(), TRUE);

-- ================================================
-- Insert Appointments
-- ================================================
INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, notes, created_by) VALUES
                                                                                                                                                       ('A0001', 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'CONFIRMED', 'First visit - general checkup', 1),
                                                                                                                                                       ('A0002', 2, 2, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', 'SCHEDULED', 'Tooth filling for cavity', 2),
                                                                                                                                                       ('A0003', 3, 3, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'SCHEDULED', 'Dental cleaning', 2),
                                                                                                                                                       ('A0004', 4, 4, 5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30:00', 'CONFIRMED', 'Root canal - tooth #14', 1),
                                                                                                                                                       ('A0005', 5, 5, 4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:00:00', 'SCHEDULED', 'Tooth extraction - wisdom tooth', 2),
                                                                                                                                                       ('A0006', 6, 1, 6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:30:00', 'SCHEDULED', 'X-ray for diagnosis', 2),
                                                                                                                                                       ('A0007', 7, 2, 7, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'SCHEDULED', 'Crown placement', 1),
                                                                                                                                                       ('A0008', 8, 3, 8, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:30:00', 'COMPLETED', 'Teeth whitening done', 2),
                                                                                                                                                       ('A0009', 9, 4, 9, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:30:00', 'CANCELLED', 'Patient cancelled', 2),
                                                                                                                                                       ('A0010', 10, 5, 10, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:00:00', 'SCHEDULED', 'Implant consultation', 1);

-- ================================================
-- Insert Bills
-- ================================================
INSERT INTO bills (bill_number, appointment_id, patient_id, treatment_cost, consultation_fee, subtotal, discount, tax, total_amount, payment_status, paid_amount, bill_date, created_by) VALUES
                                                                                                                                                                                             ('B0001', 1, 1, 0.00, 1000.00, 1000.00, 0.00, 0.00, 1000.00, 'PAID', 1000.00, CURDATE(), 1),
                                                                                                                                                                                             ('B0002', 2, 2, 8000.00, 1000.00, 9000.00, 0.00, 0.00, 9000.00, 'UNPAID', 0.00, CURDATE(), 2),
                                                                                                                                                                                             ('B0003', 3, 3, 5000.00, 1000.00, 6000.00, 500.00, 0.00, 5500.00, 'PAID', 5500.00, CURDATE(), 2),
                                                                                                                                                                                             ('B0004', 4, 4, 25000.00, 1500.00, 26500.00, 0.00, 0.00, 26500.00, 'UNPAID', 0.00, CURDATE(), 1),
                                                                                                                                                                                             ('B0005', 5, 5, 6000.00, 1000.00, 7000.00, 0.00, 0.00, 7000.00, 'PARTIAL', 3000.00, CURDATE(), 2),
                                                                                                                                                                                             ('B0006', 6, 6, 3000.00, 0.00, 3000.00, 0.00, 0.00, 3000.00, 'PAID', 3000.00, CURDATE(), 2),
                                                                                                                                                                                             ('B0007', 7, 7, 35000.00, 1500.00, 36500.00, 0.00, 0.00, 36500.00, 'UNPAID', 0.00, CURDATE(), 1),
                                                                                                                                                                                             ('B0008', 8, 8, 20000.00, 1000.00, 21000.00, 0.00, 0.00, 21000.00, 'PAID', 21000.00, CURDATE(), 2),
                                                                                                                                                                                             ('B0009', 9, 9, 0.00, 1500.00, 1500.00, 0.00, 0.00, 1500.00, 'PAID', 1500.00, CURDATE(), 2),
                                                                                                                                                                                             ('B0010', 10, 10, 75000.00, 2000.00, 77000.00, 0.00, 0.00, 77000.00, 'UNPAID', 0.00, CURDATE(), 1);

-- ================================================
-- Insert Payments
-- ================================================
INSERT INTO payments (payment_number, bill_id, amount, payment_date, payment_method, reference_number, created_by) VALUES
                                                                                                                       ('PAY001', 1, 1000.00, CURDATE(), 'CASH', NULL, 1),
                                                                                                                       ('PAY002', 3, 5500.00, CURDATE(), 'CARD', 'TX123456', 2),
                                                                                                                       ('PAY003', 5, 3000.00, CURDATE(), 'CASH', NULL, 2),
                                                                                                                       ('PAY004', 6, 3000.00, CURDATE(), 'CARD', 'TX789012', 2),
                                                                                                                       ('PAY005', 8, 21000.00, CURDATE(), 'CASH', NULL, 2),
                                                                                                                       ('PAY006', 9, 1500.00, CURDATE(), 'CASH', NULL, 2);

-- Show confirmation
SELECT 'Sample data inserted successfully!' AS message;