$().ready(function(){
    $(".load-character").click(function(event){
        var id = event.target.attr("characterid");
        var author = $('meta[name=author').attr("content");
        var game = $('meta[name=game').attr("content");
        var version = $('meta[name=version').attr("content");
        window.location.href = "/" + author + "/" + game + "/" + version + "/" + id;
    })

    $(".delete-character").click(function(event){
        var id = event.target.attr("characterid");
        var author = $('meta[name=author').attr("content");
        var game = $('meta[name=game').attr("content");
        var version = $('meta[name=version').attr("content");

        var headers = {};
                headers[csrfHeader] = csrfToken;

        $.ajax({
            url = "/" + author + "/" + game + "/" + version + "/" + id,
            type = 'DELETE',
            headers : headers
        }).done(function(){
            alert("Character deleted");

        })
    })
})