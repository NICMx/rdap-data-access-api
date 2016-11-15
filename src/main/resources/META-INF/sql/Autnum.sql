#storeToDatabase
INSERT INTO rdap.autonomous_system_number VALUES (null,?,?,?,?,?,?,?);

#getAutnumById
SELECT * FROM rdap.autonomous_system_number asn WHERE asn.asn_id = ?; 

#getByRange
SELECT * FROM rdap.autonomous_system_number asn WHERE asn.asn_start_autnum <= ? AND asn.asn_end_autnum >= ?;
	