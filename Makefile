all: build

build:
	gradle shadowJar

run:
	java -jar build/libs/cnotation2svg-0.9.jar

view: run
	gwenview test.svg

test.jpg: test.svg
	convert test.svg test.jpg

view-jpg: test.jpg
	gwenview test.jpg

document.pdf: document.md test.jpg
	~/bin/mkpan document.pdf

view-pdf: document.pdf
	okular document.pdf

clean:
	gradle clean

