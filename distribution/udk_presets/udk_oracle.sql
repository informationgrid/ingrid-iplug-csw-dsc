-- DROP TABLE IF EXISTS T011_town_loc_town;
-- Korrekt:
-- DROP TABLE T011_town_loc_town cascade constraints;
-- Besser man loescht den Inhalt nur, sonst muessen Grants fuer andere Nutzer
-- neu angelegt werden
DELETE FROM T011_town_loc_town;
COMMIT;

CREATE TABLE T011_town_loc_town (township_no VARCHAR(12), name VARCHAR(50));

INSERT INTO T011_town_loc_town
SELECT t.loc_town_no, t.township
FROM T01_st_township t, T01_st_bbox b
WHERE t.loc_town_no = b.loc_town_no;

INSERT INTO T011_town_loc_town
SELECT DISTINCT b.loc_town_no, t.state
FROM T01_st_township t, T01_st_bbox b
WHERE b.loc_town_no = SUBSTR(t.loc_town_no, 1, 2);

INSERT INTO T011_town_loc_town
SELECT DISTINCT b.loc_town_no, t.district
FROM T01_st_bbox b, T01_st_township t
WHERE b.loc_town_no = SUBSTR(t.loc_town_no, 1, 3) AND b.loc_town_no NOT LIKE '02*';

INSERT INTO T011_town_loc_town
SELECT DISTINCT b.loc_town_no, t.country
FROM T01_st_bbox b, T01_st_township t
WHERE b.loc_town_no = SUBSTR(t.loc_town_no, 1, 3) AND b.loc_town_no LIKE '02*';

INSERT INTO T011_town_loc_town
SELECT DISTINCT b.loc_town_no, t.country
FROM T01_st_bbox b, T01_st_township t
WHERE b.loc_town_no = SUBSTR(t.loc_town_no, 1, 5);