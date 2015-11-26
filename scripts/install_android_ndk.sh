#!/bin/bash

project_dir=${OLDPWD}
local_opt_dir="${HOME}/.local/opt"

ndk_name="android-ndk-r10e"
ndk_install_dest="${local_opt_dir}/${ndk_name}"

if [ -d ${ndk_install_dest} ]; then
    echo "NDK already installed in ${ndk_install_dest}"
    exit
fi

if [ ! -d ${local_opt_dir} ]; then
    mkdir -p ${local_opt_dir}
fi

cd ${local_opt_dir}

if [ `uname -m` = x86_64 ];
then
    ndk_installer_name="${ndk_name}-linux-x86_64.bin"
    wget http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O ndk.bin
else
    ndk_installer_name="${ndk_name}-linux-x86.bin"
fi

echo "Downloading NDK installer ${ndk_installer_name}"

wget "http://dl.google.com/android/ndk/${ndk_installer_name}"

chmod +x ${ndk_installer_name}

echo "Installing NDK installer ${ndk_installer_name}"

./${ndk_installer_name} -y 1>/dev/null

echo "Removing installed NDK installer ${ndk_installer_name}"

rm ${ndk_installer_name}

echo "Adding NDK install path to ${project_dir}"

echo "ndk.dir=${ndk_install_dest}" >> ${project_dir}/local.properties