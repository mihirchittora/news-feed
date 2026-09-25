import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/pages/**/*.{js,ts,jsx,tsx,mdx}", "./src/components/**/*.{js,ts,jsx,tsx,mdx}", "./src/app/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: {
    extend: {
      colors: {
        ink: "#15212b",
        paper: "#f8f7f3",
        mist: "#eef1f3",
        coral: "#e26d5c",
        "coral-dark": "#be4e40",
        line: "#d9dfe2",
        slate: "#5c6972",
      },
      fontFamily: {
        sans: ["var(--font-inter)", "Arial", "sans-serif"],
        display: ["var(--font-newsreader)", "Georgia", "serif"],
      },
      boxShadow: {
        soft: "0 18px 50px rgba(21, 33, 43, 0.08)",
      },
    },
  },
  plugins: [],
};

export default config;
