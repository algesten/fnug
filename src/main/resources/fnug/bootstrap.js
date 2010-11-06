(function () {

	var head = document.getElementsByTagName('head')[0];

	var loadScript = function (url) {
		var script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = url;
		head.appendChild(script);
	};

	var loadStyles = function (url) {
		var css = document.createElement('link');
		css.type = 'text/css';
		css.rel = 'stylesheet';
		css.href = url;
		head.appendChild(css);
	};

	var baseUrl = '/***baseUrl***/';
	var resources = [/***resource***/];

	for (var i = 0; i < resources.length; i++) {
		var cur = resources[i];
		if (cur.lastIndexOf('.js') === cur.length - 3) {
			loadScript(cur);
		} else if (cur.lastIndexOf('.css') === cur.length - 4) {
			loadStyles(cur);
		}
	}
	
}());
