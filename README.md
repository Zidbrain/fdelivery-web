Frontend for f-delivery - a groceries delivery service.

## Running a development server

To run a development server run:

```bash
$ ./gradlew kobwebStart -t
```
or, if you have kobweb installed in PATH

```bash
$ kobweb run
```
Open [http://localhost:8100](http://localhost:8100) with your browser to see the result.

You can use any editor you want for the project, but we recommend using **IntelliJ IDEA Community Edition** downloaded
using the [Toolbox App](https://www.jetbrains.com/toolbox-app/).

Press `Q` in the terminal to gracefully stop the server.

### Live Reload

Feel free to edit / add / delete new components, pages, and API endpoints! When you make any changes, the site will
indicate the status of the build and automatically reload when ready.

## Exporting the Project

To export the project run:

```bash
$ ./gradlew kobwebExport -PkobwebReuseServer=false -PkobwebEnv=DEV -PkobwebRunLayout=KOBWEB -PkobwebBuildTarget=RELEASE -PkobwebExportLayout=STATIC
```

or, if you have kobweb installed in PATH

```bash
kobweb export --layout static
```

This will export the site into .kobweb/site directory.