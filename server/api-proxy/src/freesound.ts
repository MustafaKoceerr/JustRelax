import { isRecord } from "./encoding";
import type { SearchQuery } from "./validate";

const SEARCH_URL: string = "https://freesound.org/apiv2/search/";
// "Attribution" excludes Attribution-NonCommercial on Freesound; the app checks licenses again.
const FILTER: string = 'license:("Creative Commons 0" OR "Attribution") duration:[1 TO 300]';
const FIELDS: string = "id,name,username,license,url,tags,duration,previews";

export interface FreesoundResult {
  id: number;
  name: string;
  username: string;
  license: string;
  url: string;
  tags: string[];
  duration: number;
  previews: Record<string, string>;
}

/** The app's NetworkFreesoundPage; `next` only says whether more follow. */
export interface SearchPage {
  count: number;
  next: "more" | null;
  results: FreesoundResult[];
}

/** The key goes in the Authorization header, never in this URL. */
export function buildSearchUrl(search: SearchQuery): URL {
  const url: URL = new URL(SEARCH_URL);
  url.searchParams.set("query", search.query);
  url.searchParams.set("filter", FILTER);
  url.searchParams.set("fields", FIELDS);
  url.searchParams.set("page_size", "30");
  url.searchParams.set("page", String(search.page));
  return url;
}

export function toSearchPage(body: unknown): SearchPage | null {
  if (!isRecord(body) || !Array.isArray(body.results)) return null;
  const results: FreesoundResult[] = [];
  for (const item of body.results) {
    const result: FreesoundResult | null = toResult(item);
    if (result !== null) results.push(result);
  }
  return {
    count: typeof body.count === "number" ? body.count : results.length,
    next: typeof body.next === "string" ? "more" : null,
    results,
  };
}

function toResult(item: unknown): FreesoundResult | null {
  if (!isRecord(item)) return null;
  const { id, name, username, license, url, tags, duration, previews } = item;
  if (typeof id !== "number" || typeof name !== "string" || typeof username !== "string") return null;
  if (typeof license !== "string" || typeof url !== "string" || typeof duration !== "number") return null;
  if (!isRecord(previews)) return null;
  const previewUrls: Record<string, string> = {};
  for (const [key, value] of Object.entries(previews)) {
    if (typeof value === "string") previewUrls[key] = value;
  }
  const tagList: string[] = Array.isArray(tags) ? tags.filter((tag: unknown): tag is string => typeof tag === "string") : [];
  return { id, name, username, license, url, tags: tagList, duration, previews: previewUrls };
}
