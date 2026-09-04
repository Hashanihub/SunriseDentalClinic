-- ================================================
-- Sunrise Dental Clinic Management System
-- Database Creation Script
-- ================================================

-- Drop database if exists (BE CAREFUL - THIS DELETES DATA!)
DROP DATABASE IF EXISTS sunrise_dental_db;

-- Create database with proper character set
CREATE DATABASE sunrise_dental_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE sunrise_dental_db;

-- Show confirmation
SELECT 'Database created successfully!' AS message;