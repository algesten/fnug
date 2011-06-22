/*global fnug document*/

fnug.loadScript = function (path) {
    // By document.write in the script tag we guarantee the load order
    // creating tags is obviously nicer, but would require some mechanism
    // that ensures the execution order of the parts.
    /*jslint evil: true*/
    document.write('<script type="text/javascript" src="' + 
    		fnug.resourcePath(path) + '"></script>');
    /*jslint evil: false*/
};

fnug.loadStyles = function (path, sequential) {
	if (sequential) {
		/*jslint evil: true*/
		document.write('<link type="text/css" rel="stylesheet" href="' + 
				fnug.resourcePath(path) + '/>');
		/*jslint evil: false*/
	} else {
		var css = document.createElement('link');
		css.type = 'text/css';
		css.rel = 'stylesheet';
		css.href = fnug.resourcePath(path);
		var head = document.getElementsByTagName('head')[0];
		head.appendChild(css);
	}
};

fnug.resourcePath = function (path) {
	return '/***baseUrl***/' + '/' + path;
};
