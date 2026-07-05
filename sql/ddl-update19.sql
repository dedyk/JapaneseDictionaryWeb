alter table generic_log add remote_ip_asn varchar(20) null after remote_ip;
alter table generic_log add remote_ip_asn_organization_name varchar(128) null after remote_ip_asn;
alter table generic_log add remote_ip_country varchar(30) null after remote_ip_asn_organization_name;
