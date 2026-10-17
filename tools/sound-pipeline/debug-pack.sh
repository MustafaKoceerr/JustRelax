#!/bin/bash
# Rebuilds the debug sound pack (androidApp/src/debug/assets/debug-pack) from four v1 catalog
# sounds: two crossfaded bed loops and two event sounds cut into short clips. Needs ffmpeg with
# libopus. sounds-v2.json next to the clips is maintained by hand (durations and sizes from ffprobe).
#
# Usage: tools/sound-pipeline/debug-pack.sh
set -euo pipefail

OUT="$(cd "$(dirname "$0")/../.." && pwd)/androidApp/src/debug/assets/debug-pack"
SRC="$(mktemp -d)"
trap 'rm -rf "${SRC:?}"' EXIT

for s in air_breeze water_creek nature_birds nature_campfire; do
  curl -fsS -o "$SRC/$s.mp3" "https://cdn.mustafakoceerr.com/audio/$s.mp3"
done
mkdir -p "$OUT"
rm -f "${OUT:?}"/*.ogg

# bed <name> <start> <loop length> <crossfade>
# The loop is S[F, L) followed by S[L, L+F) fading out mixed with S[0, F) fading in, so its end
# flows into its start without a gap or a click.
bed() {
  local name=$1 start=$2 len=$3 fade=$4
  ffmpeg -v error -y -ss "$start" -t "$(echo "$len + $fade" | bc)" -i "$SRC/$name.mp3" -filter_complex "
    [0:a]aformat=sample_rates=48000:channel_layouts=mono,asplit=3[a][b][c];
    [a]atrim=start=$fade:end=$len,asetpts=PTS-STARTPTS[body];
    [b]atrim=start=$len:end=$(echo "$len + $fade" | bc),asetpts=PTS-STARTPTS,afade=t=out:d=$fade:curve=qsin[tail];
    [c]atrim=start=0:end=$fade,asetpts=PTS-STARTPTS,afade=t=in:d=$fade:curve=qsin[head];
    [tail][head]amix=inputs=2:normalize=0[seam];
    [body][seam]concat=n=2:v=0:a=1[out]" -map "[out]" -c:a libopus -b:a 64k "$OUT/${name}.ogg"
}

# event <name> <index> <start> <duration>: a short clip with a quick fade in and a softer fade out.
event() {
  local name=$1 idx=$2 start=$3 dur=$4
  ffmpeg -v error -y -ss "$start" -t "$dur" -i "$SRC/$name.mp3" \
    -af "aformat=sample_rates=48000:channel_layouts=mono,afade=t=in:d=0.02,afade=t=out:st=$(echo "$dur - 0.4" | bc):d=0.4" \
    -c:a libopus -b:a 48k "$OUT/${name}_${idx}.ogg"
}

bed air_breeze 10 30 2
bed water_creek 10 30 2
event nature_birds 0 0.6 3.0
event nature_birds 1 6.1 2.9
event nature_birds 2 18.0 2.2
event nature_campfire 0 4.0 1.6
event nature_campfire 1 56.2 1.6
