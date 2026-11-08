# Hasbi — Online Build Guide (No Android Studio Needed)

> **Bhai, yeh guide APK build karne ka tareeqa hai BINA Android Studio ke.**
>
> GitHub Actions (free) APK khud build kar dega cloud mein. Aap sirf code upload karenge, APK download karenge.

---

## 🎯 Quick Summary

```
Aap ke PC → GitHub (upload) → GitHub Actions (build) → APK (download) → Phone (install)
```

**Total time:** 5-15 minutes build time (free cloud)
**Cost:** $0 (GitHub free tier — 2000 minutes/month free for public repos)
**System requirement:** Sirf browser (Chrome/Firefox) — no Android Studio, no JDK, no SDK

---

## 📋 Step-by-Step Guide

### Step 1: GitHub Account Banayein (1 min)

Agar aap ke paas GitHub account nahi hai:

1. https://github.com/signup kholein
2. Email + password daalein
3. Verify email
4. Done ✅

---

### Step 2: Naya Repository Banayein (1 min)

1. GitHub pe login karne ke baad
2. **Top-right corner** mein **"+"** icon → **"New repository"**
3. Form bharo:
   - **Repository name:** `hasbi-android`
   - **Description:** `Hasbi — Personal Finance Android App`
   - **Visibility:** ✅ **Public** (zaroori — private repos pe GitHub Actions limited)
   - **Initialize this repository with:** ❌ SAB unchecked rakhein (README/gitignore/license)
4. **"Create repository"** button click karein

---

### Step 3: Hasbi Code Upload Karein (3 min)

#### Method A: ZIP se upload (recommended for low-end systems)

1. **`hasbi-android.zip`** file download karein (jo main ne di hai)
2. Apne PC pe extract karein (Windows: right-click → "Extract All")
3. Extracted folder `hasbi-android` ke andar ke files check karein
4. GitHub pe apne repo (`hasbi-android`) mein jayein
5. **"uploading an existing file"** link pe click karein (page ke neeche)
6. Apne PC se files drag-drop karein (ya "choose your files" click kar ke select karein)
   - **TIP:** Pehle `core/` folder upload karein, phir `feature-*` folders, phir `app/`, phir root files
   - **WARNING:** Bohat saari files hain — browser thora slow ho sakta hai. Patience rakhein.
7. Jab sab upload ho jayein, neeche **"Commit changes"** button click karein
8. Commit message: `Initial Hasbi Phase 1 — Week 2 code` likhein
9. **"Commit changes"** final click

#### Method B: GitHub Desktop (better for next time)

1. https://desktop.github.com se GitHub Desktop download karein
2. Apne GitHub account se login karein
3. "Clone a repository from the Internet" → `hasbi-android` select karein
4. Local folder mein `hasbi-android.zip` ke contents paste karein
5. GitHub Desktop mein sab files dikhengi → "Commit to main" button
6. "Push origin" button click karein

---

### Step 4: Build Trigger Karein (Automatic)

Jaise hi code push hota hai, GitHub Actions **automatically build start** kar deta hai.

#### Build check karne ke liye:
1. Apne GitHub repo mein jayein
2. **Top tabs** mein **"Actions"** pe click karein
3. Left sidebar mein **"Build Hasbi APK"** workflow dikh raha hoga
4. Uske andar latest run dikhega (yellow dot = running, green ✓ = success, red X = failed)
5. **Build pe click karein** → details dekh saken

#### Manual trigger (agar automatic nahi hua):
1. Actions tab → "Build Hasbi APK"
2. Right side **"Run workflow"** button
3. Branch: `main` → green "Run workflow" button

---

### Step 5: APK Download Karein (2 min)

Jab build successful ho jaye (green ✓):

1. Successful build run pe click karein
2. Sabse neeche scroll karein
3. **"Artifacts"** section mein **"hasbi-debug-apk"** dikh raha hoga
4. Us pe click karein → APK download ho jayegi
5. File ka naam: `hasbi-debug-apk.zip` (extract karne pe `app-debug.apk` milegi)

---

### Step 6: Phone mein APK Install Karein (1 min)

#### Phone settings:
1. **Settings → Security** (ya Privacy)
2. **"Install unknown apps"** ya **"Unknown sources"** ON karein
3. Apne browser (Chrome) ko allow karein

#### APK install:
1. `app-debug.apk` file apne phone mein transfer karein:
   - WhatsApp se khud ko bhej do (easy)
   - Ya USB cable se
   - Ya Google Drive upload kar ke phone se download
