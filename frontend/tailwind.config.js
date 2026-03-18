/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'pizza-red': '#d32f2f',
        'pizza-green': '#2e7d32',
        'pizza-cream': '#fff8f0',
      }
    },
  },
  plugins: [],
}
