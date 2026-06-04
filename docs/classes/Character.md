# Dokumentasi Kelas: `fantasyrpg.entities.Character`

Kelas `fantasyrpg.entities.Character` adalah kelas induk (abstract superclass) dari semua karakter dalam permainan (pahlawan dan musuh). Kelas ini mengatur mekanisme pertahanan, kalkulasi damage dasar, dan penerimaan damage per giliran.

## Informasi Dasar
- **Package**: `fantasyrpg.entities`
- **Interface**: `fantasyrpg.interfaces.Attackable`

## Field Utama
- `private final String name`: Nama karakter.
- `private final int maxHp`: Batas HP maksimal.
- `private int hp`: Sisa HP runtime.
- `private int attackPower`: Nilai kekuatan serang fisik dasar.
- `private int defense`: Nilai tingkat pertahanan dasar.
- `private double attackMultiplier`, `defenseMultiplier`: Pengali status serangan/pertahanan dinamis.
- `private boolean defending`: Status bersiap bertahan.

## Method Utama
- `public void restoreTurnModifiers()`: Mengembalikan multiplier ke default `1.0` dan menonaktifkan status bersiap bertahan (`defending = false`).
- `public void defend()`: Mengaktifkan status bertahan dan menaikkan multiplier pertahanan ke `1.5`.
- `protected int calculateFinalDamage(int rawDamage)`: Mengurangi damage mentah dengan defense yang sudah terpengaruh multiplier.
- `protected int calculateBaseAttack()`: Menghitung attack yang dipengaruhi multiplier.
- `public void receiveDamage(int damage)`: Memotong HP berdasarkan damage bersih. Jika dalam posisi `defending`, damage akhir dipotong lagi sebesar 45% (hanya menerima 55% damage).
- `public boolean isAlive()`: Memeriksa apakah HP masih di atas 0.
- `public abstract int attack(Character target)`: Deklarasi method abstrak serangan dasar karakter.
