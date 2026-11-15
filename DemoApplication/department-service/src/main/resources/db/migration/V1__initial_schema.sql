CREATE TABLE departments (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 location VARCHAR(160) NOT NULL,
 CONSTRAINT uk_departments_name UNIQUE (name),
 INDEX idx_department_name (name)
);
