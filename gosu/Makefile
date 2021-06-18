CONTEXT = snowdrop
IMAGE_NAME = openjdk11-git-gosu
REGISTRY = quay.io
USERNAME ?= iocanel

all: build

build:
	docker build --build-arg username=${USERNAME} -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .

clean:
	rm -f build