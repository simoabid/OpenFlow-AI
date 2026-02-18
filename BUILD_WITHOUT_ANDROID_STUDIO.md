# Building OpenFlow-AI APK Without Android Studio
## Lightweight Command-Line Build Guide for Low-Resource Systems

**Good News:** You DON'T need the heavy Android Studio (20GB+)!  
**You Only Need:** ~3-4GB of tools (JDK + Android SDK command-line tools)

This guide is specifically for Ubuntu-based systems with limited resources.

---

## Prerequisites Check

### 1. Check Your Current System
```bash
# Check available disk space
df -h ~

# Check available RAM
free -h

# You need:
# - At least 5GB free disk space
# - At least 4GB RAM (8GB recommended)
```

---

## PART 1: Install Required Tools (Lightweight)

### Step 1: Install Java Development Kit (JDK)

The project requires JDK 11 or newer.

```bash
# Install OpenJDK 17 (recommended)
sudo apt update
sudo apt install openjdk-17-jdk

# Verify installation
java -version
javac -version

# Should show: openjdk version "17.x.x"
```

**Alternative if already installed:**
```bash
# Check what you have
java -version

# If you have JDK 11, 17, or 21, you're good!
```

### Step 2: Install Android SDK Command-Line Tools (NOT Studio)

**Option A: Minimal Installation (Recommended - ~2GB)**

```bash
# Create directory for Android SDK
mkdir -p ~/Android/Sdk
cd ~/Android/Sdk

# Download command-line tools (LIGHTWEIGHT!)
# Visit: https://developer.android.com/studio#command-line-tools-only
# Or use this direct download:
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

# Extract
unzip commandlinetools-linux-11076708_latest.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# Set up environment variables
echo 'export ANDROID_SDK_ROOT=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_SDK_ROOT/platform-tools' >> ~/.bashrc

# Reload environment
source ~/.bashrc

# Verify
sdkmanager --version
```

### Step 3: Install Required SDK Components

```bash
# Accept licenses first
sdkmanager --licenses

# Install only what's needed (much lighter than Android Studio!)
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

# Optional: Install SDK 24 (minimum SDK for the app)
sdkmanager "platforms;android-24"

# This installs ~1.5-2GB instead of 20GB!
```

---

## PART 2: Configure the Project

### Step 4: Set Up local.properties

The project needs API keys to run, but you can build WITHOUT them for testing.

```bash
cd ~/Documents/Projects/OpenFlow

# Create local.properties with SDK path
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

# Add dummy API keys (needed for build to complete)
cat >> local.properties << 'EOF'
GEMINI_API_KEYS=dummy_key_for_build_only
PICOVOICE_ACCESS_KEY=dummy_key_for_build_only
TAVILY_API=dummy_key_for_build_only
MEM0_API=dummy_key_for_build_only
GOOGLE_TTS_API_KEY=dummy_key_for_build_only
GCLOUD_GATEWAY_PICOVOICE_KEY=dummy_key_for_build_only
GCLOUD_GATEWAY_URL=https://example.com
GCLOUD_PROXY_URL=https://example.com
GCLOUD_PROXY_URL_KEY=dummy_key_for_build_only
REVENUE_CAT_PUBLIC_URL=https://example.com
REVENUECAT_API_KEY=dummy_key_for_build_only
EOF

# Verify the file
cat local.properties
```

**Note:** The APK will build, but won't work without real API keys. Get them from:
- Gemini: https://makersuite.google.com/app/apikey
- Picovoice: https://console.picovoice.ai/

### Step 5: Make Gradle Wrapper Executable

```bash
# Make sure gradlew is executable
chmod +x gradlew

# Test it
./gradlew --version
```

---

## PART 3: Build the APK

### Step 6: First Build (Debug APK - Takes 10-30 minutes first time)

**⚠️ IMPORTANT: DON'T PANIC!**
- First build downloads dependencies (~500MB)
- Takes 10-30 minutes
- Uses 2-3GB disk space
- Subsequent builds are MUCH faster (2-5 minutes)

```bash
# Clean build (if you had errors before)
./gradlew clean

# Build debug APK (NO version increment)
./gradlew assembleDebug

# This will:
# 1. Download all dependencies (first time only)
# 2. Compile Kotlin code
# 3. Process resources
# 4. Create APK
```

