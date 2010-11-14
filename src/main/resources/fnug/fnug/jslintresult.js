

fnug.openPopup = function() {
	var popup = window
			.open(null, 'fnugJSLintPopup',
					'location=0,menubar=0,titlebar=0,toolbar=0,status=1,scrollbars=1,width=600');
	var doc = popup.document;
	doc.open();
	doc.write('<html><head><title>JSLint Errors</title>')
	doc.write('<link type="text/css" rel="stylesheet" href="');
	doc.write(fnug.resourceUrl('/fnug/jslintresult.css'));
	doc.write('"/>');
	doc.write('<body>');
	for (i = 0; i < fnug.bundles.length; i++) {
		var cur = fnug.bundles[i];
		if (fnug.isDebug(cur.name)) {
			if (cur.jsLintResult) {
				var tmpDiv = document.createElement('div');
				for ( var j = 0; j < cur.jsLintResult.length; j++) {
					var jsLintResult = cur.jsLintResult[j];
					doc.write('<div class="part">');
					doc.write('<h1>');
					doc.write(jsLintResult.fullPath);
					doc.write('</h1>');
					// escape html via innerHTML
					tmpDiv.innerHTML = jsLintResult.html;
					doc.write(tmpDiv.innerHTML);
					doc.write('</div>');
				}
			}
		}
	}
	doc.write('</body></html>');
	doc.close();
	
};

fnug.showJSLintPopupButton = function() {
	var b = document.getElementById('fnugJSLintButton');
	if (!b) {
		b = document.createElement('div');
		b.style.background = '#ff5500';
		b.style.border = '1px solid #ffffff';
		b.style.color = '#ffffff';
		b.style.cursor = 'pointer';
		b.style.fontSize = '8px';
		b.style.padding = '0px 3px';
		b.style.fontFamily = 'Verdana';
		b.style.position = 'fixed';
		b.style.bottom = '1px';
		b.style.left = '1px';
		b.innerHTML = 'JSLint Errors';
		b.addEventListener('click', fnug.openPopup, false);
		document.body.appendChild(b);
	}
};
