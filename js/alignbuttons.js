function fixheight() {
    var maxheight = 0
    $(".fixedheight").each(function(){
        $(this).height('auto');
    })
    $(".fixedheight").each(function(){
        var height = $(this).height();
        if (height > maxheight) {
            maxheight = height;
        }
    })
    $(".fixedheight").each(function(){
        $(this).height(maxheight);
    })
}
$(function(){
    fixheight();
});
$(window).resize(function(){
    fixheight();
});