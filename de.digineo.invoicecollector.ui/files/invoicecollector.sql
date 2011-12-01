CREATE TABLE IF NOT EXISTS `invoices` (
	`id` INTEGER PRIMARY KEY AUTOINCREMENT,
	`account_id` INT NOT NULL,
	`number` VARCHAR(50) DEFAULT NULL,
	`date` date DEFAULT NULL,
	`amount` decimal(9,2) DEFAULT NULL,
	`original_file_name` VARCHAR(255) NOT NULL,
	`checksum` VARCHAR(100) NOT NULL,
	`created_at` DATE DEFAULT CURRENT_TIMESTAMP
) ;

CREATE INDEX IF NOT EXISTS invoices_date ON invoices (date);
CREATE INDEX IF NOT EXISTS invoices_account_id ON invoices (account_id);
CREATE UNIQUE INDEX IF NOT EXISTS invoices_account_number ON invoices (account_id, number);
CREATE UNIQUE INDEX IF NOT EXISTS invoices_checksum ON invoices (checksum);

CREATE TABLE IF NOT EXISTS `accounts` (
	`id` INTEGER PRIMARY KEY AUTOINCREMENT,
	`module` VARCHAR(255) NOT NULL,
	`active` tinyint(1) NOT NULL DEFAULT '1',
	`autoprint` tinyint(1) NOT NULL DEFAULT '0',
	`username` VARCHAR(255) NOT NULL,
	`password` VARCHAR(255) NOT NULL,
	`imap_account_id` INT DEFAULT NULL,
	`imap_filter_id` INT DEFAULT NULL
) ;

CREATE TABLE IF NOT EXISTS `imap_accounts` (
	`id` INTEGER PRIMARY KEY AUTOINCREMENT,
	`host` VARCHAR(255) NOT NULL,
	`username` VARCHAR(255) NOT NULL,
	`password` VARCHAR(255) NOT NULL,
	`ssl` tinyint(1) NOT NULL DEFAULT '1',
	`port` INT NOT NULL DEFAULT '993'
) ;

CREATE TABLE IF NOT EXISTS `imap_filters` (
	`id` INTEGER PRIMARY KEY AUTOINCREMENT,
	`name` VARCHAR(255) NOT NULL,
  	`search` text NOT NULL,
  	`subject` VARCHAR(255) NOT NULL,
  	`filename` VARCHAR(255) NOT NULL
) ;