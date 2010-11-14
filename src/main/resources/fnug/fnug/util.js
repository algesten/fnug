
fnug.loadScript = function (url) {
	var head = document.getElementsByTagName('head')[0];
	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = '/***baseUrl***/' + url;
	head.appendChild(script);
};

fnug.loadStyles = function (url) {
	var head = document.getElementsByTagName('head')[0];
	var css = document.createElement('link');
	css.type = 'text/css';
	css.rel = 'stylesheet';
	css.href = '/***baseUrl***/' + url;
	head.appendChild(css);
};

fnug.resourceUrl = function (path) {
	return '/***baseUrl***/' + path;
};
