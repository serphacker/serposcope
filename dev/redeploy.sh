if [ "$1" = stop ]; then
    docker-compose stop
    docker-compose rm -f
else
    docker-compose stop
    docker-compose rm -f
    docker-compose up -d
fi