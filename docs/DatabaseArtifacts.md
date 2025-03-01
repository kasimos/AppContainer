## CI/CD and database artifacts

Goal: Use a git repo and activate the database objects. All separation of concerns must be possible, individually per object.

Summary:

1. A repository should have a first directory level with the schema names as used in production, e.g. STAGING, DWH, DATA, ...
1. Within each of this schema directories create a tree of directories to manage the dependencies, e.g. 01-tables, 02-views. When activating objects the scripts/directories are executed sorted by name.
1. All schema references to this schema use `${CURRENTOWNER}`.
1. All references to other schemas, e.g. a script in DATA must access STAGING is using `${STAGING}`.
1. Optionally a `.globalmapping` file in the root directory translates these into the pyhsical names, e.g. STAGING -> DEV_STAGING. This file must be added to .gitignore.


### Separation of concerns

In a large project multiple developers work together in parallel and the code is deployed in the development system, test and production.
Dev, test, prod are usually different databases or different servers so there is no immediate problem at deployment, except that maybe the schema names are different.
The developers themselves have two contradicting requirements. On the one hand they want to use what others team members created, but they do not want to be impacted by work-in-progress database objects of others. All objects in the shared development database must be usable by others and pure local testing should happen in the developers own schema.

As a more technical statement, in each script the developer writes the schema must be parameterized. Which schema to use is decided at deploy-time.

Separation of concerns are:

- Every developer is 100% independent, all objects are activated in his personal schema
- One database supports deploying the same artifacts but in different schemas
- Objects are in the same schema no matter who is using them

As a further complication, multiple schemas are used to group database objects and for security reasons. This allows for multiple approaches.

1. No schema: The create table simply does not specify any schema `create table orders (...`. At deploy time the table is created in whatever schema is configured as the current session schema of that user.
1. Explicitly use a schema: Then the create table would look like `create table data.orders (...`. If executed, the schema is hard coded and cannot be changed at deployment. Bad idea.
1. Parameterize the schema: When this appcontainer sees the text `${any_schema_name}` it assumes this schema name is an alias and replaces the parameter with the actually configured schema for each alias. `create table ${staging}.orders (...` and `${staging}` being mapped to `DEV_STAGING_AREA` would create a table DEV_STAGING_AREA.ORDERS.
1. Parameterize the schema with ${CURRENTOWNER}: This alias maps the schema to where the script is located. So it specifies a schema (unlike in 1.), hence does not rely on the session schema settings but creates the object in a schema with the same name as the connected database user name.

This approach allows to work with multiple schemas, e.g. `create view ${monitor_schema}.status select * from ${staging_area}.load_status`.


#### Configure the schema mappings

Each git repository can have a file at root level named `.globalmapping`. It is a Json file in the format:

```
{
	"mappings" : {
		"alias1" : "dbschema1",
		"alias2" : "dbschema2",
		...
		...
	}
}
```

and contains the alias and actual schema pairs. This file will be different for each cloned repository, thus must be in `.gitignore`. 

When the deployment process is executed, the scripts are parsed and all textual occurrences of an alias gets replaced with the actual name according to above file. In case an alias is missing, the deployment process stops with an error.

Important: The SQL script processor does not understand the SQL statements, otherwise it would be too version dependent. It understands the basic script syntax, what is a line comment, a block comment, a string constant, a line comment within a SQL statement but not the actual SQL syntax as such. As result a `create table "${schema1}"."TABLE1" (...` replaces the `${schema1}` part of the string thus preserving the double quotes. A comment like `// Use ${schema1}` is left untouched.


#### CURRENTOWNER and files without schema

For SQL scripts specifying a schema is no problem - it is part of the file content. But there are other files that do not have that option, e.g. a CSV file to load static data into a table. Where to specify the schema/alias for that? The answer is simple, it is using the CURRENTOWNER semantic the SQL scripts allow for as well. Leaves the question of what the value for this variable is.

The directory structure of database artifacts should have the target schema as first level. This is used as CURRENTOWNER and modified by the globalmapping.

Example:

[ ] developer1: The root directory of the currently logged in user. This is where the git repo is cloned into.
    [ ] STAGING: All objects meant for the production schema STAGING go in this directory. So STAGING is the value of the CURRENTOWNER.
        [ ] ORDER_STATUS.csv: The table STAGING.ORDER_STATUS should contain the data of this file.
        [ ] views.sql: A script that creates multiple views. It can use ${CURRENTOWNER} but does not have to.
    [ ] DWH: All objects meant for the production schema DWH go in this directory. So DWH is the value of the CURRENTOWNER.
        [ ] objects.sql: A script that creates all/some database objects. It can use ${CURRENTOWNER} but does not have to and the value would be DWH.

As with all other schema names, the actual schema is derived using the `.globalmapping` file. So if there is an alias for `DWH` to use the actual schema `DEV_DWH` this will be the actual value of CURRENTOWNER.


### Activation Order

When activating a directory, everything underneath will be executed. The order of the objects is important of course, as e.g. the tables should be created first and then the data loaded and the views using the tables.
If one directory does have a file `.depends` all directories listed in there will be activated first. If the depends file points to a directory that is outside the activation scope, it is ignored.
Then all files of the current directory are retrieved, sorted by name and then the files, after that the directories are activated.
Therefore the dependencies can be managed by directories, naming of the files the `depends` file or any combination of that.

The structure of the `.depends` file:

```
{
	"dependents" : [
		"../tables",
		"/staging",
		...
		...
	]
}
```

#### Why this way?

For the dependency handling there would have been multiple other options:

1. Analyze each SQL statement and get the required dependencies from there.
1. Ask the developer to list the dependencies in the file header instead of one depends file per directory.
1. Use the directory tree only.

In other words, different levels of implicit and explicit dependency trees.

Another problem with the activation is that every object depends on other objects and is a dependency. If for example a database view is activated, the logic might be to force an activation of the underlying database create-table scripts. These tables cause other views to be recreated as well which in turn cause .... At the end all objects are recreated just because a single view got a column comment.

A database has a rather clearly defined hierarchy and objects must be upgraded. These are different requirements compared to a C++ compiler. While this method is not perfect either, it has proven to be the best balance between flexibility and usability and speed.

Activating the full database, a fresh install, a re-activation or an upgrade to the next version just mean activating the root directory and everything will be executed in the obvious and visual order.
