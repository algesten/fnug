var loadBundle = function (bundleName) {
	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = '../' + bundleName + '?callback=C';
	var head = document.getElementsByTagName('head')[0];
	head.appendChild(script);
}

var C = function (bundle) {
	draw(bundle)
}

var draw = function (bundle) {

	var body = document.getElementsByTagName('body')[0];
	
	var h1 = document.createElement('h1');
	h1.innerHTML = bundle.name;

	var count = 0;
	
	for (var i = 0; i < bundle.colls.length; i++) {

		var cur = bundle.colls[i];
		
		for (var j = 0; j < cur.files.length; j++) {
		
			var file = cur.files[j];
			
			if (file.lint) {
				
			    count++;
			    
				if (h1 !== null) {
					body.appendChild(h1);
					h1 = null;
				}

				var div = document.createElement('div');
				div.className = 'file';
				div.innerHTML = file.lint;
				body.appendChild(div);

				var h2 = document.createElement('h2');
				h2.innerHTML = file.path
				div.insertBefore(h2, div.firstChild);
				
			}
		}
	}
	
	var h1s = document.getElementsByTagName('h1');
	if (h1s.length === 0) {
		var p = document.createElement('p');
		p.innerHTML = 'No JSLint problems found.';
		body.appendChild(p);
	} else {
        var countTxt = 'Files failing: ' + count;
        if (document.getElementsByClassName) {
            countTxt += ', errors: ' + document.getElementsByClassName('evidence').length;
        }
        var countP = document.createElement('p');
        countP.innerHTML = countTxt;
        countP.className = 'count';
        body.appendChild(countP);
	}
	
}

var hash = location.hash;
if (hash) {
	hash = hash.substr(1);
	var split = hash.split(',');
	for (var i = 0; i < split.length; i++) {
		loadBundle(split[i]);
	}
}
