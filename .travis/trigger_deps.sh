#!/bin/sh

# Params:
#   - Downstream module name (e.g. vlingo-actors)
#   - Release version (e.g. 1.0.4)
#   - Next development version (e.g. 1.0.5-SNAPSHOT)
trigger_dependency()
{
    echo "Triggering $1 v$2. Next development version $3"

    cd $TRAVIS_BUILD_DIR/..
    git clone --depth=50 --branch=master https://${GITHUB_TOKEN}@github.com/vlingo/$1.git
    cd $1

    # Update project version and all io.vlingo dependencies
    mvn versions:set -DnewVersion=$2
    mvn versions:use-dep-version -Dincludes=io.vlingo -DdepVersion=$2 -DforceVersion=true

    git add pom.xml
    git commit -m "Release v$2"
    git tag "$2" -m "Release v$2"

    # Prepare for next development version
    mvn versions:set -DnewVersion=$3
    mvn versions:use-dep-version -Dincludes=io.vlingo -DdepVersion=$3 -DforceVersion=true
    git add pom.xml
    git commit -m "Next development version $3"

    git push --follow-tags
}

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Detected version $VERSION"

git config --global user.email "VaughnVernon"
git config --global user.name "vaughn@forcomprehension.com"

# New version
MAJOR=$(echo $VERSION | cut -f 1 -d '.')
MINOR=$(echo $VERSION | cut -f 2 -d '.')
PATCH=$(echo $VERSION | cut -f 3 -d '.')
NEW_VERSION=$MAJOR.$MINOR.$(($PATCH + 1))-SNAPSHOT

for dependency in "vlingo-wire" "vlingo-telemetry" "vlingo-streams";
do
    trigger_dependency $dependency $VERSION $NEW_VERSION
done
