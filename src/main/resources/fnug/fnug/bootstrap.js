/*global fnug window unescape*/

/**
 * @requires fnug/util.js
 * @requires fnug/jslintresult.js
 */

fnug.debugAll = false;
fnug.debugDefault = false;
fnug.debug = {};

fnug.populateDebug = function () {

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
			for (var i = 0; i < values.length; i++) {
				if (values[i] === 'all') {
					fnug.debugAll = true;
				} else if (values[i] === 'true' || values[i] === '1') {
					fnug.debugDefault = true;
				} else {
					fnug.debug[values[i]] = true;
				}
			}
		}
	}


};

fnug.isDebug = function (bundleName, bundle) {
	return fnug.debugAll || fnug.debugDefault && bundle && bundle.name === bundleName || 
		fnug.debug[bundleName];
};

// keep double quotes since closure compiler changes ' to "
fnug.bundle = "/***bundles***/";

fnug.loadBundle = function (bundle) {

	var webKit = navigator.userAgent.indexOf("AppleWebKit") > 0;
	var sequential = webKit;
	
	if (window.JSON && JSON.parse) {
		bundle = JSON.parse(bundle);
	} else {
		/*jslint evil: true*/
		bundle = eval(bundle);
		/*jslint evil: false*/
	}

	for (var i = 0; i < bundle.length; i++) {
		var cur = bundle[i];
		if (cur.bundle) {
			fnug.bundle = cur.name;
		}
		if (fnug.isDebug(cur.name, bundle)) {
			for (var j = 0; j < cur.files.length; j++) {
				var file = cur.files[j];
				if (file.lint) {
					fnug.showJSLintPopupButton(cur.name);
				}
				var path = file.path;
				if (path.lastIndexOf('.js') === path.length - 3) {
					fnug.loadScript(path, sequential);
				} else if (path.lastIndexOf('.css') === path.length - 4) {
					fnug.loadStyles(path);
				}
			}
		} else {
			if (cur.compCss) {
				fnug.loadStyles(cur.compCss);
			}
			if (cur.compJs) {
				fnug.loadScript(cur.compJs, sequential);
			}
		}
	}
	
	fnug.startLoad();

};

fnug.populateDebug();
fnug.loadBundle(fnug.bundle);
