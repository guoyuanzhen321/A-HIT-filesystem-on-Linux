/*
 * 作者：        zhouxiaotian
 * 时间：        2012-5-26
 * 描述：        对程序symlink_tagsfs.c进行修改，实现了对指定源目录下的mp3文件递归查询处理，并可以指定存放地址。
 */
#include <dirent.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h> 
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <getopt.h>
#include <mysql/mysql.h>
#define MAX_BUF_SIZE 1024 // 缓冲区最大字节数
MYSQL *g_conn; // mysql 连接
MYSQL_RES *g_res; // mysql 记录集
MYSQL_ROW g_row; // 字符串数组，mysql 记录行

const char *g_host_name = "localhost";
const char *g_user_name = "root";
const char *g_password = "xtloving"; //mysql用户root的登录密码
const char *g_db_name = "test";
const unsigned int g_db_port = 3306;

void print_mysql_error(const char *msg) {
    // 打印最后一次错误
    if (msg)
        printf("%s: %s\n", msg, mysql_error(g_conn));
    else
        puts(mysql_error(g_conn));
}

int executesql(const char * sql) {
    /*query the database according the sql*/
    if (mysql_real_query(g_conn, sql, strlen(sql))) // 如果失败
        return -1; // 表示失败

    return 0; // 成功执行
}

int init_mysql() {
    // init the database connection
    g_conn = mysql_init(NULL);

    /* connect the database */
    if (!mysql_real_connect(g_conn, g_host_name, g_user_name, g_password, g_db_name, g_db_port, NULL, 0)) // 如果失败
        return -1;

    // 是否连接已经可用
    if (executesql("set names utf8")) // 如果失败
        return -1;

    return 0; // 返回成功
}

typedef struct {
    char identify[3];
    char title[30];
    char artist[30];
    char album[30];
    char year[4];
    char comment[28];
    char reserved1[1];
    char reserved2[1];
    char reserved3[1];

} Mp3Info; //IDV3标签结构


void scan_dir(const char *directory, const char *dest);

int main(int argc, char *argv[]) {
    if (argc != 3) {
        printf("Error!nUsage:%s pathn", argv[0]);
        return 1;
    }
    scan_dir(argv[1], argv[2]);
    return 0;
}

