import { isRecord } from "./encoding";
import type { MixRequest } from "./validate";

export const OPENAI_CHAT_URL: string = "https://api.openai.com/v1/chat/completions";

interface ChatMessage {
  role: "system" | "user";
  content: string;
}

export interface ChatRequest {
  model: string;
  messages: ChatMessage[];
  response_format: { type: "json_object" };
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

/** The model, instructions and limits are fixed here, so the key cannot be used for anything else. */
export function buildMixChatRequest(request: MixRequest): ChatRequest {
  const system: string = `You are an expert ambient sound designer building a soundscape.

### INVENTORY (JSON):
${JSON.stringify(request.sounds)}

Each sound has a kind:
- BED: a continuous loop that carries the scene (rain, wind, a stream, noise).
- EVENT: short sounds that occur at random moments (a bird, an owl, a crackle, thunder).
  For events choose a frequency: RARE, NORMAL or OFTEN.

### RULES:
1. Use ONLY ids from the inventory; pick sounds whose name, category and tags fit the request.
2. Use 3-6 sounds: at least one BED when the inventory has one, plus events that bring it to life.
3. Volume range: 0.1 to 1.0. Beds that carry the scene are louder; events are usually softer.
4. If the request is unrelated to sound, create a generic calm focus scene.
5. Output STRICT JSON:
{
  "mix_name": "Name",
  "description": "Short description",
  "sounds": [ {"id": "sound_id", "volume": 0.5, "frequency": "NORMAL"} ]
}
"frequency" is only for EVENT sounds.`;

  return {
    model: "gpt-4o-mini",
    messages: [
      { role: "system", content: system },
      { role: "user", content: request.prompt },
    ],
    response_format: { type: "json_object" },
    max_tokens: 500,
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
    const frequency: unknown = item.frequency;
    if (frequency !== undefined && !isFrequency(frequency)) continue;
    seen.add(item.id);
    const volume: number = Math.min(1, Math.max(0.1, item.volume));
    sounds.push(frequency === undefined ? { id: item.id, volume } : { id: item.id, volume, frequency });
  }
  return { mix_name: parsed.mix_name, description: parsed.description, sounds };
}
