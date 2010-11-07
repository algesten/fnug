(function () {

	var head = document.getElementsByTagName('head')[0];

	var baseUrl = '/***baseUrl***/';

	var loadScript = function (url) {
		var script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = baseUrl + url;
		head.appendChild(script);
	};

	var loadStyles = function (url) {
		var css = document.createElement('link');
		css.type = 'text/css';
		css.rel = 'stylesheet';
		css.href = baseUrl + url;
		head.appendChild(css);
	};

	var bundles = [/***bundles***/];

	var debugAll = false;
	var debugDefault = false;
	var debug = {};
	var i;
	
	var query = window.location.search;
	if (query) {
	    var start = query.search(/[?&]debug=/);
	    var end;
	    if (start >= 0) {
	    	start += 7;
	    	end = query.indexOf('&', start);
	    	if (end === -1) {
	    		end = query.length;
	    	}
	    	var value = query.substring(start, end);
	        try { // Try UTF-8
	            value = decodeURIComponent(value);
	        } 
	        catch (e1) { // Try ISO
	            try {
	                value = unescape(value);
	            } 
	            catch (e2) {
	            	// give up
	            }
	        }
	        var values = value.split(',');
	        for (i = 0; i < values.length; i++) {
	        	if (values[i] === 'all') {
					debugAll = true;
				} else if (values[i] === 'true' || values[i] === '1') {
					debugDefault = true;
				} else {
					debug[values[i]] = true;
				}
	        }
	    }
	}
	
	for (i = 0; i < bundles.length; i++) {
		var cur = bundles[i];
		if (debugAll || i === 0 && debugDefault || debug[cur.name]) {
			for (var j = 0; j < cur.files.length; j++) {
				var file = cur.files[j];
				if (file.lastIndexOf('.js') === file.length - 3) {
					loadScript(file);
				} else if (file.lastIndexOf('.css') === file.length - 4) {
					loadStyles(file);
				}
			}
		} else {
			if (cur.compCss) {
				loadStyles(cur.compCss);
			}
			if (cur.compJs) {
				loadScript(cur.compJs);
			}
		}
	}
	
}());
