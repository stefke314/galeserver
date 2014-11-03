---------------------------------------
    Gale Installation Instructions
---------------------------------------

1. Software requirements

To run Gale you will need the following:
  - JDK 6 (http://java.sun.com/javase/downloads/index.jsp)
  - Maven 2 (http://maven.apache.org/download.html) 
  - Tomcat 6 (don't use the Windows installer) (http://tomcat.apache.org/download-60.cgi)
  - MySQL 5.1 (Essentials) (http://dev.mysql.com/downloads/mysql/5.1.html#downloads)

(note: JDK 5 will work as well)

2. Installation

We recommend creating a directory structure for gale that has the following layout:
  /gale
    /gale-source
    /jdk
    /maven
    /mysql
    /tomcat

Unpack/install all archives in their respective directories and create an environment
script file to setup your Gale environment. In Windows this would be a batch file (a text
file with extension .bat or .cmd) and might look like:

@echo off
set "PACKAGE_HOME=C:\gale" 
rem java 
set "JAVA_HOME=%PACKAGE_HOME%\jdk" 
set "path=%JAVA_HOME%\bin;%PATH%" 
rem tomcat 
set "CATALINA_HOME=%PACKAGE_HOME%\tomcat" 
set "path=%CATALINA_HOME%\bin;%PATH%" 
rem maven 
set "MAVEN2_HOME=%PACKAGE_HOME%\maven" 
set "path=%MAVEN2_HOME%\bin;%PATH%" 
rem mysql 
set "MYSQL_HOME=%PACKAGE_HOME%\mysql" 
set "path=%MYSQL_HOME%\bin;%PATH%" 

Download the Gale source from svn (https://svn.win.tue.nl/repos/gale/trunk) to your local
gale-source directory.

3. Setup

Run the script to ensure a proper environment.

Start and setup MySQL:
  - go to '/gale/gale-source' directory.
  - run (in Windows) 'start mysqld --standalone --console'
  - run 'mysql -u root < gale.sql'

The following steps will install GALE if you are not going to use eclipse to develop with
GALE. You can skip these steps if you use eclipse.

Go to your '/gale/gale-source/master' directory and run 'mvn install'.

Copy '/gale/gale-source/gale/target/gale.war' to your '/gale/tomcat/webapps' directory
and start Tomcat by running 'startup'.

4. Development

We recommend using eclipse (http://www.eclipse.org/downloads/) to develop with Gale. Run
'mvn eclipse:eclipse -Dwtpversion=2.0' in your '/gale/gale-source/master' directory to 
setup the project for use in eclipse. 

Additional plugins (like maven and subversion for eclipse), can be installed by adding 
update sites to eclipse and installing plugins. Proceed as follows:
  - maven, update site 'http://m2eclipse.sonatype.org/update-dev/', select
    'Maven Integration' (tree), 'Maven Central repository index' and
    'Maven Integration for WTP'
  - subclipse, update site 'http://subclipse.tigris.org/update_1.6.x',
    select 'subclipse'

To install plugins, go to 'Help', 'Software Updates...', select the tab 'Available 
Software', and add the update site there.

To import the gale projects, choose 'Import...' from the 'File' menu, select 'General', 
'Maven projects', and browse to '/gale/gale-source'.