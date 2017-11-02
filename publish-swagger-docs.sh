#!/usr/bin/env bash
# Runs the api and backend db, grabs the generated swagger json spec and compares to what is in the
# central reform-api-docs repo. Updates reform-api-docs spec if needed

if [ "$TRAVIS_BRANCH" != "master" ]
then
  echo "This commit was made against the $TRAVIS_BRANCH and not the master! Not updating docs"
  exit 0
fi

docker-compose up -d
sleep 10
REPO_NAME=$(echo ${TRAVIS_REPO_SLUG} | cut -f2- -d/)
CURRENT_DOCS=$(curl https://hmcts.github.io/reform-api-docs/specs/${REPO_NAME}.json)
NEW_DOCS=$(curl http://localhost:8800/v2/api-docs)
docker-compose stop

if [ "$NEW_DOCS" == "" ]; then
    echo "Could not retrieve new docs, aborting."
elif [[ "$CURRENT_DOCS" != "$NEW_DOCS" ]]; then
    echo "Update reform-api-docs"
    mkdir swagger-staging
    cd swagger-staging
    git init

    git config user.name "Travis CI"
    git config user.email "travis@travis-ci.org"
    git remote add upstream "https://${GH_TOKEN}@github.com/hmcts/reform-api-docs.git"
    git pull upstream master

    TARGET_SPEC=docs/specs/${REPO_NAME}.json
    echo ${NEW_DOCS} > ${TARGET_SPEC}

    git add ${TARGET_SPEC}
    git commit -m "Updating spec for ${REPO_NAME} from ${TRAVIS_PULL_REQUEST_SLUG}"
    git push --set-upstream upstream master
else
    echo "API Documentation is up to date."
fi

exit 0
