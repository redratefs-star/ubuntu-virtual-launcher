#!/data/data/com.termux/files/usr/bin/bash
set -e
pkg update -y
pkg install -y proot-distro x11-repo tigervnc
proot-distro install ubuntu
proot-distro login ubuntu --shared-tmp -- bash -lc '
apt update
DEBIAN_FRONTEND=noninteractive apt upgrade -y
DEBIAN_FRONTEND=noninteractive apt install -y xfce4 xfce4-goodies dbus-x11 tigervnc-standalone-server tigervnc-common
mkdir -p "$HOME/.vnc"
printf "#!/bin/sh\nunset SESSION_MANAGER\nunset DBUS_SESSION_BUS_ADDRESS\nexec startxfce4\n" > "$HOME/.vnc/xstartup"
chmod +x "$HOME/.vnc/xstartup"
'
echo "Ubuntu + XFCE installed. Set VNC password by running: proot-distro login ubuntu -- vncpasswd"
