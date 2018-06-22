const path = require('path');

module.exports = {
  entry: './app/assets/scripts/script.js',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'app/assets/scripts/dist')
  },
  devtool: 'inline-source-map',
  watch: true
};
