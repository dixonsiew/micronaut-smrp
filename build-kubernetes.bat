kubectl delete service smrp-service
kubectl delete deployment smrp-micronaut

mvnw clean package jib:dockerBuild "-Dimage=docker.io/wfsiew888/smrp-micronaut:1.24" "-DskipTests" "-Dpackaging=docker"

@REM  docker tag smrp-micronaut:1.22 docker.io/wfsiew888/smrp-micronaut:1.22
docker push docker.io/wfsiew888/smrp-micronaut:1.24

kubectl apply -f deployment.yaml