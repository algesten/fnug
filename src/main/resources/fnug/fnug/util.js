/*global fnug document*/
fnug.loadScript = function (path) {
	var head = document.getElementsByTagName('head')[0];
	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = fnug.resourceUrl(path);
	head.appendChild(script);
};

fnug.loadStyles = function (path) {
	var head = document.getElementsByTagName('head')[0];
	var css = document.createElement('link');
	css.type = 'text/css';
	css.rel = 'stylesheet';
	css.href = fnug.resourceUrl(path);
	head.appendChild(css);
};

fnug.resourceUrl = function (path) {
	return '/***baseUrl***/' + '/' + path;
};
