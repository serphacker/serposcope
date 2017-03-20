delete from `GOOGLE_SERP` where run_id in (select id from `RUN` where group_id is not null);
delete from `GOOGLE_RANK` where run_id in (select id from `RUN` where group_id is not null);
delete from `GOOGLE_TARGET_SUMMARY` where run_id in (select id from `RUN` where group_id is not null);
drop table if exists `GROUP_RUN`;
delete from `RUN` where group_id is not null;
alter table `RUN` drop column `group_id`;


INSERT INTO `CONFIG` VALUES ('app.dbversion','4') ON DUPLICATE KEY UPDATE `value` = '4';