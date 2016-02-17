# Serposcope docker image

This folder contains the necessary files to build and run a Serposcope Docker image on any Docker-enabled environment

## Build

Building is done by running the following command from this directory:

```docker build -t yourname/serposcope . ```

If you use Docker Machine, don't forget to configure you Docker daemon accordingly.

## Launching a container

You can launch a container this way:

```docker run -d -p 7134:7134 --name my_serposcope yourname/serposcope```

You should then be able to access the tool at ```http://youServerAddress:7134```

Note that you can change the host port on which you reach serposcope. For instance, if you want to use the 80 port from your host, you can do:

```docker run -d -p 80:7134 --name my_serposcope yourname/serposcope```

## Updating Serposcope without rebuilding the image

You can easily update the Serposcope version in your container, first, access the bash prompt:

``` docker exec -ti my_serposcope /bin/bash ```

Then download the new deb package:

```wget https://serposcope.serphacker.com/download/<replace_by_latest_version>/serposcope_<replace_by_latest_version>_all.deb```

 and install it:

 ```dpkg -i serposcope_<replace_by_latest_version>_all.deb```

 and finally, restart the container:

 ```docker restart my_serposcope```

Your running Serposcope version should now be up-to-date.

## Persisting the update

 You can also commit the changes to the Serposcope image saved on your host (or your registry) by running:

 ```docker commit my_serposcope yourname/serposcope```

## TODO

 Enable configuration of Serposcope options through environment variables.
