all: build

build:
	gradle shadowJar

run:
	java -jar build/libs/cnotation2svg-0.9.jar

clean:
	gradle clean

