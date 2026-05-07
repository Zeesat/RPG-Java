# CLI Fantasy RPG - Java OOP Scaffold

Project ini adalah workspace awal untuk game turn-based RPG berbasis Java tanpa framework.
Struktur dan file dibuat agar tim bisa langsung lanjut implementasi tanpa inisiasi manual dari nol.

## Konsep Game

- Genre: Fantasy RPG CLI
- Objective: kalahkan boss akhir
- Core loop: exploration stage -> random enemy -> battle -> level up -> final boss
- Randomizer: buff, multiplier, dan event acak setiap ronde

## Pilar OOP yang Sudah Disiapkan

- Encapsulation: atribut `private` pada entity + getter/setter terkontrol
- Inheritance: `Character` -> `Player` / `Enemy` -> `Goblin`, `OrcWarrior`, `DragonBoss`
- Polymorphism: pemanggilan `attack(...)` dan `useSkill(...)` melalui parent type
- Method Overloading: `attack(target)`, `attack(target, bonusDamage)`, `attack(target, skillName)`
- Method Overriding: implementasi `attack(...)` berbeda di tiap subclass
- Abstract Class: `Character`, `Enemy`
- Interface: `Attackable`, `SkillUser`

## Struktur Folder

```text
src/
  fantasyrpg/
    Main.java
    core/
    entities/
    interfaces/
    services/
    util/
docs/
assets/
scripts/
```

## Cara Lanjut Ngerjain

1. Lengkapi balancing damage, skill, dan jumlah stage.
2. Tambahkan item, equipment, shop, atau quest bila diperlukan.
3. Buat UML dari struktur class yang sudah ada.
4. Isi dokumen analisis di folder `docs`.

## Compile dan Run

Jika JDK sudah terpasang, jalankan:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\compile.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
```


