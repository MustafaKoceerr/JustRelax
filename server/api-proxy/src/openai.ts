import { isRecord } from "./encoding";
import type { MixRequest } from "./validate";

export const OPENAI_CHAT_URL: string = "https://api.openai.com/v1/chat/completions";

interface ChatMessage {
  role: "system" | "user";
  content: string;
}

interface JsonSchema {
  type: string;
  properties?: Record<string, unknown>;
  required?: string[];
  additionalProperties?: boolean;
  items?: unknown;
}

export interface ChatRequest {
  model: string;
  messages: ChatMessage[];
  response_format: { type: "json_schema"; json_schema: { name: string; strict: true; schema: JsonSchema } };
  max_tokens: number;
}

export interface MixSound {
  id: string;
  volume: number;
  frequency?: Frequency;
}

/** The JSON the app reads (core:network NetworkAiMix). */
export interface Mix {
  mix_name: string;
  description: string;
  sounds: MixSound[];
}

type Frequency = "RARE" | "NORMAL" | "OFTEN";

function isFrequency(value: unknown): value is Frequency {
  return value === "RARE" || value === "NORMAL" || value === "OFTEN";
}

/** The same for every request, so OpenAI can cache it once the prefix is long enough. */
const INSTRUCTIONS: string = `You are an expert ambient sound designer. Build a soundscape for the user's request from the inventory.

Inventory lines: id|kind|category|name|tags. Kind B is a bed: a continuous loop that carries the scene (rain, wind, a stream, noise). Kind E is an event: a short sound at random moments (a bird, an owl, a crackle, thunder).

Rules:
1. Use ONLY ids from the inventory; pick sounds whose name, category and tags fit the request.
2. Use 3-6 sounds: at least one bed when the inventory has one, plus events that bring it to life.
3. Volume 0.1-1.0. Beds that carry the scene are louder; events are usually softer.
4. frequency is RARE, NORMAL or OFTEN for events and null for beds.
5. mix_name: 2-4 words. description: one short sentence.
6. If the request is unrelated to sound, make a generic calm focus scene.`;

/** The answer's shape, enforced by OpenAI's Structured Outputs. */
const MIX_SCHEMA: JsonSchema = {
  type: "object",
  properties: {
    mix_name: { type: "string" },
    description: { type: "string" },
    sounds: {
      type: "array",
      items: {
        type: "object",
        properties: {
          id: { type: "string" },
          volume: { type: "number" },
          frequency: { type: ["string", "null"], enum: ["RARE", "NORMAL", "OFTEN", null] },
        },
        required: ["id", "volume", "frequency"],
        additionalProperties: false,
      },
    },
  },
  required: ["mix_name", "description", "sounds"],
  additionalProperties: false,
};

const MAX_TAGS: number = 5;

/** The model, instructions and limits are fixed here, so the key cannot be used for anything else. */
export function buildMixChatRequest(request: MixRequest): ChatRequest {
  const inventory: string = request.sounds
    .map((sound): string =>
      [sound.id, sound.kind === "BED" ? "B" : "E", sound.category, sound.name, sound.tags.slice(0, MAX_TAGS).join(",")].join("|"),
    )
    .join("\n");
  const language: string = request.language === undefined ? "" : `\n\nWrite mix_name and description in ${request.language}.`;

  return {
    model: "gpt-4o-mini",
    messages: [
      { role: "system", content: INSTRUCTIONS },
      { role: "system", content: inventory + language },
      { role: "user", content: request.prompt },
    ],
    response_format: { type: "json_schema", json_schema: { name: "mix", strict: true, schema: MIX_SCHEMA } },
    max_tokens: 250,
  };
}

/** The model's mix, with sounds outside the inventory dropped and volumes kept within 0.1–1. */
export function parseMixCompletion(body: unknown, request: MixRequest): Mix | null {
  if (!isRecord(body) || !Array.isArray(body.choices)) return null;
  const choice: unknown = body.choices[0];
  const message: unknown = isRecord(choice) ? choice.message : undefined;
  const content: unknown = isRecord(message) ? message.content : undefined;
  if (typeof content !== "string") return null;

  let parsed: unknown;
  try {
    parsed = JSON.parse(content);
  } catch {
    return null;
  }
  if (!isRecord(parsed) || typeof parsed.mix_name !== "string" || typeof parsed.description !== "string" || !Array.isArray(parsed.sounds)) {
    return null;
  }

  const inventory: ReadonlySet<string> = new Set(request.sounds.map((sound): string => sound.id));
  const seen: Set<string> = new Set();
  const sounds: MixSound[] = [];
  for (const item of parsed.sounds) {
    if (!isRecord(item) || typeof item.id !== "string" || typeof item.volume !== "number") continue;
    if (!inventory.has(item.id) || seen.has(item.id)) continue;
    const frequency: unknown = item.frequency ?? undefined;
    if (frequency !== undefined && !isFrequency(frequency)) continue;
    seen.add(item.id);
    const volume: number = Math.min(1, Math.max(0.1, item.volume));
    sounds.push(frequency === undefined ? { id: item.id, volume } : { id: item.id, volume, frequency });
  }
  return { mix_name: parsed.mix_name, description: parsed.description, sounds };
}

/** The token counts of a completion, for the log; never its text. */
export function describeUsage(body: unknown): string | null {
  const usage: unknown = isRecord(body) ? body.usage : undefined;
  if (!isRecord(usage) || typeof usage.prompt_tokens !== "number" || typeof usage.completion_tokens !== "number") return null;
  const details: unknown = usage.prompt_tokens_details;
  const cached: unknown = isRecord(details) ? details.cached_tokens : undefined;
  return `tokens in=${usage.prompt_tokens} cached=${typeof cached === "number" ? cached : 0} out=${usage.completion_tokens}`;
}
