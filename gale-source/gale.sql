drop database if exists galedb;
create database galedb default character set latin1 default collate latin1_general_cs;
grant all privileges on galedb.* to 'galedb'@'localhost' identified by 'galepw';
exit