CREATE TABLE cineast.videos
(
  id serial NOT NULL,
  name character varying(100),
  path text,
  width integer,
  height integer,
  frames integer,
  seconds real,
  CONSTRAINT videos_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE videos
  OWNER TO cineast;

CREATE INDEX video_id_index
  ON videos
  USING btree
  (id);
  
CREATE TABLE cineast.shots
(
  id bigserial NOT NULL,
  "number" integer,
  video integer,
  startframe integer,
  endframe integer,
  CONSTRAINT primary_key PRIMARY KEY (id),
  CONSTRAINT movie_key FOREIGN KEY (video)
      REFERENCES videos (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE shots
  OWNER TO cineast;


CREATE INDEX shot_id_index
  ON shots
  USING btree
  (id);
  

CREATE TABLE resultcachenames
(
  id bigserial NOT NULL,
  name character varying(50),
  CONSTRAINT resultcachenames_pkey PRIMARY KEY (id),
  CONSTRAINT resultcachenames_name_key UNIQUE (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE resultcachenames
  OWNER TO cineast;


CREATE INDEX resultcachenames_id_idx
  ON resultcachenames
  USING btree
  (id);


CREATE INDEX resultcachenames_name_idx
  ON resultcachenames
  USING btree
  (name COLLATE pg_catalog."default");



CREATE TABLE resultcacheelements
(
  chacheid bigint NOT NULL,
  shotid bigint NOT NULL,
  CONSTRAINT resultcacheelements_pkey PRIMARY KEY (chacheid, shotid),
  CONSTRAINT resultcacheelements_chacheid_fkey FOREIGN KEY (chacheid)
      REFERENCES resultcachenames (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT resultcacheelements_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE resultcacheelements
  OWNER TO cineast;


CREATE INDEX resultcacheelements_chacheid_idx
  ON resultcacheelements
  USING btree
  (chacheid);


CREATE INDEX resultcacheelements_chacheid_shotid_idx
  ON resultcacheelements
  USING btree
  (chacheid, shotid);



CREATE TABLE features.averagecolor
(
  shotid bigint NOT NULL,
  color feature,
  CONSTRAINT "AverageColor_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "AverageColor_shotId_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolor
  OWNER TO cineast;


CREATE INDEX averagecolor_shotid_idx
  ON features.averagecolor
  USING btree
  (shotid);
  
CREATE TABLE features.averagecolorarp44
(
  shotid bigint NOT NULL,
  arp feature,
  CONSTRAINT averagecolorarp44_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorarp44_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorarp44
  OWNER TO cineast;


CREATE INDEX averagecolorarp44_shotid_idx
  ON features.averagecolorarp44
  USING btree
  (shotid);
  
CREATE TABLE features.averagecolorarp44normalized
(
  shotid bigint NOT NULL,
  arp feature,
  CONSTRAINT averagecolorarp44normalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorarp44normalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorarp44normalized
  OWNER TO cineast;


CREATE INDEX averagecolorarp44normalized_shotid_idx
  ON features.averagecolorarp44normalized
  USING btree
  (shotid);

CREATE TABLE features.averagecolorcld
(
  shotid bigint NOT NULL,
  cld feature,
  CONSTRAINT averagecolorcld_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorcld_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorcld
  OWNER TO cineast;

CREATE INDEX averagecolorcld_shotid_idx
  ON features.averagecolorcld
  USING btree
  (shotid);

CREATE TABLE features.averagecolorcldnormalized
(
  shotid bigint NOT NULL,
  cld feature,
  CONSTRAINT averagecolorcldnormalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorcldnormalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorcldnormalized
  OWNER TO cineast;

CREATE INDEX averagecolorcldnormalized_shotid_idx
  ON features.averagecolorcldnormalized
  USING btree
  (shotid);

  CREATE TABLE features.averagecolorgrid8
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT averagecolorgrid8_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorgrid8_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorgrid8
  OWNER TO cineast;


CREATE INDEX averagecolorgrid8_shotid_idx
  ON features.averagecolorgrid8
  USING btree
  (shotid);
  

  CREATE TABLE features.averagecolorgrid8normalized
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT averagecolorgrid8normalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorgrid8normalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorgrid8normalized
  OWNER TO cineast;


CREATE INDEX averagecolorgrid8normalized_shotid_idx
  ON features.averagecolorgrid8
  USING btree
  (shotid);

  
  CREATE TABLE features.averagecolorraster
(
  shotid bigint NOT NULL,
  hist feature,
  raster feature,
  CONSTRAINT averagecolorraster_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagecolorraster_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagecolorraster
  OWNER TO cineast;

CREATE INDEX averagecolorraster_shotid_idx
  ON features.averagecolorraster
  USING btree
  (shotid);
  
CREATE TABLE features.averagefuzzyhist
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT averagefuzzyhist_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagefuzzyhist_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagefuzzyhist
  OWNER TO cineast;


CREATE INDEX averagefuzzyhist_shotid_idx
  ON features.averagefuzzyhist
  USING btree
  (shotid);

 
CREATE TABLE features.averagefuzzyhistnormalized
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT averagefuzzyhistnormalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT averagefuzzyhistnormalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.averagefuzzyhistnormalized
  OWNER TO cineast;


CREATE INDEX averagefuzzyhistnormalized_shotid_idx
  ON features.averagefuzzyhistnormalized
  USING btree
  (shotid);


CREATE TABLE features.chromagrid8
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT chromagrid8_pkey PRIMARY KEY (shotid),
  CONSTRAINT chromagrid8_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.chromagrid8
  OWNER TO cineast;


CREATE INDEX chromagrid8_shotid_idx
  ON features.chromagrid8
  USING btree
  (shotid);


CREATE TABLE features.cld
(
  shotid bigint NOT NULL,
  cld feature,
  CONSTRAINT cld_pkey PRIMARY KEY (shotid),
  CONSTRAINT cld_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.cld
  OWNER TO cineast;

CREATE INDEX cld_shotid_idx
  ON features.cld
  USING btree
  (shotid);


CREATE TABLE features.cldnormalized
(
  shotid bigint NOT NULL,
  cld feature,
  CONSTRAINT cldnormalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT cldnormalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.cldnormalized
  OWNER TO cineast;

CREATE INDEX cld_shotidnormalized_idx
  ON features.cldnormalized
  USING btree
  (shotid);


CREATE TABLE features.contrast
(
  shotid bigint NOT NULL,
  lmin real,
  lmax real,
  CONSTRAINT "Contrast_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "Contrast_shotId_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.contrast
  OWNER TO cineast;


CREATE INDEX contrast_lmax_idx
  ON features.contrast
  USING btree
  (lmax);


CREATE INDEX contrast_lmin_idx
  ON features.contrast
  USING btree
  (lmin);


CREATE INDEX contrast_shotid_idx
  ON features.contrast
  USING btree
  (shotid);


CREATE TABLE features.dominantcolors
(
  shotid bigint NOT NULL,
  colors feature,
  CONSTRAINT "DominantColors_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "DominantColors_shotId_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.dominantcolors
  OWNER TO cineast;

CREATE INDEX dominantcolors_shotid_idx
  ON features.dominantcolors
  USING btree
  (shotid);
  

CREATE TABLE features.dominantedgegrid16
(
  shotid bigint NOT NULL,
  edges feature,
  CONSTRAINT dominantedgegrid16_pkey PRIMARY KEY (shotid),
  CONSTRAINT dominantedgegrid16_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.dominantedgegrid16
  OWNER TO cineast;

CREATE INDEX dominantedgegrid16_shotid_idx
  ON features.dominantedgegrid16
  USING btree
  (shotid);


CREATE TABLE features.dominantedgegrid8
(
  shotid bigint NOT NULL,
  edges feature,
  CONSTRAINT dominantedgegrid8_pkey PRIMARY KEY (shotid),
  CONSTRAINT dominantedgegrid8_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.dominantedgegrid8
  OWNER TO cineast;


CREATE INDEX dominantedgegrid8_shotid_idx
  ON features.dominantedgegrid8
  USING btree
  (shotid);

  CREATE TABLE features.edgearp88
(
  shotid bigint NOT NULL,
  arp feature,
  CONSTRAINT edgearp88_pkey PRIMARY KEY (shotid),
  CONSTRAINT edgearp88_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.edgearp88
  OWNER TO cineast;


CREATE INDEX edgearp88_shotid_idx
  ON features.edgearp88
  USING btree
  (shotid);
  
CREATE TABLE features.edgearp88full
(
  shotid bigint NOT NULL,
  arp feature,
  CONSTRAINT edgearp88full_pkey PRIMARY KEY (shotid),
  CONSTRAINT edgearp88full_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.edgearp88full
  OWNER TO cineast;

CREATE INDEX edgearp88full_shotid_idx
  ON features.edgearp88full
  USING btree
  (shotid);
  

CREATE TABLE features.edgegrid16
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT edgegrid16_pkey PRIMARY KEY (shotid),
  CONSTRAINT edgegrid16_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.edgegrid16
  OWNER TO cineast;

CREATE INDEX edgegrid16_shotid_idx
  ON features.edgegrid16
  USING btree
  (shotid);
  

CREATE TABLE features.edgegrid16full
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT edgegrid16full_pkey PRIMARY KEY (shotid),
  CONSTRAINT edgegrid16full_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.edgegrid16full
  OWNER TO cineast;

CREATE INDEX edgegrid16full_shotid_idx
  ON features.edgegrid16full
  USING btree
  (shotid);
  
CREATE TABLE features.ehd
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT ehd_pkey PRIMARY KEY (shotid),
  CONSTRAINT ehd_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.ehd
  OWNER TO cineast;


CREATE INDEX ehd_shotid_idx
  ON features.ehd
  USING btree
  (shotid);
  
  
CREATE TABLE features.fulltext
(
  shotid bigint NOT NULL,
  text text,
  tsv tsvector,
  CONSTRAINT fulltext_pkey PRIMARY KEY (shotid),
  CONSTRAINT fulltext_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.fulltext
  OWNER TO cineast;

CREATE INDEX fulltext_shotid_idx
  ON features.fulltext
  USING btree
  (shotid);

CREATE INDEX fulltext_tsv_idx
  ON features.fulltext
  USING gin
  (tsv);

CREATE TRIGGER tsvupdate
  BEFORE INSERT OR UPDATE
  ON features.fulltext
  FOR EACH ROW
  EXECUTE PROCEDURE tsvector_update_trigger('tsv', 'pg_catalog.english', 'text');
  

CREATE TABLE features.huevaluevariancegrid8
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT huevaluevariancegrid8_pkey PRIMARY KEY (shotid),
  CONSTRAINT huevaluevariancegrid8_shtoid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.huevaluevariancegrid8
  OWNER TO cineast;


CREATE INDEX huevaluevariancegrid8_shotid_idx
  ON features.huevaluevariancegrid8
  USING btree
  (shotid);
  

CREATE TABLE features.mediancolor
(
  shotid bigint NOT NULL,
  color feature,
  CONSTRAINT "MedianColor_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "MedianColor_shotId_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.mediancolor
  OWNER TO cineast;

CREATE INDEX mediancolor_shotid_idx
  ON features.mediancolor
  USING btree
  (shotid);
  
  
CREATE TABLE features.mediancolorarp44
(
  shotid bigint NOT NULL,
  arp feature,
  CONSTRAINT mediancolorarp44_pkey PRIMARY KEY (shotid),
  CONSTRAINT mediancolorarp44_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.mediancolorarp44
  OWNER TO cineast;

CREATE INDEX mediancolorarp44_shotid_idx
  ON features.mediancolorarp44
  USING btree
  (shotid);
  
  
CREATE TABLE features.mediancolorarp44normalized
(
  shotid bigint NOT NULL,
  arp feature,
  CONSTRAINT mediancolorarp44normalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT mediancolorarp44normalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.mediancolorarp44normalized
  OWNER TO cineast;

CREATE INDEX mediancolorarp44normalized_shotid_idx
  ON features.mediancolorarp44normalized
  USING btree
  (shotid);
  
  
CREATE TABLE features.mediancolorgrid8
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT mediancolorgrid8_pkey PRIMARY KEY (shotid),
  CONSTRAINT mediancolorgrid8_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.mediancolorgrid8
  OWNER TO cineast;

CREATE INDEX mediancolorgrid8_shotid_idx
  ON features.mediancolorgrid8
  USING btree
  (shotid);
  
CREATE TABLE features.mediancolorgrid8normalized
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT mediancolorgrid8normalized_pkey PRIMARY KEY (shotid),
  CONSTRAINT mediancolorgrid8normalized_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.mediancolorgrid8normalized
  OWNER TO cineast;

CREATE INDEX mediancolorgrid8normalized_shotid_idx
  ON features.mediancolorgrid8normalized
  USING btree
  (shotid);  
  
CREATE TABLE features.mediancolorraster
(
  shotid bigint NOT NULL,
  hist feature,
  raster feature,
  CONSTRAINT mediancolorraster_pkey PRIMARY KEY (shotid),
  CONSTRAINT mediancolorraster_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.mediancolorraster
  OWNER TO cineast;

CREATE INDEX mediancolorraster_shotid_idx
  ON features.mediancolorraster
  USING btree
  (shotid);
  
CREATE TABLE features.medianfuzzyhist
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT "MedianFuzzyHist_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "MedianFuzzyHist_shotid_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.medianfuzzyhist
  OWNER TO cineast;

CREATE INDEX medianfuzzyhist_shotid_idx
  ON features.medianfuzzyhist
  USING btree
  (shotid);
  
    
CREATE TABLE features.medianfuzzyhistnormalized
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT "MedianFuzzyHistnormalized_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "MedianFuzzyHistnormalized_shotid_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.medianfuzzyhistnormalized
  OWNER TO cineast;

CREATE INDEX medianfuzzyhistnormalized_shotid_idx
  ON features.medianfuzzyhistnormalized
  USING btree
  (shotid);
  
CREATE TABLE features.motionhistogram
(
  shotid bigint NOT NULL,
  sum real,
  hist feature,
  CONSTRAINT motionhistogram_pkey PRIMARY KEY (shotid),
  CONSTRAINT motionhistogram_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.motionhistogram
  OWNER TO cineast;


CREATE INDEX motionhistogram_shotid_idx
  ON features.motionhistogram
  USING btree
  (shotid);
  
CREATE TABLE features.saturationandchroma
(
  shotid bigint NOT NULL,
  chroma double precision,
  saturation double precision,
  CONSTRAINT "SaturationAndChroma_pkey" PRIMARY KEY (shotid),
  CONSTRAINT "SaturationAndChroma_shotId_fkey" FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.saturationandchroma
  OWNER TO cineast;

CREATE INDEX saturationandchroma_chroma_idx
  ON features.saturationandchroma
  USING btree
  (chroma);

CREATE INDEX saturationandchroma_saturation_idx
  ON features.saturationandchroma
  USING btree
  (saturation);

CREATE INDEX saturationandchroma_shotid_idx
  ON features.saturationandchroma
  USING btree
  (shotid);
  
CREATE TABLE features.saturationgrid8
(
  shotid bigint NOT NULL,
  grid feature,
  CONSTRAINT saturationgrid8_pkey PRIMARY KEY (shotid),
  CONSTRAINT saturationgrid8_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.saturationgrid8
  OWNER TO cineast;

CREATE INDEX saturationgrid8_shotid_idx
  ON features.saturationgrid8
  USING btree
  (shotid);
  

CREATE TABLE features.stmp7eh
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT stmp7eh_pkey PRIMARY KEY (shotid),
  CONSTRAINT stmp7eh_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.stmp7eh
  OWNER TO cineast;


CREATE INDEX stmp7eh_shotid_idx
  ON features.stmp7eh
  USING btree
  (shotid);


CREATE TABLE features.subdivaveragefuzzycolor
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT subdivaveragefuzzycolor_pkey PRIMARY KEY (shotid),
  CONSTRAINT subdivaveragefuzzycolor_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.subdivaveragefuzzycolor
  OWNER TO cineast;

CREATE INDEX subdivaveragefuzzycolor_shotid_idx
  ON features.subdivaveragefuzzycolor
  USING btree
  (shotid);
  

CREATE TABLE features.subdivmedianfuzzycolor
(
  shotid bigint NOT NULL,
  hist feature,
  CONSTRAINT subdivmedianfuzzycolor_pkey PRIMARY KEY (shotid),
  CONSTRAINT subdivmedianfuzzycolor_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.subdivmedianfuzzycolor
  OWNER TO cineast;

CREATE INDEX subdivmedianfuzzycolor_shotid_idx
  ON features.subdivmedianfuzzycolor
  USING btree
  (shotid);
  
CREATE TABLE features.subdivmotionhistogram2
(
  shotid bigint NOT NULL,
  sums feature,
  hists feature,
  CONSTRAINT subdivmotionhistogram2_pkey PRIMARY KEY (shotid),
  CONSTRAINT subdivmotionhistogram2_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.subdivmotionhistogram2
  OWNER TO cineast;

CREATE INDEX subdivmotionhistogram2_shotid_idx
  ON features.subdivmotionhistogram2
  USING btree
  (shotid);

CREATE TABLE features.subdivmotionhistogram3
(
  shotid bigint NOT NULL,
  sums feature,
  hists feature,
  CONSTRAINT subdivmotionhistogram3_pkey PRIMARY KEY (shotid),
  CONSTRAINT subdivmotionhistogram3_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.subdivmotionhistogram3
  OWNER TO cineast;

CREATE INDEX subdivmotionhistogram3_shotid_idx
  ON features.subdivmotionhistogram3
  USING btree
  (shotid);

CREATE TABLE features.subdivmotionhistogram4
(
  shotid bigint NOT NULL,
  sums feature NOT NULL,
  hists feature,
  CONSTRAINT subdivmotionhistogram4_pkey PRIMARY KEY (shotid),
  CONSTRAINT subdivmotionhistogram4_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.subdivmotionhistogram4
  OWNER TO cineast;

CREATE INDEX subdivmotionhistogram4_shotid_idx
  ON features.subdivmotionhistogram4
  USING btree
  (shotid);
  
CREATE TABLE features.subdivmotionhistogram5
(
  shotid bigint NOT NULL,
  sums feature NOT NULL,
  hists feature,
  CONSTRAINT subdivmotionhistogram5_pkey PRIMARY KEY (shotid),
  CONSTRAINT subdivmotionhistogram5_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.subdivmotionhistogram5
  OWNER TO cineast;

CREATE INDEX subdivmotionhistogram5_shotid_idx
  ON features.subdivmotionhistogram5
  USING btree
  (shotid);
  

CREATE TABLE features.surf
(
  shotid bigint,
  surf feature,
  CONSTRAINT surf_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.surf
  OWNER TO cineast;

CREATE INDEX surf_shotid_idx
  ON features.surf
  USING btree
  (shotid);
  
CREATE TABLE features.surffull
(
  shotid bigint,
  surf feature,
  CONSTRAINT surf_shotid_fkey FOREIGN KEY (shotid)
      REFERENCES shots (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE features.surffull
  OWNER TO cineast;

CREATE INDEX surffull_shotid_idx
  ON features.surffull
  USING btree
  (shotid);