# register.sh
A script for automated creation and registration of users via Kartr's RegisterServlet. The arguments are displayname, email and password are passed when called.

Example Values:
- displayname = name-lastname
- email = name@lastname.de
- password = 123abc

## Configuration

- The URL in the script must be modified

## usage
./register.sh <displayname> <email> <password>

# mkanddeploy.sh
The script compiles the project and copies the resulting class files to the build directory. Then a .war file is created. After that, the existing application is undeployed and deleted and the new one uploaded and deployed.

## Configuration

- If you don't want to use the name of the executing user, you have to create a file ".username-override" in the project directory, where the first line contains the corresponding name

- The wildfly URLs have to be adjusted before execution

- Configuartions for the mail server can be adjusted in the script

- In order to upload the file successfully, the access data from the wildfly server must be entered in the .netrc file. This file has to be located in the home directory of the user. Alternatively, the .netrc can be created using the script misc/wildfly/createwildflyusers.sh.

- Configure your database connection in "database settings"
- Configure your storage path settings  in "storage path settings"
- Configure your mail settings in "mail settings"

## usage
./mkanddeploy.sh

# undeploy.sh

The undeploy script is used to undeploy a user specific .war file. The first step is to check if there is a .username-override file. If this is not present, the username of the executing user is used as the basis for the variable *$username-dev*. In addition, the URL of the Wildfly server has to be specified, since this will be used for undeploying and the subsequent check.

Afterwards a request is sent to the server, which checks whether the .war file is still accessible. Depending on the result, a success message or an error is output here as well.

## Configuration

- The URLs in the script has to be adjusted

## usage
./undeploy.sh

# adddatasources.sh

This bash script sets up the database connection from Wildfly to the database in a user-specific way. This script has to be executed on the server where Wildfly is installed.

## Configuration

- The users you want to create are defined in the variable *users*

- database URL, database port and driver name are defined

## usage
./adddatasources.sh

# createwildflyusers.sh

This bash script is used to create the .netrc which contains the userspecific credentials to the Wildfly server. This script has to be executed on the server where Wildfly is installed.

## Configuration

- The Wildfly server URL must be modified

- The users you want to create are defined in the variable *users*

## usage
./createwildflyusers.sh

# cleandatabase.sh

This skript deletes POIs without stories and stories without content.

## configuration

- Database must be set up

## usage
./cleandatabase.sh

# clean.sh

Deletes the build directory from which the .war file is composed. You may know this as 'mvn clean'

## usage
./clean.sh

# garbage-collector.sh

This script deletes POIs without stories and stories without content. It also deletes unused application content such as images.

## Configuration
- The current user is used as the basis of the folder structure

- The application content directory is defined with the *data_dir* variable

- Database must be set up

## usage
./garbage-collector.sh


# init.sh

The bash script, which is used for project initialization automation for predefined or executing user. It creates necessary directories, database tables and inserts some data for testing.

## Configuration 

- If necessary, the user can be pre-defined as the *first line* in the file ".username-override" located in the project directory. Otherwise, the current username, which can be found in the $USER variable, will be used as the executing user for this script.

## Usage

./init.sh


# format-java.sh

This script runs google-java-format on all java files under the src directory, which will format them according to the Google Java Style guidelines.

## Usage

./format-java.sh


# compile.sh

A script, that creates a build folder of the project, which is a copy of "app/", compiles all Java source files into "build/WEB-INF/classes" and includes all required libraries for the project to be able to run correctly.

## Usage

./compile.sh


# deploy-root.sh

The deployment script automates the process of building and deploying the "ROOT.war" package for a project.

Deployment process involves following steps:
- Compiling all Java classes.
- Collecting all project files from "app/" directory.
- Packaging them into "ROOT.war" file.
- Deploying the "ROOT.war" file using the *kartr* user account.
- Creating a directory for file storage and database sources with the same username (kartr).

## Usage

./deploy-root.sh