**Watch the Progress:**
```bash
# In another terminal, monitor:
tail -f ~/.gradle/daemon/*/daemon-*.out.log
```

**If Build is Too Slow:**
```bash
# Increase Gradle memory (if you have RAM)
echo "org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m" >> gradle.properties

# Use parallel execution
echo "org.gradle.parallel=true" >> gradle.properties

# Use Gradle daemon
echo "org.gradle.daemon=true" >> gradle.properties
```

### Step 7: Find Your APK

```bash
# After successful build, find the APK:
ls -lh app/build/outputs/apk/debug/

# You'll see: app-debug.apk (around 15-30MB)

# Full path:
echo "Your APK is at:"
realpath app/build/outputs/apk/debug/app-debug.apk
```

### Step 8: Install on Your Phone

**Option A: Using USB**
```bash
# Connect phone via USB
# Enable USB Debugging on phone:
# Settings → About Phone → Tap "Build Number" 7 times
# Settings → Developer Options → Enable "USB Debugging"

# Install ADB if not already
sudo apt install adb

# Check device connected
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# If already installed, use -r to reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Option B: Transfer Manually**
```bash
# Copy APK to your phone's Downloads folder
# Then on phone: Files → Downloads → tap app-debug.apk → Install

# Or use cloud:
# Upload to Google Drive, Dropbox, etc.
# Download on phone and install
```

---

## PART 4: Building Release APK (For Distribution)

### Step 9: Create Signing Key

```bash
# Generate keystore (do this ONCE)
keytool -genkey -v -keystore ~/openflow-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias openflow-key

# Enter password (REMEMBER THIS!)
# Fill in the information when prompted
```

### Step 10: Configure Signing

```bash
# Create signing config
cat >> app/build.gradle.kts << 'EOF'

android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("HOME") + "/openflow-release-key.jks")
            storePassword = "YOUR_STORE_PASSWORD"  // Change this!
            keyAlias = "openflow-key"
            keyPassword = "YOUR_KEY_PASSWORD"  // Change this!
        }
    }
    
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            // ... existing release config
        }
    }
}
EOF
```

**Better: Use environment variables (more secure)**
```bash
# Add to ~/.bashrc
echo 'export OPENFLOW_KEYSTORE_PASSWORD="your_password"' >> ~/.bashrc
echo 'export OPENFLOW_KEY_PASSWORD="your_password"' >> ~/.bashrc
source ~/.bashrc
```

### Step 11: Build Release APK

```bash
# Build release APK (auto-increments version!)
./gradlew assembleRelease

# Find it:
ls -lh app/build/outputs/apk/release/
# Output: app-release.apk (smaller, optimized ~10-20MB)
```

---

## TROUBLESHOOTING

### Problem: "sdk.dir not found"
```bash
# Solution: Set ANDROID_SDK_ROOT
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

### Problem: "OutOfMemoryError"
```bash
# Solution: Reduce Gradle memory
echo "org.gradle.jvmargs=-Xmx1536m" >> gradle.properties

# Or close other apps while building
```

### Problem: "Build takes forever"
```bash
# Solution 1: Use build cache
echo "org.gradle.caching=true" >> gradle.properties

# Solution 2: Disable unnecessary tasks
./gradlew assembleDebug --no-daemon --parallel

# Solution 3: Build only what changed
./gradlew assembleDebug --configure-on-demand
```

### Problem: "Could not find or load main class"
```bash
# Solution: Clean and rebuild
./gradlew clean
rm -rf .gradle ~/.gradle/caches
./gradlew assembleDebug
```

### Problem: "License not accepted"
```bash
# Solution: Accept all licenses
sdkmanager --licenses
# Press 'y' for each license
```

### Problem: Build fails with dependency errors
```bash
# Solution: Clear cache
./gradlew clean --refresh-dependencies
rm -rf ~/.gradle/caches

# Then rebuild
./gradlew assembleDebug
```

---

## DISK SPACE MANAGEMENT

### Check Space Usage
```bash
# Check project size
du -sh ~/Documents/Projects/OpenFlow

# Check Gradle cache
du -sh ~/.gradle

# Check Android SDK
du -sh ~/Android/Sdk
```