void scan_dir(const char *directory, const char *dest) {
    DIR *dp;
    struct dirent *ptr;
    struct stat statbuf;

    if ((dp = opendir(directory)) == NULL) {
        perror("opendir");
        return;
    }
    chdir(directory);
    int mknewdir;
    mknewdir = mkdir(dest, 0777);
    while ((ptr = readdir(dp)) != NULL) {
        lstat(ptr->d_name, &statbuf);
        if (S_ISDIR(statbuf.st_mode)) {

            if ((strcmp(ptr->d_name, ".") != 0) &&
                    (strcmp(ptr->d_name, "..") != 0) &&
                    (ptr->d_name[0] != '.')) {
                printf("%s/n", ptr->d_name);
                scan_dir(ptr->d_name, dest);
            }
        } else {
            if (ptr->d_name[0] != '.') {
                printf("%s\n", ptr->d_name);
                /*----------------------------new code---------------------*/

                char files[100][100];
                int count = 0;
                int i, j, paste;

                char dirnow[100] = "\0"; //粘贴symlink源目录
                char dirthen[100] = "\0"; //创建symlink目标目录

                char gang[5] = "/"; //就是一个含有“/”的数组，目的是为了添加字符“/”在一个现有目录后面，挺笨的方法，但是确实挺好使
                char mp3[5] = "mp3"; //同上


                FILE *pFile; //文件指针
                Mp3Info song;
                int length = strlen(ptr->d_name);
                for (i = 0; i < length; i++) {
                    files[count][i] = ptr->d_name[i];
                }
                files[count][length] = '\0'; //将文件名存入files[][]二维数组

                if (ptr->d_name[length - 1] == '3' && ptr->d_name[length - 2] == 'p' && ptr->d_name[length - 3] == 'm' && ptr->d_name[length - 4] == '.') {//如果是MP3文件
                    getcwd(dirnow, sizeof (dirnow) - 1); //得到当前目录
                    strcat(dirnow, gang); //目录后加上“/”		
                    strcat(dirnow, ptr ->d_name); //再加上文件名，dirnow现在已经变为MP3文件的绝对地址			

                    pFile = fopen(dirnow, "r"); //文件指针指向MP3文件，类型为只读

                    if (pFile == NULL) {
                        perror("Error opening the file！");
                    } else {
                        fseek(pFile, -128, SEEK_END);
                        fread(&song, 1, sizeof (Mp3Info), pFile); //读取MP3文件ID3V标签
                        printf("ID3V tags : \n");
                        printf("title: %s\n", song.title);
                        printf("artist: %s\n", song.artist);
                        printf("album: %s\n", song.album);
                        printf("year: %s\n", song.year);
                        printf("comment: %s\n\n", song.comment); //打印标签内容

                        //getcwd(dirthen, sizeof (dirthen) - 1); //得到当前目录
                        strcat(dirthen, "/home/zxt/desktop");
                        strcat(dirthen, gang); //目录后加上“/”		
                        strcat(dirthen, mp3); //再加上"mp3"
                        strcat(dirthen, gang); //再加上"/"
                        strcat(dirthen, song.artist); //再加上MP3文件的artist标签，现在dirthen变成了要新创建的MP3文件夹下面的子文件夹
                        mknewdir = mkdir(dirthen, 0777); //创建artist标签文件夹

                        strcat(dirthen, gang); //再加上“/”
                        strcat(dirthen, ptr -> d_name); //再加上MP3文件的文件名，现在dirthen变成了要创建的symlink链接的绝对地址

                        printf("Absdirectory : %s\n", dirnow);
                        printf("Symlinkdirectory : %s\n", dirthen);

                        symlink(dirnow, dirthen); //创建symlink链接

                        if (init_mysql()); //初始化数据库
                        print_mysql_error(NULL);
			//if (executesql("delete  from test")) 
                        //    print_mysql_error(NULL);
                        //char sql[MAX_BUF_SIZE];

                        char youkuohao[] = ")"; //同上，只是给字符串后面加上右括号
                        char douhao[] = ","; //同上，只是给字符串后面加上逗号
                        char danyinhao[] = "'"; //同上，只是给字符串后面加上单引号
                        char sqlinsert[200]; //输入到sql中的insert命令
                        char formerinsert[] = "insert into test values('','"; //输入的sql命令字符串前缀
                        char find[200] = "select count(*) from test where abs-directory='";
                        strcpy(sqlinsert, formerinsert);
                        strcat(sqlinsert, dirnow);
                        strcat(sqlinsert, danyinhao);
                        strcat(sqlinsert, douhao);
                        strcat(sqlinsert, danyinhao);
                        strcat(sqlinsert, dirthen);
                        strcat(sqlinsert, danyinhao);
                        strcat(sqlinsert, youkuohao); //现在的sqlinsert完整了
                        //sprintf(sql, sqlinsert);
                        strcat(find, dirnow);
                        strcat(find, danyinhao);
			int number=0;
			number=mysql_query(g_conn,find);
			if(number<=0)
				
                        {
				if (executesql(sqlinsert))//将命令sqlinsert输入到mysql执行
                            print_mysql_error(NULL);
			}

                        if (executesql("select * from test")) // 得到表test全部信息
                            print_mysql_error(NULL);

                        g_res = mysql_store_result(g_conn); // 从服务器传送结果集至本地，mysql_use_result直接使用服务器上的记录集

                        int iNum_rows = mysql_num_rows(g_res); // 得到记录的行数
                        int iNum_fields = mysql_num_fields(g_res); // 得到记录的列数

                        printf("Database : \n");
                        printf("共%d个记录，每个记录%d字段\n", iNum_rows, iNum_fields);

                        puts("id\tabsdirectory\tsymlinkdirectory\n");

                        while ((g_row = mysql_fetch_row(g_res))) // 打印结果集
                            printf("%s\t%s\t%s\n", g_row[0], g_row[1], g_row[2]); // 第一，第二和第三字段

                        mysql_free_result(g_res); // 释放结果集

                        mysql_close(g_conn); // 关闭链接

                    }
                    fclose(pFile);
                }

                count++;
                /*-----------------------------new code end-----------------*/
            }
        }
    }
    chdir("..");
    closedir(dp);
}
void mysql_action(){


}
