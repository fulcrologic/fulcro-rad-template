#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 target-package"
  exit 1
fi

directory=$(echo $1 | tr -- '-.' '_/')

mkdir -p src/main/$directory
mkdir -p src/test/$directory
rsync -a src/main/com/example/* src/main/$directory/
rsync -a src/test/com/example/* src/test/$directory/
rm -rf src/main/com/example src/test/com/example
for f in shadow-cljs.edn Makefile $(find src/dev src/main/$directory src/test/$directory -type f)
do
  sed -i '' -e "s/com.example/$1/g" $f
done
