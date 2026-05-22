# Referensi Kelas dan Interface

Dokumen ini merangkum seluruh file Java pada proyek agar pembaca bisa mengenali peran setiap kelas tanpa harus membuka source satu per satu.

## 1. Ringkasan per paket

| Paket | Isi utama | Peran |
| --- | --- | --- |
| `fantasyrpg` | `Main` | bootstrap aplikasi |
| `fantasyrpg.core` | `Game` | bootstrap battle GUI berbasis domain |
| `fantasyrpg.entities` | `Character`, `Player`, `Enemy`, `Goblin`, `OrcWarrior`, `DragonBoss` | model domain battle |
| `fantasyrpg.interfaces` | `Attackable`, `SkillUser` | kontrak perilaku |
| `fantasyrpg.services` | `BattleService`, `RandomEventService`, `EnemyFactory` | aturan permainan/orchestration |
| `fantasyrpg.ui` | `BattleGUI` | battle GUI yang memakai domain layer |
| `fantasyrpg.ui.start` | `StartingScreenFrame` | layar awal |
| `fantasyrpg.ui.world` | frame, panel, loader, sprite player, collision, spawn marker | world exploration |
| `fantasyrpg.ui.battle` | frame, panel, action panel, health bar | battle prototype/self-contained |
| `fantasyrpg.util` | `ConsoleFormatter` | helper output console |

## 2. Paket `fantasyrpg`

### `Main`

- Tipe: class
- Peran: entry point utama aplikasi.
- Tanggung jawab: memanggil `StartingScreenFrame.showScreen()`.
- Dependensi utama: `fantasyrpg.ui.start.StartingScreenFrame`.
- Catatan: inilah entry point yang paling representatif untuk flow aplikasi saat ini.

## 3. Paket `fantasyrpg.core`

### `Game`

- Tipe: class
- Peran: bootstrap battle GUI langsung.
- Tanggung jawab:
  - membuat `entities.Player`,
  - menyiapkan `JFrame`,
  - memasang `ui.BattleGUI`.
- Dependensi utama: `entities.Player`, `ui.BattleGUI`, Swing.
- Catatan OOP: bukan domain object, tetapi composition root kecil untuk merakit object graph battle.

## 4. Paket `fantasyrpg.interfaces`

### `Attackable`

- Tipe: interface
- Method:
  - `receiveDamage(int damage)`
  - `boolean isAlive()`
  - `String getName()`
- Peran: mendefinisikan kontrak minimum objek yang bisa ikut dalam sistem combat.
- Implementor utama: `Character` melalui inheritance chain.

### `SkillUser`

- Tipe: interface
- Method:
  - `int useSkill(Character target)`
- Peran: memisahkan kemampuan skill dari tipe dasar karakter.
- Implementor utama: `entities.Player`, `Goblin`, `DragonBoss`.

## 5. Paket `fantasyrpg.entities`

### `Character`

- Tipe: abstract class
- Peran: fondasi semua karakter tempur.
- Field penting:
  - `name`
  - `maxHp`
  - `hp`
  - `attackPower`
  - `defense`
  - `attackMultiplier`
  - `defenseMultiplier`
  - `defending`
- Method penting:
  - getter untuk atribut inti,
  - `setHp(...)`,
  - `setAttackMultiplier(...)`,
  - `setDefenseMultiplier(...)`,
  - `heal(...)`,
  - `restoreTurnModifiers()`,
  - `defend()`,
  - `stopDefending()`,
  - `receiveDamage(...)`,
  - `attack(Character target)` sebagai abstract method,
  - overload `attack(Character, int)` dan `attack(Character, String)`.
- Catatan desain:
  - `Character` mengimplementasikan `Attackable`.
  - Field sensitif dibuat `private`.
  - Setter `setAttackPower` dan `setDefense` dibuat `protected`, artinya hanya subclass yang boleh mengubah stat dasar.

### `Player` pada `fantasyrpg.entities`

