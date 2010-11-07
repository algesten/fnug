{
	mybundle: {
		matches: [ 'test/.*' ],
		jsLint: true,
		checkModified: true,
		jsCompilerArgs: [],
		files: [
		        'test/js-resource1.js',
		        'test/js-inbundle1.js'
		]
	},
	
	myotherbundle: {
		matches: [ 'bundle2/.*' ]
	}

}
