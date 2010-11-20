/*global fnug document*/

fnug.loadQueue = [];
fnug.loadCount = 0;

fnug.doLoadScript = function (script) {
	var head = document.getElementsByTagName('head')[0];
	fnug.loadCount++;
	head.appendChild(script);
};

fnug.loadScript = function (path, sequential) {
	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = fnug.resourcePath(path);
	script.onload = function () {
		fnug.loadCount--;
		if (fnug.loadQueue.length > 0) {
			fnug.doLoadScript(fnug.loadQueue.shift());
		}
	};
	if (sequential && fnug.loadCount > 0) {
		fnug.loadQueue.push(script);
	} else {
		fnug.doLoadScript(script);
	}
};

fnug.startLoad = function () {
	if (fnug.loadCount === 0 && fnug.loadQueue.length > 0) {
		fnug.doLoadScript(fnug.loadQueue.shift());
	}
};

fnug.loadStyles = function (path) {
	var head = document.getElementsByTagName('head')[0];
	var css = document.createElement('link');
	css.type = 'text/css';
	css.rel = 'stylesheet';
	css.href = fnug.resourcePath(path);
	head.appendChild(css);
};

fnug.resourcePath = function (path) {
	return '/***baseUrl***/' + '/' + path;
};
