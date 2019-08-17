var auth2;
var user;
var authUrl = "https://accounts.google.com/o/oauth2/v2/auth";

function fitIframe(){
    var precedingHeight = $("#navbar").outerHeight(true)
        + $("#signin-warning").outerHeight(true)
        + $("#signout").outerHeight(true);
    $("#content").height(($(window).height() - precedingHeight) * .95)
}

$().ready(function(){
    "use strict";
    var csrfToken = $("meta[name=_csrf]").attr("content");
    var csrfHeader = $("meta[name=_csrf_header]").attr("content");

    var author = $("meta[name=author]").attr("content");
    var game = $("meta[name=game]").attr("content");
    var version = $("meta[name=version]").attr("content");
    var characterid = $("meta[name=characterid]").attr("content");

	var contentContainer= $("#content");
	contentContainer.attr('src', "/pluginresource/" + author + "/" + game + "/" + version + "/");

	function login(){
	    var user = auth2.currentUser.get();
	    var headers = {};
	    headers[csrfHeader] = csrfToken;
	    $.ajax("/login/google", {
	        type : "POST",
	        data : user.Zi.access_token,
	        contentType : 'application/json; charset=UTF-8',
	        headers : headers
    	}).done(function(response){
	        $("#save-character").prop("disabled", false);
	        $("#open-character").prop("disabled", false);
	        $("#delete-character").prop("disabled", false);
	        $("#signin-warning").hide();
	        $("#signout").show();
	        $("#login-modal").modal("hide");
	    }).fail(function(response){
	    logout();
	    alert("Something went wrong syncing authentication with the server.");
	});
	}

	function logout(){
	    var headers = {};
    	headers[csrfHeader] = csrfToken;
    	$.post("logout/google", {
    	    headers: headers
    	});
	}


	$(document).on("click", ".google-login", function(){
	    auth2.signIn().then(function(result){
            $("#login-modal").modal("hide");
        });
	});
	$(document).on("click", ".logout", function(){
	    auth2.signOut();
    });

	$("#gap").ready(function(){
		gapi.load("auth2", function(){
			gapi.auth2.init({
				client_id : $("#meta[name=google_client_id]").attr("content"),
				scope : 'email',
				redirect_uri :  "/login/google"
			});
			auth2 = gapi.auth2.getAuthInstance();
			auth2.isSignedIn.listen(function(googleUser){
				if(googleUser){
				    login();
    		    } else {
    		    	logout();
    		    }
				});
			});
		});

	$(document).on("click", ".login-menu", function(){
		$("#login-modal").modal("show");
	});

	function onSignIn(){
		alert("Logged in");
	}

    contentContainer.ready(function(){
    	$("#new-character").click(function(){
        	var url = $("#new-character").data("url");
        	window.location.href = url;
    	});

    	$("#save-character").click(function(){
        	var url = $("#save-character").data("url");
        	var headers = {};
        	headers[csrfHeader] = csrfToken;
        	//JS API
	        if (typeof(contentContainer[0].contentWindow.character) === "function"){
	            var characterData = contentContainer[0].contentWindow.character();
	            var characterId = $("#save-character").data("characterid");
	            var type = "POST";
	            if (characterId !== undefined){
	                url = url + characterId;
	                type = "PUT";
	            }
	            var ajax = $.ajax({
	                url: url,
	                "type": type,
	                headers : headers,
	                data: typeof characterData === "string" ? characterData : JSON.stringify(characterData),
	                contentType : "application/json;charset=UTF-8",
	                cache : false

	                }
	            );
	            ajax.done(function(result, response, xhr){
	                 alert("Save Complete");
	                 if(!characterId){
	                 	//Move to the new url if we created a new character.
                 		if(window.location.href.substring(window.location.href.length-1) !== "/"){
	                 		window.location.href += "/character/" + result.id;
	                 	} else {
	                 		window.location.href += "character/" + result.id;
	                 	}
                 	}
                }).fail(function(result){
                    console.log(result.responseText);
                    alert("Sorry, there was an error trying to save the character.");
                });
        } else {
            var characterForm = $("#character-form");
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
                        "class" : "row"
                        });

                        var nameCol = $("<div>", {
                        "class" : "col-md-8"
                        }).text(element.character.name ? element.character.name : (element.character._name ? element.character._name : "Unnamed Character"));
                        row.append(nameCol);

                        var loadButton = $("<button>",{
                            "class" : "btn btn-primary load-character",
                            "data-url" : url + id,
                            "data-characterid" : id
                        }).text("Open");
                        row.append(loadButton);

                        var deleteButton = $("<button>", {
                            "class" : "btn btn-warning delete-character",
                            "data-url" : url + id
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
	        });
	    });

	    $("#delete-character").click(function(event){
    	var id = $(event.target).data("characterid");
    	var urlBase = "/games/" + author +"/"+game+"/"+version;

    	var headers = {};
    	headers[csrfHeader] = csrfToken;
    	$.ajax({
    		url : urlBase + "/characters/" + id,
    		type : "DELETE",
    		headers : headers,
    		contentType : "application/json;charset=UTF-8",
    		cache : false
    	}).done(function(){
    		window.location.href = urlBase+"/character/";
    		alert("Character deleted");
    	}).fail(function(result){
    		alert("Sorry, something went wrong while trying to delete the character.")
    	});
    });

    	$("#export-character").click(function(event){
	    	var headers = {};
	    	headers[csrfHeader] = csrfToken;
	    	if (typeof(contentContainer[0].contentWindow.export) === "function"){
	    		var url = "/games/" + author + "/" +game + "/" + version + "/characters/pdf";
	    		$.post({
		    		url: url,
		    		headers : headers,
		    		data : JSON.stringify(contentContainer[0].contentWindow.export()),
		    		contentType : "application/json"
		    	}).success(function(result){
		    		console.log(result);
		    		window.location = url + "/" + result;
		    	}).fail(function(result){
		    		console.log(result.responseText);
		    		alert("There was an error while trying to export the character to PDF.");
		    	});
		    } else {
		    	alert("It looks like this plugin doesn't correctly implement export functionality.");
		    };
    	});

    	$(contentContainer[0].contentWindow).ready(function(){
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
    	    			try {
    	    				if(typeof contentContainer[0].contentWindow.character === "function"){
	    	    				contentContainer[0].contentWindow.character(JSON.stringify(wrapperResult.character));
	    	    				clearInterval(timer);
	    	    			} else if(i < 30) {
		    	    			i++;
		    	    		} else {
		    	    			clearInterval(timer);
		    	    		}
	    	    		} catch(ex){
		    	    			console.log(ex);
		    	    			alert("Sorry, but there was an error trying to open the character.");
		    	    			var url = $("#new-character").data("url");
		    	    			clearInterval(timer);
		    	    	}
	    	    	}, 1000);
    	    	}).fail(function(result){
    	    		alert("Sorry, but there was an error trying to open the character.");
    	    	});
        	};
        })
	});
	fitIframe();
});

$(window).resize(fitIframe);