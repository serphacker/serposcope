drop table if exists `GOOGLE_TARGET_SUMMARY`;
create table `GOOGLE_TARGET_SUMMARY` (
    group_id int,
    google_target_id int,
    run_id int,

    previous_score int,
    score int,
    total_top_3 int,
    total_top_10 int,
    total_top_100 int,
    total_out int,

    top_ranks varchar(128),
    top_improvements varchar(128),
    top_losts varchar(128),

    primary key(group_id, google_target_id, run_id),
    foreign key (group_id) references `GROUP`(id),
    foreign key (google_target_id) references `GOOGLE_TARGET`(id),
    foreign key (run_id) references `RUN`(id)
) engine = innodb;

INSERT INTO `CONFIG` VALUES ('app.dbversion','3') ON DUPLICATE KEY UPDATE `value` = '3';