### Clean Up After Build
```bash
# Clean build artifacts (safe, regenerates on next build)
./gradlew clean

# Remove Gradle cache (downloads again on next build)
rm -rf ~/.gradle/caches

# Keep only last APK, remove old builds
rm -rf app/build/intermediates
```

---

## COMPARISON: Command-Line vs Android Studio

| Aspect | Command-Line Tools | Android Studio |
|--------|-------------------|----------------|
| Disk Space | ~3-4GB | ~20-25GB |
| RAM Usage | ~2GB during build | ~4-8GB constant |
| Install Time | 10 minutes | 30-60 minutes |
| Build Speed | Same | Same |
| Can Edit Code? | Yes (VS Code) | Yes (Built-in) |
| Can Debug? | Yes (ADB) | Yes (GUI) |
| Can Build APK? | ✅ YES | ✅ YES |

**Verdict:** For your low-resource laptop, command-line tools are PERFECT!

---

## QUICK REFERENCE CARD

```bash
# Daily workflow:

# 1. Edit code in VS Code
code ~/Documents/Projects/OpenFlow

# 2. Build APK
cd ~/Documents/Projects/OpenFlow
./gradlew assembleDebug

# 3. Find APK
ls app/build/outputs/apk/debug/

# 4. Install on phone
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 5. View logs
adb logcat | grep OpenFlow

# That's it! No Android Studio needed.
```

---

## ADDRESSING YOUR REACT NATIVE CONCERNS

### You Said: "React Native is easier to build"

**Actually, both are similar difficulty:**

**React Native with Expo:**
- Still needs Android SDK (~2-3GB)
- Still needs JDK
- EAS Build (cloud) costs money ($29/month for frequent builds)
- Expo Go only works for managed workflow (not for this app's features)
- Limited native module access

**Current Kotlin Android:**
- ✅ Same tools (JDK + SDK)
- ✅ Free builds (local)
- ✅ Full native access (needed for your app!)
- ✅ Mature codebase ready to use

**The Real Problem You're Facing:**
- Not the technology (Kotlin vs React Native)
- It's the setup (which this guide solves!)

### Building APK: Kotlin Android vs React Native

**Kotlin Android (This App):**
```bash
./gradlew assembleDebug  # Done!
```

**React Native with Expo:**
```bash
npm install                    # Install dependencies
eas build --platform android   # Needs EAS account + credit card
# OR
npx expo prebuild             # Then still need Android SDK
./gradlew assembleDebug       # Same command!
```

**Bottom Line:** Building is equally easy once set up. The migration won't solve your build challenges.

---

## NEXT STEPS AFTER SUCCESSFUL BUILD

1. **Test the APK** on your phone
2. **Get Real API Keys** (app won't work with dummy keys)
3. **Build Release APK** when ready for distribution
4. **Consider Migration** only after trying to build with these instructions

**My Recommendation:**
1. Try building with this guide FIRST (takes 1 hour to set up)
2. If it works → No need to migrate!
3. If it fails → Then reconsider migration

The migration to React Native is a 12-18 month project costing $300k+. Building the APK takes 1 hour. Try the simpler solution first! 😊

---

## GETTING HELP

If you encounter errors:

1. **Check the Error Message**
   - Read carefully, it usually tells you what's wrong

2. **Google the Error**
   - Usually someone else solved it

3. **Check Logs**
   ```bash
   # Gradle logs
   ./gradlew assembleDebug --stacktrace --info
   
   # Build scan
   ./gradlew assembleDebug --scan
   ```

4. **Ask for Help**
   - Copy the FULL error message
   - Share your system specs
   - Share what you've tried

---

## SUMMARY

✅ **You DON'T need Android Studio**  
✅ **You CAN build on low-resource laptops**  
✅ **Command-line tools use only ~3-4GB**  
✅ **First build: 10-30 minutes**  
✅ **Later builds: 2-5 minutes**  
✅ **Migration to React Native won't make building easier**  

**Total Setup Time:** ~1 hour  
**Total Disk Space:** ~3-4GB (vs 20GB for Studio)  
**Total Complexity:** Medium (this guide makes it easy!)

Good luck! 🚀

---

**Pro Tip:** Bookmark this file! You'll reference it often.
