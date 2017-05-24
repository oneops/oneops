
FORCE=false
NO_DEPLOY=true
OO_SKIP=true
GIT_UPDATE=true

while getopts ":fsz" opt; do
  case $opt in 
  s ) OO_SKIP=true ;;
  f ) FORCE=true ;;
	z ) NO_DEPLOY=true ;;
	g ) GIT_UPDATE=true ;;
  esac
done

if [ $FORCE ]; then
  rm -rf oneops-build-converter
fi 

if [ $GIT_UPDATE ]; then
 git submodule update --recursive --remote
fi

cd oneops-build-converter

if [ $OO_SKIP ]; then
	./convert-clean.sh
	./convert.sh
	./mvnw clean package
fi

if [ $NO_DEPLOY ]; then
	version=$(mvn help:evaluate -Dexpression=project.version|grep -v '\[INFO\]')
	oo_artifact=$(pwd)/oneops-distribution/target/oneops-$version.tar.gz 

	cd ..

	packer build -var "oneops_artifact=$oo_artifact" -var-file=centos73.json centos.json
fi




