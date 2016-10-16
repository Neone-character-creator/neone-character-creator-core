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

###Persisting Plugin Data
Plugins are required to provide implementations for a few simple 
javascript methods: one for retrieving the character data in a form for 
persistence in the database, another for the character data intended to
be used to fill a copy of the character PDF.

###User Authentication
The application is responsible for authentication, which is shared among
all plugins and is required for actions that involve the database, 
specifically saving and loading characters.

Authentication is currently implemented via OAuth with Google as the 
sole OAuth provider.

##Future Improvements
- Replace core with pluggable front-end.
- Add support for addtional OAuth providers (Twitter, Facebook, etc.)
- Remove plugin class methods