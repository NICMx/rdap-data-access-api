#storeToDatabase
INSERT INTO rdap.domain VALUES (null,?,?,?,?);

#storeDomainEntityRoles 
INSERT INTO rdap.domain_entity_roles VALUES (?,?,?);

#getByLdhName
SELECT * FROM rdap.domain WHERE dom_ldh_name=? AND zone_id = ?;

#getDomainById
SELECT * FROM rdap.domain WHERE dom_id=?;

#searchByPartialNameWZone
SELECT domain.* FROM rdap.domain WHERE domain.dom_ldh_name LIKE ? AND domain.zone_id = ? ORDER BY 1 LIMIT ?;

#searchByNameWZone
SELECT domain.* FROM rdap.domain WHERE domain.dom_ldh_name = ? AND domain.zone_id = ? ORDER BY 1 LIMIT ?;

#searchByPartialNameWPartialZone
SELECT DISTINCT(d.dom_id), d.dom_handle, d.dom_ldh_name, d.dom_port43, d.zone_id FROM rdap.domain d JOIN rdap.zone z on d.zone_id = z.zone_id AND z.zone_id IN (?) WHERE d.dom_ldh_name LIKE ? AND z.zone_name like ? LIMIT ?;

#searchByNameWPartialZone
SELECT DISTINCT(d.dom_id), d.dom_handle, d.dom_ldh_name, d.dom_port43, d.zone_id FROM rdap.domain d JOIN rdap.zone z on d.zone_id = z.zone_id AND z.zone_id IN (?) WHERE d.dom_ldh_name = ? AND z.zone_name like ? LIMIT ?;

#searchByNameWOutZone
SELECT d.* FROM rdap.domain d WHERE d.dom_ldh_name = ? AND d.zone_id IN (?) ORDER BY 1 LIMIT ?;

#searchByPartialNameWOutZone
SELECT d.* FROM rdap.domain d WHERE d.dom_ldh_name LIKE ? AND d.zone_id IN (?) ORDER BY 1 LIMIT ?;

#searchByNsLdhName
SELECT DISTINCT (dom.dom_id), dom.dom_ldh_name, dom.dom_handle, dom.dom_port43, dom.zone_id FROM rdap.domain dom JOIN rdap.domain_nameservers dom_ns ON dom_ns.dom_id = dom.dom_id JOIN rdap.nameserver ns ON ns.nse_id = dom_ns.nse_id WHERE  ns.nse_ldh_name LIKE ? ORDER BY 1 LIMIT ?;

#searchByNsIp
SELECT DISTINCT (dom.dom_id), dom.dom_ldh_name, dom.dom_handle, dom.dom_port43, dom.zone_id FROM rdap.domain dom JOIN rdap.domain_nameservers dom_ns ON dom_ns.dom_id = dom.dom_id JOIN rdap.nameserver ns ON ns.nse_id = dom_ns.nse_id JOIN rdap.ip_address ip	ON ip.nse_id = ns.nse_id WHERE IF(?=4, INET_ATON(?),INET6_ATON(?)) = ip.iad_value ORDER BY 1 LIMIT ?; 