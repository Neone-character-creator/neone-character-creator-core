$().ready(function(){
    "use strict";
    var csrfToken = $("meta[name=_csrf]").attr("content");
    var csrfHeader = $("meta[name=_csrf_header]").attr("content");

    var author = $('meta[name=author]').attr("content");
    var game = $('meta[name=game]').attr("content");
    var version = $('meta[name=version]').attr("content");
    var characterid = $('meta[name=characterid]').attr("content");

    $("#new-character").click(function(){
        var url = $("#new-character").data("url");
        window.location.href = url;
    });

    $("#save-character").click(function(){
        var url = $("#save-character").data("url");
        var headers = {};
        headers[csrfHeader] = csrfToken;
        //JS API
        if (typeof(character) === 'function'){
            var characterObject = character();
            var characterId = $("#save-character").data("characterid");
            var type = 'POST';
            if (characterId !== undefined){
                url = url + characterId;
                type = 'PUT';
            }
            var ajax = $.ajax({
                url: url,
                "type": type,
                headers : headers,
                data: JSON.stringify(characterObject),
                contentType : "application/json;charset=UTF-8",
                cache : false
                }
            );
            ajax.done(function(result, response, xhr){
                 alert("Save Complete");
                 switch(xhr.status){
                 	case 202:

                 	break;
                 	default:
                 	//Move to the new url if we created a new character.
                 	if(window.location.href.substring(window.location.href.length-1) !== "/"){
                 		window.location.href += "/" + result.id;
                 	} else {
                 		window.location.href += result.id;
                 	}
                 }

            }).fail(function(result){
                alert(result.responseText);
            });
        } else {
            var characterForm = $('#character-form');
            characterForm.submit();
        }
    });

    $("#open-character").click(function(event){
        var source = $(this)
        var url = $("#open-character").data("url");
        var redirect = $("#open-character").data("redirecturlbase");
        var headers = {};
        headers[csrfHeader] = csrfToken;
        $("#modal-content").text("Loading...")

        $.get({
            url : url,
            headers : headers,
            accept : "application/json;charset=UTF-8"
        }).done(function(response){
            $("#modal-content").empty()
            if(response.length == 0){
                $("#modal-content").text("No characters found.");
            } else {
            $.each(response, function(i, element)
            {
            	var id =element.id;
                        var row = $("<div>", {
                        'class' : 'row'
                        });

                        var nameCol = $("<div>", {
                        'class' : 'col-md-8'
                        }).text(element.character.name ? element.character.name : "Unnamed Character");
                        row.append(nameCol);

                        var loadButton = $("<button>",{
                            'class' : "btn btn-primary load-character",
                            'data-url' : url + id,
                            'data-characterid' : id
                        }).text("Open");
                        row.append(loadButton);

                        var deleteButton = $("<button>", {
                            'class' : "btn btn-warning delete-character",
                            'data-url' : url + id
                         }).text("Delete")
                         .click(function(){
                            $.ajax({
                                url : deleteButton.attr("url"),
                                type : "GET",
                                headers : headers,
                                accept : "application/json;charset=UTF-8"
                            })
                        });
                        row.append(deleteButton);

                        $("#modal-content").append(row)
            })
            }
        }).fail(function(error){
            $("#loading-modal").modal("hide");
            alert("There was an error loading from the server.");
        }).always(function(){
        })
    });

    $("#delete-character").click(function(event){
    	var id = $(event.target).data("characterid");
    	var urlBase = "/games/" + author +"/"+game+"/"+version;

    	var headers = {};
    	headers[csrfHeader] = csrfToken;
    	$.ajax({
    		url : urlBase + "/characters/" + id,
    		type : 'DELETE',
    		headers : headers
    	}).done(function(){
    		window.location.href = urlBase+"/pages/character/";
    		alert("Character deleted");
    	}).error(function(result){
    		alert("Sorry, something went wrong while trying to delete the character.")
    	});
    })

    $().ready(function(){
    	    if(characterid){
    	    	var headers = {};
    	    	headers[csrfHeader] = csrfToken;
    	    	$.ajax({
    	    		url : "/games/" + author + "/" + game + "/" + version + "/characters/" + characterid,
    	    		type: "GET",
    	    		headers : headers
    	    	}).done(function(wrapperResult){
    	    		var i = 0;
    	    		var timer = setInterval(function(){
    	    			if(typeof character != 'function' && i < 10){
    	    				i++;
    	    			} else {
    	    				character(wrapperResult.character);
    	    				clearInterval(timer);
    	    			}
    	    			if(i === 10){
    	    				alert("Sorry, but there was an error trying to open the character.");
    	    			}
    	    		}, 500);
    	    	}).error(function(result){
    	    		alert("Sorry, but there was an error trying to open the character.");
    	    	});
        	};
        })
});