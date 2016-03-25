# Serposcope DEV docker

This folder contains the necessary files to develop a Serposcope on any Docker&Docker-Compose - enabled environment, you need to have installed:
* docker
* docker-compose



## COMPILE and RUN
When we start, we build and run two docker containers. First `dev_serposcope_1` with Serposcope, second `dev_db_1` with DataBase(mysql:5.7). In first run `entrypoint.sh` witch compile and run Serposcope code. Main relations between containers present in `docker-compose.yml`.
Look configuration of Database in file: `serposcope/web/src/main/resources/serposcope.conf`.

This is done by running the following command from this directory:

```sh redeploy.sh``` 

For stopping container use the next command:

```sh redeploy.sh stop```