# sprite-exporter
Exports OSRS sprites with correct sizing


##  Command

`./gradlew dumpSprites`

### Properties

| Property        | Default          | Description                                                       |
| --------------- | ---------------- | ----------------------------------------------------------------- |
| `cache.new`     | `<empty>`        | The cache revision to fetch. Empty version will fetch the latest. |
| `cache.old`     | `<empty>`        | The previous cache revision. Empty version will fetch the latest. |
| `cache.folder`  | `cache`          | Folder to download and dump cache revisions to.                   |
| `cache.sprites` | `sprites`        | Folder under `cache/REVISION` to dump sprites                     |
