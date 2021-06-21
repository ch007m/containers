REGISTRY = quay.io
CONTEXT = snowdrop
IMAGE_NAME = ubi8-openjdk11-git-entrypoint

all: build

build:
	docker build -t ${REGISTRY}/${CONTEXT}/${IMAGE_NAME} -t ${CONTEXT}/${IMAGE_NAME} .