CONTEXT = snowdrop
IMAGE_NAME = nss-wrapper
REGISTRY = quay.io
LOCAL_REGISTRY = localhost:5000

all: build

build:
	docker build -t ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .

build-test:
	docker build -f Dockerfile.test -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME}-test -t ${CONTEXT}/${IMAGE_NAME}-test .

push-local:
	docker push ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME}