const MomentLocalesPlugin = require('moment-locales-webpack-plugin');

module.exports = {
  "transpileDependencies": [
    "vuetify"
  ],
  configureWebpack: {
    plugins: [
      new MomentLocalesPlugin({localesToKeep: ['de']})
    ]
  },
  devServer: {
    port: 3000,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  }
}