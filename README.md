# Inventari — Aplikacion Android (APK)

Aplikacion native Android që hap sistemin e inventarit (`http://13.140.152.29/`)
në një WebView. Punon në çdo telefon **Android 5.0+** (mbi 99% e pajisjeve).

Veçori:
- Ekran i plotë, pa shfletues
- Ruajtje token-i (localStorage) — qëndron i kyçur
- Butoni **Back** kthen mbrapsht brenda app-it
- **Pull-to-refresh** (tërhiq poshtë për të rifreskuar)
- **Shkarkim Excel** ruhet te dosja *Downloads*

---

## 📦 Si ta marrësh APK-në — 3 mënyra

### Mënyra A — GitHub Actions (e rekomanduar, pa instaluar asgjë)

1. Krijo një llogari falas te [github.com](https://github.com) (nëse s'ke).
2. Krijo një repository të ri (p.sh. `inventari-android`).
3. Ngarko **të gjithë këtë dosje** te repo-ja:
   - ose me web: "Add file → Upload files" → tërhiq gjithë përmbajtjen
   - ose me git:
     ```
     git init
     git add .
     git commit -m "Inventari Android"
     git branch -M main
     git remote add origin https://github.com/USERI-YT/inventari-android.git
     git push -u origin main
     ```
4. Shko te skeda **Actions** te repo-ja → workflow-u "Build Android APK" niset vetë.
5. Kur mbaron (~3-5 min), kliko run-in → te **Artifacts** shkarko **Inventari-APK**.
6. Brenda zip-it është `Inventari.apk` — dërgoje te telefoni dhe instaloje.

> Nëse Actions s'niset vetë: skeda Actions → "Build Android APK" → **Run workflow**.

### Mënyra B — Android Studio (nëse do ta ndërtosh vetë)

1. Shkarko [Android Studio](https://developer.android.com/studio) (falas).
2. **Open** këtë dosje. Prit sa të bëhet "Gradle sync".
3. Menu: **Build → Build Bundle(s)/APK(s) → Build APK(s)**.
4. APK-ja del te `app/build/outputs/apk/debug/app-debug.apk`.

### Mënyra C — Linje komande (nëse ke JDK 17 + Android SDK)

```
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

---

## 📲 Si ta instalosh APK-në në telefon

1. Kopjo `Inventari.apk` te telefoni (USB, email, ose Drive).
2. Hape skedarin → Android do kërkojë leje **"Instalo nga burime të panjohura"** → lejo.
3. Instalo → hap **Inventari**.

---

## ⚙️ Konfigurim

- **Adresa e serverit:** ndryshohet te `app/src/main/java/.../MainActivity.java`
  (`START_URL`) dhe te `res/xml/network_security_config.xml`.
- **Emri / ikona:** `res/values/strings.xml` dhe `res/drawable/ic_launcher.xml`.

## ⚠️ Siguri
App-i komunikon me serverin përmes **HTTP** (jo HTTPS). Për përdorim publik,
rekomandohet të vihet serveri pas një domaini me **HTTPS** (Let's Encrypt) dhe
të përditësohet `START_URL`. Pa HTTPS, kredencialet udhëtojnë të pakriptuara.

## Paketa
`al.mediaproduction.inventari` · versionCode 1 · minSdk 21 · targetSdk 34
