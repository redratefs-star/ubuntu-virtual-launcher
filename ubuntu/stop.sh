#!/data/data/com.termux/files/usr/bin/bash
proot-distro login ubuntu -- bash -lc 'vncserver -kill :1 || true'
echo "VNC session stopped."
