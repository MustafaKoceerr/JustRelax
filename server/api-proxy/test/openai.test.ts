import { describe, expect, it } from "vitest";
import { buildMixChatRequest, describeUsage, parseMixCompletion } from "../src/openai";
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
  it("uses the server's model and limits, and a strict schema for the answer", () => {
    const body = buildMixChatRequest(request);

    expect(body.model).toBe("gpt-4o-mini");
    expect(body.max_tokens).toBe(250);
    expect(body.response_format.type).toBe("json_schema");
    expect(body.response_format.json_schema.strict).toBe(true);
    expect(body.response_format.json_schema.schema.required).toEqual(["mix_name", "description", "sounds"]);
  });

  it("sends the fixed instructions first, then the inventory, then the user's words", () => {
    const body = buildMixChatRequest(request);

    expect(body.messages.map((message: { role: string }): string => message.role)).toEqual(["system", "system", "user"]);
    expect(body.messages[0]?.content).not.toContain("rain_light");
    expect(body.messages[0]?.content).toBe(buildMixChatRequest({ ...request, sounds: [request.sounds[1]!] }).messages[0]?.content);
    expect(body.messages[2]).toEqual({ role: "user", content: "rainy night" });
  });

  it("writes the inventory one compact line per sound, with at most five tags", () => {
    const tagged: MixRequest = {
      prompt: "rain",
      sounds: [{ id: "rain_heavy", name: "Heavy Rain", category: "RAIN", kind: "BED", tags: ["a", "b", "c", "d", "e", "f"] }],
    };

    expect(buildMixChatRequest(request).messages[1]?.content).toBe("rain_light|B|RAIN|Light Rain|rain\nthunder|E|RAIN|Thunder|");
    expect(buildMixChatRequest(tagged).messages[1]?.content).toBe("rain_heavy|B|RAIN|Heavy Rain|a,b,c,d,e");
  });

  it("asks for the name and description in the app's language, English by default", () => {
    expect(buildMixChatRequest({ ...request, language: "Turkish" }).messages[1]?.content).toContain("Write mix_name and description in Turkish.");
    expect(buildMixChatRequest(request).messages[1]?.content).not.toContain("Write mix_name");
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

  it("treats a null frequency as no frequency", () => {
    const mix = parseMixCompletion(
      completion({ mix_name: "Rain", description: "d", sounds: [{ id: "rain_light", volume: 0.5, frequency: null }] }),
      request,
    );

    expect(mix?.sounds).toEqual([{ id: "rain_light", volume: 0.5 }]);
  });

  it("gives nothing for an empty or unreadable answer", () => {
    expect(parseMixCompletion({ choices: [] }, request)).toBeNull();
    expect(parseMixCompletion(completion("not json"), request)).toBeNull();
    expect(parseMixCompletion(completion({ mix_name: "x" }), request)).toBeNull();
  });
});

describe("describeUsage", () => {
  it("gives the token counts only", () => {
    const body: unknown = {
      usage: { prompt_tokens: 600, completion_tokens: 90, prompt_tokens_details: { cached_tokens: 0 } },
      choices: [{ message: { content: "the user's words" } }],
    };

    expect(describeUsage(body)).toBe("tokens in=600 cached=0 out=90");
    expect(describeUsage({})).toBeNull();
  });
});
