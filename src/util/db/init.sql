-- Create admin_transaction table
CREATE TABLE IF NOT EXISTS admin_transaction (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id INT NOT NULL,
    action_type VARCHAR(50) NOT NULL,  -- e.g., 'APPROVE_RESERVATION', 'REJECT_RESERVATION', 'ADD_COURT', etc.
    entity_type VARCHAR(50) NOT NULL,  -- e.g., 'RESERVATION', 'COURT'
    entity_id INT NOT NULL,            -- ID of the affected reservation/court
    old_value TEXT,                    -- Previous state (if applicable)
    new_value TEXT,                    -- New state
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES account(account_id)
); 

-- Modify reservation table to include audit fields
ALTER TABLE reservation 
ADD COLUMN approved_by INT NULL,
ADD COLUMN rejected_by INT NULL,
ADD COLUMN cancelled_by INT NULL,
ADD COLUMN last_modified_by INT NULL,
ADD FOREIGN KEY (approved_by) REFERENCES account(account_id),
ADD FOREIGN KEY (rejected_by) REFERENCES account(account_id),
ADD FOREIGN KEY (cancelled_by) REFERENCES account(account_id),
ADD FOREIGN KEY (last_modified_by) REFERENCES account(account_id);

-- Modify court table to include audit fields
ALTER TABLE court
ADD COLUMN created_by INT NOT NULL,
ADD COLUMN last_modified_by INT NULL,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD FOREIGN KEY (created_by) REFERENCES account(account_id),
ADD FOREIGN KEY (last_modified_by) REFERENCES account(account_id); 