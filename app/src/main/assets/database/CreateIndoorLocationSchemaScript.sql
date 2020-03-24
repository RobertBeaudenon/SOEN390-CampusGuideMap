BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "IndoorLocation" (
	"lID"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	"location_name"	TEXT NOT NULL,
	"floor_num"	INTEGER NOT NULL,
	"location_type"	TEXT NOT NULL
);
INSERT INTO "IndoorLocation" VALUES
 (1,'H-400',4,'classroom'),
 (2,'H-400-2',4,'classroom'),
 (3,'H-401',4,'classroom'),
 (4,'H-403',4,'classroom'),
 (5,'H-407',4,'classroom'),
 (6,'H-411',4,'classroom'),
 (7,'H-415',4,'classroom'),
 (8,'H-420',4,'classroom'),
 (9,'H-423',4,'classroom'),
 (10,'H-427',4,'classroom'),
 (11,'H-429',4,'classroom'),
 (12,'H-431',4,'classroom'),
 (13,'H-433',4,'classroom'),
 (14,'H-435',4,'classroom'),
 (15,'H-437',4,'classroom'),
 (16,'H-439',4,'classroom'),
 (17,'H-441',4,'classroom'),
 (18,'H-459',4,'classroom');
COMMIT;
