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

	var ie = navigator.userAgent.indexOf("MSIE") > 0;

	var iecss = ie ? [] : null;
	
	if (typeof bundle === 'string') {
		if (window.JSON && JSON.parse) {
			bundle = JSON.parse(bundle);
		} else {
			/*jslint evil: true*/
			eval('fnug.evil = ' + bundle + ';');
			/*jslint evil: false*/
			bundle = fnug.evil;
			delete fnug.evil;
		}
	}

	for (var i = 0; i < bundle.colls.length; i++) {
		var cur = bundle.colls[i];
		if (fnug.isDebug(cur.name, bundle)) {
			for (var j = 0; j < cur.files.length; j++) {
				var file = cur.files[j];
				if (file.lint) {
					fnug.showJSLintPopupButton(cur.name);
				}
				var path = file.path;
				if (path.lastIndexOf('.js') === path.length - 3) {
					fnug.loadScript(path);
				} else if (path.lastIndexOf('.css') === path.length - 4) {
					if (ie) {
						// ie can only handle a maximum of 31 stylesheets per page
						iecss.push(path);
					} else {
						fnug.loadStyles(path);
					}
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
	
	if (ie && iecss.length > 0) {
		var start = 0;
		var end = 0;
		while (end < (iecss.length - 1)) {
			start = end;
			end = Math.min(end + 24, iecss.length - 1);
			fnug.loadStyles('ie.css?f=' + iecss.slice(start, end).join(','));
		}
	}
	
};

fnug.populateDebug();
fnug.loadBundle(fnug.bundle);
