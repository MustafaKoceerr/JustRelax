"""Builds a v2 sound pack (sounds-v2.json + Ogg Opus clips) from a manifest.

    python3 tools/sound-pipeline/pipeline.py <manifest.json> <out_dir> [--draft]

Beds become one crossfaded loop, events become short clips (cut by hand or found by loudness).
Every file is loudness-normalised with a single gain, so loops keep their seam. --draft accepts
"TODO" licenses so a pack can be listened to before the licenses are confirmed. Needs curl and ffmpeg
with libopus. The pure functions at the top are unit tested in test_pipeline.py.
"""
import argparse
import html
import json
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

# Mirrors core:model (SoundCategory, SoundKind, EventFrequency, License, SoundIcon).
CATEGORIES = {"RAIN", "WATER", "NATURE", "AIR", "CITY", "TRAVEL", "NOISE", "ASMR", "ZEN", "MUSIC"}
KINDS = {"BED", "EVENT"}
FREQUENCIES = {"RARE", "NORMAL", "OFTEN"}
LICENSES = {"CC0", "CC_BY_3_0", "CC_BY_4_0", "OWN"}
ATTRIBUTION_LICENSES = {"CC_BY_3_0", "CC_BY_4_0"}
ICONS = {
    "RAIN", "THUNDER", "WATER_DROP", "WAVES", "STREAM", "WIND", "STORM", "SNOW",
    "FIRE", "FIREPLACE", "FOREST", "LEAF", "GRASS", "BIRD", "INSECT", "ANIMAL", "NIGHT",
    "CITY", "CAFE", "TRAIN", "CAR", "PLANE", "BOAT",
    "NOISE", "FAN", "KEYBOARD", "ASMR", "BELL", "MEDITATION", "MUSIC", "PIANO", "MIC",
}
UNCONFIRMED_LICENSE = "TODO"
# The app's catalog mapper and downloader accept only these ids (SAFE_SOUND_ID).
SAFE_ID = re.compile(r"^[a-z0-9_-]{1,64}$")
V1_PREFIX = "v1:"

START_ABOVE_FLOOR_DB = 10  # a segment needs a peak this far over the quiet floor...
KEEP_ABOVE_FLOOR_DB = 4  # ...and lasts while the level stays this far over it (a thunder's rumble)
BRIDGED_GAP = 0.3  # seconds; quieter dips shorter than this stay inside one segment
PAD_BEFORE = 0.15  # seconds kept before a segment, so the attack is not cut
PAD_AFTER = 0.4  # seconds kept after it; the clip fades out over this tail
SILENCE_DB = -120.0
PEAK_CEILING_DB = -1.0


