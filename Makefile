default: versioncheck

clean:
	./gradlew clean

compile: build

build: clean
	./gradlew build -xtest

versioncheck:
	./gradlew dependencyUpdates