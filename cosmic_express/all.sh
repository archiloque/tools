mvn clean install -DskipTests
parallel --jobs 3 --ungroup -v < levels.txt

