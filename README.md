# Jenkins configuration cloner

## Requirements

- Bash and Java to run. GIT and Maven to build.
- This permissions needs to be granted to either anonymous user or the user authenticated using [public key](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+CLI#JenkinsCLI-WorkingwithCredentials).
  - Item.EXTENDED_READ to read jobs from source instance
  - Item.CREATE to create jobs on target instance
  - Item.CONFIGURE to update existing job on target instance
  - Jenkins.ADMINISTER to manipulate nodes

### Authentication

Quoting [Jenkins CLI wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+CLI):

> If your Jenkins requires authentication, you should set up public key authentication. Login from the web UI and go to http://yourserver.com/me/configure, then set your public keys in the designated text area. When connecting to the server, the CLI will look for `~/.ssh/identity`, `~/.ssh/id_dsa`, `~/.ssh/id_rsa` and use those to authenticate itself against the server.

## Usage

Get and build:

	$ git clone git://github.com/olivergondza/jenkins-config-cloner.git
	$ mvn package

Get help:

	$ ./clone help

Clone `my-great-job` from `jenkins-old` to `jenkins-new/ci` with the same name and to`jenkins-pub` having new name `my-great-job-pub`:

	$ ./clone job http://jenkins.old/job/my-great-job http://jenkins.new/ci/ http://jenkins.pub/job/my-great-job-pub

Likewise for nodes

	$ ./clone node http://jenkins.old/computer/my-slave http://jenkins.new/ci/ http://jenkins.pub/computer/my-cloned-slave
