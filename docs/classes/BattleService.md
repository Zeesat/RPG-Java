# Dokumentasi Kelas: `fantasyrpg.services.BattleService`

Kelas `fantasyrpg.services.BattleService` berisi logika dasar alur pertarungan berbasis giliran (turn-based flow). Kelas ini bertindak sebagai pengendali pertempuran teks (konsol) dan pembantu rumus pertempuran grafis.

## Informasi Dasar
- **Package**: `fantasyrpg.services`

## Field Utama
- `private final RandomEventService randomEventService`: Layanan pemicu kejadian ronde acak.

## Method Utama
- `public boolean startBattle(Player player, Enemy enemy, Scanner scanner)`: Memulai eksekusi pertempuran simulasi di konsol berbasis masukan teks pengguna.
- `public String beginRound(Player player, Enemy enemy)`: Memulai ronde, menyegarkan parameter modifier turn, memperbarui giliran cooldown, dan memicu event acak di awal ronde.
- `public ActionResult executePlayerAction(Player player, Enemy enemy, PlayerAction action)`: Mengeksekusi aksi yang dipilih player (Attack, Skill, Defend, Potion) dan menghasilkan damage beserta status log.
- `public ActionResult executeEnemyTurn(Player player, Enemy enemy)`: Mengendalikan kecerdasan buatan musuh untuk memilih serangan biasa atau skill khusus berdasarkan tingkat persentase sisa HP-nya.
- `public ActionResult applyVictoryRewards(Player player, Enemy enemy)`: Memberikan poin hadiah kemenangan berupa EXP dan skor untuk pahlawan.
