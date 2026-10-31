# DailySpend – Everyday Expense Manager (Android)

Native Android app: Kotlin + Jetpack Compose + Room (offline, data stays on the phone).

## Features
- Add / edit / delete expenses and income (amount, title, category, date, note)
- Monthly balance, income, spending, with month switcher
- Optional monthly budget with progress bar and "over budget" warning
- Stats: last-6-months spending chart and per-category breakdown
- Currency symbol setting (default ₹)
- Export all data to CSV (share to Drive, email, WhatsApp, etc.)
- Light/dark theme, Material You colours on Android 12+

## 1. Open and run on your phone
1. Install **Android Studio** (latest stable): https://developer.android.com/studio
2. `File > Open` and choose this `ExpenseTracker` folder. Let Gradle sync finish (first sync downloads dependencies; if it asks to update the Android Gradle Plugin or Gradle, accept).
3. On your phone: `Settings > About phone`, tap **Build number** 7 times, then `Settings > Developer options > USB debugging` ON.
4. Connect the phone by USB, accept the prompt on the phone, choose it in the device dropdown, press **Run ▶**.

To get an installable APK file instead: `Build > Build Bundle(s) / APK(s) > Build APK(s)`. Copy `app/build/outputs/apk/debug/app-debug.apk` to the phone and open it (allow "install unknown apps").

## 2. Before publishing: make it yours
- Change `applicationId` and `namespace` in `app/build.gradle.kts` from `com.dailyspend.app` to your own unique ID (for example `com.yourname.dailyspend`). It cannot be changed after upload. If you rename the namespace, also rename the Kotlin package folders (Android Studio: right-click the package > Refactor > Rename) and update the `.fileprovider` authority if needed (it uses `${applicationId}` already).
- Change the app name in `res/values/strings.xml` and the icon in `res/drawable/ic_launcher_foreground.xml` (or use `File > New > Image Asset`).
- Check `compileSdk`/`targetSdk` in `app/build.gradle.kts`. Google Play requires new apps and updates to target a recent API level; check the current requirement at https://developer.android.com/google/play/requirements/target-sdk and bump if needed.

## 3. Create a signed release bundle (.aab)
1. Create an upload keystore: `Build > Generate Signed Bundle / APK > Android App Bundle > Create new...`. **Back up the .jks file and passwords somewhere safe** – losing it makes updates painful.
2. (Optional, for command-line builds) create `keystore.properties` in the project root (never commit it):
   ```
   storeFile=upload-keystore.jks
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=upload
   keyPassword=YOUR_KEY_PASSWORD
   ```
   Then run `./gradlew bundleRelease`. Output: `app/build/outputs/bundle/release/app-release.aab`.
3. For each update, increase `versionCode` (and `versionName`) in `app/build.gradle.kts`.

## 4. Publish on Google Play
1. Create a Play Console developer account: https://play.google.com/console (one-time registration fee; personal accounts need identity verification).
2. `Create app`: name, language, App, Free.
3. Complete the setup tasks:
   - **Privacy policy URL** – required. The app collects no data and has no internet permission; say so in a short page hosted anywhere (GitHub Pages, Google Sites).
   - **Data safety** form: no data collected or shared (data stays on device).
   - Content rating questionnaire, target audience, ads declaration (No ads).
4. Store listing: short description, full description, app icon 512×512, feature graphic 1024×500, at least 2 phone screenshots.
5. Upload the `.aab` to a **Testing** track first. Note: new *personal* developer accounts must run a closed test with a minimum number of testers for a minimum period before they can apply for production; the Play Console shows the current requirement.
6. Promote to **Production** and submit for review (usually a few days).

## Ideas for next versions
Recurring expenses, reminders, per-category budgets, backup/restore, PIN or fingerprint lock, multi-currency.
