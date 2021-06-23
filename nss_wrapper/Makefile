CONTEXT = snowdrop
IMAGE_NAME = nss-wrapper
REGISTRY = quay.io
LOCAL_REGISTRY = localhost:5000

all: build build2 push-local

build:
	docker build -f Dockerfile1 -t ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME}:1 -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME}:1 -t ${CONTEXT}/${IMAGE_NAME}:1 .

build2:
	docker build -f Dockerfile2 -t ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME}:2 -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME}:2 -t ${CONTEXT}/${IMAGE_NAME}:2 .

push-local:
	docker push ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME}:1
	docker push ${LOCAL_REGISTRY}/${CONTEXT}/${IMAGE_NAME}:2