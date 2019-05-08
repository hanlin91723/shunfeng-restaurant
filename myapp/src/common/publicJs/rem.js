(function (win, doc) {
	function changeSize() {
	doc.documentElement.style.fontSize = 100 * doc.documentElement.clientWidth / 1242 + 'px';
	}
	changeSize();
	win.addEventListener('resize', changeSize, false);
})(window, document);