DROP DATABASE if exists cloudstenography;

CREATE DATABASE cloudstenography;

use cloudstenography;

DROP SCHEMA if exists pig;

create schema pig;

CREATE TABLE pig.language_words (
   id serial primary key,
   name varchar( 255 ) NOT NULL ,
   working text NOT NULL ,
   language varchar( 255 ) NOT NULL
);

create index name_language_idx on pig.language_words(name, language);