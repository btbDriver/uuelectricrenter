#!/usr/bin/env bash
echo ".............................................................."
echo "开始从svn中更新protobuffer文件........"
echo ".............................................................."

svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-access/UUAccess-App/src/main/resources/proto pbDefine/UUAccess-App
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UU-Facade/src/main/resources/proto pbDefine/UU-Facade
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Account/src/main/resources/proto pbDefine/UUFacade-Account
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Activity/src/main/resources/proto pbDefine/UUFacade-Activity
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Dot/src/main/resources/proto pbDefine/UUFacade-Dot
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Auth/src/main/resources/proto pbDefine/UUFacade-Auth
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Passport/src/main/resources/proto pbDefine/UUFacade-Passport
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Trip/src/main/resources/proto pbDefine/UUFacade-Trip
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Usecar/src/main/resources/proto pbDefine/UUFacade-Usecar
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Message/src/main/resources/proto pbDefine/UUFacade-Message
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Order/src/main/resources/proto pbDefine/UUFacade-Order
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Pay/src/main/resources/proto pbDefine/UUFacade-Pay
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-User/src/main/resources/proto pbDefine/UUFacade-User
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Ext/src/main/resources/proto pbDefine/UUFacade-Ext
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-access/UUAccess-LongConnection/src/main/resources/proto pbDefine/UUAccess-LongConnection
svn checkout svn://svnserver.carp2p.com/uuzuche/uuusecar/trunk/uu-facade/UUFacade-Advertisement/src/main/resources/proto pbDefine/UUFacade-Advertisement
echo ".............................................................."
echo "从svn中更新protubuffer文件完成..........."
echo ".............................................................."


    

:<<!
echo ".............................................................."
echo "开始复制protobuffer 文件........"
echo ".............................................................."
function scandir() {
    local cur_dir parent_dir workdir
    workdir=$1
    cd ${workdir}
    if [ ${workdir} = "/" ]
    then
        cur_dir=""
    else
        cur_dir=$(pwd)
    fi

    for dirlist in $(ls ${cur_dir})
    do
        if test -d ${dirlist};then
            cd ${dirlist}
            scandir ${cur_dir}/${dirlist}
            cd ..
        else
            echo ${cur_dir}/${dirlist}
            cp -R ${cur_dir}/${dirList} /Users/aaron/Program/protobuffer/uuclientlite
        fi
    done
}

rm -rf uuclientlite
if test -d uuproject
then
    scandir uuproject
elif test -f uuproject
then
    echo "you input a file but not a directory,pls reinput and try again"
    exit 1
else
    echo "the Directory isn't exist which you input,pls input a new one!!"
    exit 1
fi
echo ".............................................................."
echo "复制protobuffer 文件结束.........."
echo ".............................................................."



cd ..
echo ".............................................................."
echo "开始执行修改protobuffer文件........."
echo ".............................................................."
lite_fold=pbDefine
rm -rf ${lite_fold}
cp -R uuclientlite ${lite_fold}
cd ${lite_fold}
ls -l | fgrep proto | awk '{print $NF}' | while read filename;
do
echo "trans ${filename}"
awk 'BEGIN{print "option optimize_for = LITE_RUNTIME;"} {print}' ${filename} >  ${filename}.lite
rm -rf ${filename}
mv ${filename}.lite ${filename}
done
echo ".............................................................."
echo "修改pb文件完成...................."
echo ".............................................................."

cd ..
echo ".............................................................."
echo "开始执行复制pb文件到项目目录........."
echo ".............................................................."
cp -R ./pbDefine /Users/aaron/Program/uuelectricrenter/uuconfig/pbUpdate
echo ".............................................................."
echo "执行复制pb文件到项目目录结束........."
echo ".............................................................."
!


#echo ".............................................................."
#echo "开始生成java文件................."
#echo ".............................................................."
#./protobuf-ios/compiler/src/protoc --proto_path=./uufinal --java_out./src/main/java ./uufinal/*.proto
#echo ".............................................................."
#echo "生成java文件结束................."
#echo ".............................................................."