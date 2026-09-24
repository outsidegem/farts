#!/bin/bash
LIBRARY="/sdcard/Notifications/fart_library"

PAYLOAD=$(find "$LIBRARY" -type f -name "*.mp3" | shuf -n 1)

# --length=3 guarantees the notification never plays longer than 3 seconds!
echo "Playing: $PAYLOAD (Muffled, Max 3s)"
mpv --length=3 --af=lowpass=f=400 "$PAYLOAD"
