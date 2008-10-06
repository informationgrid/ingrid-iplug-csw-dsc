set -e

cd core
maven clean:clean jar:deploy

cd ../ui
maven clean war:deploy

#todo: deploy snapshot of distribution
cd ..
