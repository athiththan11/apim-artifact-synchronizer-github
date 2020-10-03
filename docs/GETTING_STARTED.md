# Using GitHub as Artifact Synchronizer

This guide explains how to configure the WSO2 API Manager v3.2.0 and a public GitHub repository to store and retrieve Gateway Runtime artifacts.

Learn more about Artifact Synchronizer feature in [here](https://apim.docs.wso2.com/en/latest/install-and-setup/setup/distributed-deployment/synchronizing-artifacts-in-a-gateway-cluster/).

> The guide is demonstrated using a distributed environment where an All-In-One API Manager node and two Gateway nodes are deployed.

## Build & Deploy

Build the project by executing the following maven command from the root directory of the project

> You can also find pre-built Jar artifacts from [Releases](https://github.com/athiththan11/apim-artifact-synchronizer-github/releases)

```sh
mvn clean install
```

After a successful build, copy the built Jar artifacts from the following defined location and place it inside your API Manager components as instructed

- `components/synchronizer-github-core`: Copy the built JAR artifact and place it inside the `<apim>/repository/components/dropins` directory of both Publisher and Gateway nodes
- `components/synchronizer-github-saver`: Copy the built Jar artifact to the `<apim-publisher>/repository/components/dropins` directory of the Publisher node
- `components/synchronizer-github-retriever`: Copy the built Jar artifact and place it inside the `<apim-gateway>/repository/components/dropins` directory of the Gateway nodes

## Configure GitHub

> The following section is demonstrated using a public repository

- Sign-in to GitHub and create a new Public Repository (ex: `artifact-repository`)
- Let's create a GitHub Access Token to read and write to the Repositories
  - From your GitHub profile page > Go to Settings
  - Select Developer Settings > Personal Access Tokens
  - Click on `Generate new token`
  - Give a description in the Note space and select the following permissions
    - `repo` > `public_repo`
  - Click on `Generate Token to generate a Personal Access Token
  - Copy the Access Token and save it locally as we will be passing this Access Token when starting up the servers

## Configure API Manager

### Publisher Node

Configure the Publisher node as described below to use the GitHub Saver to save the Gateway Runtime artifacts in the specified GitHub location

- Open the `<apim-publisher>/repository/conf/deployment.toml` configuration and add the following `sync_runtime_artifacts` configurations

    ```toml
    [apim.sync_runtime_artifacts.publisher]
    artifact_saver = "GHSynchronizerSaver"
    publish_directly_to_gateway = false
    ```

- Start the Publisher node (with or without profile optimization) with the following System Properties passing the credentials
  - `-DGitHubRepo`: The GitHub repository name
  - `-DGitHubOwner`: The GitHub username
  - `-DGitHubAccessToken`: The GitHub Access Token generate in the previous step

  A sample startup command will be as following

  ```sh
  sh wso2server.sh -DGitHubAccessToken=1234567890987654321 -DGitHubOwner=athiththan11 -DGitHubRepo=artifact-repository
  ```

### Gateway Node

Configure the Gateway nodes as described below to use the GitHub Retriever to retrieve the Gateway Runtime artifacts from the specified GitHub location.

- Open the `<apim-gateway>/repository/conf/deployment.toml` configuration and add the following `sync_runtime_artifacts` configurations

    ```toml
    [apim.sync_runtime_artifacts.gateway]
    # configure the Gateway Labels to pull the related artifacts
    gateway_labels = ["Production Gateway"]
    artifact_retriever = "GHSynchronizerRetriever"
    deployment_retry_duration = 15000
    data_retrieval_mode = "sync"
    event_waiting_time = 5000
    ```

- Start the Gateway node (with profile optimization) with the following System Properties passing the credentials
  - `-DGitHubRepo`: The GitHub repository name
  - `-DGitHubOwner`: The GitHub username
  - `-DGitHubAccessToken`: The GitHub Access Token generate in the previous step
  
  A sample startup command will be as following

  ```sh
  sh wso2server.sh --optimize -Dprofile=gateway-worker --skipConfigOptimization -DGitHubAccessToken=1234567890987654321 -DGitHubOwner=athiththan11 -DGitHubRepo=artifact-repository
  ```

## Troubleshooting

You can enable the following `DEBUG` logs in the API Manager components to verify whether the GitHub Synchronizer components are getting activated and working as expected

```properties
# on publisher and gateway nodes
logger.github-core.name=synchronizer.github.core
logger.github-core.level=DEBUG

# on publisher nodes
logger.github-saver.name=synchronizer.github.saver
logger.github-saver.level=DEBUG

# on gateway nodes
logger.github-retriever.name=synchronizer.github.retriever
logger.github-retriever.level=DEBUG
```

Place the above-mentioned properties in the `<apim>/repository/conf/log4j2.properties` and add the respective logger names to the `loggers`

```properties
# example
loggers = github-core, github-saver, AUDIT_LOG ...
```
