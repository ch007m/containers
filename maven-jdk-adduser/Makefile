CONTEXT = snowdrop
IMAGE_NAME = maven-openjdk11
REGISTRY = quay.io

all: build

build:
	docker build -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .

push:
	docker push ${REGISTRY}/${CONTEXT}/${IMAGE_NAME}

clean:
	rm -f build