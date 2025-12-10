# Items tracker
<img alt="Image showing the HUD" src="https://github.com/user-attachments/assets/97de2132-2476-4d4d-9287-ceb469e8f90c" />

This relatively simple fabric mod tracks which unique items that have been picked up by player(s) in a specific world.
This mod does work server-side, but to see the counter you have to have it also installed client-side.

## Basic usage
Press `X`(keybind can be changed) to open screen containing a list of items that were collected, and ones that were not.

<img alt="Remaining items screen" src="https://github.com/user-attachments/assets/cf8ec83e-6237-49f0-8dbf-0850bcc8d510" />


Client-side HUD can be configured by using `/itemstracker config` command

## Blacklist
This mod fetches all items that the game had registered, which includes ones not obtainable in survival. Thats why there is a `blacklist.txt` file that dictates which items should be excluded from counting towards the collection.
It is located at `.minecraft/config/itemstracker/blacklist.txt`. You might want to add things to it if you for example have installed a mod that adds some unobtainable items.

By using a frase like: `birch` you will exclude all items that have `birch` in their item id, so for example both `birch_log` and `birch_planks` and more will be excluded. <br>If instead you use a frase prefixed with `minecraft:` like `minecraft:sand` the mod will only exclude `minecraft:sand` and wont exclude `minecraft:sandstone`
