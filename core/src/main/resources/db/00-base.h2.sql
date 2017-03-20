SET FOREIGN_KEY_CHECKS=0;
drop table if exists `CONFIG`;
create table `CONFIG` (
    name varchar(255) primary key,
    value text
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `USER`;
create table `USER` (
    id int primary key auto_increment,
    email varchar(255),
    password_hash tinyblob,
    password_salt tinyblob,
    admin boolean,
    logout datetime
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `GROUP`;
create table `GROUP` (
    id int primary key auto_increment,
    module_id int,
    name varchar(255)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;

drop table if exists `EVENT`;
create table `EVENT` (
    group_id int,
    day date,
    title varchar(255),
    description text,
    primary key(group_id,day),
    foreign key (group_id) references `GROUP`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;


drop table if exists `USER_GROUP`;
create table `USER_GROUP` (
    user_id int,
    group_id int,
    primary key(user_id, group_id),
    foreign key (user_id) references `USER`(id),
    foreign key (group_id) references `GROUP`(id)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;


drop table if exists `RUN`;
create table `RUN` (
    id int primary key auto_increment,
    module_id int,
    day date,
    started datetime,
    finished datetime,
    progress int,
    captchas int,
    errors int,
    status int, -- running, aborted, finished, error
    mode int
) engine = innodb default charset=utf8 /*! collate utf8_bin */;
create index RUN_MODULE_ID_DAY on RUN(module_id,day);

drop table if exists `PROXY`;
create table `PROXY` (
    `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `type` int,
    `ip` text,
    `port` int,
    `user` text,
    `password` text,
    `last_check` datetime,
    `status` tinyint,
    `remote_ip` varchar(256)
) engine = innodb default charset=utf8 /*! collate utf8_bin */;


SET FOREIGN_KEY_CHECKS=1;