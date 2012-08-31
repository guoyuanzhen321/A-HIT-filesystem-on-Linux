#include "widget.h"
#include "ui_widget.h"
#include <QtGui>
#include <sys/stat.h>
#include <sys/types.h>
#include <stdio.h>
#include <dirent.h>
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <getopt.h>
#include <string.h>
#include <mysql/mysql.h>
#include <QString>
#include <vector>
#include <iostream>
#include <string>
#include <sys/inotify.h>
#include <map>
#include <errno.h>
#include <vector>

#include <QtCore/QCoreApplication>
#include <QtSql>
#include <QDebug>
#include <iostream>
#include <QTextStream>
#include <QMessageBox>

using namespace std;

void addwatch(int, char*, int);
static int filter_action(inotify_event *event);
int watch_init(int mask, char *root);
void addwatch(int fd, char *dir, int mask);
static void do_action(int fd, struct inotify_event *event);
void watch_mon(int fd);
static void send_mess(char *name, char *act, int ewd);
void append_dir(int fd, struct inotify_event *event, int mask);
void scan_dir(const char *directory, const char *dest);


enum{MASK = IN_MODIFY | IN_CREATE | IN_DELETE| IN_MOVED_FROM};
enum {EVENT_SIZE = sizeof(struct inotify_event)};
enum {BUF_SIZE = (EVENT_SIZE + 16) << 10};

map<int, string> dirset;

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

Widget::Widget(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Widget)
{
    ui->setupUi(this);
}

Widget::~Widget()
{
    delete ui;
}

QString s2q(const string &s)
{
return QString(QString::fromLocal8Bit(s.c_str()));
}
string q2s(const QString &s)
{
return string((const char *)s.toLocal8Bit());
}

void Widget::on_pushButtonchyuanmulu_clicked()
{
    QString dirget = QFileDialog::getExistingDirectory(this, tr("Open Directory"),"/home",QFileDialog::ShowDirsOnly);
    ui->lineEdityuanmulu->setText(dirget);
}

void Widget::on_pushButtonchmubiaomulu_clicked()
{
    QString dirget = QFileDialog::getExistingDirectory(this, tr("Open Directory"),"/home",QFileDialog::ShowDirsOnly);
    ui->lineEditmubiaomulu->setText(dirget);
}

int watch_init(int mask, char *root)
{
        int i, fd;

        if ((fd = inotify_init()) < 0)
                perror("inotify_init");
        addwatch(fd, root, mask);
        return fd;
}

void addwatch(int fd, char *dir, int mask)
{
        int wd;
        char subdir[512];
        DIR *odir;
        struct dirent *dent;

        if ((odir = opendir(dir)) == NULL)
        {
                perror("fail to open root dir");
                exit(1);
        }
        wd = inotify_add_watch(fd, dir, mask);
        dirset.insert(make_pair(wd, string(dir)));

        errno = 0;
        while ((dent = readdir(odir)) != NULL)
        {
                if (strcmp(dent->d_name, ".") == 0
                        || strcmp(dent->d_name, "..") == 0)
                        continue;
                if (dent->d_type == DT_DIR)
                {
                        sprintf(subdir, "%s/%s", dir, dent->d_name);
                        addwatch(fd, subdir, mask);
                        printf("addwatch %s\n",subdir);
                        fflush(stdout);
                }
        }

        if (errno != 0)
        {
                perror("fail to read dir");
                exit(1);
        }

        closedir (odir);
}



void watch_mon(int fd)
{
        int i, length;
        void *buf;
        struct inotify_event *event;
        buf = malloc(BUF_SIZE);

        while ((length = read(fd, buf, BUF_SIZE)) >= 0)
        {
                i = 0;
                while (i < length)
                {
                        event = (struct inotify_event*)(buf + i);
                        if (event->len)
                                do_action(fd, event);
                        i += EVENT_SIZE + event->len;
                }
        }
        close(fd);
        exit(1);
}

static char action[][10] =
{
        "modified",
        "accessed",
        "created",
        "removed"
};

enum{NEWDIR = IN_CREATE | IN_ISDIR};

static void do_action(int fd, struct inotify_event *event)
{
        int  i;

        if ((filter_action(event)) < 0)
                return;
        if ((event->mask & NEWDIR) == NEWDIR)
                append_dir(fd, event, MASK);

       //send_mess(event->name, action[ia], event->wd);
}

