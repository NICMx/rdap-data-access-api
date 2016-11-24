#getByIPv4
SELECT * FROM rdap.ip_network WHERE ip_version_id = 4 AND ine_cidr <= ? AND ine_start_address_down <= ? AND ine_end_address_down >= ? ORDER BY ine_cidr DESC;

#getByIPv6
SELECT * FROM rdap.ip_network WHERE ip_version_id = 6 AND ine_cidr <= ? AND ine_start_address_up <= ? AND ine_start_address_down <= ? AND ine_end_address_up >= ? AND ine_end_address_down >= ? ORDER BY ine_cidr DESC;

#storeToDatabase
INSERT INTO rdap.ip_network VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

#getByEntityId
SELECT ipn.* FROM rdap.ip_network ipn JOIN rdap.ipn_entity_roles ent ON ent.ine_id = ipn.ine_id WHERE ent.ent_id = ?;

#getByDomainId
SELECT ipn.* FROM rdap.ip_network ipn JOIN rdap.domain_networks dom ON dom.ine_id = ipn.ine_id WHERE dom.dom_id = ?;