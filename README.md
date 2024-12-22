## Welcome to Schematica!

### Compiling

[Setup Java](#setup-java)

[Setup Git](#setup-git)

[Setup Schematica](#setup-schematica)

[Compile Schematica](#compile-schematica)

[Updating Your Repository](#updating-your-repository)

#### Setup Java

The Java JDK is used to compile Schematica.

1. Download and install the Java JDK.
   * [Windows/Mac download link](https://adoptium.net/temurin/releases/?os=any).
   * Linux: Installation methods for certain popular flavors of Linux are listed below. If your distribution is not
     listed, follow the instructions specific to your package manager or install it
     manually [here](https://adoptium.net/temurin/releases/?os=any).
      * Archlinux: `pacman -S jdk21-openjdk`
2. Set up the environment.
    * Windows: Set environment variables for the JDK.
       1. Go to `Control Panel\System and Security\System`, and click on `Advanced System Settings` on the left-hand
          side.
        2. Click on `Environment Variables`.
        3. Under `System Variables`, click `New`.
        4. For `Variable Name`, input `JAVA_HOME`.
       5. For `Variable Value`, input something similar to `C:\Program Files\Java\jdk1.7.0_45` exactly as shown (or
          wherever your Java JDK installation is), and click `Ok`.
        6. Scroll down to a variable named `Path`, and double-click on it.
       7. Append `;%JAVA_HOME%\bin` EXACTLY AS SHOWN and click `Ok`. Make sure the location is correct; double-check
          just to make sure.
3. Open up your command line and run `javac`. If it spews out a bunch of possible options and the usage, then you're
   good to go. If not try the steps again.

#### Setup Git

Git is used to clone Schematica and update your local copy.

1. Download and install Git [here](http://git-scm.com/download/).
2. *Optional* Download and install a Git GUI client, such as Github for Windows/Mac, SmartGitHg, TortoiseGit, etc. A
   nice list is available [here](http://git-scm.com/downloads/guis).

#### Setup Schematica

This section assumes that you're using the command-line version of Git.

1. Open up your command line.
2. Navigate to a place where you want to download Schematica's source (eg `C:\Development\Github\Minecraft\`) by
   executing `cd [folder location]`. This location is known as `mcdev` from now on.
3. Execute `git clone https://github.com/TimMayr/Schematica-Modern.git`. This will download Schematica's source into
   `mcdev`.
4. Download and build [LunatriusCore-Modern](https://github.com/TimMayr/LunatriusCore-Modern). Then copy both of the
   files in build/libs to the libs folder in this project
5. Right now, you should have a directory that looks something like:

***

    mcdev
    \-Schematica
        \-Schematica's files (should have build.gradle)

***

#### Compile Schematica

1. Import the gradle project in an ide like intellij
2. Run the build task
3. Go to `mcdev\Schematica-Modern\build\libs`.
   * You should see a `.jar` file named `LunatriusCore-#.#.#-#.#.#.#.jar`.
4. Copy the jar into your Minecraft mods folder, and you are done!
5. Alternatively you can use the Client run configuration in your ide

#### Updating Your Repository

In order to get the most up-to-date builds, you'll have to periodically update your local repository.

1. Open up your command line.
2. Navigate to `mcdev` in the console.
3. Make sure you have not made any changes to the local repository, or else there might be issues with Git.
    * If you have, try reverting them to the status that they were when you last updated your repository.
4. Execute `git pull master`. This pulls all commits from the official repository that do not yet exist on your local
   repository and updates it.

Shamelessly based this README off [pahimar's version](https://github.com/pahimar/Equivalent-Exchange-3).