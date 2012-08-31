//symlink_tagsfs v0.1：


// "THE BEER-WARE LICENSE" (Revision 43):
// Steve Guo wrote this file. As long as you retain this notice you
// can do whatever you want with this stuff. If we meet some day, and you think
// this stuff is worth it, you can buy me a beer in return.

1.安装mysql并且手动创建database：test，在其中创建table：test，形式为：id | absdirectory | symlinkdirectory。

	参考步骤：
	1）通过网络安装sql，打开终端，输入：sudo apt-get install mysql-server，设置root密码。

	2）启动sql，在终端中输入：mysql -u root -p ，之后按提示输入密码。如果出现“mysql >”字样，则证明启动正常。

	3）输入：create database test;（最后的分号异常重要！）创建数据库“test”。

	4）输入：use test；启用test数据库。

	5）输入：create table test (id int(11) auto_increment not null primary key); 创建表“test”。

	6)输入：alter table test add absdirectory varchar(200); 创建项abs-directory代表原音乐文件的绝对地址。

	7)输入：alter table test add symlinkdirectory varchar(200); 创建项symlink-directory代表symlink文件的地址。
	
	8）输入：describe test；查看表test的结构。
	
	9)输入：select * from test; 即可查看test表全部内容。	
      
2.编译symlink_tagsfs.c文件：在终端中输入：gcc symlink_tagsfs.c -o symlink_tagsfs -lmysqlclient

3.在同一文件夹下放入几个MP3文件。（最好为拥有完整规范IDV3标签的MP3文件）（文件名中不能含有“'”符号，因为这会影响sql命令的识别！）

4.运行symlink_tagsfs文件:在终端中输入：./symlink_tagsfs

5.程序会首先创建MP3文件夹，然后读取MP3文件的IDV3标签，根据其中的artist标签创建MP3文件夹下面相应artist的子文件夹，之后创建指向相对应源音乐文件的symlink链接。
  同时将源文件绝对地址和新创建的symlink链接文件地址存入mysql数据库中。

6.后续版本中准备实现：1）实时对symlink链接文件的监控，如果检查到对文件的重命名或者删除，则根据相应数据库数据重命名或者删除源音乐文件。

                  2）针对不同类型音乐文件如wma,wav,midi等，进行操作。 
	
		  3）实现传递进更多参数，对应实现更多功能。


