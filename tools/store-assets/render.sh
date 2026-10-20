#!/usr/bin/env bash
# Renders the store and icon artwork to PNG with headless Chrome (no other tools needed).
#
#   tools/store-assets/render.sh icons     # Play icon, iOS icons, in-app mark, notification artwork
#   tools/store-assets/render.sh screens   # Play screenshots and feature graphic
#   tools/store-assets/render.sh           # both
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# shot <url> <width> <height> <out.png> [transparent]
shot() {
  local bg=()
  [[ "${5:-}" == transparent ]] && bg=(--default-background-color=00000000)
  "$CHROME" --headless --disable-gpu --hide-scrollbars --allow-file-access-from-files \
    --force-device-scale-factor=1 --window-size="$2,$3" ${bg[@]+"${bg[@]}"} \
    --screenshot="$4" "$1" >/dev/null 2>&1
}

# svg <file.svg> <size> <out.png> [transparent] [css filter]
svg() {
  local page="$TMP/$(basename "$3").html"
  printf '<!doctype html><body style="margin:0;background:transparent"><img src="file://%s" style="display:block;width:%spx;height:%spx;filter:%s"></body>' \
    "$1" "$2" "$2" "${5:-none}" >"$page"
  shot "file://$page" "$2" "$2" "$3" "${4:-}"
}

icons() {
  node "$HERE/icon/build-icon.ts"
  local out="$HERE/icon/out" play="$REPO/fastlane/metadata/android/en-US/images"
  local ios="$REPO/iosApp/iosApp/Assets.xcassets"
  mkdir -p "$play"
  svg "$out/icon.svg" 512 "$REPO/androidApp/src/main/ic_launcher-playstore.png"
  cp "$REPO/androidApp/src/main/ic_launcher-playstore.png" "$play/icon.png"
  svg "$out/icon.svg" 512 "$REPO/core/ui/src/main/res/drawable/app_mark.png"
  svg "$out/icon.svg" 512 "$REPO/androidApp/src/main/res/drawable-nodpi/notification_artwork.png"
  svg "$out/icon.svg" 1024 "$ios/AppIcon.appiconset/app-icon-1024.png"
  svg "$out/mark.svg" 1024 "$ios/AppIcon.appiconset/app-icon-1024-dark.png" transparent
  svg "$out/mark.svg" 1024 "$ios/AppIcon.appiconset/app-icon-1024-tinted.png" transparent "grayscale(1) brightness(1.15)"
  svg "$out/icon.svg" 512 "$ios/AppMark.imageset/app-mark.png"
  svg "$out/icon.svg" 512 "$ios/NowPlayingArtwork.imageset/now-playing-artwork.png"
}

screens() {
  node "$HERE/screens/render-screens.ts" "$CHROME"
}

case "${1:-all}" in
  icons) icons ;;
  screens) screens ;;
  all) icons; screens ;;
  *) echo "usage: $0 [icons|screens]" >&2; exit 1 ;;
esac
