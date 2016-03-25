docker-compose stop
docker-compose rm -f

if [ "$1" = stop ]; then
    exit 1
fi

docker-compose up -d