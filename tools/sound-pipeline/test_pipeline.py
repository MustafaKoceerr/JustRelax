"""Tests for the pure parts of the sound pipeline: python3 -m unittest discover -s tools/sound-pipeline"""
import unittest

from pipeline import (
    catalog_entry, find_event_segments, normalization_gain, parse_loudnorm, parse_rms_envelope, review_page,
    validate_manifest,
)

WINDOW = 0.05  # seconds per envelope value


def envelope(*parts):
    """[(seconds, dB), ...] -> one dB value per WINDOW."""
    values = []
    for seconds, db in parts:
        values += [db] * round(seconds / WINDOW)
    return values


class FindEventSegmentsTest(unittest.TestCase):

    def test_loud_moments_over_a_quiet_floor_become_padded_segments(self):
        env = envelope((2, -60), (1, -20), (3, -60), (0.5, -25), (2, -60))

        segments = find_event_segments(env, WINDOW, count=5, min_length=0.3, max_length=4)

        self.assertEqual(2, len(segments))
        (s1, l1), (s2, l2) = segments
        self.assertAlmostEqual(2 - 0.15, s1, places=2)
        self.assertAlmostEqual(1 + 0.15 + 0.4, l1, places=2)
        self.assertAlmostEqual(6 - 0.15, s2, places=2)

    def test_the_loudest_segments_are_kept_in_time_order(self):
        env = envelope((1, -60), (0.5, -30), (1, -60), (0.5, -10), (1, -60), (0.5, -20), (1, -60))

        segments = find_event_segments(env, WINDOW, count=2, min_length=0.3, max_length=4)

        self.assertEqual([2.35, 3.85], [round(start, 2) for start, _ in segments])

    def test_short_gaps_are_bridged_and_long_calls_are_cut(self):
        env = envelope((1, -60), (1, -20), (0.2, -60), (1, -20), (1, -60), (6, -20), (1, -60))

        segments = find_event_segments(env, WINDOW, count=5, min_length=0.3, max_length=3)

        self.assertEqual(2, len(segments))
        self.assertGreater(segments[0][1], 2.2)
        self.assertAlmostEqual(3, segments[1][1], places=2)

    def test_a_rumble_between_its_peaks_stays_in_the_segment_and_a_rumble_without_peaks_is_ignored(self):
        env = envelope((2, -60), (0.3, -55), (0.2, -45), (0.6, -54), (0.2, -46), (0.5, -55),
                       (2, -60), (1, -55), (2, -60))

        segments = find_event_segments(env, WINDOW, count=5, min_length=0.5, max_length=4)

        self.assertEqual(1, len(segments))
        self.assertAlmostEqual(2 - 0.15, segments[0][0], places=2)
        self.assertAlmostEqual(1.8 + 0.15 + 0.4, segments[0][1], places=2)

    def test_clicks_shorter_than_the_minimum_are_ignored(self):
        env = envelope((1, -60), (0.1, -10), (1, -60))

        self.assertEqual([], find_event_segments(env, WINDOW, count=3, min_length=0.3, max_length=3))


def bed(**overrides):
    sound = {
        "id": "rain_light", "source": "v1:rain_light", "category": "RAIN", "kind": "BED",
        "license": "CC0", "bed": {"start": 10, "length": 30, "crossfade": 2},
    }
    sound.update(overrides)
    return sound


class ValidateManifestTest(unittest.TestCase):

    def test_a_complete_manifest_is_valid(self):
        self.assertEqual([], validate_manifest({"sounds": [bed()]}))

    def test_an_unconfirmed_license_blocks_a_release_build_but_not_a_draft(self):
        manifest = {"sounds": [bed(license="TODO")]}

        self.assertTrue(validate_manifest(manifest))
        self.assertEqual([], validate_manifest(manifest, allow_unconfirmed=True))

    def test_cc_by_needs_an_attribution(self):
        errors = validate_manifest({"sounds": [bed(license="CC_BY_4_0")]})

        self.assertTrue(any("attribution" in e for e in errors))

    def test_ids_must_be_safe_file_names_and_unique(self):
        errors = validate_manifest({"sounds": [bed(id="../x"), bed(), bed()]})

        self.assertTrue(any("../x" in e for e in errors))
        self.assertTrue(any("duplicate" in e for e in errors))

    def test_an_event_needs_clips_or_detection_and_a_bed_needs_a_loop(self):
        errors = validate_manifest({"sounds": [
            bed(bed=None),
            bed(id="owl", kind="EVENT", bed=None),
        ]})

        self.assertEqual(2, len(errors))

    def test_unknown_categories_kinds_and_frequencies_are_reported(self):
        errors = validate_manifest({"sounds": [bed(category="SPACE", kind="LOOP", default_frequency="SOMETIMES")]})

        self.assertEqual(3, len(errors))

    def test_a_sound_not_from_v1_needs_an_english_name(self):
        errors = validate_manifest({"sounds": [bed(source="raw/rain.wav")]})
        named = validate_manifest({"sounds": [bed(source="raw/rain.wav", names={"en": "Rain"})]})

        self.assertTrue(any("names.en" in e for e in errors))
        self.assertEqual([], named)

    def test_an_unknown_icon_is_reported(self):
        errors = validate_manifest({"sounds": [bed(icon="UMBRELLA")]})

        self.assertEqual(1, len(errors))


