#echo "Project Build"
#./gradlew clean build -x test

echo "Docker Build"
docker build --no-cache -t dvlprjw/yeoyeo .

echo "Docker Push"
docker push dvlprjw/yeoyeo