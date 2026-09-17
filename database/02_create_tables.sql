CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE,
                       role ENUM('ADMIN', 'RECEPTIONIST', 'DENTIST') NOT NULL,
                       active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_username (username),
                       INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE patients (
                          patient_id INT AUTO_INCREMENT PRIMARY KEY,
                          patient_number VARCHAR(20) UNIQUE NOT NULL,
                          full_name VARCHAR(100) NOT NULL,
                          address TEXT,
                          contact_number VARCHAR(20) NOT NULL,
                          email VARCHAR(100),
                          date_of_birth DATE,
                          gender ENUM('MALE', 'FEMALE', 'OTHER'),
                          registration_date DATE NOT NULL,
                          active BOOLEAN DEFAULT TRUE,
                          notes TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          INDEX idx_patient_number (patient_number),
                          INDEX idx_full_name (full_name),
                          INDEX idx_contact_number (contact_number),
                          CONSTRAINT chk_contact_number CHECK (contact_number REGEXP '^[0-9]{10,15}$')
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dentists (
                          dentist_id INT AUTO_INCREMENT PRIMARY KEY,
                          dentist_number VARCHAR(20) UNIQUE NOT NULL,
                          full_name VARCHAR(100) NOT NULL,
                          specialization VARCHAR(100),
                          contact_number VARCHAR(20),
                          email VARCHAR(100),
                          consultation_fee DECIMAL(10,2) DEFAULT 0.00,
                          availability_status ENUM('AVAILABLE', 'BUSY', 'ON_LEAVE', 'INACTIVE') DEFAULT 'AVAILABLE',
                          active BOOLEAN DEFAULT TRUE,
                          joined_date DATE,
                          notes TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          INDEX idx_dentist_number (dentist_number),
                          INDEX idx_full_name (full_name),
                          INDEX idx_availability (availability_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE treatments (
                            treatment_id INT AUTO_INCREMENT PRIMARY KEY,
                            treatment_code VARCHAR(20) UNIQUE NOT NULL,
                            treatment_name VARCHAR(100) NOT NULL,
                            description TEXT,
                            treatment_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            INDEX idx_treatment_code (treatment_code),
                            INDEX idx_treatment_name (treatment_name),
                            CONSTRAINT chk_treatment_cost CHECK (treatment_cost >= 0),
                            CONSTRAINT chk_consultation_fee CHECK (consultation_fee >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE appointments (
                              appointment_id INT AUTO_INCREMENT PRIMARY KEY,
                              appointment_number VARCHAR(20) UNIQUE NOT NULL,
                              patient_id INT NOT NULL,
                              dentist_id INT NOT NULL,
                              treatment_id INT NOT NULL,
                              appointment_date DATE NOT NULL,
                              appointment_time TIME NOT NULL,
                              status ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',
                              notes TEXT,
                              cancellation_reason TEXT,
                              rescheduled_from INT NULL,
                              created_by INT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                              FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id),
                              FOREIGN KEY (treatment_id) REFERENCES treatments(treatment_id),
                              FOREIGN KEY (rescheduled_from) REFERENCES appointments(appointment_id),
                              FOREIGN KEY (created_by) REFERENCES users(user_id),
                              INDEX idx_appointment_number (appointment_number),
                              INDEX idx_patient_id (patient_id),
                              INDEX idx_dentist_id (dentist_id),
                              INDEX idx_appointment_date (appointment_date),
                              INDEX idx_status (status),
                              UNIQUE KEY unique_dentist_slot (dentist_id, appointment_date, appointment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bills (
                       bill_id INT AUTO_INCREMENT PRIMARY KEY,
                       bill_number VARCHAR(20) UNIQUE NOT NULL,
                       appointment_id INT NOT NULL,
                       patient_id INT NOT NULL,
                       treatment_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       discount DECIMAL(10,2) DEFAULT 0.00,
                       tax DECIMAL(10,2) DEFAULT 0.00,
                       total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       payment_status ENUM('PAID', 'UNPAID', 'PARTIAL') DEFAULT 'UNPAID',
                       paid_amount DECIMAL(10,2) DEFAULT 0.00,
                       bill_date DATE NOT NULL,
                       notes TEXT,
                       created_by INT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
                       FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                       FOREIGN KEY (created_by) REFERENCES users(user_id),
                       INDEX idx_bill_number (bill_number),
                       INDEX idx_appointment_id (appointment_id),
                       INDEX idx_patient_id (patient_id),
                       INDEX idx_bill_date (bill_date),
                       INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
                          payment_id INT AUTO_INCREMENT PRIMARY KEY,
                          payment_number VARCHAR(20) UNIQUE NOT NULL,
                          bill_id INT NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          payment_date DATETIME NOT NULL,
                          payment_method ENUM('CASH', 'CARD', 'BANK_TRANSFER', 'INSURANCE', 'OTHER') DEFAULT 'CASH',
                          reference_number VARCHAR(50),
                          notes TEXT,
                          created_by INT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
                          FOREIGN KEY (created_by) REFERENCES users(user_id),
                          INDEX idx_payment_number (payment_number),
                          INDEX idx_bill_id (bill_id),
                          INDEX idx_payment_date (payment_date),
                          INDEX idx_payment_method (payment_method),
                          CONSTRAINT chk_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 8. AUDIT LOG TABLE (Optional Advanced Feature)
-- ================================================
CREATE TABLE audit_log (
                           log_id INT AUTO_INCREMENT PRIMARY KEY,
                           user_id INT,
                           action VARCHAR(50) NOT NULL,
                           entity_type VARCHAR(50) NOT NULL,
                           entity_id VARCHAR(50),
                           old_values TEXT,
                           new_values TEXT,
                           ip_address VARCHAR(45),
                           user_agent TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (user_id) REFERENCES users(user_id),
                           INDEX idx_user_id (user_id),
                           INDEX idx_entity_type (entity_type),
                           INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Show confirmation
SELECT 'All tables created successfully!' AS message;