void append_dir(int fd, struct inotify_event *event, int mask)
{
        char ndir[512];
        int wd;

        sprintf(ndir, "%s/%s", dirset.find(event->wd)->second.c_str(),
                        event->name);
        wd = inotify_add_watch(fd, ndir, mask);
        dirset.insert(make_pair(wd, string(ndir)));
}
/*
static int filter_action(uint32_t mask)
{
        if (mask & IN_MODIFY)
                return 0;
        if (mask & IN_ACCESS)
                return 1;
        if (mask & IN_CREATE)
                return 2;
        if (mask & IN_DELETE)
                return 3;
        if (mask & IN_MOVED_FROM)
        {
            printf("change name");
            fflush(stdout);
            return 4;
        }
        return -1;
}
*/
static int filter_action(struct inotify_event *event)
{
    QSqlQuery query;

        if (event->mask & IN_MODIFY)
                return 1;
        if (event->mask & IN_ACCESS)
                return 1;
        if (event->mask & IN_CREATE)
                return 1;
        if (event->mask & IN_DELETE)
        {
            printf("%s have been deleted!",event->name);
            fflush(stdout);
            QString name=QString::fromLocal8Bit(event->name,30);
            QString sql="delete from test WHERE symlinkdirectory = "+name;
            //strcat(sql,event->name);
            query.exec(sql);
            //on_pushButtonrefresh_clicked();
            return 1;
        }
        if (event->mask & IN_MOVED_FROM)
        {
            printf("%s change name!\n",event->name);
            fflush(stdout);
            return 1;
        }
        return -1;
}
static void send_mess(char *name, char *act, int ewd)
{
        char format[] = "%s was %s.\n";
        char file[512];

        sprintf(file, "%s/%s", dirset.find(ewd)->second.c_str(), name);

        printf(format, file, act);
        fflush(stdout);
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
    printf("%s\n",dest);
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

                        char youkuohao[] = ")"; //同上，只是给字符串后面加上右括号
                        char douhao[] = ","; //同上，只是给字符串后面加上逗号
                        char danyinhao[] = "'"; //同上，只是给字符串后面加上单引号
                        char sqlinsert[200]; //输入到sql中的insert命令
                        char formerinsert[] = "INSERT INTO test VALUES('0','"; //输入的sql命令字符串前缀
                        char find[200] = "select count(*) from test where absdirectory='";
                        strcpy(sqlinsert, formerinsert);
                        strcat(sqlinsert, dirnow);
                        strcat(sqlinsert, danyinhao);
                        strcat(sqlinsert, douhao);
                        strcat(sqlinsert, danyinhao);
                        strcat(sqlinsert, dirthen);
                        strcat(sqlinsert, danyinhao);
                        strcat(sqlinsert, youkuohao); //现在的sqlinsert完整了
                        strcat(find, dirnow);
                        strcat(find, danyinhao);
                        int strsize=strlen(sqlinsert);
                        QString insertsql = QString::fromLocal8Bit(sqlinsert,strsize);
                        insertsql.trimmed();
                        QString findsql=QString::fromLocal8Bit(find,200);
                        QSqlQuery query;
                        //query.exec(findsql);
                        //query.next();
                        //int findnum = query.value(0).toInt();
                       // if(findnum<=0){
                        //qDebug() << insertsql;
                        query.exec(insertsql);
                       // }
                        //query.exec("SELECT * FROM test");
                        //sprintf(sql, sqlinsert);



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




void Widget::on_pushButtoncreate_clicked()
{
    QSqlDatabase db = QSqlDatabase::addDatabase("QMYSQL");
    db.setHostName("localhost");
    db.setDatabaseName("test");
    db.setUserName("root");
    db.setPassword("xtloving");
    if (!db.open())
         qDebug() << "Failed to connect to root mysql admin";
    else
         std::cout<<"succeed!"<<std::endl;

    QSqlQuery query;
    query.exec("delete from test");
    query.exec("SELECT * FROM test");

    while(query.next())
    {
        QString id = query.value(0).toString();
        QString type = query.value(1).toString();
        QString data = query.value(2).toString();
        QString sqlout = id + "\n" + type + "\n" + data;
        //ui->listWidget->addItem(sqlout);
    }


    QString change1 = ui->lineEdityuanmulu->text();
    QString change2 = ui->lineEditmubiaomulu->text() + "/mp3";
    //const char* strchange1 = change1.toAscii().constData();
    //const char* strchange2 = change2.toAscii().constData();
    QByteArray ba = change1.toLatin1();
    const char *strchange1 = ba.data();
    //const char* strchange2 = change2.toAscii().constData();
    QByteArray ba2 = change2.toLatin1();
    const char *strchange2 = ba2.data();
    scan_dir(strchange1, strchange2);
    fflush(stdout);

    /*
    char *dest = const_cast<char*>(strchange2);
    strcat(dest,"/mp3");
    fd=watch_init(MASK,dest);
    watch_mon(fd);
    */
    pid_t pid;
    if((pid=fork())<0)
        printf("Fork error!");
    else if(pid==0){
    int fd;
    char *dest = const_cast<char*>(strchange2);
    //strcat(dest,"/mp3");
    fd=watch_init(MASK,dest);
    watch_mon(fd);
    }


    //ui->lineEditmubiaomulu->setText(ui->lineEditmubiaomulu->text() + "/mp3");
    QDirModel *model = new QDirModel;
    ui->treeView->setModel(model);
    ui->treeView->setRootIndex(model->index(ui->lineEditmubiaomulu->text() + "/mp3"));

}



void Widget::on_pushButtonGetIDV3_clicked()
{
    ui->treeView->setSelectionMode(QTreeView::ExtendedSelection);
    ui->treeView->setSelectionBehavior(QTreeView::SelectRows);
    QModelIndexList list = ui->treeView->selectionModel()->selectedIndexes();
    QDirModel* model = (QDirModel*)ui->treeView->model();
    QModelIndex index = list[0];
    QFileInfo fileInfo = model->fileInfo(index);
    QString selectedfilepath = fileInfo.absoluteFilePath();
    qDebug() << selectedfilepath << '\n';

    QSqlQuery query;
    //query.exec("INSERT INTO test VALUES('','sss','sss')");
    //query.exec("SELECT * FROM test");
    QString selectedfromdb = "SELECT absdirectory FROM test WHERE symlinkdirectory = '" + selectedfilepath + "'";
    query.exec(selectedfromdb);
    query.next();
    QString selectedasbd = query.value(0).toString();
    qDebug() << selectedasbd << "\n";

    FILE *pFile; //文件指针
    Mp3Info song;
    QByteArray ba = selectedasbd.toAscii();//将所得文件名转换为字符//yfx0527
    const char *dirnow = ba.data();
    //printf("%s\n",dirnow);
    pFile = fopen(dirnow, "r");
    fseek(pFile, -128, SEEK_END);
    fread(&song, 1, sizeof (Mp3Info), pFile); //读取MP3文件ID3V标签
    //printf("%s\n",song.album);
    QString title = QString::fromLocal8Bit(song.title,30);
    QString artist = QString::fromLocal8Bit(song.artist,30);
    QString album = QString::fromLocal8Bit(song.album,30);
    QString year = QString::fromLocal8Bit(song.year,4);
    QString comment = QString::fromLocal8Bit(song.comment,28);
    ui->lineEditTitle->setText(title);
    ui->lineEditArtist->setText(artist);
    ui->lineEditAlbum->setText(album);
    ui->lineEditYear->setText(year);
    ui->lineEditComment->setText(comment);
    fclose(pFile);
}
void Widget::on_pushButtonDelete_clicked()
{

    ui->treeView->setSelectionMode(QTreeView::ExtendedSelection);
    ui->treeView->setSelectionBehavior(QTreeView::SelectRows);
    QModelIndexList list = ui->treeView->selectionModel()->selectedIndexes();
    QDirModel* model = (QDirModel*)ui->treeView->model();
    QModelIndex index = list[0];
    QFileInfo fileInfo = model->fileInfo(index);
    QString selectedfilepath = fileInfo.absoluteFilePath();
    qDebug() << selectedfilepath << '\n';

    QSqlQuery query;
    //query.exec("INSERT INTO test VALUES('','sss','sss')");
    //query.exec("SELECT * FROM test");
    QString selectedfromdb = "SELECT absdirectory FROM test WHERE symlinkdirectory = '" + selectedfilepath + "'";
    query.exec(selectedfromdb);
    query.next();
    QString selectedasbd = query.value(0).toString();
    qDebug() << selectedasbd << "\n";

    QByteArray ba1 = selectedasbd.toAscii();//将所得文件名转换为字符//yfx0527
    const char *dirnow1 = ba1.data();
    remove(dirnow1);
    QByteArray ba2 = selectedfilepath.toAscii();//将所得文件名转换为字符//yfx0527
    const char *dirnow2 = ba2.data();
    remove(dirnow2);
}

void Widget::on_pushButtonrefresh_clicked()
{
    QDirModel *model = new QDirModel;
    ui->treeView->setModel(model);
    ui->treeView->setRootIndex(model->index(ui->lineEditmubiaomulu->text() + "/mp3"));
}

void Widget::on_pushButtonChIDV3_clicked()
{
    ui->treeView->setSelectionMode(QTreeView::ExtendedSelection);
    ui->treeView->setSelectionBehavior(QTreeView::SelectRows);
    QModelIndexList list = ui->treeView->selectionModel()->selectedIndexes();
    QDirModel* model = (QDirModel*)ui->treeView->model();
    QModelIndex index = list[0];
    QFileInfo fileInfo = model->fileInfo(index);
    QString selectedfilepath = fileInfo.absoluteFilePath();
    qDebug() << selectedfilepath << '\n';

    QSqlQuery query;
    //query.exec("INSERT INTO test VALUES('','sss','sss')");
    //query.exec("SELECT * FROM test");
    QString selectedfromdb = "SELECT absdirectory FROM test WHERE symlinkdirectory = '" + selectedfilepath + "'";
    query.exec(selectedfromdb);
    query.next();
    QString selectedasbd = query.value(0).toString();
    qDebug() << selectedasbd << "\n";

    FILE *pFile; //文件指针
    Mp3Info song;
    QByteArray ba = selectedasbd.toAscii();//将所得文件名转换为字符//yfx0527
    const char *dirnow = ba.data();
    //printf("%s\n",dirnow);
    pFile = fopen(dirnow, "wb");
    fseek(pFile, -125, SEEK_END);

    int i = 0;
    char title[30];
    for(i = 0;i < 30;i ++)
    {
        title[i] = ' ';
    }
    char artist[30];
    for(i = 0;i < 30;i ++)
    {
        artist[i] = ' ';
    }
    char album[30];
    for(i = 0;i < 30;i ++)
    {
        album[i] = ' ';
    }
    char year[4];
    for(i = 0;i < 4;i ++)
    {
        year[i] = ' ';
    }
    char comment[28];
    for(i = 0;i < 28;i ++)
    {
        comment[i] = ' ';
    }

    QByteArray ba1;
    ba1 = ui->lineEditArtist->text().toAscii();//将所得文件名转换为字符//yfx0527
    strcpy(artist,ba1.data());
    ba1 = ui->lineEditTitle->text().toAscii();//将所得文件名转换为字符//yfx0527
    strcpy(title,ba1.data());
    ba1 = ui->lineEditAlbum->text().toAscii();//将所得文件名转换为字符//yfx0527
    strcpy(album,ba1.data());
    ba1 = ui->lineEditYear->text().toAscii();//将所得文件名转换为字符//yfx0527
    strcpy(year,ba1.data());
    ba1 = ui->lineEditComment->text().toAscii();//将所得文件名转换为字符//yfx0527
    strcpy(comment,ba1.data());

    char whatmm[3] = "XX";
    fwrite(whatmm,sizeof(char),3,pFile);
    fwrite(title,sizeof(char),30,pFile);
    fwrite(artist,sizeof(char),30,pFile);
    fwrite(album,sizeof(char),30,pFile);
    fwrite(year,sizeof(char),4,pFile);
    fwrite(comment,sizeof(char),28,pFile);

    fclose(pFile);
}

void Widget::on_pushButtoncopy_clicked()
{
    QSqlQuery query;
    QString selectedfromdb = "SELECT absdirectory,symlinkdirectory FROM test";
    QString selectedasbd;
    QString symlinkdir;
    query.exec(selectedfromdb);
    QString move2;
    QDir movedir;
    FILE *pFile; //文件指针
    Mp3Info song;
    QByteArray ba;
    while(query.next() != 0)
    {
        selectedasbd = query.value(0).toString();
        symlinkdir = query.value(1).toString();
        //qDebug() << selectedasbd << "\n";
        //QString move1 = selectedasbd;
        move2 = ui->lineEdityuanmulu->text();
        qDebug() << move2;

        ba = selectedasbd.toAscii();//将所得文件名转换为字符//yfx0527
        const char *dirnow = ba.data();
        //printf("%s\n",dirnow);
        pFile = fopen(dirnow, "r");
        fseek(pFile, -128, SEEK_END);
        fread(&song, 1, sizeof (Mp3Info), pFile); //读取MP3文件ID3V标签
        //printf("%s\n",song.album);
        QString title = QString::fromLocal8Bit(song.title,30);
        QString artist = QString::fromLocal8Bit(song.artist,30);
        QString album = QString::fromLocal8Bit(song.album,30);
        QString year = QString::fromLocal8Bit(song.year,4);
        QString comment = QString::fromLocal8Bit(song.comment,28);

        //QString artist2 = artist.trimmed();

        QString dirmubiao = ui->lineEditmubiaomulu->text();
        int dirlong = dirmubiao.length();
        symlinkdir = symlinkdir.remove(0,dirlong + 4);
        qDebug() << symlinkdir;
        QString move3 = move2 + "/" + artist;
        qDebug() << move3;
        bool mkdir = movedir.mkdir(move3);
        QString move4 = move2 + symlinkdir;
        qDebug() << move4;
        bool rename = movedir.rename(selectedasbd , move4);
        //qDebug() << selectedasbd;
        qDebug() << mkdir << rename;
        fclose(pFile);

    }
}
