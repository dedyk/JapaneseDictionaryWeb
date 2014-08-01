alter table suggestion_send_log modify body mediumtext null;

alter table daily_report_log modify report mediumtext not null;

alter table general_exception_log modify exception mediumtext null;