class ParseFfmpegOutputTest(unittest.TestCase):

    def test_loudnorm_measurements_become_second_pass_parameters(self):
        stderr = """size=N/A time=00:00:30.00 bitrate=N/A speed= 250x
[Parsed_loudnorm_0 @ 0x600001b04000]
{
	"input_i" : "-27.61",
	"input_tp" : "-4.47",
	"input_lra" : "18.06",
	"input_thresh" : "-39.20",
	"output_i" : "-23.58",
	"output_tp" : "-2.00",
	"output_lra" : "9.70",
	"output_thresh" : "-34.80",
	"normalization_type" : "dynamic",
	"target_offset" : "0.58"
}
"""

        self.assertEqual({
            "measured_I": -27.61, "measured_TP": -4.47, "measured_LRA": 18.06,
            "measured_thresh": -39.20, "offset": 0.58,
        }, parse_loudnorm(stderr))

    def test_the_rms_envelope_is_read_in_order_and_silence_becomes_a_floor(self):
        text = """frame:0    pts:0       pts_time:0
lavfi.astats.Overall.RMS_level=-42.5
frame:1    pts:2400    pts_time:0.05
lavfi.astats.Overall.RMS_level=-inf
frame:2    pts:4800    pts_time:0.1
lavfi.astats.Overall.RMS_level=-18.25
"""

        self.assertEqual([-42.5, -120.0, -18.25], parse_rms_envelope(text))


class NormalizationGainTest(unittest.TestCase):

    def test_the_gain_reaches_the_target_loudness(self):
        self.assertAlmostEqual(3.5, normalization_gain({"measured_I": -27.5, "measured_TP": -9.0}, target=-24))

    def test_the_gain_never_pushes_the_true_peak_over_the_ceiling(self):
        self.assertAlmostEqual(2.0, normalization_gain({"measured_I": -30.0, "measured_TP": -3.0}, target=-24))


class CatalogEntryTest(unittest.TestCase):

    def test_an_entry_has_the_fields_the_app_reads(self):
        sound = bed(icon="RAIN", tags=["rain"], is_initial=True,
                    attribution={"title": "Rain", "author": "me", "url": "https://x"})

        entry = catalog_entry(sound, {"en": "Light Rain"}, [("rain_light.ogg", 30000, 240000)])

        self.assertEqual({
            "id": "rain_light",
            "names": {"en": "Light Rain"},
            "category": "RAIN",
            "kind": "BED",
            "license": "CC0",
            "clips": [{"url": "audio/rain_light.ogg", "duration_ms": 30000, "size_bytes": 240000}],
            "icon": "RAIN",
            "tags": ["rain"],
            "attribution": {"title": "Rain", "author": "me", "url": "https://x"},
            "is_initial": True,
        }, entry)

    def test_optional_fields_are_left_out_and_events_keep_their_frequency(self):
        entry = catalog_entry(
            bed(id="owl", kind="EVENT", default_frequency="RARE", bed=None),
            {"en": "Owl"},
            [("owl_0.ogg", 2000, 9000)],
        )

        self.assertEqual("RARE", entry["default_frequency"])
        self.assertNotIn("icon", entry)
        self.assertNotIn("attribution", entry)


class ReviewPageTest(unittest.TestCase):

    def test_names_are_escaped_and_only_beds_loop(self):
        bed_entry = catalog_entry(bed(), {"en": "Rain <b>& wind</b>"}, [("rain_light.ogg", 30000, 1)])
        event_entry = catalog_entry(bed(id="owl", kind="EVENT", bed=None), {}, [("owl_0.ogg", 2000, 1), ("owl_1.ogg", 2000, 1)])

        page = review_page([bed_entry, event_entry])

        self.assertIn("Rain &lt;b&gt;&amp; wind&lt;/b&gt;", page)
        self.assertNotIn("<b>& wind", page)
        self.assertIn('<audio controls preload="none" loop src="audio/rain_light.ogg">', page)
        self.assertIn('<audio controls preload="none" src="audio/owl_1.ogg">', page)
        self.assertIn("<b>owl</b>", page)


if __name__ == "__main__":
    unittest.main()
