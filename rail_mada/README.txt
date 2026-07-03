oex export
==========

Generated:        2026-06-23 18:21:49 UTC
oex version:      0.4.1
Project:          https://github.com/osgeonepal/oex

Country (ISO3):   MDG
Boundary:         geoBoundaries CGAZ ADM0
Bounding box:     (43.2202, -25.6071, 50.4863, -11.9520)

Dataset:          railways
Format:           ESRI Shapefile (shp)
Features:         877

Source:           OpenStreetMap contributors
Source URL:       https://www.openstreetmap.org/
Snapshot:         2026-06-19
License:          hdx-odc-odbl
License URL:      https://opendatacommons.org/licenses/odbl/1-0/

About the source
  OpenStreetMap is a community-edited geographic dataset of the world. Country
  features are extracted from the source PBF via quackosm with the union of
  all category tag filters; per-category exports apply tag predicates at query
  time.

Notes
  - Shapefile output is split by geometry type:
    <category>_polygons.shp, <category>_lines.shp, <category>_points.shp.
    This is a shapefile-format limitation, not a data limitation.
  - Field names are truncated to 10 characters in shp; gpkg keeps them full.

Feedback:         https://github.com/osgeonepal/oex/issues
Engine: geofabrik