- Tipe: concrete class
- Inheritance: `Player extends Character implements SkillUser`
- Peran: model pemain untuk domain battle.
- Field penting:
  - `level`
  - `experience`
  - `score`
  - `potionCount`
  - `fireballCharges`
  - `MAX_FIREBALL_CHARGES`
- Method penting:
  - `usePotion()`
  - `gainExperience(...)`
  - `addScore(...)`
  - `attack(...)` override
  - `useSkill(...)` override
  - `resetFireballCharges()`
  - `canUseFireball()`
- Catatan desain:
  - Menunjukkan gabungan inheritance + interface implementation.
  - `levelUp()` bersifat private, sehingga logika level-up tidak bocor ke luar class.

### `Enemy`

- Tipe: abstract class
- Inheritance: `Enemy extends Character`
- Peran: base class semua musuh.
- Field penting:
  - `rewardExperience`
  - `rewardScore`
- Method penting:
  - `getRewardExperience()`
  - `getRewardScore()`
- Catatan desain:
  - menambahkan properti reward yang tidak dimiliki semua `Character`.

### `Goblin`

- Tipe: concrete class
- Inheritance: `Goblin extends Enemy implements SkillUser`
- Peran: enemy yang punya basic attack dan skill.
- Method penting:
  - `attack(...)`
  - `useSkill(...)`
- Catatan:
  - override damage formula sederhana,
  - menjadi contoh musuh yang memenuhi kontrak `SkillUser`.

### `OrcWarrior`

- Tipe: concrete class
- Inheritance: `OrcWarrior extends Enemy`
- Peran: enemy tanpa skill khusus.
- Method penting:
  - `attack(...)`
- Catatan:
  - penting untuk menunjukkan bahwa tidak semua `Enemy` wajib menjadi `SkillUser`.

### `DragonBoss`

- Tipe: concrete class
- Inheritance: `DragonBoss extends Enemy implements SkillUser`
- Peran: boss dengan damage lebih tinggi dan special attack.
- Method penting:
  - `attack(...)`
  - `useSkill(...)`
- Catatan:
  - di `BattleService`, class ini juga dipakai untuk bonus reward dan penamaan skill khusus.

## 6. Paket `fantasyrpg.services`

### `EnemyFactory`

- Tipe: class
- Peran: membuat enemy berdasarkan stage.
- Method:
  - `createStageEnemy(int stage, Random random)`
- Perilaku:
  - stage <= 1 -> `Goblin`
  - stage == 2 -> `OrcWarrior`
  - stage lain -> `DragonBoss`
- Catatan desain:
  - pola factory sudah mulai diterapkan,
  - parameter `Random random` belum dipakai sehingga integrasinya belum selesai.

### `RandomEventService`

- Tipe: class
- Peran: menghasilkan event acak per ronde.
- Dependensi:
  - `Random`
  - `entities.Player`
  - `entities.Character`
- Method:
  - `triggerRoundEvent(Player player, Character enemy)`
- Perilaku:
  - meningkatkan attack multiplier,
  - meningkatkan defense multiplier,
  - memberi heal,
  - menurunkan defense enemy,
  - atau tidak ada event.
- Catatan desain:
  - service ini mengubah state object domain, bukan sekadar mengembalikan deskripsi.

### `BattleService`

- Tipe: class
- Peran: pusat orchestration battle.
- Inner type:
  - `PlayerAction` enum
  - `ActionResult` static class
- Method penting:
  - `startBattle(...)`
  - `beginRound(...)`
  - `executePlayerAction(...)`
  - `executeEnemyTurn(...)`
  - `applyVictoryRewards(...)`
- Peran teknis:
  - memisahkan rule battle dari UI,
  - mengubah input aksi menjadi perubahan state pada domain,
  - menghasilkan object hasil aksi untuk ditampilkan UI.
- Catatan desain:
  - salah satu class paling penting di proyek ini untuk pembelajaran OOP dan layering.

### `BattleService.PlayerAction`

- Tipe: enum
- Nilai:
  - `ATTACK`
  - `SKILL`
  - `DEFEND`
  - `POTION`
