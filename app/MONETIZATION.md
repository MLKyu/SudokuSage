# Monetization — Setup checklist

The app ships with monetization *interfaces* and the *UX flows* fully wired:

- `monetization/Monetization.kt` — `EntitlementGate`, `AdProvider`, `IapProvider`
- `platform/sync/CloudSync.kt` — `CloudSyncProvider`
- Pro upgrade screen at `Routes.PRO`
- In-game hint gate (3 free hints, then ad/Pro dialog)
- Settings entry "SudokuSage Pro"

All interfaces currently use **No-op implementations** in `AppContainer`. Real
impls swap in via single-line edits — no changes to call sites or UX. This
document lists what external setup each swap requires.

## 1. In-app Purchase (Pro upgrade)

**Required external setup:**
1. Create a Google Play Console listing for the app and get it approved for
   internal testing at minimum.
2. Create an in-app product:
   - Product type: One-time purchase
   - Product ID: `sudoku_sage_pro` (matches `PlayBillingIapProvider.PRODUCT_ID`)
   - Price: ₩4,900 (or your locale equivalent)
   - State: Active
3. Sign the release APK with the same key registered in Play Console.
4. Add testers under Play Console → License testing.

**Code swap (when ready):**
1. Add dependency to `gradle/libs.versions.toml`:
   ```toml
   billing = "7.1.1"
   billing-ktx = { group = "com.android.billingclient", name = "billing-ktx", version.ref = "billing" }
   ```
2. Add to `app/build.gradle.kts`:
   ```kotlin
   implementation(libs.billing.ktx)
   ```
3. Implement `platform/billing/PlayBillingIapProvider.kt` and
   `platform/billing/PlayBillingEntitlementGate.kt` (BillingClient wrapper +
   purchase listener + entitlement query).
4. In `AppContainer`:
   ```kotlin
   val iapProvider: IapProvider = PlayBillingIapProvider(application, appScope)
   val entitlementGate: EntitlementGate = PlayBillingEntitlementGate(iapProvider, appScope)
   ```

The Pro screen and hint-gate flow already call these interfaces correctly.

## 2. Rewarded ads

**Required external setup:**
1. Create an AdMob account and add the app.
2. Get the **App ID** (e.g., `ca-app-pub-1234567890123456~1234567890`).
3. Create a **Rewarded ad unit** and copy its ad-unit ID.
4. Add the App ID to `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-XXXX~XXXX" />
   ```
   For dev/test, AdMob's universal test ID is
   `ca-app-pub-3940256099942544~3347511713` (test app) and
   `ca-app-pub-3940256099942544/5224354917` (test rewarded).

**Code swap:**
1. Add dependency:
   ```toml
   playServicesAds = "23.6.0"
   play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
   ```
2. Implement `platform/ads/AdMobAdProvider.kt`:
   - `MobileAds.initialize(context)` on app start
   - Preload `RewardedAd.load(...)`
   - `showRewarded(activity)` shows ad, suspendCancellableCoroutine for reward callback
3. In `AppContainer`:
   ```kotlin
   val adProvider: AdProvider = AdMobAdProvider(application).also { it.preload() }
   ```

The hint-gate dialog already calls `viewModel.watchAdForHint(activity)` →
`adProvider.showRewarded(activity)`. When the real impl returns true, the user
gets one bonus hint.

## 3. Cloud sync

**Recommended approach — Firebase:**
1. Create a Firebase project; add the Android app; download
   `google-services.json` and place in `app/`.
2. Add Firebase Auth (anonymous + Google) and Firestore.
3. Schema:
   ```
   /users/{uid}/stats           — mirror of puzzle_stats table
   /users/{uid}/dailyCompletions/{date}
   /users/{uid}/achievements/{id}
   /users/{uid}/save            — current game (single document)
   ```
4. Implement `platform/sync/FirebaseCloudSyncProvider.kt` extending
   `CloudSyncProvider`:
   - `pushAll()` writes the four repositories to Firestore.
   - `pullAll()` reads remote and merges (last-write-wins on `lastModified`
     timestamps; for stats, sum is preferred).

**Code swap:**
```kotlin
val cloudSyncProvider: CloudSyncProvider = FirebaseCloudSyncProvider(...)
```

UI surfaces for sync (e.g., a "마지막 동기화: HH:mm" line in Settings) are
NOT wired yet — add when you have a working impl, since the no-op surface
would be empty.

## Feature flags

`featureflags/FlagKeys.kt` defines toggles you can flip via `LocalFeatureFlags`
overrides today (or remote config tomorrow):

- `EnableAds` — disable ad surface even if AdProvider is real
- `EnableProUpsell` — hide Pro entry points (e.g., regional restrictions)
- `EnableCloudSync` — disable sync UI even if provider is wired

Flip them in `AppContainer.featureFlags = LocalFeatureFlags(mapOf(...))` for
quick local testing, or wire to Firebase Remote Config when you set up
Firebase for sync.
