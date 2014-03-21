var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-40375612-1']);
_gaq.push(['_setDomainName', 'openshift.github.io']);
_gaq.push(['_trackPageview']);

(function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
})();

$(document).ready(function() {
    _gaq.push(function() {
        $('a[href^="http"]').on("click", function(e) {
            e.preventDefault();
            var link = $(this).attr("href");
            _gaq.push(["_trackEvent", "Outbound Links", "click", link]);
            setTimeout('document.location = "' + link + '"', 100);
        });
    });
});