CREATE TABLE employees (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 email VARCHAR(180) NOT NULL,
 department_id INT NOT NULL,
 CONSTRAINT uk_employees_email UNIQUE (email),
 INDEX idx_employee_department (department_id),
 INDEX idx_employee_name (name)
);
