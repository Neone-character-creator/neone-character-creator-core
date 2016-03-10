$().ready(function(){
    "use strict";
    var csrfToken = $("meta[name=_csrf]").attr("content");
    var csrfHeader = $("meta[name=_csrf_header]").attr("content");

    $("#new-character").click(function(){
        var url = $("#new-character").data("url");
        window.location = url;
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
                contentType : "application/json;charset=UTF-8"
                }
            );
            ajax.done(function(result){
                 alert("Save Complete")
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
                        var row = $("<div>", {
                        'class' : 'row'
                        });

                        var nameCol = $("<div>", {
                        'class' : 'col-md-8'
                        }).text(element.character.firstName + " " + element.character.secondName);
                        row.append(nameCol);

                        var loadButton = $("<button>",{
                            'class' : "btn btn-primary"
                        }).text("Open")
                        .click(function(){
                            window.location = redirect + "/" + element.id
                        });
                        row.append(loadButton);

                        var deleteButton = $("<button>", {
                            'class' : "btn btn-warning",
                            'data-url' : url + "/" + i.id
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
    })
});