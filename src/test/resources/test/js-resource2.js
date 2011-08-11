/*global alert*/
/**
 * @requires test/js-resource2.js
 * @requires test/js-nonexistant.js
 * @requires test/css-resource1.css
 * @requires test/css-nonexistant.css
 */
var a = function () { 
	alert('this is jozt a test');
};

var b = function () {
	a();
};
