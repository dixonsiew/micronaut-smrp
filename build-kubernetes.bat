kubectl delete service smrp-service
kubectl delete deployment smrp-micronaut

mvnw clean package jib:dockerBuild "-Dimage=smrp-micronaut:1.19" "-DskipTests" "-Dpackaging=docker"

docker tag smrp-micronaut:1.19 docker.io/wfsiew888/smrp-micronaut:1.19
docker push docker.io/wfsiew888/smrp-micronaut:1.19

kubectl apply -f deployment.yaml