-- base
ALTER TABLE `PROXY` ADD COLUMN `last_check` datetime;
ALTER TABLE `PROXY` ADD COLUMN `status` tinyint DEFAULT 0;
ALTER TABLE `PROXY` ADD COLUMN `remote_ip` varchar(256);

-- google
ALTER TABLE `GOOGLE_TARGET` ADD COLUMN `pattern_type` tinyint DEFAULT 2 AFTER `name`;

ALTER TABLE `GOOGLE_RANK` ADD COLUMN `url` varchar(256);

create table `GOOGLE_RANK_BEST` (
    group_id int,
    google_target_id int,
    google_search_id int,

    rank smallint,
    run_day datetime,
    url varchar(256),

    primary key(group_id, google_target_id, google_search_id),
    foreign key (group_id) references `GROUP`(id),
    foreign key (google_target_id) references `GOOGLE_TARGET`(id),
    foreign key (google_search_id) references `GOOGLE_SEARCH`(id)
) engine = innodb;

INSERT INTO `CONFIG` VALUES ('app.dbversion','1') ON DUPLICATE KEY UPDATE `value` = '1';
