var webpack = require('webpack');
var path = require('path');

var ExtractTextPlugin = require('extract-text-webpack-plugin');
var UglifyJSPlugin = require('uglifyjs-webpack-plugin');

var APP_JS_DIR = path.resolve(__dirname, 'src/app/js');
var APP_CSS_DIR = path.resolve(__dirname, 'src/contents/css');
var BUILD_JS_DIR = path.resolve(__dirname, 'src/client/js');
var BUILD_CSS_DIR = path.resolve(__dirname, 'src/client/css');
var BUILD_GRADLE_JS_DIR = path.resolve(__dirname, 'src/main/resources/static/js');
var BUILD_GRADLE_CSS_DIR = path.resolve(__dirname, 'src/main/resources/static/css');

var AUTH_JS_DR = path.resolve(__dirname, 'src/auth/js');

var glob = require("glob");
var APP_ALL_JS_FILES = glob.sync('./src/contents/js/**/*.js');


/*
* Webpack Config
*/

var config = {
  entry: {
    "bundle": APP_JS_DIR + '/index.jsx',
    "bundle.min": APP_JS_DIR + '/index.jsx',
    "authBundle": AUTH_JS_DR + '/index.jsx',
    "authBundle.min": AUTH_JS_DR + '/index.jsx',
    "common": glob.sync('./src/contents/js/**/*.js'),
    "common.min": glob.sync('./src/contents/js/**/*.js')
  },
  devtool: 'source-map',
  output: {
    path: BUILD_GRADLE_JS_DIR,
    filename: "[name].js"
  },
  module : {
    loaders : [
      {
        test : /\.jsx?/,
        loader : 'babel-loader',
        exclude: /node_modules/,
      },
    	{
        test: /\.scss$/,
        loader: ExtractTextPlugin.extract('css-loader!sass-loader')
    	}
    ]
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  },
  plugins: [
  	new ExtractTextPlugin({
  		filename: '../css/style.css',
  		allChunks: true
  	}),
    new webpack.optimize.UglifyJsPlugin({
      include: /\.min\.js$/,
      minimize: true
    })
	]
};

module.exports = config;