- Peran:
  - menormalisasi opsi aksi player sehingga UI tidak perlu menyimpan logika string bebas.

### `BattleService.ActionResult`

- Tipe: static nested class
- Field:
  - `message`
  - `damage`
- Peran:
  - value object kecil untuk mengembalikan hasil eksekusi aksi.
- Manfaat:
  - UI dapat menampilkan pesan dan damage tanpa harus mengetahui detail perhitungan internal.

## 7. Paket `fantasyrpg.ui`

### `BattleGUI`

- Tipe: class, extends `JPanel`
- Peran: battle GUI berbasis domain layer.
- State penting:
  - `player`
  - `currentEnemy`
  - `battleService`
  - `battleLogs`
  - `stage`
  - `round`
  - banyak field animasi seperti `playerOffsetX`, `bossOpacity`, `showFireball`
- Tanggung jawab:
  - memuat image,
  - menerima input keyboard/mouse,
  - menjalankan animasi,
  - memanggil `BattleService`,
  - merender HP bar, log, overlay kemenangan/kekalahan.
- Catatan desain:
  - contoh nyata pemisahan rule dan UI,
  - tetapi class ini cukup besar sehingga sudah mengandung beberapa concern sekaligus: input, animation, scene state, dan rendering.

## 8. Paket `fantasyrpg.ui.start`

### `StartingScreenFrame`

- Tipe: class, extends `JFrame`
- Peran: layar awal aplikasi.
- Tanggung jawab:
  - menyiapkan window,
  - memasang panel internal,
  - mengarahkan user ke world scene.
- Catatan:
  - ada guard `movingToWorld` untuk mencegah double transition.

### `StartingScreenFrame.StartingScreenPanel`

- Tipe: private static nested class, extends `JPanel`
- Peran: panel internal untuk render teks start screen dan menangkap input keyboard.
- Catatan:
  - karena private nested class, implementasinya tersembunyi dari package lain.

## 9. Paket `fantasyrpg.ui.world`

### `Main`

- Tipe: class
- Peran: entry point alternatif untuk world scene.

### `WorldFrame`

- Tipe: class, extends `JFrame`
- Peran: container utama untuk `ui.world.GamePanel`.
- Tanggung jawab:
  - set frame properties,
  - membuat `GamePanel`,
  - memulai game thread.

### `GameFrame`

- Tipe: class, extends `JFrame`
- Peran: container alternatif yang pada praktiknya duplikat `WorldFrame`.
- Catatan:
  - secara teknis fungsinya hampir sama dengan `WorldFrame`.

### `GamePanel` pada `ui.world`

- Tipe: class, extends `JPanel`, implements `Runnable`
- Peran: inti world exploration.
- Tanggung jawab:
  - memegang world loop 60 FPS,
  - memuat map dan collision,
  - memegang player sprite,
  - merender map bawah dan atas,
  - menempatkan placeholder monster,
  - memicu transisi ke battle.
- Dependensi utama:
  - `KeyboardInput`
  - `ui.world.Player`
  - `TiledMapLoader`
  - `CollisionBlock`
  - `EnemySpawnPoint`
  - `EnemySpawnConfig`
- Catatan:
  - class ini adalah pusat koordinasi scene world.

### `KeyboardInput`

- Tipe: class, implements `KeyListener`
- Peran: menyimpan state input WASD dan tombol `E`.
- Method penting:
  - `keyPressed(...)`
  - `keyReleased(...)`
  - `consumeInteractPressed()`
- Catatan:
  - memakai pola stateful input buffer sederhana.

### `Player` pada `ui.world`

- Tipe: class
- Peran: sprite player pada peta.
- Field penting:
  - `x`, `y`
  - `speed`
  - sprite `front/back/left/right`
  - `currentSprite`
  - `solidArea`
- Method penting:
  - `loadPlayerImages()`
  - `update()`
  - `draw(Graphics2D g2)`
- Catatan:
  - ini bukan `entities.Player`.
  - class ini lebih dekat ke "world actor/sprite controller" daripada domain combat model.

### `TiledMapLoader`