2. File manager mein ja ke APK pe tap karein
3. **"Install"** button
4. **"Open"** button — Hasbi app khul jayegi! 🎉

---

## 🛠 Build Status Indicators

GitHub Actions page pe yeh colors dikhenge:

| Icon | Meaning | Action |
|------|---------|--------|
| 🟡 Yellow circle | Build running | Wait 5-15 min |
| ✅ Green check | Build successful | Download APK from Artifacts |
| ❌ Red X | Build failed | Click kar ke error log dekho, mujhe bhejo |
| 🟣 Purple dot | Queued (waiting for runner) | Wait |

---

## ⚠️ Common Issues + Solutions

### Issue 1: Build fails with "Could not resolve dependencies"
**Cause:** Network issue on GitHub's end
**Solution:** 
- Actions tab → "Re-run failed jobs" button
- Ya manual "Run workflow" dobara

### Issue 2: "SDK license not accepted"
**Cause:** Android SDK license issue
**Solution:** Main fix kar dunga workflow file mein — bata dena

### Issue 3: Build succeeds but APK not downloading
**Cause:** Aap private repo use kar rahe hain (free tier limited)
**Solution:** Repo public karein (Settings → Danger Zone → Change visibility)

### Issue 4: Code upload fails (files too many)
**Solution:** 
1. GitHub Desktop use karein (Method B above)
2. Ya git command line:
```bash
git init
git add .
git commit -m "Initial Hasbi Phase 1"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/hasbi-android.git
git push -u origin main
```

### Issue 5: Phone mein "App not installed"
**Cause:** 
- Phone Android 8.0 (API 26) se purani hai → app minSdk 26 hai
- Ya storage kam hai
- Ya "Unknown sources" properly allow nahi hua

**Solution:**
- Phone Android version check karein (Settings → About phone)
- Storage clear karein
- Browser ko specifically allow karein "Install unknown apps" mein

---

## 📊 Free Tier Limits (Important)

GitHub Actions **free tier**:
- **Public repos:** Unlimited minutes ✅ (hamara case)
- **Private repos:** 2000 minutes/month (build ~10 min = 200 builds/month)

Hasbi build time: ~10 minutes per build

**Tip:** Public repo rakhein — unlimited builds free.

---

## 🔄 Next Time Workflow

Jab main aap ko naya code du (Week 3, 4, etc.):

1. Main updated `hasbi-android.zip` dunga
2. Aap GitHub Desktop se files replace karein
3. Commit + push
4. GitHub Actions automatically build karega
5. New APK download karein
6. Phone mein install (purani APK overwrite ho jayegi)

**Sameer circular process — no Android Studio needed ever.** 🎉

---

## 📱 Testing on Real Phone vs Emulator

**Hasbi development ke liye: REAL PHONE best hai.**

| Aspect | Real Phone | Emulator |
|--------|-----------|----------|
| Speed | Fast | Slow |
| Real testing | ✅ | ❌ |
| Storage permissions | Real behavior | Simulated |
| Biometric | Real fingerprint | None |
| APK install | Direct | ADB push |
| Free | Already have | Needs Android Studio |

Hasbi ki testing real phone pe karein. Emulator ki zaroorat nahi.

---

## 🆘 Need Help?

Agar masla aaye to mujhe ye bhej do:

1. **Screenshot** GitHub Actions error ka
2. **Build log** — Actions → failed run → "Build Debug APK" step → copy last 30 lines
3. **Apka GitHub repo URL** (jaise `github.com/yourname/hasbi-android`)

Main fix kar dunga ya workflow file update kar dunga.

---

## ✅ Abhi Kya Karein?

1. **GitHub account** banayein (agar nahi hai)
2. **Public repo** `hasbi-android` banayein
3. **ZIP extract** kar ke files GitHub pe upload karein
4. **Actions tab** mein jayein — build auto-start hoga
5. **5-15 min** wait
6. **APK download** from Artifacts
7. **Phone mein install**
8. **Hasbi app open** — splash + dashboard dekho
9. **Mujhe screenshot bhej do** 📸

Phir Week 3 shuru karenge!

---

## 🔗 Useful Links

- **GitHub signup:** https://github.com/signup
- **GitHub Desktop:** https://desktop.github.com
- **Git for Windows:** https://git-scm.com/download/win
- **GitHub Actions docs:** https://docs.github.com/en/actions

**Hasbi ban gaya — bina Android Studio ke!** 🚀
