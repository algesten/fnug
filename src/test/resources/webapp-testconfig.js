{
	test: {
        jsLint: '/*jslint browser: true, continue: true, indent: 4, maxlen: 120, plusplus: true, sloppy: true, undef: true, unparam: true, vars: true */',
		checkModified: 1000,
		jsCompilerArgs: [],
		files: [
		        'test/js-resource1.js',
		        'test/js-inbundle1.js'
		]
	},
	
	bundle2: {
		checkModified: 0
	}

}