def find_event_segments(envelope, window, count, min_length, max_length):
    """Finds the [count] loudest moments in an RMS [envelope] (dB per [window] seconds).

    Returns padded (start, length) pairs in seconds, in time order. Moments shorter than
    [min_length] are ignored and lengths are capped at [max_length].
    """
    if not envelope:
        return []
    floor = sorted(envelope)[len(envelope) // 5]

    regions = []  # [first, last] window indexes
    for i, db in enumerate(envelope):
        if db < floor + KEEP_ABOVE_FLOOR_DB:
            continue
        if regions and (i - regions[-1][1] - 1) * window < BRIDGED_GAP:
            regions[-1][1] = i
        else:
            regions.append([i, i])
    peak = floor + START_ABOVE_FLOOR_DB
    regions = [r for r in regions if max(envelope[r[0]:r[1] + 1]) >= peak]

    total = len(envelope) * window
    candidates = []
    for first, last in regions:
        start, end = first * window, (last + 1) * window
        if end - start < min_length:
            continue
        level = sum(envelope[first:last + 1]) / (last - first + 1)
        padded_start = max(0.0, start - PAD_BEFORE)
        length = min(end + PAD_AFTER, total) - padded_start
        candidates.append((level, padded_start, min(length, max_length)))

    loudest = sorted(candidates, key=lambda c: c[0], reverse=True)[:count]
    return sorted((start, length) for _, start, length in loudest)


def validate_manifest(manifest, allow_unconfirmed=False):
    """Returns one message per problem in [manifest]; empty when it can be built."""
    errors = []
    seen = set()
    licenses = LICENSES | ({UNCONFIRMED_LICENSE} if allow_unconfirmed else set())
    for sound in manifest.get("sounds", []):
        sid = sound.get("id", "")
        if not SAFE_ID.match(sid):
            errors.append(f"{sid!r}: id must be lowercase letters, digits, '_' or '-'")
        elif sid in seen:
            errors.append(f"{sid}: duplicate id")
        seen.add(sid)

        if sound.get("category") not in CATEGORIES:
            errors.append(f"{sid}: unknown category {sound.get('category')!r}")
        if sound.get("icon") is not None and sound["icon"] not in ICONS:
            errors.append(f"{sid}: unknown icon {sound['icon']!r}")
        frequency = sound.get("default_frequency")
        if frequency is not None and frequency not in FREQUENCIES:
            errors.append(f"{sid}: unknown default_frequency {frequency!r}")

        license_ = sound.get("license")
        if license_ not in licenses:
            errors.append(f"{sid}: license {license_!r} is not confirmed")
        if license_ in ATTRIBUTION_LICENSES and not sound.get("attribution"):
            errors.append(f"{sid}: {license_} needs an attribution")

        if not str(sound.get("source", "")).startswith(V1_PREFIX) and not sound.get("names", {}).get("en"):
            errors.append(f"{sid}: a sound not from v1 needs names.en")

        kind = sound.get("kind")
        if kind not in KINDS:
            errors.append(f"{sid}: unknown kind {kind!r}")
        elif kind == "BED" and not sound.get("bed"):
            errors.append(f"{sid}: a bed needs a 'bed' loop (start, length, crossfade)")
        elif kind == "EVENT" and not sound.get("clips") and not sound.get("detect"):
            errors.append(f"{sid}: an event needs 'clips' or 'detect'")
    return errors


def catalog_entry(sound, names, clips):
    """One sounds-v2.json entry (core:network NetworkSound). [clips]: (file name, ms, bytes)."""
    entry = {
        "id": sound["id"],
        "names": names,
        "category": sound["category"],
        "kind": sound["kind"],
        "license": sound["license"],
        "clips": [
            {"url": f"audio/{name}", "duration_ms": duration_ms, "size_bytes": size_bytes}
            for name, duration_ms, size_bytes in clips
        ],
    }
    for key in ("icon", "tags", "attribution"):
        if sound.get(key):
            entry[key] = sound[key]
    if sound["kind"] == "EVENT" and sound.get("default_frequency"):
        entry["default_frequency"] = sound["default_frequency"]
    if sound.get("is_initial"):
        entry["is_initial"] = True
    return entry


def parse_loudnorm(stderr):
    """The measurements from ffmpeg's loudnorm=print_format=json, named as its second pass wants."""
    report = json.loads(stderr[stderr.rindex("{"):stderr.rindex("}") + 1])
    return {
        "measured_I": float(report["input_i"]),
        "measured_TP": float(report["input_tp"]),
        "measured_LRA": float(report["input_lra"]),
        "measured_thresh": float(report["input_thresh"]),
        "offset": float(report["target_offset"]),
    }


def parse_rms_envelope(text):
    """RMS levels (dB) from ffmpeg's astats + ametadata=print output; silence becomes a floor."""
    values = []
    for line in text.splitlines():
        if line.startswith("lavfi.astats.Overall.RMS_level="):
            value = float(line.split("=", 1)[1])
            values.append(value if value > SILENCE_DB else SILENCE_DB)
    return values


def normalization_gain(measured, target, peak_ceiling=PEAK_CEILING_DB):
    """The one gain (dB) that brings [measured] loudness to [target] without clipping its peak."""
    return min(target - measured["measured_I"], peak_ceiling - measured["measured_TP"])


# --- I/O: ffmpeg, ffprobe and the v1 CDN. Checked by running the pipeline, not by unit tests. ---

V1_BASE_URL = "https://cdn.mustafakoceerr.com/"
SAMPLE_RATE = 48000
ENVELOPE_WINDOW = 0.05
BED_TARGET_LUFS = -24
EVENT_TARGET_LUFS = -20
BED_BITRATE = "64k"
EVENT_BITRATE = "48k"
EVENT_FADE_IN = 0.02
MONO_48K = f"aformat=sample_rates={SAMPLE_RATE}:channel_layouts=mono"


def ffmpeg(*args):
    return subprocess.run(["ffmpeg", "-hide_banner", "-nostdin", "-y", *args],
                          check=True, capture_output=True, text=True)


def fetch(url, destination):
    if not destination.exists():
        destination.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(["curl", "-fsS", "-o", str(destination), url], check=True)
    return destination


def resolve_source(sound, manifest_dir, cache):
    source = sound["source"]
    if source.startswith(V1_PREFIX):
        v1_id = source.removeprefix(V1_PREFIX)
        return fetch(f"{V1_BASE_URL}audio/{v1_id}.mp3", cache / "v1" / f"{v1_id}.mp3")
    return (manifest_dir / source).resolve()


def v1_names(cache):
    catalog = fetch(f"{V1_BASE_URL}config/sounds.json", cache / "v1" / "sounds.json")
    return {s["id"]: s["names"] for s in json.loads(catalog.read_text())}


def render_bed(src, out_wav, start, length, crossfade):
    """The loop is S[F, L) followed by S[L, L+F) fading out mixed with S[0, F) fading in."""
    ffmpeg("-ss", str(start), "-t", str(length + crossfade), "-i", str(src), "-filter_complex", f"""
        [0:a]{MONO_48K},asplit=3[a][b][c];
        [a]atrim=start={crossfade}:end={length},asetpts=PTS-STARTPTS[body];
        [b]atrim=start={length}:end={length + crossfade},asetpts=PTS-STARTPTS,afade=t=out:d={crossfade}:curve=qsin[tail];
        [c]atrim=start=0:end={crossfade},asetpts=PTS-STARTPTS,afade=t=in:d={crossfade}:curve=qsin[head];
        [tail][head]amix=inputs=2:normalize=0[seam];
        [body][seam]concat=n=2:v=0:a=1[out]""", "-map", "[out]", "-c:a", "pcm_f32le", str(out_wav))


def render_event_clip(src, out_wav, start, length):
    fade_out = min(PAD_AFTER, length / 2)
    ffmpeg("-ss", str(start), "-t", str(length), "-i", str(src), "-af",
           f"{MONO_48K},afade=t=in:d={EVENT_FADE_IN},afade=t=out:st={length - fade_out}:d={fade_out}",
           "-c:a", "pcm_f32le", str(out_wav))


def envelope_of(src):
    samples = round(SAMPLE_RATE * ENVELOPE_WINDOW)
    result = ffmpeg("-i", str(src), "-af",
                    f"{MONO_48K},asetnsamples=n={samples}:p=0,astats=metadata=1:reset=1,"
                    "ametadata=print:key=lavfi.astats.Overall.RMS_level:file=-",
                    "-f", "null", "-")
    return parse_rms_envelope(result.stdout)


def encode_normalized(wav, out_ogg, target, bitrate):
    measured = parse_loudnorm(ffmpeg("-i", str(wav), "-af", f"loudnorm=I={target}:print_format=json",
                                     "-f", "null", "-").stderr)
    gain = normalization_gain(measured, target)
    ffmpeg("-i", str(wav), "-af", f"volume={gain:.2f}dB", "-c:a", "libopus", "-b:a", bitrate, str(out_ogg))


def probe(path):
    result = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration",
                             "-of", "csv=p=0", str(path)], check=True, capture_output=True, text=True)
    return round(float(result.stdout.strip()) * 1000), path.stat().st_size


