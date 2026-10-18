import { describe, expect, it } from "vitest";
import { buildMixChatRequest, parseMixCompletion } from "../src/openai";
import type { MixRequest } from "../src/validate";

const request: MixRequest = {
  prompt: "rainy night",
  sounds: [
    { id: "rain_light", name: "Light Rain", category: "RAIN", kind: "BED", tags: ["rain"] },
    { id: "thunder", name: "Thunder", category: "RAIN", kind: "EVENT", tags: [] },
  ],
};

function completion(content: unknown): unknown {
  return { choices: [{ message: { content: typeof content === "string" ? content : JSON.stringify(content) } }] };
}

describe("buildMixChatRequest", () => {
  it("uses the server's model, instructions and limits, with the inventory and the user's words", () => {
    const body = buildMixChatRequest(request);

    expect(body.model).toBe("gpt-4o-mini");
    expect(body.response_format).toEqual({ type: "json_object" });
    expect(body.max_tokens).toBe(500);
    expect(body.messages).toHaveLength(2);
    expect(body.messages[0]?.role).toBe("system");
    expect(body.messages[0]?.content).toContain('"id":"rain_light"');
    expect(body.messages[1]).toEqual({ role: "user", content: "rainy night" });
  });
});

describe("parseMixCompletion", () => {
  it("returns the mix, keeping only inventory sounds and sane values", () => {
    const mix = parseMixCompletion(
      completion({
        mix_name: "Storm",
        description: "Rain with thunder",
        sounds: [
          { id: "rain_light", volume: 1.7 },
          { id: "thunder", volume: 0.3, frequency: "RARE" },
          { id: "made_up", volume: 0.5 },
          { id: "thunder", volume: 0.4, frequency: "SOMETIMES" },
        ],
      }),
      request,
    );

    expect(mix).toEqual({
      mix_name: "Storm",
      description: "Rain with thunder",
      sounds: [
        { id: "rain_light", volume: 1 },
        { id: "thunder", volume: 0.3, frequency: "RARE" },
      ],
    });
  });

  it("gives nothing for an empty or unreadable answer", () => {
    expect(parseMixCompletion({ choices: [] }, request)).toBeNull();
    expect(parseMixCompletion(completion("not json"), request)).toBeNull();
    expect(parseMixCompletion(completion({ mix_name: "x" }), request)).toBeNull();
  });
});
