# Ashika Live TV — Native Android project

Kotlin + AndroidX Media3 ExoPlayer starter project.

## ফিচার
- Native Android interface
- ExoPlayer HLS playback
- IPTV-org All / Bangladesh / India M3U playlist
- Channel search ও Favorites (বর্তমানে session-only)
- Buffering ও stream error status

## GitHub Actions দিয়ে APK তৈরি (মোবাইল থেকেও)
1. ZIP extract করুন।
2. GitHub-এ `AshikaLiveTV` নামে নতুন **Public বা Private repository** তৈরি করুন।
3. ZIP-এর ভেতরের `AshikaLiveTV` ফোল্ডারের সব ফাইল ও `.github` hidden folder repository-র root-এ upload করুন। `.github/workflows/android.yml` ফাইলটি অবশ্যই থাকতে হবে।
4. GitHub repository-তে **Actions** tab খুলুন। প্রথমবার workflow অনুমোদন চাইলে Enable করুন।
5. `Build Ashika Live TV APK` workflow নির্বাচন করে **Run workflow** চাপুন।
6. কাজ সফল হলে workflow run-এর নিচে **Artifacts** থেকে `Ashika-Live-TV-debug-apk` ZIP ডাউনলোড করুন। ZIP খুলে `app-debug.apk` ইনস্টল করুন।

## Android Studio দিয়ে build
Android Studio-তে project root খুলুন, Gradle sync শেষ হলে Build > Build APK(s) নির্বাচন করুন।

## Playlist source
- https://iptv-org.github.io/iptv/index.m3u
- https://iptv-org.github.io/iptv/countries/bd.m3u
- https://iptv-org.github.io/iptv/countries/in.m3u

চ্যানেল ও স্ট্রিমের প্রাপ্যতা এবং ব্যবহারের অধিকার সংশ্লিষ্ট provider-এর ওপর নির্ভরশীল।
