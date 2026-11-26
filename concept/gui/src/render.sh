#!/bin/bash
# usage: render.sh name width height
G="$(cd "$(dirname "$0")" && pwd)"
EDGE="/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"
"$EDGE" --headless --disable-gpu --hide-scrollbars --force-device-scale-factor=1 --virtual-time-budget=8000 --window-size=$2,$3 --screenshot="$(cygpath -w "$G/$1.png")" "file:///$(cygpath -m "$G/$1.html")" 2>/dev/null
ls -la "$G/$1.png" | awk '{print $5, $9}'
