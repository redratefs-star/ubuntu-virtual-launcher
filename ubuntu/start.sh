#!/data/data/com.termux/files/usr/bin/bash
set -e
if ! proot-distro list | grep -q ubuntu; then
  echo "Ubuntu belum terpasang. Jalankan ~/uvl/install.sh terlebih dahulu."
  exit 1
fi
proot-distro login ubuntu --shared-tmp -- bash -lc '
mkdir -p "$HOME/.vnc"
if [ ! -f "$HOME/.vnc/passwd" ]; then
  echo "VNC password belum dibuat. Jalankan: proot-distro login ubuntu -- vncpasswd"
  exit 1
fi
vncserver :1 -localhost yes -geometry 1280x720 -depth 24
'
echo "XFCE berjalan pada 127.0.0.1:5901. Buka AVNC dan sambungkan ke alamat tersebut."
