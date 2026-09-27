export {};

declare global {
  interface Window {
    __NEWS_PLATFORM_CONFIG__?: {
      apiBaseUrl?: string;
      siteUrl?: string;
    };
  }
}
