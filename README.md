### What is this repository for? ###

* Multitenancy webapp hosting container.

This project is the core subproject of a system that is meant to provide 
character creation aids for a multitude of tabletop RPG system.

This project also includes the following subprojects:
- Plugin API : https://bitbucket.org/ThisIsNoZaku/neone-character-creator-plugin-api

This is the container which hosts the individual webapps and provides 
shared backend functionality, such as database persistence and user authentication.

## Components
The majority of the application use involves plugins. It provides three 
major services for plugins:
 
- Serving Plugin resources
- Persisting Plugin Data
- User Authentication

###Serving Plugin Resources
Each plugin is identified by the name of its creator, the name of the 
game system it is for, and its version.

The plugins contain all the html, javascript, css, etc. files. When a 
request to a url associated with a plugin, the application fetches the
resource from within the plugin to serve it.

Plugins content is served under the /games.

For example, getting index.html for a Plugin written by the author Foo, for the game Bar and plugin version Baz, would
be found at the url "/games/Foo/Bar/Baz/index.html".

###Persisting Plugin Data
Plugins are required to implement an API with a single simple
javascript method for retrieving the character data in a form for
persistence in the database and to fill a copy of the character PDF.

This API requires the plugin to attach a method named `character` to the window object. This method may take a single
parameter and serves as a combined getter/setter. If called without an argument, the method will return a
JSON-stringified form of the current state of the character object.

If called with a String argument, the plugin must attempt to parse the string and use the parsed object to set the
current character state if valid.

The core displays a fixed wrapper page with controls, with the plugin content wrapped inside a frame. These wrapper
controls handle retrieving the information from the backend and pushing it into the plugin as well as pulling from the
plugin to send to the server.

This character information is sent and retrieved as raw JSON and is persisted in a MongoDB store.

###User Authentication
The application is responsible for authentication, which is shared among all plugins and is required for actions that
involve reading and writing to the database, specifically saving and loading characters.

Authentication is currently implemented via OAuth with Google as the  sole OAuth provider.

##Planned Improvements
- Replace core with pluggable front-end.
- Add support for addtional OAuth providers (Twitter, Facebook, etc.)