import { describe, expect, it } from "vitest";
import { buildSearchUrl, toSearchPage } from "../src/freesound";

describe("buildSearchUrl", () => {
  it("adds the server's license and length filter, fields and page size, and no key", () => {
    const url: URL = buildSearchUrl({ query: "kettle", page: 2 });

    expect(url.origin + url.pathname).toBe("https://freesound.org/apiv2/search/");
    expect(url.searchParams.get("query")).toBe("kettle");
    expect(url.searchParams.get("page")).toBe("2");
    expect(url.searchParams.get("page_size")).toBe("30");
    expect(url.searchParams.get("filter")).toBe('license:("Creative Commons 0" OR "Attribution") duration:[1 TO 300]');
    expect(url.searchParams.get("fields")).toBe("id,name,username,license,url,tags,duration,previews");
    expect(url.searchParams.has("token")).toBe(false);
  });
});

describe("toSearchPage", () => {
  it("passes on the page and whether more follow, with only the fields the app reads", () => {
    const page = toSearchPage({
      count: 31,
      next: "https://freesound.org/apiv2/search/?page=2&token=leak",
      previous: null,
      results: [
        {
          id: 1, name: "Rain", username: "a", license: "https://creativecommons.org/publicdomain/zero/1.0/",
          url: "https://freesound.org/s/1/", tags: ["rain"], duration: 2.5,
          previews: { "preview-hq-ogg": "https://cdn.freesound.org/1-hq.ogg" }, extra: "dropped",
        },
      ],
    });

    expect(page).toEqual({
      count: 31,
      next: "more",
      results: [
        {
          id: 1, name: "Rain", username: "a", license: "https://creativecommons.org/publicdomain/zero/1.0/",
          url: "https://freesound.org/s/1/", tags: ["rain"], duration: 2.5,
          previews: { "preview-hq-ogg": "https://cdn.freesound.org/1-hq.ogg" },
        },
      ],
    });
  });

  it("drops malformed results and reads a last page", () => {
    expect(toSearchPage({ count: 2, next: null, results: [{ id: "x" }] })).toEqual({ count: 2, next: null, results: [] });
    expect(toSearchPage("nope")).toBeNull();
  });
});
