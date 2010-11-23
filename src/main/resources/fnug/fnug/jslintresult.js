/*global fnug window*/

fnug.jsLintBundles = [];

fnug.openJSLintResultPopup = function (evt) {
	if (evt) {
		if (evt.stopPropagation) {
			evt.stopPropagation();
		} else {
			evt.cancelBubble = true;
		}
	}
	var path = fnug.resourcePath('fnug/jslintresult-popup.html#' + fnug.jsLintBundles.join(','));
	var popup = window.open(path, 'fnugJSLintPopup',
		'location=0,menubar=0,titlebar=0,toolbar=0,status=1,scrollbars=1,width=600');
	popup.focus();
};

fnug.showJSLintPopupButton = function (bundleName) {

	if (!fnug.showJSLintPopupButton[bundleName]) {
		fnug.jsLintBundles.push(bundleName);
		fnug.showJSLintPopupButton[bundleName] = true;
	}
	
	if (!fnug.jslintbutton) {
		fnug.loadStyles('fnug/jslintresult-button.css');
		var b = document.createElement('div');
		b.id = 'fnugJSLintButton';
		b.innerHTML = 'JSLint Errors';
		b.onclick = fnug.openJSLintResultPopup;
		fnug.jslintbutton = b;
		if (document.body) {
	        document.body.appendChild(b);
		} else {
		    var intId = setInterval(function () {
		        if (document.body) {
		            document.body.appendChild(b);
		            clearInterval(intId);
		        }
		    }, 10);
		}
	}
};
