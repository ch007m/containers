CONTEXT = snowdrop
IMAGE_NAME = ubi8-openjdk11-git
REGISTRY = quay.io
LOCAL_REGISTRY = localhost:5000

all: build push-local

build:
	docker build -t ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .

push-local:
	docker push ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME}