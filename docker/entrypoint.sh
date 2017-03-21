#! /bin/bash

conf_file="/etc/serposcope.conf"

function replace_param {
  sed -i -r -e "/^# *${1}=/ {s|^# *||;s|=.*$|=|;s|$|$(eval echo \$$2)|}" $conf_file
}

if [ -n "$SERPOSCOPE_DB_URL" ]
then
  replace_param "serposcope.db.url" "SERPOSCOPE_DB_URL"
else
  echo "SERPOSCOPE_DB_URL is not set, keeping the default value"
fi

if [ -n "$SERPOSCOPE_DB_OPTIONS" ]
then
  replace_param  "serposcope.db.options" "SERPOSCOPE_DB_OPTIONS"
else
  echo "SERPOSCOPE_DB_OPTIONS not set, keeping the default value"
fi

if [ -n "$SERPOSCOPE_DB_DEBUG" ]
then
replace_param  "serposcope.db.debug" "SERPOSCOPE_DB_DEBUG"
else
  echo "SERPOSCOPE_DB_DEBUG not set, keeping the default value"
fi

service serposcope start && tail -F /var/log/serposcope/startup.log