def build_sound(sound, src, audio_dir, work):
    """Writes the sound's clips to [audio_dir]; returns their (file name, ms, bytes)."""
    if sound["kind"] == "BED":
        loop = sound["bed"]
        wav = work / f"{sound['id']}.wav"
        render_bed(src, wav, loop["start"], loop["length"], loop["crossfade"])
        cuts = [(wav, f"{sound['id']}.ogg")]
        target, bitrate = BED_TARGET_LUFS, BED_BITRATE
    else:
        spans = [(c["start"], c["length"]) for c in sound.get("clips") or []]
        if not spans:
            detect = sound["detect"]
            spans = find_event_segments(envelope_of(src), ENVELOPE_WINDOW, detect["count"],
                                        detect.get("min_length", 0.3), detect.get("max_length", 4))
            if not spans:
                sys.exit(f"{sound['id']}: no events found; lower detect.min_length or list its clips")
        cuts = []
        for i, (start, length) in enumerate(spans):
            wav = work / f"{sound['id']}_{i}.wav"
            render_event_clip(src, wav, start, length)
            cuts.append((wav, f"{sound['id']}_{i}.ogg"))
        target, bitrate = EVENT_TARGET_LUFS, EVENT_BITRATE

    clips = []
    for wav, name in cuts:
        out = audio_dir / name
        encode_normalized(wav, out, target, bitrate)
        clips.append((name, *probe(out)))
    return clips


def review_page(entries):
    rows = []
    for entry in entries:
        loop = " loop" if entry["kind"] == "BED" else ""
        players = "".join(f'<audio controls preload="none"{loop} src="{html.escape(c["url"])}"></audio>'
                          for c in entry["clips"])
        rows.append(f'<tr><td><b>{html.escape(entry["names"].get("en", entry["id"]))}</b><br>'
                    f'<code>{html.escape(entry["id"])}</code></td><td>{entry["kind"]}</td>'
                    f'<td>{html.escape(entry["license"])}</td><td>{players}</td></tr>')
    return ("<!doctype html><meta charset=utf-8><title>Sound pack review</title>"
            "<style>body{font:15px system-ui;margin:16px}td{padding:8px;vertical-align:top;"
            "border-bottom:1px solid #ccc}audio{display:block;margin:2px 0}</style>"
            "<table>" + "".join(rows) + "</table>")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("manifest", type=Path)
    parser.add_argument("out_dir", type=Path)
    parser.add_argument("--draft", action="store_true", help='accept "TODO" licenses')
    parser.add_argument("--no-review", action="store_true", help="skip review.html (for the APK's debug pack)")
    parser.add_argument("--cache", type=Path, default=Path(__file__).parent / ".cache")
    args = parser.parse_args(argv)

    manifest = json.loads(args.manifest.read_text())
    errors = validate_manifest(manifest, allow_unconfirmed=args.draft)
    if errors:
        sys.exit("\n".join(errors))

    audio_dir = args.out_dir / "audio"
    shutil.rmtree(audio_dir, ignore_errors=True)
    audio_dir.mkdir(parents=True)
    names_v1 = v1_names(args.cache) if any(s["source"].startswith(V1_PREFIX) for s in manifest["sounds"]) else {}

    entries = []
    with tempfile.TemporaryDirectory() as work:
        for sound in manifest["sounds"]:
            print(f"{sound['id']}...", flush=True)
            src = resolve_source(sound, args.manifest.parent, args.cache)
            names = {**names_v1.get(sound["source"].removeprefix(V1_PREFIX), {}), **sound.get("names", {})}
            entries.append(catalog_entry(sound, names, build_sound(sound, src, audio_dir, Path(work))))

    catalog = {"version": 2, "sounds": entries}
    (args.out_dir / "sounds-v2.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n")
    if not args.no_review:
        (args.out_dir / "review.html").write_text(review_page(entries))
    print(f"{len(entries)} sounds -> {args.out_dir}")


if __name__ == "__main__":
    main()
