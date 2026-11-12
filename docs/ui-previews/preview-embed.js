(function () {
  var params = new URLSearchParams(location.search);
  var embed = params.has('embed') || params.get('app') === '1';
  if (embed) document.documentElement.classList.add('embed');

  function withEmbed(href) {
    if (!href || href === '#' || href.indexOf('javascript:') === 0) return href;
    try {
      var url = new URL(href, location.href);
      if (embed) url.searchParams.set('embed', '1');
      else url.searchParams.delete('embed');
      return url.pathname.split('/').pop() + url.search + url.hash;
    } catch (_) {
      return href;
    }
  }

  document.querySelectorAll('a[href]').forEach(function (a) {
    var href = a.getAttribute('href');
    if (!href || href === '#') return;
    if (/^https?:/i.test(href)) return;
    a.setAttribute('href', withEmbed(href));
  });

  window.peyyaPreview = {
    embed: embed,
    go: function (href) {
      location.href = withEmbed(href);
    },
  };
})();
