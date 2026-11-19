@echo off
setlocal EnableDelayedExpansion

REM ============================================
REM Configuration
REM ============================================
set IMAGE_NAME=docker.io/wfsiew888/smrp-mn
set IMAGE_TAG=1.0.0
set SHORT_IMAGE=wfsiew888/smrp-mn:%IMAGE_TAG%
set SERVICE_NAME=smrp-mn
set DEPLOYMENT_NAME=smrp-mn
set DEPLOY_FILE=deployment.yaml

echo ============================================
echo  Starting deployment of %SERVICE_NAME%
echo ============================================
echo.

REM ============================================
REM Step 1: Delete Service
REM ============================================
echo [1/6] Deleting service %SERVICE_NAME%...
kubectl delete service %SERVICE_NAME% --ignore-not-found=true
if errorlevel 1 (
    echo [WARN] Failed to delete service. Continuing...
)

REM ============================================
REM Step 2: Delete Deployment
REM ============================================
echo.
echo [2/6] Deleting deployment %DEPLOYMENT_NAME%...
kubectl delete deployment %DEPLOYMENT_NAME% --ignore-not-found=true
if errorlevel 1 (
    echo [WARN] Failed to delete deployment. Continuing...
)

REM ============================================
REM Step 3: Remove Local Docker Image
REM ============================================
echo.
echo [3/6] Removing local Docker image %SHORT_IMAGE%...
docker rmi -f %SHORT_IMAGE% 2>nul
if errorlevel 1 (
    echo [WARN] Local image not found or already removed. Continuing...
) else (
    echo [OK] Local image removed.
)

REM ============================================
REM Step 4: Maven Build with Jib
REM ============================================
echo.
echo [4/6] Running mvnw clean package jib:dockerBuild...
call mvnw.bat clean package jib:dockerBuild "-Dimage=%IMAGE_NAME%:%IMAGE_TAG%" "-Dpackaging=docker"
if errorlevel 1 (
    echo [ERROR] Maven/Jib build failed. Aborting.
    exit /b 1
)

REM ============================================
REM Step 5: Docker Push
REM ============================================
echo.
echo [5/6] Pushing Docker image to registry...
docker push %IMAGE_NAME%:%IMAGE_TAG%
if errorlevel 1 (
    echo [ERROR] Docker push failed. Aborting.
    exit /b 1
)

REM ============================================
REM Step 6: Apply Deployment
REM ============================================
echo.
echo [6/6] Applying %DEPLOY_FILE%...
kubectl apply -f %DEPLOY_FILE%
if errorlevel 1 (
    echo [ERROR] kubectl apply failed.
    exit /b 1
)

echo.
echo ============================================
echo  Deployment completed successfully!
echo ============================================
endlocal
exit /b 0