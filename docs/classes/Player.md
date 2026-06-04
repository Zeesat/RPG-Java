# Dokumentasi Kelas: `fantasyrpg.entities.Player`

Kelas `fantasyrpg.entities.Player` adalah representasi dari pahlawan (hero) yang dikontrol oleh pemain. Mengatur status level-up, kalkulasi damage berbasis formula PBO, penggunaan potion penyembuh, dan pelacakan cooldown skill fireball.

## Informasi Dasar
- **Package**: `fantasyrpg.entities`
- **Superclass**: `fantasyrpg.entities.Character`
- **Interface**: `fantasyrpg.interfaces.SkillUser`

## Field Utama
- `private int maxFireballCharges`, `fireballCharges`: Jumlah maksimal dan sisa skill fireball.
- `private int skillCooldown`: Waktu tunggu skill fireball (1 turn).
- `private int potionCount`: Jumlah sisa ramuan penyembuh (3 buah).
- `private int potionCooldown`: Waktu tunggu ramuan penyembuh (3 turn).
- `private int level`: Level pahlawan saat ini (dimulai dari level 1, naik s.d level 5).
- `private int experience`: Poin pengalaman bertualang.
- `private int score`: Skor pertempuran pemain.

## Method Utama
- `public void tickCooldowns()`: Mengurangi sisa turn cooldown skill dan potion sebanyak 1.
- `public boolean canUseFireball()`, `canUsePotion()`: Memeriksa kelayakan penggunaan aksi.
- `public boolean usePotion()`: Memulihkan HP sebesar `22 + (level * 4)` dan memicu cooldown ramuan.
- `public void gainExperience(int amount)`: Menambah EXP dan memicu level-up jika melewati batas tertentu.
- `private void levelUp()`: Meningkatkan statistik pahlawan (Level +1, Attack Power +3, Defense +1, memulihkan HP +20).
- `public int attack(Character target)`: Serangan fisik biasa dengan variasi acak damage dan bonus level.
- `public int useSkill(Character target)`: Serangan bola api berdamage besar dan memicu cooldown.