- Tipe: class
- Peran: parser TMX/TSX untuk map world.
- Data penting:
  - `mapLayers`
  - `mapLayerNames`
  - `tiles`
  - `collisions`
  - `spawnX`, `spawnY`
  - `enemySpawnPoints`
- Method penting:
  - `loadMap(...)`
  - `loadTSX(...)`
  - `loadCollisionObjects(...)`
  - `loadSpawnPoint(...)`
  - `loadEnemySpawnPoints(...)`
  - helper parsing attribute/property.
- Catatan:
  - class ini memadukan parsing XML, resource loading, dan pembentukan data runtime.

### `CollisionBlock`

- Tipe: class
- Peran: wrapper sederhana untuk `Rectangle` collision.
- Catatan:
  - field `rectangle` tidak private, sehingga encapsulation masih minimal.

### `EnemySpawnPoint`

- Tipe: class
- Peran: value object untuk koordinat dan `enemyId`.
- Field:
  - `x`
  - `y`
  - `enemyId`
- Catatan:
  - immutable setelah dibuat karena field final dan hanya punya getter.

### `EnemySpawnConfig`

- Tipe: final utility class
- Peran: menentukan style visual marker monster berdasarkan `enemyId`.
- Method penting:
  - `drawPlaceholderMarker(...)`
  - `normalizeEnemyId(...)`
- Nested type:
  - `MarkerStyle`
- Catatan:
  - ini contoh konfigurasi statis untuk rendering yang terpusat.

### `EnemySpawnConfig.MarkerStyle`

- Tipe: private static nested class
- Peran: menyimpan warna dan radius marker.
- Catatan:
  - nested private class ini mencegah detail style bocor keluar utility class.

## 10. Paket `fantasyrpg.ui.battle`

### `MainFrame`

- Tipe: class
- Peran: entry point alternatif untuk battle prototype.

### `GameFrame`

- Tipe: class, extends `JFrame`
- Peran: container Swing untuk `ui.battle.GamePanel`.

### `GamePanel` pada `ui.battle`

- Tipe: class, extends `JPanel`
- Peran: battle scene mandiri.
- State penting:
  - `heroHp`
  - `heroMp`
  - `enemyHp`
  - `turn`
  - `effectQueue`
  - `activeEffect`
  - `battleEnded`
- Tanggung jawab:
  - layout panel,
  - key binding,
  - render karakter dan UI frame,
  - menghitung damage,
  - memutar antrean animasi,
  - menulis battle log.
- Catatan:
  - class ini kuat di sisi visual, tetapi belum terhubung ke domain service battle.

### `ActionPanel`

- Tipe: class, extends `JPanel`
- Peran: tombol aksi battle prototype.
- Tanggung jawab:
  - hit-testing tombol,
  - hover state,
  - selected state,
  - callback `Consumer<String>`.
- Catatan:
  - ini komponen UI reusable di dalam paket battle prototype.

### `HealthBar`

- Tipe: final utility class
- Peran: helper render HP/MP bar.
- Method:
  - `draw(...)`
- Catatan:
  - utility class stateless dan cocok dijadikan helper visual.

## 11. Paket `fantasyrpg.util`

### `ConsoleFormatter`

- Tipe: final utility class
- Peran: helper output console.
- Method:
  - `printSection(...)`
  - `printRound(...)`
- Catatan:
  - cocok untuk CLI prototype dan contoh utility stateless sederhana.

## 12. Ringkasan kelas yang paling penting dipelajari lebih dulu

Jika pembaca ingin memahami proyek dari sisi OOP, lima kelas yang paling penting dibaca lebih dahulu adalah:

1. `fantasyrpg.entities.Character`
2. `fantasyrpg.entities.Player`
3. `fantasyrpg.entities.Enemy`
4. `fantasyrpg.services.BattleService`
5. `fantasyrpg.ui.world.GamePanel`

Urutan ini efektif karena dimulai dari model abstrak, turun ke implementasi konkret, lalu berakhir pada kelas orkestrasi dan scene utama.
