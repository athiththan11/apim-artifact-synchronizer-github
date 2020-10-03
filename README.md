# APIM Gateway Artifact Synchronizer with GitHub

[:construction: Dev in progress]

A sample implementation of Gateway Artifact Synchronizer using GitHub APIs to store and retrieve the Gateway Runtime artifacts. Learn more about Artifact Synchronizations and support in WSO2 API Manager v3.2.0 in [here](https://apim.docs.wso2.com/en/latest/install-and-setup/setup/distributed-deployment/synchronizing-artifacts-in-a-gateway-cluster/).

> Artifact Synchronizer is a new feature available in WSO2 API Manager from v3.2.0 to sync Gateway Runtime artifacts in a Gateway Cluster environment

This repo contains the following components and implementations

- `synchronizer-github-core`: Core package containing the utilities and client implementations
- `synchronizer-github-saver`: Artifact Saver implementation with GitHub
- `synchronizer-github-retriever`: Artifact Retriever implementation with GitHub

## Getting Started

To get started, please refer to [Using GitHub as Artifact Synchronizer](docs/GETTING_STARTED.md).

## Build

Execute the following maven command from the root directory to build the project and its modules

```sh
mvn clean install
```

Please follow [Using GitHub as Artifact Synchronizer](docs/GETTING_STARTED.md) to configure, deploy and run the synchronizer.

## License

[Apache 2.0](LICENSE)
