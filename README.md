# Waystones
This Waystones plugin is to be used to help make travel around servers easier. Providing a custom crafting recipe which people can utilise in survival to teleport around the waystone network.

## Downloading the plugin
There are two main sources of the plugin. 
The first source is on Modrinth at: https://modrinth.com/plugin/waystones-ninjamcr
The second is at PaperMC's Hangar: https://hangar.papermc.io/NinjaMCR/Waystone
Both these sources will always include the latest stable release.

To add the plugin to your server download the jar and add it to your server's plugin folder. On startup you should see the plugin enable in the console log.


## Database migrations

Waystones applies pending SQLite migrations during startup, before loading saved waystones.
The first migration also registers databases created by older releases and preserves their rows.

To change the schema, add a numbered SQL script such as `002-add-description.sql`
under `src/main/resources/database/migrations/` and append its filename to `index.txt`.
The position in the index is the schema version, so keep published scripts and their order fixed.
Scripts may contain multiple SQL statements. Leave transaction control and `PRAGMA user_version`
to the migration runner, which rolls back pending changes if a script fails.
Update the SQL files under `src/main/resources/database/queries/` when the queries need to change.

## License

Waystones is licensed under the PolyForm Noncommercial License 1.0.0.

You are free to use, modify, fork, and redistribute the project for
non-commercial purposes.

Commercial use requires a separate licence from the copyright holder.

If you wish to use Waystones commercially, please contact me.
