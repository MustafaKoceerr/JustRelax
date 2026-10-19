import { isRecord } from "./encoding";

export interface SessionRequest {
  installId: string;
  integrityToken: string;
}

export interface InventorySound {
  id: string;
  name: string;
  category: string;
  kind: "BED" | "EVENT";
  tags: string[];
}

export interface MixRequest {
  prompt: string;
  sounds: InventorySound[];
  /** The English name of the app's language, such as "Turkish"; English when missing. */
  language?: string;
}

export interface SearchQuery {
  query: string;
  page: number;
}

const INSTALL_ID: RegExp = /^[A-Za-z0-9-]{8,64}$/;
const SOUND_ID: RegExp = /^[a-z0-9_-]{1,64}$/;
const LANGUAGE: RegExp = /^[A-Za-z]{2,30}$/;
const MAX_PROMPT: number = 500;
const MAX_SOUNDS: number = 200;
const MAX_SEARCH_PAGE: number = 50;

export function parseSessionRequest(body: unknown): SessionRequest | null {
  if (!isRecord(body)) return null;
  const { installId, integrityToken } = body;
  if (typeof installId !== "string" || !INSTALL_ID.test(installId)) return null;
  if (typeof integrityToken !== "string" || integrityToken.length === 0 || integrityToken.length > 20_000) return null;
  return { installId, integrityToken };
}

export function parseMixRequest(body: unknown): MixRequest | null {
  if (!isRecord(body) || typeof body.prompt !== "string" || !Array.isArray(body.sounds)) return null;
  const prompt: string = body.prompt.trim();
  if (prompt.length === 0 || prompt.length > MAX_PROMPT) return null;
  if (body.sounds.length === 0 || body.sounds.length > MAX_SOUNDS) return null;

  const sounds: InventorySound[] = [];
  for (const item of body.sounds) {
    const sound: InventorySound | null = parseSound(item);
    if (sound === null) return null;
    sounds.push(sound);
  }
  const language: unknown = body.language;
  if (language === undefined) return { prompt, sounds };
  if (typeof language !== "string" || !LANGUAGE.test(language)) return null;
  return { prompt, sounds, language };
}

export function parseSearchQuery(url: URL): SearchQuery | null {
  const query: string = (url.searchParams.get("query") ?? "").trim();
  if (query.length === 0 || query.length > 100) return null;
  const pageText: string | null = url.searchParams.get("page");
  const page: number = pageText === null ? 1 : Number(pageText);
  if (!Number.isInteger(page) || page < 1 || page > MAX_SEARCH_PAGE) return null;
  return { query, page };
}

function parseSound(item: unknown): InventorySound | null {
  if (!isRecord(item)) return null;
  const { id, name, category, kind, tags } = item;
  if (typeof id !== "string" || !SOUND_ID.test(id)) return null;
  if (typeof name !== "string" || name.length > 80) return null;
  if (typeof category !== "string" || category.length > 20) return null;
  if (kind !== "BED" && kind !== "EVENT") return null;
  if (!Array.isArray(tags) || tags.length > 20) return null;
  const tagList: string[] = [];
  for (const tag of tags) {
    if (typeof tag !== "string" || tag.length > 40) return null;
    tagList.push(tag);
  }
  return { id, name, category, kind, tags: tagList };
}
