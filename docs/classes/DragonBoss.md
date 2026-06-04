# Dokumentasi Kelas: `fantasyrpg.entities.DragonBoss`

Kelas `fantasyrpg.entities.DragonBoss` merepresentasikan bos akhir naga Azhrax di Map 2 (Dungeon). Kelas ini mewarisi properti musuh dan mengimplementasikan keahlian menggunakan skill mematikan.

## Informasi Dasar
- **Package**: `fantasyrpg.entities`
- **Superclass**: `fantasyrpg.entities.Enemy`
- **Interface**: `fantasyrpg.interfaces.SkillUser`

## Method Utama
- `public int attack(Character target)`: Melancarkan serangan fisik naga Azhrax dengan kalkulasi damage dasar `calculateBaseAttack() + 5` (sekitar 27 damage).
- `public int useSkill(Character target)`: Mengembuskan skill legendaris `DRAGON BREATH` berdamage api masif `calculateBaseAttack() + 12` (sekitar 34 damage).
