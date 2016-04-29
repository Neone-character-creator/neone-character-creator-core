$(document).ready(function(){
	var availablePlugins = $("#available-plugins");
		$.get({
			url : "/games/",
			contentType : "application/json",
			cache : true
		}).success(function(result){
			availablePlugins.empty();
			$.each(result, function(index, element){
				var item= $("<a>", {
					class : "list-group-item",
					href : "/games/" + element.author + "/" + element.system + "/" + element.version,
					text : element.system + " " + element.version
				});
				availablePlugins.append(item);
			})
		}).error(function(result){
			select.empty();
			select.append("Sorry, there was an error contacting the server.");
		})
});