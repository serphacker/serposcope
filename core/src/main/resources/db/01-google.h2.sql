-- http://www.h2database.com/html/grammar.html
SET FOREIGN_KEY_CHECKS=0;

drop table if exists `GOOGLE_SEARCH`;
create table `GOOGLE_SEARCH` (
    id int primary key auto_increment,
    keyword varchar(255) not null,
    tld varchar(16),
    datacenter varchar(64),
    device tinyint,
    local varchar(64),
    custom_parameters varchar(255)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GOOGLE_SERP`;
create table `GOOGLE_SERP` (
    run_id int,
    google_search_id int,
    run_day datetime default null,
    serp blob,
    primary key(run_id, google_search_id),
    foreign key (google_search_id) references `GOOGLE_SEARCH`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GOOGLE_SEARCH_GROUP`;
create table `GOOGLE_SEARCH_GROUP` (
    google_search_id int,
    group_id int,
    primary key (google_search_id, group_id),
    foreign key (group_id) references `GROUP`(id),
    foreign key (google_search_id) references `GOOGLE_SEARCH`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GOOGLE_TARGET`;
create table `GOOGLE_TARGET` (
    id int primary key auto_increment,
    group_id int,
    name varchar(255),
    pattern_type tinyint,
    pattern varchar(255),
    foreign key (group_id) references `GROUP`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GOOGLE_RANK`;
create table `GOOGLE_RANK` (
    run_id int,
    group_id int,
    google_target_id int,
    google_search_id int,

    rank smallint,
    previous_rank smallint,
    hits smallint,
    diff smallint,
    url varchar(256),

    primary key(run_id, group_id, google_target_id, google_search_id),
    foreign key (run_id) references `RUN`(id),
    foreign key (group_id) references `GROUP`(id),
    foreign key (google_target_id) references `GOOGLE_TARGET`(id),
    foreign key (google_search_id) references `GOOGLE_SEARCH`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GOOGLE_RANK_BEST`;
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
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GOOGLE_TARGET_SUMMARY`;
create table `GOOGLE_TARGET_SUMMARY` (
    group_id int,
    google_target_id int,
    run_id int,

    total_top_3 int,
    total_top_10 int,
    total_top_100 int,
    total_out int,

    top_ranks varchar(128),
    top_improvements varchar(128),
    top_losts varchar(128),

    score_raw int default 0,
    score_basis_point int default 0,
    previous_score_basis_point int default 0,

    primary key(group_id, google_target_id, run_id),
    foreign key (group_id) references `GROUP`(id),
    foreign key (google_target_id) references `GOOGLE_TARGET`(id),
    foreign key (run_id) references `RUN`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

SET FOREIGN_KEY_CHECKS=1;