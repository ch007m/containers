CONTEXT = snowdrop
IMAGE_NAME = nss-wrapper
REGISTRY = quay.io

all: build

build:
	docker build -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .

build-test:
	docker build -f Dockerfile.test -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME}-test -t ${CONTEXT}/${IMAGE_NAME}-test .

push:
	docker push ${REGISTRY}/${CONTEXT}/${IMAGE_NAME}