# (1) MODE
LOCAL_BUILD_MODE=dev
echo "> ====== (1) Build Mode ====== : $LOCAL_BUILD_MODE"

# (2) JAVA PATH
LOCAL_JAVA_PATH=/usr/local/java-17-openjdk-17.0.8.0.7-1.portable.jdk.el.x86_64/bin/java
echo "> ====== (2) Java Path ====== : $LOCAL_JAVA_PATH"

# (3) PAKAGE NAME
LOCAL_PAKAGE_PATH=ebmp-proj-standard-api
echo "> ====== (3) Pakage Path ====== : $LOCAL_PAKAGE_PATH"

# (4) JAR
LOCAL_REPOSITORY_PATH=/home/$LOCAL_PAKAGE_PATH
LOCAL_BUILD_PATH=$(ls -tr ${LOCAL_REPOSITORY_PATH}/*.jar | tail -1)
LOCAL_JAR_NAME=$(basename $LOCAL_BUILD_PATH)
echo "> ====== (4) Build File ====== : $LOCAL_JAR_NAME"

cd $LOCAL_REPOSITORY_PATH

# (5) KILL -15 PID
echo "> ====== (5) Check application PID. ======"
LOCAL_PID_PATH=$(cat ${LOCAL_PAKAGE_PATH}.pid)
echo "> ====== (5) kill -15 $LOCAL_PID_PATH ======"
kill -15 $LOCAL_PID_PATH
echo "$LOCAL_PID_PATH"

# (6) KILL -9 PID
echo "> ====== (6) Check application PID. ======"
LOCAL_CURRENT_PID=$(pgrep -f -n $LOCAL_JAR_NAME)
echo "$LOCAL_CURRENT_PID"

if [ -z $LOCAL_CURRENT_PID ];
  then
    echo "> ====== (6) No running applications found. ======"
else
	echo "> ====== (6) kill -9 $LOCAL_CURRENT_PID ======"
	kill -9 $LOCAL_CURRENT_PID
	sleep 5
fi

# (7) Run application.
echo "> ====== (7) Run application. ======"
nohup $LOCAL_JAVA_PATH -Dspring.profiles.active=$LOCAL_BUILD_MODE -jar -Xms512m -Xmx512m $LOCAL_JAR_NAME > /dev/null 2>&1 &