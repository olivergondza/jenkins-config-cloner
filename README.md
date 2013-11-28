# Jenkins configuration cloner

Command line utility to clone stored configuration between instances to facilitate migration. Config cloner invokes CLI commands remotely to get and create/update `config.xml`s. For this to work source and destination instances are expected to use compatible versions of core and plugins.

## Requirements

- Bash and Java to run. GIT and Maven to build.
- This permissions needs to be granted to either anonymous user or the user authenticated using [public key](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+CLI#JenkinsCLI-WorkingwithCredentials).
  - Jenkins.READ as a minimum to call remote CLI commands
  - Item.EXTENDED_READ to read jobs from source instance
  - Item.CREATE to create jobs on target instance
  - Item.CONFIGURE to update existing job on target instance
  - Computer.READ to read node config from source instance
  - Computer.CREATE to create nodes on target instance
  - Computer.CONFIGURE to update nodes on target instance
  - View.READ to read view config from source instance
  - View.CREATE to create views on target instance
  - View.CONFIGURE to update views on target instance

### Authentication

The tools supports public key authentization only.

Quoting [Jenkins CLI wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+CLI):

> If your Jenkins requires authentication, you should set up public key authentication. Login from the web UI and go to http://yourserver.com/me/configure, then set your public keys in the designated text area. When connecting to the server, the CLI will look for `~/.ssh/identity`, `~/.ssh/id_dsa`, `~/.ssh/id_rsa` and use those to authenticate itself against the server.

## Usage

Get and build:

	$ git clone git://github.com/olivergondza/jenkins-config-cloner.git
	$ mvn package

There is a convenient wrapper called `clone.sh` to invoke `target/config-cloner-${VERSION}-jar-with-dependencies.jar`
in a comfortable way: `./clone.sh help`.


All the commands have the same pattern 

	./clone.sh TYPE SRC_ITEM DST_ITEM...

`TYPE` identifies the type of item to clone (`job`, `node` etc.). For the convenience items are identified with their URLs.

Clone `my-great-job` from `jnks.old` to `jnks.new/ci` with the same name and to`jnks.pub` having new name `my-great-job-pub`:

	$ ./clone.sh job http://jnks.old/job/my-great-job http://jnks.new/ci/ http://jnks.pub/job/my-great-job-pub

Likewise for nodes

	$ ./clone.sh node http://jnks.old/computer/my-slave http://jnks.new/ci/ http://jnks.pub/computer/cloned-slave

### Options common to all types

`-f|--force` Overwrite destination item if already exists.
