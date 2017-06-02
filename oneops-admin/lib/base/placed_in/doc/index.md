This relation is used to link deployed instances (bom instances) with cloud zones.
If a given instance uses zone level services it should declare these zones in its recipe and the corresponding
"base.PlacedIn" relations will be added between the instance and cloud zones.

