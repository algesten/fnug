{
	testbundle1: {
//		matches: [ 'test/.*' ], // this is allowed since bundle name makes default namespace.
		jsLint: false,
		checkModified: false,
		jsCompilerArgs: [ '--debug' ],
		files: [
		        'test/file1.js',
		        'test/file2.js'
		]
	}
}
