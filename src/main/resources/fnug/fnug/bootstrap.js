/*global fnug window unescape*/

/**
 * @requires fnug/util.js
 * @requires fnug/jslintresult.js
 */

fnug.populateDebug = function () {

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
			} catch (e1) { // Try ISO
				try {
					value = unescape(value);
				} catch (e2) {
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

	fnug.isDebug = function (bundleName) {
		return debugAll || debugDefault && fnug.bundle === bundleName || debug[bundleName];
	};

};

// keep double quotes since closure compiler changes ' to "
fnug.bundles = "/***bundles***/";

if (window.JSON && JSON.parse) {
	fnug.bundles = JSON.parse(fnug.bundles);
} else {
	/*jslint evil: true*/
	fnug.bundles = eval(fnug.bundles);
	/*jslint evil: false*/
}

fnug.init = function () {

	for (var i = 0; i < fnug.bundles.length; i++) {
		var cur = fnug.bundles[i];
		if (cur.bundle) {
			fnug.bundle = cur.name;
		}
		if (fnug.isDebug(cur.name)) {
			for (var j = 0; j < cur.files.length; j++) {
				var file = cur.files[j];
				if (file.lint) {
					fnug.showJSLintPopupButton();
				}
				var path = file.path;
				if (path.lastIndexOf('.js') === path.length - 3) {
					fnug.loadScript(path);
				} else if (path.lastIndexOf('.css') === path.length - 4) {
					fnug.loadStyles(path);
				}
			}
		} else {
			if (cur.compCss) {
				fnug.loadStyles(cur.compCss);
			}
			if (cur.compJs) {
				fnug.loadScript(cur.compJs);
			}
		}
	}

};

fnug.populateDebug();
fnug.init();
