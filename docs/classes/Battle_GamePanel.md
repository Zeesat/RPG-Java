# Dokumentasi Kelas: `fantasyrpg.ui.battle.GamePanel`

Kelas `fantasyrpg.ui.battle.GamePanel` merupakan komponen utama dalam sistem pertarungan (battle engine) berbasis GUI. Kelas ini memproses perenderan karakter, efek visual, suara ending, dan transisi pertarungan turn-based.

## Informasi Dasar
- **Package**: `fantasyrpg.ui.battle`
- **Superclass**: `javax.swing.JPanel`
- **Interface**: `java.awt.event.ActionListener`

## Field Utama
- `private fantasyrpg.entities.Player playerEntity`: Model PBO pahlawan/hero.
- `private fantasyrpg.entities.Enemy enemyEntity`: Model PBO musuh yang dihadapi.
- `private int heroHp`, `enemyHp`: Status HP saat pertarungan berlangsung.
- `private int maxSkillUses`, `skillUsesLeft`: Batasan penggunaan skill fireball.
- `private BufferedImage bgImg`: Gambar latar belakang aktif.
- `private BufferedImage playerImg`: Gambar sprite pahlawan.
- `private BufferedImage endScreenImg`: Gambar overlay penutup game.
- `private final List<FloatingText> floatingTexts`: Menyimpan objek angka damage melayang.
- `private final Queue<String> effectQueue`: Antrean urutan efek animasi pertarungan.

## Method Utama
- `private void initBattle()`: Menginisialisasi entitas player dan musuh, mengkalkulasi level player secara adaptif berdasarkan progres kekalahan musuh global di `GameState`, dan mereset status cooldown serta skill.
- `private void drawCharacters(Graphics2D g)`: Menggambar bayangan karakter, sprite player, sprite musuh, efek aura defensif, ledakan sihir, flash tebasan serangan, dan kedipan lampu HP rendah secara dinamis.
- `private void handleAction(String action)`: Memproses pilihan aksi tombol (Attack, Skill, Defend, Potion, Run).
- `private void doAttack()`, `doSkill()`, `doDefend()`: Melaksanakan kalkulasi formula damage OOP pahlawan terhadap musuh dan sebaliknya.
- `private void enemyTurnIfAlive()`: Menjalankan AI giliran musuh untuk menyerang atau menggunakan skill khusus.
- `private void handleEnemyDefeat()`: Menangani akhir pertempuran. Jika musuh adalah Dragon Boss, memutar musik `"ENDING"`, memblokir tombol aksi, dan mengaktifkan overlay end screen selama 5 detik sebelum menutup game dengan bersih via `System.exit(0)`.
