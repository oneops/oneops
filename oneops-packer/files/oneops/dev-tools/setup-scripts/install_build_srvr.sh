#!/bin/sh
export BUILD_BASE='/home/oneops/build'
export OO_HOME='/home/oneops'
export SEARCH_SITE=localhost
export GITHUB_URL='https://github.com/oneops'
echo "$BUILD_BASE"

mkdir -p "$BUILD_BASE"
cd "$BUILD_BASE"
pwd

if [ -d "$BUILD_BASE/build-wf" ]; then
  echo "doing git pull"
  cd "$BUILD_BASE/build-wf"
  git pull
else
  echo "doing git clone"
  git clone "$GITHUB_URL/build-wf.git"
fi
sleep 2

cd "$BUILD_BASE/build-wf"

if [ ! -e "$BUILD_BASE/build-wf/queue.xml" ]; then
	rake stop
	rake install
	if [ ! $? -eq 0 ]; then
		echo "rake install failed, retrying"
		rake install
		if [ ! $? -eq 0 ]; then
			echo "Can not instal jenkins, exiting :-("
			exit 1
		fi
	fi
fi

mkdir -p "$OO_HOME/dist"

export OO_PACKAGE_PATH="$OO_HOME/dist"
#export OO_AUTH="6ocBr2X5aHNB9WsrWdAw:x"
export M2_HOME=/usr/local/maven
export M2=/usr/local/maven/bin

cp $OO_HOME/hudson.tasks.Maven.xml $BUILD_BASE/build-wf

rake server

sleep 5

ref="$1"
if [[ -z  $ref ]]; then
  ref=17.04.26-RC2
fi

echo "submit build $ref"
curl -X POST http://localhost:3001/job/oo-all-oss/build --data token=TOKEN --data-urlencode json="{'parameter': [{'name':'REF', 'value':'$ref'}, {'name':'VERSION', 'value':'continuous'}, {'name':'TAG', 'value':'false'}, {'name':'PUSH_TAG', 'value':''}]}"

GREP_RETURN_CODE=0

# Poll every thirty seconds until the build is finished
while [ $GREP_RETURN_CODE -eq 0 ]
do
    echo "waiting jenkins build job to complete"
    sleep 30
    # Grep will return 0 while the build is running:
    curl --silent http://localhost:3001/job/oo-all-oss/lastBuild/api/json | grep building\":true > /dev/null
    GREP_RETURN_CODE=$?
done

curl --silent http://localhost:3001/job/oo-all-oss/lastBuild/api/json | grep result\":\"SUCCESS > /dev/null

if [ $? -eq 1 ]; then
    echo "build failed, please check jenkins log, or go to http://localhost:3001"
    exit 1;
else
    echo "build is complete, stoping jenkins!"
    rake stop
fi
