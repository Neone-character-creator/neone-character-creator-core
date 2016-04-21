$().ready(function(){
	var csrfToken = $("meta[name=_csrf]").attr("content");
    var csrfHeader = $("meta[name=_csrf_header]").attr("content");
    var headers = {};
    headers[csrfHeader] = csrfToken;

    var author = $('meta[name=author').attr("content");
    var game = $('meta[name=game').attr("content");
    var version = $('meta[name=version').attr("content");

    $(document).on("click", ".load-character", function(event){
        var url = $(event.target).data("url");
        window.location.href = "/games/" + author + "/" + game + "/" + version + "/pages/character/" + $(event.target).data('characterid');
    })

    $(document).on("click", ".delete-character", function(event){
    	var url = $(event.target).data("url");
        var author = $('meta[name=author').attr("content");
        var game = $('meta[name=game').attr("content");
        var version = $('meta[name=version').attr("content");

        $.ajax({
            url : url,
            type : 'DELETE',
            headers : headers
        }).done(function(){
            alert("Character deleted");
            $(event.target.parentNode).empty();
        }).error(function(result){
        	alert("Sorry, something went wrong while trying to delete the character.")
        });
    });
})