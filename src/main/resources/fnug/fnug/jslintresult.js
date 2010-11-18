/*global fnug window*/

fnug.openJSLintResultPopup = function (evt) {
	if (evt) {
		if (evt.stopPropagation) {
			evt.stopPropagation();
		} else {
			evt.cancelBubble = true;
		}
	}
	var popup = window.open(null, 'fnugJSLintPopup',
		'location=0,menubar=0,titlebar=0,toolbar=0,status=1,scrollbars=1,width=600');
	fnug.populateJSLintResultPopup(popup);
	popup.focus();
};

fnug.showJSLintPopupButton = function () {
	var b = document.getElementById('fnugJSLintButton');
	if (!b) {
		fnug.loadStyles('fnug/jslintresult-button.css');
		b = document.createElement('div');
		b.id = 'fnugJSLintButton';
		b.innerHTML = 'JSLint Errors';
		b.addEventListener('click', fnug.openJSLintResultPopup, false);
		if (document.body) {
	        document.body.appendChild(b);
		} else {
		    var intId = setInterval(function() {
		        if (document.body) {
		            document.body.appendChild(b);
		            clearInterval(intId);
		        }
		    }, 10);
		}
	}
};

fnug.populateJSLintResultPopup = function (popup) {
	var doc = popup.document;
	doc.open();
	doc.write('<html><head><title>JSLint Errors</title>');
	doc.write('<link type="text/css" rel="stylesheet" href="');
	doc.write(fnug.resourceUrl('fnug/jslintresult.css'));
	doc.write('"/>');
	doc.write('<body>');
	var tmpDiv = document.createElement('div');
	for (var i = 0; i < fnug.bundles.length; i++) {
		var cur = fnug.bundles[i];
		if (fnug.isDebug(cur.name)) {
			for (var j = 0; j < cur.files.length; j++) {
				var file = cur.files[j];
				if (file.lint) {
					doc.write('<div class="part">');
					doc.write('<h1>');
					doc.write(file.path);
					doc.write('</h1>');
					// escape html via innerHTML
					tmpDiv.innerHTML = file.lint;
					doc.write(tmpDiv.innerHTML);
					doc.write('</div>');
				}
			}
		}
	}
	doc.write('</body></html>');
	doc.close();
};