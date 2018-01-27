echo '==> Configuring OneOps for vagrant'

export BUILD_BASE='/home/oneops/build'
export OO_HOME='/home/oneops'
export GITHUB_URL='https://github.com/oneops'
export PATH=$PATH:/usr/local/bin


if [ ! -d $BUILD_BASE ]; then
  mkdir -p $BUILD_BASE
fi

if [ ! -d $OO_HOME ]; then
  mkdir -p $OO_HOME
fi

mv /tmp/oneops-continuous.tar.gz $OO_HOME/oneops-continuous.tar.gz

cd $OO_HOME

tar zxf oneops-continuous.tar.gz -C /home/

azure=${azure_inductor:-false}

mkdir -p $OO_HOME/dist/oneops-admin-inductor

if [ "$azure" = "true" ]; then
  tar zxf $OO_HOME/dist/oneops-admin-*-inductor-az.tar.gz -C $OO_HOME/dist/oneops-admin-inductor
else
  tar zxf $OO_HOME/dist/oneops-admin-*-inductor.tar.gz -C $OO_HOME/dist/oneops-admin-inductor
fi

mkdir -p $OO_HOME/dist/oneops-admin-adapter && tar zxf $OO_HOME/dist/oneops-admin-*-adapter.tar.gz -C $OO_HOME/dist/oneops-admin-adapter

chmod a+x /etc/init.d/display

./oneops_build.sh "$@"

if [ $? -ne 0 ]; then
  exit 1;
fi

source /tmp/create_user.sh
