{
	test: {
		jsLint: '/*jslint white: true, onevar: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, regexp: true, newcap: true, immed: true */',
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
