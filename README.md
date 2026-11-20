# Ubuntu Virtual Launcher (UVL)

Starter Android launcher for a local Ubuntu ARM64 + XFCE desktop hosted by Termux/PRoot.

## Requirements
- Android 8+ (tested target: Android 15)
- Termux installed from one consistent official source (F-Droid or Termux GitHub releases)
- Termux RUN_COMMAND enabled for external apps
- AVNC or another VNC client
- At least 5–8 GB free storage recommended; performance depends on device

## Important architecture note
This is **not** a full PC emulator. Android app is a launcher. Ubuntu runs in Termux using PRoot. XFCE is exposed through a localhost-only VNC server. GitHub login is not required. The GitHub button in this starter version opens GitHub in a browser; OAuth is not yet implemented.

## Termux setup
1. Install Termux from F-Droid or official Termux GitHub releases. Do not mix Termux and plugin apps from different sources.
2. Open Termux once and run:
   ```sh
   pkg update && pkg upgrade -y
   pkg install git -y
   git clone https://github.com/YOUR_USERNAME/ubuntu-virtual-launcher.git ~/uvl
   chmod +x ~/uvl/ubuntu/*.sh
   ```
3. In Termux settings, enable **Allow external apps** / RUN_COMMAND. Grant the Android permission if prompted.
4. Run:
   ```sh
   bash ~/uvl/ubuntu/install.sh
   ```
5. Set a VNC password:
   ```sh
   proot-distro login ubuntu -- vncpasswd
   ```
6. Start the desktop:
   ```sh
   bash ~/uvl/ubuntu/start.sh
   ```
7. Install AVNC and connect to `127.0.0.1`, port `5901`. Use the VNC password you created.
8. Stop:
   ```sh
   bash ~/uvl/ubuntu/stop.sh
   ```

## Build APK
Open this project in Android Studio, use JDK 17, then Build > Build Bundle(s) / APK(s) > Build APK(s).
Or push to GitHub and use the included Actions workflow.

## Security
- VNC binds to localhost only; do not expose port 5901 to public networks.
- Never commit GitHub tokens, passwords, signing keys, or local.properties.
- The Android launcher asks Termux to run local scripts. Review scripts before running.
