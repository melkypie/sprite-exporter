# sprite-exporter
Exports OSRS sprites with correct sizing


##  Command

`./gradlew generatePack`

### Properties

| Property        | Default          | Description                                                       |
| --------------- | ---------------- | ----------------------------------------------------------------- |
| `cache.new`     | \`<empty>\`      | The cache revision to fetch. Empty version will fetch the latest. |
| `cache.folder`  | `cache`          | Folder to download and dump cache revisions to.                   |
| `cache.pack`    | `sample-vanilla` | Folder to generate sample pack at.                                |
| `cache.sprites` | `sprites`        | Folder under `cache/REVISOIN` to dump sprites                     |
