const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');
const webpack = require('webpack');

const config = {
	context: path.resolve(__dirname,'src'),
	entry: {
		app:'./js/index.js',
//		vendor: './src/assets/js/vendors.js'
	},
	output: {
		filename: 'bundle.js',
		path: path.resolve(__dirname,'dist')
	},
	devServer: {
		contentBase: path.resolve(__dirname,"./dist"),
		compress: true,
		port: 12000,
		stats: 'errors-only',
		open: true
	},
	stats: {
		// copied from `'minimal'`
		all: true,
		modules: true,
		maxModules: 0,
		errors: true,
		warnings: true,
		// our additional options
		moduleTrace: true,
		errorDetails: true
	},
	devtool: 'inline-source-map',
	module:{
		rules: [
			{test: /\.js$/,
			include: /src/,
			exclude: /node_modules/,
			use: {
				loader: "babel-loader",

			}},
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: ['eslint-loader']
			},
			{test: /\.html$/,use: ['html-loader']},
			{
				test: /\.css$/,
				use: [MiniCssExtractPlugin.loader,'css-loader'],
				//options: {
					// you can specify a publicPath here
					// by default it uses publicPath in webpackOptions.output
					//publicPath: '../',
				//	hmr: process.env.NODE_ENV === 'development',
				//},
			}
		]
	},
	plugins:[
		new MiniCssExtractPlugin({
			// Options similar to the same options in webpackOptions.output
			// both options are optional
			filename: '[name].css',
			chunkFilename: '[id].css',
		}),
		new HtmlWebpackPlugin({hash: true,
			title: 'React - Webauthn Demo',
			myPageHeader: 'Webauthn Demo',
			template: './html/index.htmlt',
			chunks: ['vendor', 'shared', 'app'],
			path: path.join(__dirname, "../../dist/"),
			filename: 'index.html'
		}),
		new ProgressBarPlugin(),
        new webpack.DefinePlugin({
            'WEBAUTHN_SERVER_URL':JSON.stringify("http://localhost"),
			'WEBAUTHN_SERVER_PORT':JSON.stringify("8080"),
			'DEFAULT_USER_EMAIL':JSON.stringify('johndoe@blabla.org')
        })

	]
};//webpack.js.org/configuration/dev-server/#devserver-stats-
module.exports = config;
