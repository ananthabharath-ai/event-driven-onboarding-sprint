# This file builds our custom Jenkins image
# It takes the official, NEW image...
FROM jenkins/jenkins:lts-jdk17

# ...and installs the Docker CLI inside it.
USER root

# 1. Install prerequisites
RUN apt-get update && apt-get install -y \
    ca-certificates \
    curl \
    gnupg

# 2. Add Docker's official GPG key
#    [THE FIX]: Added the "-k" flag to the curl command
#    to skip SSL verification for this download.
RUN install -m 0755 -d /etc/apt/keyrings
RUN curl -fsSLk https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
RUN chmod a+r /etc/apt/keyrings/docker.asc

# 3. Set up the Docker repository
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

# 4. Install the Docker CLI (docker-ce-cli)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    docker-ce-cli

# 5. Fix the Git SSL error
RUN git config --global http.sslVerify false

# 6. Switch back to the jenkins user
USER jenkins