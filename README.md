# Multi-threaded Java Http Server
![](http_art.png)

## Introduction
This was a little project I worked on to learn about the fundamentals of Networking. You can
    examine the source code of this project to understand how to get started with a multi-threaded
    HTTP server in Java that server HTML content and .png files.

## How to compile and run the program
1. Open your CLI of choice ideally with the JAVA_HOME path configured.
2. Using the "cd" command to change your directory to the source of this project "simple-http-server/src".
3. Now type "javac http_server/HttpServerListener.java" to compile the program.
4. Once that is completed, type:
    "java http_server/HttpServerListener.java [port] [relative path to public file]".
    It is recommended that you use port 80. If you didn't change
    the structure of the project, write "../public" for the path.
5. The program should now be up and running!


## How to use the program
### Generating code 200 - Successfully accessing pages in the "/public" directoryjav
1. Open up the web browser of your choice.
2. Enter the URI "localhost:[port]/[file]" to access the file of your choice
    that resides in the "/public" directory. For most web browsers, you can
    leave the [port] part empty if you picked 80, since it's the default.
    If you don't specify a file, it will look for an "index.html" in either the
    "/public" directory, or in the directory that you have specified.

### Generating code 302
1. Try to access the image "/funny_clown.png" or "/blue_sphere.png".
2. You will redirected to "/clown.png" or "/world.png" respectively.

### Generating code 404
1. Try to access a file that doesn't exist in the "/public" directory,
    for example: "/a/l/lol.html"
2. You will be given an error code of 404 and redirected to the 404 page.

### Generating code 500
This one is difficult to generate organically and while it is possible to do so,
    a preset page has been made to automatically generate this error.

#### Method 1:
1. Try to access the page "/500.html".
2. You will be given error code 500 and directed to its page.

#### Method 2:
1. Try escaping the "/public" directory using ".." in the URI.
2. You will be given error code 500 and directed to its page.
    This is so that the potential hacker doesn't get much information.