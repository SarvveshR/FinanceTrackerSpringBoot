-- Transaction categories. IDs must match the app's lists in ViewModel.kt.
-- INSERT IGNORE: runs on every startup, only adds rows that are missing.
INSERT IGNORE INTO categories_entity (id, name) VALUES
('ic_sw', 'Salary / Wages'),
('ic_bi', 'Business Income'),
('ic_in', 'Investments'),
('ic_ri', 'Rental Income'),
('ic_rr', 'Refunds & Reimbursements'),
('ic_oi', 'Other Income'),
('ec_ho', 'Housing'),
('ec_tr', 'Transportation'),
('ec_fg', 'Food & Groceries'),
('ec_bu', 'Bills & Utilities'),
('ec_hm', 'Health & Medical'),
('ec_lp', 'Lifestyle & Personal'),
('ec_fi', 'Finance & Insurance'),
('ec_ed', 'Education'),
('ec_en', 'Entertainment'),
('ec_tl', 'Travel'),
('ec_fk', 'Family'),
('ec_mi', 'Miscellaneous');

-- Cards created before login existed have no owner; give them to the first user who signed up.
-- Does nothing until a user exists, or once every card has an owner.
UPDATE card_entity SET user_id = (SELECT MIN(id) FROM user_entity) WHERE user_id IS NULL;