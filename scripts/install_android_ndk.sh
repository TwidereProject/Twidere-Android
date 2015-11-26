#!/bin/bash

local_opt_dir="${HOME}/.local/opt"

ndk_name="android-ndk-r10e"
ndk_install_dest="${local_opt_dir}/${ndk_name}"

if [ -d ${ndk_install_dest} ];
    then exit
fi

cd ${local_opt_dir}

if [ `uname -m` = x86_64 ];
then
    ndk_installer_name="${ndk_name}-linux-x86_64.bin"
    wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O ndk.bin
else
    ndk_installer_name="${ndk_name}-linux-x86.bin"
fi

wget "http://dl.google.com/android/ndk/${ndk_installer_name}"

chmod +x ${ndk_installer_name}

./${ndk_installer_name} -y

rm ${ndk_installer_name}

echo "ndk.dir=${ndk_install_dest}" >> ./local.properties