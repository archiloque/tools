mvn clean install -DskipTests
caffeinate -i parallel --shuf --jobs 3 --ungroup -v < levels.sh

