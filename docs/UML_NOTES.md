# Catatan UML

Dokumen ini berfungsi sebagai panduan untuk menggambar UML berdasarkan implementasi proyek saat ini, bukan berdasarkan rencana awal semata.

## 1. Diagram class domain yang paling penting

Fokus utama UML sebaiknya dimulai dari domain battle karena di sanalah pilar OOP paling jelas terlihat.

```text
<<interface>> Attackable
+ receiveDamage(damage: int): void
+ isAlive(): boolean
+ getName(): String

<<abstract>> Character implements Attackable
- name: String
- maxHp: int
- hp: int
- attackPower: int
- defense: int
- attackMultiplier: double
- defenseMultiplier: double
- defending: boolean
+ getName(): String
+ getMaxHp(): int
+ getHp(): int
+ setHp(hp: int): void
+ getAttackPower(): int
+ getDefense(): int
+ setAttackMultiplier(multiplier: double): void
+ setDefenseMultiplier(multiplier: double): void
+ heal(amount: int): void
+ restoreTurnModifiers(): void
+ defend(): void
+ stopDefending(): void
+ isDefending(): boolean
+ receiveDamage(damage: int): void
+ isAlive(): boolean
+ attack(target: Character): int
+ attack(target: Character, bonusDamage: int): int
+ attack(target: Character, skillName: String): int

<<interface>> SkillUser
+ useSkill(target: Character): int

Player extends Character implements SkillUser
- level: int
- experience: int
- score: int
- potionCount: int
- fireballCharges: int
+ usePotion(): boolean
+ gainExperience(amount: int): void
+ addScore(amount: int): void
+ attack(target: Character): int
+ useSkill(target: Character): int

<<abstract>> Enemy extends Character
- rewardExperience: int
- rewardScore: int
+ getRewardExperience(): int
+ getRewardScore(): int

Goblin extends Enemy implements SkillUser
OrcWarrior extends Enemy
DragonBoss extends Enemy implements SkillUser
```

## 2. Relasi yang wajib tampil

Relasi minimal yang sebaiknya ada pada UML:

- inheritance `Character -> Player`
- inheritance `Character -> Enemy`
- inheritance `Enemy -> Goblin`
- inheritance `Enemy -> OrcWarrior`
- inheritance `Enemy -> DragonBoss`
- interface realization `Character -> Attackable`
- interface realization `Player -> SkillUser`
- interface realization `Goblin -> SkillUser`
- interface realization `DragonBoss -> SkillUser`

## 3. Relasi service layer

Untuk diagram yang lebih lengkap, tambahkan juga:

```text
BattleService --> RandomEventService
BattleService --> Player
BattleService --> Enemy
RandomEventService --> Player
RandomEventService --> Character
EnemyFactory --> Enemy
```

Catatan:

- `BattleService` tidak "memiliki" player dan enemy sebagai field permanen, jadi relasinya lebih tepat dianggap dependency/uses daripada composition.
- `BattleService` memang memiliki field `randomEventService`, sehingga relasi ke `RandomEventService` lebih kuat.

## 4. Relasi UI yang penting

Untuk diagram arsitektur tingkat aplikasi, relasi berikut juga relevan:

```text
Main --> StartingScreenFrame
StartingScreenFrame --> WorldFrame
WorldFrame --> ui.world.GamePanel
ui.world.GamePanel --> TiledMapLoader
ui.world.GamePanel --> ui.world.Player
ui.world.GamePanel --> KeyboardInput
ui.world.GamePanel --> EnemySpawnPoint
core.Game --> ui.BattleGUI
ui.BattleGUI --> BattleService
ui.BattleGUI --> Player
ui.BattleGUI --> Enemy
```

## 5. Hal yang perlu dijelaskan saat mempresentasikan UML

Jangan hanya menunjukkan garis relasi. Jelaskan juga makna desainnya:

1. `Character` dijadikan abstract karena tidak pernah diinstansiasi langsung.
2. `Enemy` dijadikan abstract karena hanya musuh konkret yang benar-benar dipakai game.
3. `SkillUser` adalah interface opsional, sehingga tidak semua subclass `Enemy` harus memiliki skill.
4. `BattleService` adalah orchestration layer, bukan entity.
5. `ui.world.Player` berbeda dari `entities.Player`, karena satu mewakili sprite di map dan satu lagi mewakili combat actor.

## 6. Diagram yang disarankan untuk laporan

Untuk laporan kuliah, paling aman gunakan dua diagram:

1. `Class Diagram Domain Battle`
   Fokus pada `Attackable`, `SkillUser`, `Character`, `Player`, `Enemy`, dan subclass musuh.
2. `Application/Package Interaction Diagram`
   Fokus pada hubungan `Main`, start screen, world, battle UI, dan service layer.

Pendekatan ini membuat UML lebih mudah dibaca daripada memaksa seluruh proyek ke satu diagram besar yang padat.

