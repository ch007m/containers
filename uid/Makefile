CONTEXT = snowdrop
IMAGE_NAME = openjdk11-git
REGISTRY = quay.io

all: build

build:
	docker build -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .

build2:
	docker build -f Dockerfile2 -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .


clean:
	rm -f build