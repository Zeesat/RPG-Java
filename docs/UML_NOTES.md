# Catatan UML Awal

Gunakan struktur berikut saat membuat UML:

- `Character` sebagai abstract class
- `Player` mewarisi `Character`
- `Enemy` mewarisi `Character`
- `Goblin`, `OrcWarrior`, `DragonBoss` mewarisi `Enemy`
- `Character` mengimplementasikan `Attackable`
- `Player` dan `DragonBoss` mengimplementasikan `SkillUser`
- `Game` menggunakan `BattleService`, `EnemyFactory`, dan `RandomEventService`

Relasi penting yang bisa ditunjukkan:

- Inheritance
- Interface implementation
- Association antara `Game` dan `Player`
- Dependency dari `BattleService` ke `RandomEventService`

