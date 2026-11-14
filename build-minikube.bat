& minikube -p minikube docker-env --shell powershell | Invoke-Expression

kubectl delete service smrp-mn
kubectl delete deployment smrp-mn

mvnw clean package jib:dockerBuild "-Dimage=smrp-mn:1.0.0" "-DskipTests" "-Dpackaging=docker"

kubectl apply -f minikube.yaml

minikube cache delete smrp-mn:1.0.0
minikube image rm smrp-mn:1.0.0

minikube image load smrp-mn:1.0.0 --overwrite