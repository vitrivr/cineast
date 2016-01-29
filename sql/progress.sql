WITH p AS (SELECT video, MAX(endframe) as end FROM cineast.shots GROUP BY video)
SELECT name, (100.0 * p.end / frames) as progress FROM cineast.videos, p WHERE videos.id = p.video AND p.end < frames
ORDER BY progress ASC