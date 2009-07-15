A = load 'passwd' using PigStorage(':') AS (username:chararray, passwd:chararray, uid:int, gid:int, fullname:chararray, homedir:chararray,shell:chararray);

B = FILTER A by uid > 50;

INSPECT B;