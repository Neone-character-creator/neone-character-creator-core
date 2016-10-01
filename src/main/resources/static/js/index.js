$(document).ready(function(){
	var spinner = $("#spinner");
	var availablePlugins = $("#available-plugins");
		$.get({
			url : "/games/",
			contentType : "application/json",
			cache : true
		}).success(function(result){
			availablePlugins.empty();
			$.each(result, function(index, element){
				var item= $("<a>", {
					class : "list-group-item plugin",
					href : encodeURI("/games/" + element.author + "/" + element.system + "/" + element.version),
					text : element.system + " " + element.version
				});
				availablePlugins.append(item);
			})
		}).error(function(result){
			select.empty();
			select.append("Sorry, there was an error contacting the server.");
		});

	$(document).on("mouseenter", ".plugin", function(e){
		var target = $(e.target);
		$("#plugin-description").empty().append(spinner);
		$.ajax(target.attr("href") + "/pages/info", {
			dataType : "html"
		}).done(function(r){
			$("#plugin-description").empty().append(r);
		});
	});
});